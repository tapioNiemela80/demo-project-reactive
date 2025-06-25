package tn.portfolio.reactive.project.repository;

import reactor.core.publisher.Mono;
import tn.portfolio.reactive.project.domain.Project;
import tn.portfolio.reactive.project.domain.ProjectId;
import tn.portfolio.reactive.project.domain.ProjectTaskId;

public interface ProjectRepository {
    Mono<Project> findById(ProjectId id);

    Mono<Project> save(Project project);

    Mono<Project> findByTaskId(ProjectTaskId taskId);
}