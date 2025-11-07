package tn.portfolio.reactive.project.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import tn.portfolio.reactive.common.EmailMessage;
import tn.portfolio.reactive.common.ReactiveEventListener;
import tn.portfolio.reactive.common.domain.EmailAddress;
import tn.portfolio.reactive.common.service.EmailClientService;
import tn.portfolio.reactive.project.domain.EmailNotificationPolicy;
import tn.portfolio.reactive.project.domain.*;
import tn.portfolio.reactive.project.events.TaskAddedToProjectEvent;
import tn.portfolio.reactive.project.repository.ProjectRepository;
import tn.portfolio.reactive.team.events.TeamTaskCompletedEvent;

import java.time.Duration;

@Component
public class DomainEventListeners {
    private final ProjectRepository projects;
    private final EmailClientService emailClientService;
    private final EmailNotificationPolicy emailNotificationPolicy;
    private final EmailAddress sender;
    private static final Logger log = LoggerFactory.getLogger(DomainEventListeners.class);

    public DomainEventListeners(ProjectRepository projects, EmailClientService emailClientService, EmailNotificationPolicy emailNotificationPolicy,
                                @Value("${email.sender}") String sender) {
        this.projects = projects;
        this.emailClientService = emailClientService;
        this.emailNotificationPolicy = emailNotificationPolicy;
        this.sender = EmailAddress.of(sender);
    }

    @ReactiveEventListener(TaskAddedToProjectEvent.class)
    public Mono<Void> onTaskAdded(TaskAddedToProjectEvent event) {
        return findProject(event.projectId())
                .flatMap(project -> buildEmailMessage(project, event.taskId()))
                .flatMap(emailClientService::send)
                .doOnError(err -> log.error("Error handling TaskAddedToProjectEvent {}", event, err));
    }

    private Mono<EmailMessage> buildEmailMessage(Project project, ProjectTaskId taskId) {
        return findTask(project, taskId)
                .flatMap(task ->
                        resolveContactEmail(project, taskId)
                                .flatMap(email -> requirePolicyAllows(email, project, taskId))
                                .map(email -> toEmailMessage(task, email))
                );
    }

    private Mono<ProjectTaskSnapshot> findTask(Project project, ProjectTaskId taskId) {
        return Mono.justOrEmpty(project.getTask(taskId))
                .switchIfEmpty(Mono.error(new UnknownProjectTaskIdException(taskId)));
    }

    private Mono<EmailAddress> resolveContactEmail(Project project, ProjectTaskId taskId) {
        return Mono.defer(() -> Mono.justOrEmpty(project.validContactEmail()))
                .switchIfEmpty(warnInvalidRecipientAddress(project, taskId));
    }

    private Mono<EmailAddress> warnInvalidRecipientAddress(Project project, ProjectTaskId taskId) {
        return Mono.fromRunnable(() ->
                log.warn("Skipping sending email notification about new task {} for project {}: invalid contact email",
                        taskId, project.getId())
        ).then(Mono.empty());
    }

    private Mono<EmailAddress> requirePolicyAllows(EmailAddress emailAddress, Project project, ProjectTaskId taskId) {
        return emailNotificationPolicy.notificationToEmailIsAllowed(emailAddress)
                .flatMap(allowed -> enforcePolicy(emailAddress, project, taskId, allowed));
    }

    private Mono<EmailAddress> enforcePolicy(EmailAddress emailAddress, Project project, ProjectTaskId taskId, Boolean allowed) {
        if (!allowed) {
            log.info("Skipping email notification about new task {} for project {}: policy denied",
                    taskId.value(), project.getId());
            return Mono.empty();
        }
        return Mono.just(emailAddress);
    }

    private EmailMessage toEmailMessage(ProjectTaskSnapshot task, EmailAddress contactEmailAddress) {
        return new EmailMessage(sender, contactEmailAddress, "Task added", "Task %s was added".formatted(task), false);
    }

    private Mono<Project> findProject(ProjectId id) {
        return projects.findById(id)
                .switchIfEmpty(Mono.error(new UnknownProjectIdException(id)));
    }

    @ReactiveEventListener(TeamTaskCompletedEvent.class)
    public Mono<Void> onTeamTaskCompletedEvent(TeamTaskCompletedEvent event) {
        return Mono.defer(() -> projects.findByTaskId(event.projectTaskId())
                        .switchIfEmpty(Mono.error(new UnknownProjectTaskIdException(event.projectTaskId())))
                        .map(project -> project.completeTask(event.projectTaskId(), event.actualSpentTime()))
                        .flatMap(projects::save)
                )
                .retryWhen(
                        Retry.backoff(5, Duration.ofMillis(200))
                                .filter(throwable -> throwable instanceof OptimisticLockingFailureException)
                )
                .doOnError(err -> log.error("Got error on listening TeamTaskCompletedEvent %s".formatted(event), err))
                .then();
    }

}
