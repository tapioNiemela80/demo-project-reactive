package tn.portfolio.reactive.team.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.team.domain.*;
import tn.portfolio.reactive.team.view.TeamViewService;
import tn.portfolio.reactive.team.service.TeamService;
import tn.portfolio.reactive.team.view.TeamView;
import tn.portfolio.reactive.team.view.TeamsView;

import java.net.URI;
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
    public Mono<ResponseEntity<Void>> createTeam(@RequestBody CreateTeamRequest request) {
        return teamService.createTeam(request.name())
                .map(teamId -> uri("/teams/"+teamId.value()))
                .map(location -> ResponseEntity.created(location).build());
    }

    @PostMapping("/{teamId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<Void>> addMember(@PathVariable UUID teamId, @RequestBody AddMemberRequest request) {
        return teamService.addMember(new TeamId(teamId), request.name(), request.profession())
                .map(memberId -> uri("/teams/"+teamId+"/members/"+memberId))
                .map(location -> ResponseEntity.created(location).build());
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> removeMember(@PathVariable UUID teamId, @PathVariable UUID memberId) {
        return teamService.removeMember(new TeamId(teamId), new TeamMemberId(memberId))
                .then(noContent());
    }

    @PostMapping("/{teamId}/tasks/by-project-id/{projectTaskId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<Void>> addTask(@PathVariable UUID teamId, @PathVariable UUID projectTaskId){
        return teamService.addTask(new TeamId(teamId), new ProjectTaskId(projectTaskId))
                .map(taskId -> uri("/teams/"+teamId+"/tasks/"+taskId))
                .map(location -> ResponseEntity.created(location).build());
    }

    @PatchMapping("/{teamId}/tasks/{taskId}/assignee")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> assignTask(@PathVariable UUID teamId, @PathVariable UUID taskId, @RequestBody AssignTaskInput assignTaskInput) {
        return teamService.assignTask(new TeamId(teamId), new TeamTaskId(taskId), new TeamMemberId(assignTaskInput.assigneeId()))
                .then(noContent());
    }

    @PostMapping("/{teamId}/tasks/{taskId}/mark-in-progress")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> markTaskInProgress(@PathVariable UUID teamId, @PathVariable UUID taskId) {
        return teamService.markTaskInProgress(new TeamId(teamId), new TeamTaskId(taskId))
                .then(noContent());
    }

    @PostMapping("/{teamId}/tasks/{taskId}/unassign")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> markTaskUnassigned(@PathVariable UUID teamId, @PathVariable UUID taskId) {
        return teamService.unassignTask(new TeamId(teamId), new TeamTaskId(taskId))
                .then(noContent());
    }

    @PostMapping("/{teamId}/tasks/{taskId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> markTaskCompleted(@PathVariable UUID teamId, @PathVariable UUID taskId, @RequestBody ActualSpentTime actualSpentTime) {
        return teamService.completeTask(new TeamId(teamId), new TeamTaskId(taskId), actualSpentTime)
                .then(noContent());
    }
    @DeleteMapping("/{teamId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> removeTask(@PathVariable UUID teamId, @PathVariable UUID taskId) {
        return teamService.removeTask(new TeamId(teamId), new TeamTaskId(taskId))
                .then(noContent());
    }

    @GetMapping
    public Flux<TeamsView> findAll() {
        return teamViewService.findAll();
    }

    @GetMapping("/{teamId}")
    public Mono<TeamView> findOne(@PathVariable UUID teamId) {
        return teamViewService.findOne(teamId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Unknown team "+teamId)));
    }

    private Mono<ResponseEntity<Void>> noContent() {
        return Mono.just(ResponseEntity.noContent().build());
    }

    private URI uri(String value){
        return URI.create(value);
    }
}
