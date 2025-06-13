package tn.portfolio.reactive.team.infrastructure;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

interface TeamTaskEntityRepository extends R2dbcRepository<TeamTaskEntity, UUID> {
    Flux<TeamTaskEntity> findByTeamId(UUID teamId);

    Mono<Void> deleteByTeamId(UUID teamId);

    Mono<TeamTaskEntity> findByProjectTaskId(UUID projectTaskId);

}