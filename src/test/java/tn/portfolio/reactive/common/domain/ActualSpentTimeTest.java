package tn.portfolio.reactive.common.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tn.portfolio.reactive.common.EmailMessage;
import tn.portfolio.reactive.common.service.EmailClientService;
import tn.portfolio.reactive.project.domain.*;
import tn.portfolio.reactive.project.events.TaskAddedToProjectEvent;
import tn.portfolio.reactive.project.repository.ProjectRepository;
import tn.portfolio.reactive.project.service.DomainEventListeners;
import tn.portfolio.reactive.team.domain.TeamTaskId;
import tn.portfolio.reactive.team.events.TeamTaskCompletedEvent;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainEventListenersTest {
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private EmailClientService emailClientService;

    private DomainEventListeners underTest;

    String sender = "noreply@example.com";

    @BeforeEach
    void setUp() {
        underTest = new DomainEventListeners(projectRepository, emailClientService, sender);
    }

    @Test
    void shouldSendEmailWhenTaskIsAdded() {
        ProjectId projectId = new ProjectId(UUID.randomUUID());
        ProjectTaskId taskId = new ProjectTaskId(UUID.randomUUID());
        TaskAddedToProjectEvent event = new TaskAddedToProjectEvent(projectId, taskId);

        Project mockProject = mock(Project.class);
        when(projectRepository.findById(projectId)).thenReturn(Mono.just(mockProject));
        ProjectTaskSnapshot taskSnapshot = new ProjectTaskSnapshot(taskId, projectId, "Task added", "desc", TimeEstimation.fromMinutes(10));
        String content = "Task %s was added".formatted(taskSnapshot);
        EmailMessage expectedEmail = new EmailMessage(sender, "contact@example.com", "Task added", content, false);
        when(mockProject.getTask(taskId)).thenReturn(Optional.of(taskSnapshot));
        when(mockProject.getContactPersonEmail()).thenReturn("contact@example.com");
        when(emailClientService.send(any(EmailMessage.class))).thenReturn(Mono.empty());

        StepVerifier.create(underTest.onTaskAdded(event))
                .verifyComplete();

        verify(emailClientService).send(expectedEmail);
    }

    @Test
    void shouldErrorWhenTaskNotFoundInProject() {
        ProjectId projectId = new ProjectId(UUID.randomUUID());
        ProjectTaskId taskId = new ProjectTaskId(UUID.randomUUID());
        TaskAddedToProjectEvent event = new TaskAddedToProjectEvent(projectId, taskId);

        Project mockProject = mock(Project.class);

        when(projectRepository.findById(projectId)).thenReturn(Mono.just(mockProject));
        when(mockProject.getTask(taskId)).thenReturn(Optional.empty());

        StepVerifier.create(underTest.onTaskAdded(event))
                .expectError(UnknownProjectTaskIdException.class)
                .verify();
    }

    @Test
    void shouldErrorWhenProjectNotFound() {
        ProjectId projectId = new ProjectId(UUID.randomUUID());
        ProjectTaskId taskId = new ProjectTaskId(UUID.randomUUID());
        TaskAddedToProjectEvent event = new TaskAddedToProjectEvent(projectId, taskId);

        when(projectRepository.findById(projectId)).thenReturn(Mono.empty());

        StepVerifier.create(underTest.onTaskAdded(event))
                .expectError(UnknownProjectTaskIdException.class)
                .verify();
    }

    @Test
    void shouldCompleteTeamTaskSuccessfully() {
        TeamTaskId taskId = new TeamTaskId(UUID.randomUUID());
        ProjectTaskId projectTaskId = new ProjectTaskId(UUID.randomUUID());
        TeamTaskCompletedEvent event = new TeamTaskCompletedEvent(taskId, projectTaskId, new ActualSpentTime(1, 30));

        Project project = mock(Project.class);
        Project updatedProject = mock(Project.class);

        when(projectRepository.findByTaskId(event.projectTaskId())).thenReturn(Mono.just(project));
        when(project.completeTask(event.projectTaskId(), event.actualSpentTime())).thenReturn(updatedProject);
        when(projectRepository.save(updatedProject)).thenReturn(Mono.just(updatedProject));

        StepVerifier.create(underTest.onTeamTaskCompletedEvent(event))
                .verifyComplete();

        verify(projectRepository).save(updatedProject);
    }

    @Test
    void shouldRetryOnOptimisticLockFailure() {
        TeamTaskId taskId = new TeamTaskId(UUID.randomUUID());
        ProjectTaskId projectTaskId = new ProjectTaskId(UUID.randomUUID());
        TeamTaskCompletedEvent event = new TeamTaskCompletedEvent(taskId, projectTaskId, new ActualSpentTime(2, 15));

        Project project = mock(Project.class);
        Project updatedProject = mock(Project.class);

        when(projectRepository.findByTaskId(event.projectTaskId())).thenReturn(Mono.just(project));
        when(project.completeTask(event.projectTaskId(), event.actualSpentTime())).thenReturn(updatedProject);
        when(projectRepository.save(updatedProject))
                .thenReturn(Mono.error(new OptimisticLockingFailureException("optimistic lock failed")))
                .thenReturn(Mono.error(new OptimisticLockingFailureException("retry")))
                .thenReturn(Mono.just(updatedProject));

        StepVerifier.create(underTest.onTeamTaskCompletedEvent(event))
                .verifyComplete();

        verify(projectRepository, times(3)).save(updatedProject);
    }

    @Test
    void shouldErrorWhenProjectNotFoundForTeamTask() {
        TeamTaskId taskId = new TeamTaskId(UUID.randomUUID());
        ProjectTaskId projectTaskId = new ProjectTaskId(UUID.randomUUID());
        TeamTaskCompletedEvent event = new TeamTaskCompletedEvent(taskId, projectTaskId, new ActualSpentTime(2, 0));

        when(projectRepository.findByTaskId(event.projectTaskId())).thenReturn(Mono.empty());

        StepVerifier.create(underTest.onTeamTaskCompletedEvent(event))
                .expectError(UnknownProjectTaskIdException.class)
                .verify();
    }
}