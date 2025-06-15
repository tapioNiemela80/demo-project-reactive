package tn.portfolio.reactive.project.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.common.ReactiveDomainEventPublisher;
import tn.portfolio.reactive.common.service.IDService;
import tn.portfolio.reactive.project.controller.ContactPersonInput;
import tn.portfolio.reactive.project.domain.*;
import tn.portfolio.reactive.project.events.TaskAddedToProjectEvent;
import tn.portfolio.reactive.project.repository.ProjectRepository;

import java.time.LocalDate;

@Service
public class ProjectService {
    private final ProjectRepository projects;
    private final ProjectFactory projectFactory;
    private final IDService idService;
    private final ReactiveDomainEventPublisher eventPublisher;

    public ProjectService(ProjectRepository projects, ProjectFactory projectFactory, IDService idService, ReactiveDomainEventPublisher eventPublisher) {
        this.projects = projects;
        this.projectFactory = projectFactory;
        this.idService = idService;
        this.eventPublisher = eventPublisher;
    }

    public Mono<ProjectId> createProject(String name, String description,
                                       LocalDate plannedEndDate,
                                       tn.portfolio.reactive.project.controller.TimeEstimation timeEstimation,
                                       ContactPersonInput contactPersonInput) {
        return idService.newProjectId()
                .map(projectId -> projectFactory.createNew(projectId, name, description, plannedEndDate, timeEstimation, contactPersonInput))
                .flatMap(projects::save)
                .map(Project::getId);
    }

    public Mono<ProjectTaskId> addTaskToProject(ProjectId projectId, String title, String description,
                                                tn.portfolio.reactive.project.controller.TimeEstimation timeEstimation) {
        return Mono.zip(findProject(projectId), newProjectTaskId())
                .flatMap(tuple -> addTaskToProject(tuple.getT1(), tuple.getT2(), title, description, timeEstimation));
    }

    private Mono<ProjectTaskId> newProjectTaskId() {
        return idService.newProjectTaskId();
    }

    private Mono<ProjectTaskId> addTaskToProject(Project project, ProjectTaskId taskId, String title, String description, tn.portfolio.reactive.project.controller.TimeEstimation timeEstimation) {
        return projects.save(project.addTask(taskId, title, description, toDomain(timeEstimation)))
                .doOnSuccess(savedProject -> eventPublisher.publish(new TaskAddedToProjectEvent(savedProject.getId(), taskId)))
                .thenReturn(taskId);
    }

    private Mono<Project> findProject(ProjectId projectId){
        return projects.findById(projectId)
                .switchIfEmpty(Mono.error(new UnknownProjectIdException(projectId)));
    }

    private TimeEstimation toDomain(tn.portfolio.reactive.project.controller.TimeEstimation timeEstimation) {
        return new TimeEstimation(timeEstimation.hours(), timeEstimation.minutes());
    }

}