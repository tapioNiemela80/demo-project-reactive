package tn.portfolio.reactive.team.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tn.portfolio.reactive.common.ReactiveDomainEventPublisher;
import tn.portfolio.reactive.common.service.IDService;
import tn.portfolio.reactive.project.domain.Project;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.project.domain.UnknownProjectTaskIdException;
import tn.portfolio.reactive.project.repository.ProjectRepository;
import tn.portfolio.reactive.team.controller.ActualSpentTime;
import tn.portfolio.reactive.team.domain.*;
import tn.portfolio.reactive.team.events.TeamTaskCompletedEvent;
import tn.portfolio.reactive.team.repository.TeamRepository;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private IDService idService;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ReactiveDomainEventPublisher eventPublisher;
    @Mock
    private TeamFactory teamFactory;
    @Mock
    private Team team;
    @Mock
    private Project project;

    @InjectMocks
    TeamService underTest;
    private TeamId teamId;
    private TeamTaskId teamTaskId;
    private TeamMemberId teamMemberId;
    private ProjectTaskId projectTaskId;
    private ActualSpentTime actualSpentTime;

    @BeforeEach
    void setUp() {
        teamId = new TeamId(UUID.randomUUID());
        teamTaskId = new TeamTaskId(UUID.randomUUID());
        teamMemberId = new TeamMemberId(UUID.randomUUID());
        projectTaskId = new ProjectTaskId(UUID.randomUUID());
        actualSpentTime = new ActualSpentTime(1, 30);
    }

    @Test
    void createTeamShouldCreateAndSaveTeam() {
        when(idService.newTeamId()).thenReturn(Mono.just(teamId));
        when(teamFactory.create(teamId, "Dev Team")).thenReturn(team);
        when(teamRepository.save(team)).thenReturn(Mono.just(team));
        when(team.getId()).thenReturn(teamId);

        StepVerifier.create(underTest.createTeam("Dev Team"))
                .expectNext(teamId)
                .verifyComplete();
    }

    @Test
    void assignTaskShouldAssignTaskAndSaveTeam() {
        when(teamRepository.findById(teamId)).thenReturn(Mono.just(team));
        when(team.assignTask(teamTaskId, teamMemberId)).thenReturn(team);
        when(teamRepository.save(team)).thenReturn(Mono.just(team));

        StepVerifier.create(underTest.assignTask(teamId, teamTaskId, teamMemberId))
                .verifyComplete();
    }

    @Test
    void assignTaskShouldFailWhenUnknownTeamId() {
        when(teamRepository.findById(teamId)).thenReturn(Mono.empty());

        StepVerifier.create(underTest.assignTask(teamId, teamTaskId, teamMemberId))
                .expectError(UnknownTeamIdException.class)
                .verify();
    }

    @Test
    void completeTaskShouldMarkTaskCompleteAndPublishEvent() {
        when(teamRepository.findById(teamId)).thenReturn(Mono.just(team));
        var timeSpent = new tn.portfolio.reactive.common.domain.ActualSpentTime(1, 30);
        when(team.markTaskCompleted(teamTaskId, timeSpent)).thenReturn(team);
        when(teamRepository.save(team)).thenReturn(Mono.just(team));
        when(team.getOriginalTaskId(teamTaskId)).thenReturn(Optional.of(projectTaskId));

        StepVerifier.create(underTest.completeTask(teamId, teamTaskId, actualSpentTime))
                .verifyComplete();

        verify(eventPublisher).publish(new TeamTaskCompletedEvent(teamTaskId, projectTaskId, timeSpent));
    }

    @Test
    void completeTaskShouldLogWarningWhenNoProjectTaskId() {
        when(teamRepository.findById(teamId)).thenReturn(Mono.just(team));
        when(team.markTaskCompleted(eq(teamTaskId), any())).thenReturn(team);
        when(teamRepository.save(team)).thenReturn(Mono.just(team));
        when(team.getOriginalTaskId(teamTaskId)).thenReturn(Optional.empty());

        StepVerifier.create(underTest.completeTask(teamId, teamTaskId, actualSpentTime))
                .verifyComplete();

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void addTaskShouldThrowIfTaskAlreadyAssigned() {
        when(teamRepository.findByProjectTaskId(projectTaskId)).thenReturn(Mono.just(team));

        StepVerifier.create(underTest.addTask(teamId, projectTaskId))
                .expectError(TaskAlreadyAssignedException.class)
                .verify();
    }

    @Test
    void addTaskShouldFailIfProjectNotFound() {
        when(teamRepository.findByProjectTaskId(projectTaskId)).thenReturn(Mono.empty());
        when(idService.newTeamTaskId()).thenReturn(Mono.just(teamTaskId));
        when(teamRepository.findById(teamId)).thenReturn(Mono.just(team));
        when(projectRepository.findByTaskId(projectTaskId)).thenReturn(Mono.empty());

        StepVerifier.create(underTest.addTask(teamId, projectTaskId))
                .expectError(UnknownProjectTaskIdException.class)
                .verify();
    }

    @Test
    void removeMemberShouldRemoveAndSaveTeam() {
        when(teamRepository.findById(teamId)).thenReturn(Mono.just(team));
        when(team.removeMember(teamMemberId)).thenReturn(team);
        when(teamRepository.save(team)).thenReturn(Mono.just(team));

        StepVerifier.create(underTest.removeMember(teamId, teamMemberId))
                .verifyComplete();
    }

    // todo more tests..
}
