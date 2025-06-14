package tn.portfolio.reactive.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.project.domain.Project;
import tn.portfolio.reactive.project.domain.ProjectId;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.project.view.ProjectViewService;
import tn.portfolio.reactive.project.service.ProjectService;
import tn.portfolio.reactive.project.view.ProjectView;
import tn.portfolio.reactive.project.view.ProjectsView;

import java.net.URI;
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
    public Mono<ResponseEntity<Void>> createProject(@RequestBody CreateProjectRequest request) {
        return projectService.createProject(
                        request.name(),
                        request.description(),
                        request.estimatedEndDate(),
                        request.estimation(),
                        request.contactPersonInput()
                )
                .map(Project::getId)
                .map(projectId -> uri("projects/"+projectId.value()))
                .map(location -> ResponseEntity.created(location).build());

    }
    @PostMapping("/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<Void>> addTask(@PathVariable UUID projectId, @RequestBody AddTaskRequest request) {
        return projectService.addTaskToProject(
                        new ProjectId(projectId),
                        request.name(),
                        request.description(),
                        request.estimation()
                )
                .map(ProjectTaskId::value)
                .map(taskId -> uri("projects/"+projectId+"/tasks/"+taskId))
                .map(location -> ResponseEntity.created(location).build());
    }
    @PostMapping("/{projectId}/rename")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> rename(@PathVariable UUID projectId, @RequestBody String newName) {
        return projectService.rename(new ProjectId(projectId), newName)
                .then(); // palautetaan Mono<Void>
    }

    @GetMapping
    public Flux<ProjectsView> findAll(){
        return projectViewService.findAll();
    }

    @GetMapping("/{projectId}")
    public Mono<ResponseEntity<ProjectView>> findById(@PathVariable UUID projectId){
        return projectViewService.getProjectView(projectId)
                .map(data -> ResponseEntity.ok(data))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Unknown project "+projectId)));
    }

    private URI uri(String value){
        return URI.create(value);
    }
}
