package tn.portfolio.reactive.project.infrastructure;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

interface ProjectTaskEntityRepository extends R2dbcRepository<ProjectTaskEntity, UUID> {

    Flux<ProjectTaskEntity> findByProjectId(UUID projectId);
    Mono<Void> deleteByProjectId(UUID projectId);

}