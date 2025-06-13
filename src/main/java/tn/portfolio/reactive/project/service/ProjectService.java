package tn.portfolio.reactive.project.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.common.ReactiveDomainEventPublisher;
import tn.portfolio.reactive.common.service.DateService;
import tn.portfolio.reactive.common.service.IDService;
import tn.portfolio.reactive.project.controller.ContactPersonInput;
import tn.portfolio.reactive.project.domain.Project;
import tn.portfolio.reactive.project.domain.ProjectId;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.project.domain.TimeEstimation;
import tn.portfolio.reactive.project.events.TaskAddedToProjectEvent;
import tn.portfolio.reactive.project.repository.ProjectRepository;

import java.time.LocalDate;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final IDService idService;
    private final DateService dateService;
    private final ReactiveDomainEventPublisher eventPublisher;

    public ProjectService(ProjectRepository projectRepository, IDService idService, DateService dateService, ReactiveDomainEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.idService = idService;
        this.dateService = dateService;
        this.eventPublisher = eventPublisher;
    }

    public Mono<Project> createProject(String name, String description,
                                       LocalDate plannedEndDate,
                                       tn.portfolio.reactive.project.controller.TimeEstimation timeEstimation,
                                       ContactPersonInput contactPersonInput) {
        return idService.newProjectId()
                .map(projectId -> Project.create(projectId, name, description, dateService.now(), plannedEndDate, toDomain(timeEstimation), contactPersonInput.name(), contactPersonInput.email()))
                .flatMap(projectRepository::save);

    }

    public Mono<ProjectTaskId> addTaskToProject(ProjectId projectId, String title, String description,
                                                tn.portfolio.reactive.project.controller.TimeEstimation timeEstimation) {
        /*return projectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Project not found: " + projectId)))
                .map(project -> addTask(project, name, description, toDomain(estimation)))
                .flatMap(tuple -> projectRepository.save(tuple.getT1())
                        .thenReturn(tuple.getT2()))
                .doOnSuccess(id -> eventPublisher.publish(new TaskAddedToProjectEvent(projectId, taskId)));
         */
        return idService.newProjectTaskId() // haetaan ID reaktiivisesti
                .flatMap(taskId ->
                        projectRepository.findById(projectId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Project not found " + projectId)))
                                .map(project -> project.addTask(taskId, title, description, toDomain(timeEstimation)))
                                .flatMap(updatedProject ->
                                        projectRepository.save(updatedProject)
                                                .doOnSuccess(project -> System.out.println("---------> " + project + " <------"))
                                                .thenReturn(taskId) // palautetaan ID
                                )
                                .doOnSuccess(aTaskId -> eventPublisher.publish(new TaskAddedToProjectEvent(projectId, aTaskId)))
                );
    }

    private TimeEstimation toDomain(tn.portfolio.reactive.project.controller.TimeEstimation timeEstimation) {
        return new TimeEstimation(timeEstimation.hours(), timeEstimation.minutes());
    }

    public Mono<Project> rename(ProjectId projectId, String newName) {
        return projectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Project not found: " + projectId)))
                .map(project -> project.rename(newName))
                .flatMap(projectRepository::save);
    }
}