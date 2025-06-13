package tn.portfolio.reactive.team.infrastructure;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

interface TeamMemberEntityRepository extends R2dbcRepository<TeamMemberEntity, UUID> {
    Flux<TeamMemberEntity> findByTeamId(UUID teamId);
    Mono<Void> deleteByTeamId(UUID teamId);
}