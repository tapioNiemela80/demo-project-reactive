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
import tn.portfolio.reactive.common.service.EmailClientService;
import tn.portfolio.reactive.project.domain.Project;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.project.domain.UnknownProjectTaskIdException;
import tn.portfolio.reactive.project.events.TaskAddedToProjectEvent;
import tn.portfolio.reactive.project.repository.ProjectRepository;
import tn.portfolio.reactive.team.events.TeamTaskCompletedEvent;

import java.time.Duration;
import java.util.Optional;

@Component
public class DomainEventListeners {
    private final ProjectRepository projects;
    private final EmailClientService emailClientService;
    private final String sender;
    private static final Logger log = LoggerFactory.getLogger(DomainEventListeners.class);

    public DomainEventListeners(ProjectRepository projects, EmailClientService emailClientService, @Value("${email.sender}") String sender) {
        this.projects = projects;
        this.emailClientService = emailClientService;
        this.sender = sender;
    }

    @ReactiveEventListener(TaskAddedToProjectEvent.class)
    public Mono<Void> onTaskAdded(TaskAddedToProjectEvent event) {
        return projects.findById(event.projectId())
                .flatMap(project -> Mono.justOrEmpty(getEmail(project, event.taskId())))
                .flatMap(email -> emailClientService.send(email))
                .doOnError(err -> log.error("Got error on listening TaskAddedToProjectEvent %s".formatted(event), err));
    }

    private Optional<EmailMessage> getEmail(Project project, ProjectTaskId taskId) {
        return project.getTask(taskId)
                .map(task -> new EmailMessage(sender, project.getContactPersonEmail(), "Task added", "Task %s was added".formatted(task), false));
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
