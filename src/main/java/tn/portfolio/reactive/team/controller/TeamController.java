package tn.portfolio.reactive.team.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.team.domain.*;
import tn.portfolio.reactive.team.infrastructure.TeamViewService;
import tn.portfolio.reactive.team.service.TeamService;

import java.util.UUID;

@RestController
@RequestMapping("/teams")
public class TeamController {
    private final TeamService teamService;
    private final TeamViewService teamViewService;

    public TeamController(TeamService teamService, TeamViewService teamViewService) {
        this.teamService = teamService;
        this.teamViewService = teamViewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UUID> createTeam(@RequestBody CreateTeamRequest request) {
        return teamService.createTeam(
                        request.name()
                )
                .map(Team::getId)
                .map(TeamId::value);
    }

    @PostMapping("/{teamId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UUID> addMember(@PathVariable UUID teamId, @RequestBody AddMemberRequest request) {
        return teamService.addMember(new TeamId(teamId), request.name(), request.profession())
                .map(TeamMemberId::value);
    }

    @PostMapping("/{teamId}/tasks/by-project-id/{projectTaskId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UUID> addTask(@PathVariable UUID teamId, @PathVariable UUID projectTaskId){
        return teamService.addTask(new TeamId(teamId), new ProjectTaskId(projectTaskId))
                .map(TeamTaskId::value);
    }

    @PatchMapping("/{teamId}/tasks/{taskId}/assignee")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> assignTask(@PathVariable UUID teamId, @PathVariable UUID taskId, @RequestBody AssignTaskInput assignTaskInput) {
        return teamService.assignTask(new TeamId(teamId), new TeamTaskId(taskId), new TeamMemberId(assignTaskInput.assigneeId()))
                .then();
    }

    @PostMapping("/{teamId}/tasks/{taskId}/mark-in-progress")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> markTaskInProgress(@PathVariable UUID teamId, @PathVariable UUID taskId) {
        return teamService.markTaskInProgress(new TeamId(teamId), new TeamTaskId(taskId));
    }

    @PostMapping("/{teamId}/tasks/{taskId}/unassign")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> markTaskUnassigned(@PathVariable UUID teamId, @PathVariable UUID taskId) {
        return teamService.unassignTask(new TeamId(teamId), new TeamTaskId(taskId));
    }

    @PostMapping("/{teamId}/tasks/{taskId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> markTaskCompleted(@PathVariable UUID teamId, @PathVariable UUID taskId, @RequestBody ActualSpentTime actualSpentTime) {
        return teamService.completeTask(new TeamId(teamId), new TeamTaskId(taskId), actualSpentTime);
    }
    @DeleteMapping("/{teamId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeTask(@PathVariable UUID teamId, @PathVariable UUID taskId) {
        return teamService.removeTask(new TeamId(teamId), new TeamTaskId(taskId));
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeMember(@PathVariable UUID teamId, @PathVariable UUID memberId) {
        return teamService.removeMember(new TeamId(teamId), new TeamMemberId(memberId));
    }

    @GetMapping("/{teamId}")
    public Mono<TeamView> findOne(@PathVariable UUID teamId) {
        return teamViewService.findOne(teamId);
    }
}
