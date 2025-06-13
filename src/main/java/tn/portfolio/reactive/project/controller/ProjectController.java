package tn.portfolio.reactive.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.project.domain.Project;
import tn.portfolio.reactive.project.domain.ProjectId;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.project.infrastructure.ProjectViewService;
import tn.portfolio.reactive.project.service.ProjectService;
import tn.portfolio.reactive.project.view.ProjectView;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectViewService projectViewService;
    public ProjectController(ProjectService projectService, ProjectViewService projectViewService) {
        this.projectService = projectService;
        this.projectViewService = projectViewService;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UUID> createProject(@RequestBody CreateProjectRequest request) {
        return projectService.createProject(
                        request.name(),
                        request.description(),
                        request.estimatedEndDate(),
                        request.estimation(),
                        request.contactPersonInput()
                )
                .map(Project::getId)
                .map(ProjectId::value);
    }
    @PostMapping("/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UUID> addTask(@PathVariable UUID projectId, @RequestBody AddTaskRequest request) {
        return projectService.addTaskToProject(
                        new ProjectId(projectId),
                        request.name(),
                        request.description(),
                        request.estimation()
                )
                .map(ProjectTaskId::value);
    }
    @PostMapping("/{projectId}/rename")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> rename(@PathVariable UUID projectId, @RequestBody String newName) {
        return projectService.rename(new ProjectId(projectId), newName)
                .then(); // palautetaan Mono<Void>
    }
    @GetMapping("/{projectId}")
    public Mono<ProjectView> findById(@PathVariable UUID projectId){
        return projectViewService.getProjectView(projectId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Unknown project "+projectId)));
    }
}
