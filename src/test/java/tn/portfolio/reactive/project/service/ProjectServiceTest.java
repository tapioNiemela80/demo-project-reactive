package tn.portfolio.reactive.project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tn.portfolio.reactive.common.ReactiveDomainEventPublisher;
import tn.portfolio.reactive.common.service.IDService;
import tn.portfolio.reactive.project.controller.ContactPersonInput;
import tn.portfolio.reactive.project.controller.TimeEstimation;
import tn.portfolio.reactive.project.domain.Project;
import tn.portfolio.reactive.project.domain.ProjectId;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.project.domain.UnknownProjectIdException;
import tn.portfolio.reactive.project.events.TaskAddedToProjectEvent;
import tn.portfolio.reactive.project.repository.ProjectRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    @Mock
    private ProjectRepository projects;
    @Mock
    private ProjectFactory projectFactory;
    @Mock
    private IDService idService;
    @Mock
    private ReactiveDomainEventPublisher eventPublisher;
    @Mock
    private ProjectService underTest;

    @BeforeEach
    void setUp() {
        underTest = new ProjectService(projects, projectFactory, idService, eventPublisher);
    }

    @Test
    void createProjectShouldReturnNewProjectId() {
        ProjectId projectId = new ProjectId(UUID.randomUUID());
        TimeEstimation inputEstimation = new TimeEstimation(5, 30);
        ContactPersonInput contact = new ContactPersonInput("John Doe", "john@example.com");
        LocalDate plannedEndDate = LocalDate.of(2025, 12, 31);
        String name = "New Project";
        String description = "A new test project";

        Project project = mock(Project.class);

        when(idService.newProjectId()).thenReturn(Mono.just(projectId));
        when(projectFactory.createNew(projectId, name, description, plannedEndDate, inputEstimation, contact)).thenReturn(project);
        when(projects.save(project)).thenReturn(Mono.just(project));
        when(project.getId()).thenReturn(projectId);

        StepVerifier.create(underTest.createProject(name, description, plannedEndDate, inputEstimation, contact))
                .expectNext(projectId)
                .verifyComplete();

        verify(idService).newProjectId();
        verify(projectFactory).createNew(projectId, name, description, plannedEndDate, inputEstimation, contact);
        verify(projects).save(project);
    }

    @Test
    void addTaskToProjectShouldAddTaskAndReturnTaskIdAndPublishEvent() {
        UUID projectUuid = UUID.randomUUID();
        UUID taskUuid = UUID.randomUUID();
        ProjectId projectId = new ProjectId(projectUuid);
        ProjectTaskId taskId = new ProjectTaskId(taskUuid);

        TimeEstimation inputEstimation = new TimeEstimation(2, 15);
        tn.portfolio.reactive.project.domain.TimeEstimation domainEstimation = new tn.portfolio.reactive.project.domain.TimeEstimation(2, 15);

        String title = "New Task";
        String description = "Task description";

        Project project = mock(Project.class);
        Project updatedProject = mock(Project.class);

        when(projects.findById(projectId)).thenReturn(Mono.just(project));
        when(idService.newProjectTaskId()).thenReturn(Mono.just(taskId));
        when(project.addTask(taskId, title, description, domainEstimation)).thenReturn(updatedProject);
        when(projects.save(updatedProject)).thenReturn(Mono.just(updatedProject));
        when(updatedProject.getId()).thenReturn(projectId);

        StepVerifier.create(underTest.addTaskToProject(projectId, title, description, inputEstimation))
                .expectNext(taskId)
                .verifyComplete();

        verify(eventPublisher).publish(new TaskAddedToProjectEvent(projectId, taskId));
    }

    @Test
    void addTaskToProjectShouldFailForUnknownProject() {
        ProjectId projectId = new ProjectId(UUID.randomUUID());
        ProjectTaskId taskId = new ProjectTaskId(UUID.randomUUID());

        when(projects.findById(projectId)).thenReturn(Mono.empty());
        when(idService.newProjectTaskId()).thenReturn(Mono.just(taskId));

        StepVerifier.create(underTest.addTaskToProject(projectId, "title", "desc",
                        new TimeEstimation(1, 0)))
                .expectError(UnknownProjectIdException.class)
                .verify();
    }
}
