package tn.portfolio.reactive.project.infrastructure;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

interface ProjectEntityRepository extends R2dbcRepository<ProjectEntity, UUID> {
}