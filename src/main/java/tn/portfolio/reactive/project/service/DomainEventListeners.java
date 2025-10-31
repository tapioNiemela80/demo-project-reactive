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
import tn.portfolio.reactive.common.domain.Email;
import tn.portfolio.reactive.common.service.EmailClientService;
import tn.portfolio.reactive.project.domain.*;
import tn.portfolio.reactive.project.events.TaskAddedToProjectEvent;
import tn.portfolio.reactive.project.repository.ProjectRepository;
import tn.portfolio.reactive.team.events.TeamTaskCompletedEvent;

import java.time.Duration;

@Component
public class DomainEventListeners {
    private final ProjectRepository projects;
    private final EmailClientService emailClientService;
    private final Email sender;
    private static final Logger log = LoggerFactory.getLogger(DomainEventListeners.class);

    public DomainEventListeners(ProjectRepository projects, EmailClientService emailClientService, @Value("${email.sender}") String sender) {
        this.projects = projects;
        this.emailClientService = emailClientService;
        this.sender = Email.of(sender);
    }

    @ReactiveEventListener(TaskAddedToProjectEvent.class)
    public Mono<Void> onTaskAdded(TaskAddedToProjectEvent event) {
        return findProject(event.projectId())
                .flatMap(project -> buildEmailMessage(project, event.taskId()))
                .flatMap(emailClientService::send)
                .doOnError(err -> log.error("Error handling TaskAddedToProjectEvent {}", event, err));
    }

    private Mono<EmailMessage> buildEmailMessage(Project project, ProjectTaskId taskId) {
        return Mono.justOrEmpty(project.getTask(taskId))
                .switchIfEmpty(Mono.error(new UnknownProjectTaskIdException(taskId)))
                .flatMap(task -> project.validContactEmail()
                        .map(email -> Mono.just(toEmailMessage(task, email)))
                        .orElseGet(() -> warnInvalidRecipientAddress(project, taskId)));
    }

    private Mono<EmailMessage> warnInvalidRecipientAddress(Project project, ProjectTaskId taskId) {
        log.warn("Skipping sending email notification about new task {} for project {}: invalid contact email '{}'",
                taskId, project.getId(), project.contactEmailValue());
        return Mono.empty();
    }

    private EmailMessage toEmailMessage(ProjectTaskSnapshot task, Email contactEmail) {
        return new EmailMessage(
                sender,
                contactEmail,
                "Task added",
                "Task %s was added".formatted(task),
                false
        );
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
