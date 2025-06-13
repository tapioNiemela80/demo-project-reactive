package tn.portfolio.reactive.team.repository;

import reactor.core.publisher.Mono;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.team.domain.Team;
import tn.portfolio.reactive.team.domain.TeamId;

import java.util.UUID;

public interface TeamRepository {
    Mono<Team> findById(TeamId id);
    Mono<Team> save(Team team);

    Mono<Team> findByProjectTaskId(ProjectTaskId projectTaskId);
}
