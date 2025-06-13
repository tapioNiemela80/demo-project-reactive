package tn.portfolio.reactive.team.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
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

@Service
public class TeamService {
    private final TeamRepository teams;
    private final IDService idService;
    private final ProjectRepository projects;
    private final ReactiveDomainEventPublisher eventPublisher;
    private final TeamFactory teamFactory;
    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    public TeamService(TeamRepository teams, IDService idService, ProjectRepository projects, ReactiveDomainEventPublisher eventPublisher, TeamFactory teamFactory) {
        this.teams = teams;
        this.idService = idService;
        this.projects = projects;
        this.eventPublisher = eventPublisher;
        this.teamFactory = teamFactory;
    }

    public Mono<Team> createTeam(String name) {
        return idService.newTeamId()
                .map(teamId -> teamFactory.create(teamId, name))
                .flatMap(teams::save);
    }

    public Mono<Void> assignTask(TeamId teamId, TeamTaskId taskID, TeamMemberId toMemberId) {
        return findTeam(teamId)
                .map(team -> team.assignTask(taskID, toMemberId))
                .flatMap(teams::save)
                .then();
    }

    public Mono<Void> markTaskInProgress(TeamId teamId, TeamTaskId taskID) {
        return findTeam(teamId)
                .map(team -> team.markTaskInProgress(taskID))
                .flatMap(teams::save)
                .then();
    }

    public Mono<Void> unassignTask(TeamId teamId, TeamTaskId taskID) {
        return findTeam(teamId)
                .map(team -> team.markTaskUnassigned(taskID))
                .flatMap(teams::save)
                .then();
    }

    public Mono<Void> removeTask(TeamId teamId, TeamTaskId taskID) {
        return findTeam(teamId)
                .map(team -> team.removeTask(taskID))
                .flatMap(teams::save)
                .then();
    }

    public Mono<Void> completeTask(TeamId teamId, TeamTaskId taskID, ActualSpentTime actualSpentTime) {
        return findTeam(teamId)
                .map(team -> team.markTaskCompleted(taskID, toDomain(actualSpentTime)))
                .flatMap(teams::save)
                .doOnSuccess(team -> publishTeamTaskCompletedEvent(taskID, actualSpentTime, team))
                .then();
    }

    private void publishTeamTaskCompletedEvent(TeamTaskId taskID, ActualSpentTime actualSpentTime, Team team) {
        team.getOriginalTaskId(taskID)
                .ifPresentOrElse(projectTaskId -> eventPublisher.publish(new TeamTaskCompletedEvent(taskID, projectTaskId, toDomain(actualSpentTime))),
                        () -> log.warn("No corresponding project taskId for team %s and task %s".formatted(team.getId(), taskID)));
    }

    private tn.portfolio.reactive.common.domain.ActualSpentTime toDomain(ActualSpentTime actualSpentTime) {
        return new tn.portfolio.reactive.common.domain.ActualSpentTime(actualSpentTime.hours(), actualSpentTime.minutes());
    }

    public Mono<TeamMemberId> addMember(TeamId teamId, String name, String profession) {
        return Mono.zip(idService.newTeamMemberId(), findTeam(teamId))
                .map(tuple -> new MemberCreationContext(tuple.getT1(), tuple.getT2()))
                .flatMap(context -> context.addMember(name, profession, teams));
    }

    private record MemberCreationContext(TeamMemberId memberId, Team team) {
        Mono<TeamMemberId> addMember(String name, String profession, TeamRepository teamRepository) {
            return teamRepository.save(team.addMember(memberId, name, profession))
                    .thenReturn(memberId);
        }
    }

    public Mono<Void> removeMember(TeamId teamId, TeamMemberId memberId) {
        return findTeam(teamId)
                .map(team -> team.removeMember(memberId))
                .flatMap(teams::save)
                .then();
    }

    public Mono<TeamTaskId> addTask(TeamId teamId, ProjectTaskId projectTaskId) {
        return findIfTaskIsAlreadyAssigned(projectTaskId)
                .map(TaskAssignmentGuard::validateTaskIsFreeToAssign)
                .flatMap(passedGuard -> generateTaskIdAndAddToTeam(teamId, projectTaskId));
    }

    private Mono<TeamTaskId> generateTaskIdAndAddToTeam(TeamId teamId, ProjectTaskId projectTaskId) {
        return Mono.zip(idService.newTeamTaskId(), findTeam(teamId), findProject(projectTaskId))
                .map(tuple -> new TaskCreationContext(tuple.getT3(), tuple.getT2(), tuple.getT1()))
                .flatMap(context -> teams.save(context.withTask(projectTaskId))
                        .thenReturn(context.taskId()));
    }

    private Mono<Team> findTeam(TeamId teamId) {
        return teams.findById(teamId)
                .switchIfEmpty(Mono.error(new UnknownTeamIdException(teamId)));
    }

    private Mono<Project> findProject(ProjectTaskId projectTaskId) {
        return projects.findByTaskId(projectTaskId)
                .switchIfEmpty(Mono.error(new UnknownProjectTaskIdException(projectTaskId)));
    }

    private Mono<TaskAssignmentGuard> findIfTaskIsAlreadyAssigned(ProjectTaskId projectTaskId) {
        return teams.findByProjectTaskId(projectTaskId)
                .map(team -> new TaskAssignmentGuard(projectTaskId, true))
                .defaultIfEmpty(new TaskAssignmentGuard(projectTaskId, false));
    }

    private record TaskAssignmentGuard(ProjectTaskId projectTaskId, boolean alreadyAssigned) {
        TaskAssignmentGuard validateTaskIsFreeToAssign() {
            if (alreadyAssigned) {
                throw new TaskAlreadyAssignedException("Task is already assigned to some team");
            }
            return this;
        }
    }

    private record TaskCreationContext(Project project, Team team, TeamTaskId taskId) {
        Team withTask(ProjectTaskId projectTaskId) {
            var projectTaskSnapshot = project.getTask(projectTaskId)
                    .orElseThrow(() -> new UnknownProjectTaskIdException(projectTaskId));
            return team.addTask(taskId, projectTaskId, projectTaskSnapshot.title(), projectTaskSnapshot.description());
        }
    }

}
