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
    private final TeamRepository teamRepository;
    private final IDService idService;

    private final ProjectRepository projectRepository;
    private final ReactiveDomainEventPublisher eventPublisher;

    public TeamService(TeamRepository teamRepository, IDService idService, ProjectRepository projectRepository, ReactiveDomainEventPublisher eventPublisher) {
        this.teamRepository = teamRepository;
        this.idService = idService;
        this.projectRepository = projectRepository;
        this.eventPublisher = eventPublisher;
    }

    public Mono<Team> createTeam(String name) {
        return idService.newTeamId()
                .map(teamId -> Team.createNew(teamId, name))
                .flatMap(teamRepository::save);
    }

    public Mono<Void> assignTask(TeamId teamId, TeamTaskId taskID, TeamMemberId toMemberId) {
        return findTeam(teamId)
                .map(team -> team.assignTask(taskID, toMemberId))
                .flatMap(teamRepository::save)
                .then();
    }

    public Mono<Void> markTaskInProgress(TeamId teamId, TeamTaskId taskID) {
        return findTeam(teamId)
                .map(team -> team.markTaskInProgress(taskID))
                .flatMap(teamRepository::save)
                .then();
    }

    public Mono<Void> unassignTask(TeamId teamId, TeamTaskId taskID) {
        return findTeam(teamId)
                .map(team -> team.markTaskUnassigned(taskID))
                .flatMap(teamRepository::save)
                .then();
    }

    public Mono<Void> removeTask(TeamId teamId, TeamTaskId taskID){
        return findTeam(teamId)
                .map(team -> team.removeTask(taskID))
                .flatMap(teamRepository::save)
                .then();
    }

    public Mono<Void> completeTask(TeamId teamId, TeamTaskId taskID, ActualSpentTime actualSpentTime){
        return findTeam(teamId)
                .map(team -> team.markTaskCompleted(taskID, toDomain(actualSpentTime)))
                .flatMap(teamRepository::save)
                .doOnSuccess(team -> eventPublisher.publish(new TeamTaskCompletedEvent(taskID, team.getOriginalTaskId(taskID).get(), toDomain(actualSpentTime))))
                .then();
    }

    private tn.portfolio.reactive.common.domain.ActualSpentTime toDomain(ActualSpentTime actualSpentTime) {
        return new tn.portfolio.reactive.common.domain.ActualSpentTime(actualSpentTime.hours(), actualSpentTime.minutes());
    }

    public Mono<TeamMemberId> addMember(TeamId teamId, String name, String profession) {
        return idService.newTeamMemberId() // haetaan ID reaktiivisesti
                .flatMap(memberId ->
                        findTeam(teamId)
                                .map(team -> team.addMember(memberId, name, profession))
                                .flatMap(team ->
                                        teamRepository.save(team)
                                                .thenReturn(memberId) // palautetaan ID
                                )
                );
    }

    public Mono<Void> removeMember(TeamId teamId, TeamMemberId memberId) {
        return findTeam(teamId)
                .map(team -> team.removeMember(memberId))
                .flatMap(teamRepository::save)
                .then();
    }

    public Mono<TeamTaskId> addTask(TeamId teamId, ProjectTaskId projectTaskId) {
        return find(teamId, projectTaskId)
                .map(team -> team.validate())
                .flatMap(team -> idService.newTeamTaskId())
                .flatMap(taskId -> findData(teamId, projectTaskId, taskId)
                .map(data -> data.addTask(projectTaskId))
                .flatMap(teamWithTaskId -> teamRepository.save(teamWithTaskId.team()).thenReturn(teamWithTaskId.taskId())));
    }

    private Mono<TeamWithTaskInformation> find(TeamId teamId, ProjectTaskId taskID){
        return Mono.zip(findTeam(teamId), isAlreadyAssigned(taskID))
                .map(tuple -> new TeamWithTaskInformation(tuple.getT1(), tuple.getT2()));
    }

    private Mono<Boolean> isAlreadyAssigned(ProjectTaskId taskId){
        return teamRepository.findByProjectTaskId(taskId)
                .map(ignored -> true)
                .defaultIfEmpty(false);
    }

    record TeamWithTaskInformation(Team team, boolean alreadyAssigned){
        TeamWithTaskInformation validate(){
            if(alreadyAssigned){
                throw new TaskAlreadyAssignedException("Task is already assigned to some team");
            }
            return this;
        }
    }

    record ProjectAndTeamAndTeamTaskId(Project project, Team team, TeamTaskId taskId) {
        TeamWithTaskId addTask(ProjectTaskId projectTaskId) {
            var projectTaskSnapshot = project.getTask(projectTaskId).orElseThrow(() -> new UnknownProjectTaskIdException(projectTaskId));
            return new TeamWithTaskId(team.addTask(taskId, projectTaskSnapshot.projectTaskId(), projectTaskSnapshot.title(), projectTaskSnapshot.description()), taskId);
        }
    }

    record TeamWithTaskId(Team team, TeamTaskId taskId) {
    }

    private Mono<ProjectAndTeamAndTeamTaskId> findData(TeamId teamId, ProjectTaskId projectTaskId, TeamTaskId taskId) {
        return Mono.zip(findProject(projectTaskId), findTeam(teamId))
                .map(tuple -> new ProjectAndTeamAndTeamTaskId(tuple.getT1(), tuple.getT2(), taskId));
    }

    private Mono<Team> findTeam(TeamId teamId) {
        return teamRepository.findById(teamId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Team not found " + teamId)));
    }

    private Mono<Project> findProject(ProjectTaskId projectTaskId) {
        return projectRepository.findByTaskId(projectTaskId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Project not found by task id" + projectTaskId)));
    }

}
