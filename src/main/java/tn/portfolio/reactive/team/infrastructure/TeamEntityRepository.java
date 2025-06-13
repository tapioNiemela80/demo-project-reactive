package tn.portfolio.reactive.team.infrastructure;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

interface TeamEntityRepository extends R2dbcRepository<TeamEntity, UUID> { }