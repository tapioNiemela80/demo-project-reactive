package tn.portfolio.reactive.team.service;

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
                .doOnSuccess(team -> eventPublisher.publish(new TeamTaskCompletedEvent(taskID, team.getOriginalTaskId(taskID).get(), toDomain(actualSpentTime))))
                .then();
    }

    private tn.portfolio.reactive.common.domain.ActualSpentTime toDomain(ActualSpentTime actualSpentTime) {
        return new tn.portfolio.reactive.common.domain.ActualSpentTime(actualSpentTime.hours(), actualSpentTime.minutes());
    }

    public Mono<TeamMemberId> addMember(TeamId teamId, String name, String profession) {
        return Mono.zip(idService.newTeamMemberId(), findTeam(teamId))
                .map(tuple -> new MemberCreationContext(tuple.getT1(), tuple.getT2()))
                .flatMap(context -> context.addMember(name ,profession, teams));
    }

    private record MemberCreationContext(TeamMemberId memberId, Team team){
        Mono<TeamMemberId> addMember(String name, String profession, TeamRepository teamRepository){
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
        return find(teamId, projectTaskId)
                .map(guard -> guard.validateTaskIsFree())
                .flatMap(team -> idService.newTeamTaskId())
                .flatMap(taskId -> findData(teamId, projectTaskId, taskId)
                        .map(data -> data.addTask(projectTaskId))
                        .flatMap(teamWithTaskId -> teams.save(teamWithTaskId.team()).thenReturn(teamWithTaskId.taskId())));
    }

    private Mono<TaskAssignmentGuard> find(TeamId teamId, ProjectTaskId taskID) {
        return Mono.zip(findTeam(teamId), isAlreadyAssigned(taskID), idService.newTeamTaskId())
                .map(tuple -> new TaskAssignmentGuard(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    private Mono<Boolean> isAlreadyAssigned(ProjectTaskId taskId) {
        return teams.findByProjectTaskId(taskId)
                .map(ignored -> true)
                .defaultIfEmpty(false);
    }

    private record TaskAssignmentGuard(Team team, boolean alreadyAssigned, TeamTaskId teamTaskId) {
        TaskAssignmentGuard validateTaskIsFree() {
            if (alreadyAssigned) {
                throw new TaskAlreadyAssignedException("Task is already assigned to some team");
            }
            return this;
        }
    }

    private record ProjectAndTeamAndTeamTaskId(Project project, Team team, TeamTaskId taskId) {
        TeamWithTaskId addTask(ProjectTaskId projectTaskId) {
            var projectTaskSnapshot = project.getTask(projectTaskId).orElseThrow(() -> new UnknownProjectTaskIdException(projectTaskId));
            return new TeamWithTaskId(team.addTask(taskId, projectTaskSnapshot.projectTaskId(), projectTaskSnapshot.title(), projectTaskSnapshot.description()), taskId);
        }
    }

    private record TeamWithTaskId(Team team, TeamTaskId taskId) {
    }

    private Mono<ProjectAndTeamAndTeamTaskId> findData(TeamId teamId, ProjectTaskId projectTaskId, TeamTaskId taskId) {
        return Mono.zip(findProject(projectTaskId), findTeam(teamId))
                .map(tuple -> new ProjectAndTeamAndTeamTaskId(tuple.getT1(), tuple.getT2(), taskId));
    }

    private Mono<Team> findTeam(TeamId teamId) {
        return teams.findById(teamId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Team not found " + teamId)));
    }

    private Mono<Project> findProject(ProjectTaskId projectTaskId) {
        return projects.findByTaskId(projectTaskId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Project not found by task id" + projectTaskId)));
    }

}
