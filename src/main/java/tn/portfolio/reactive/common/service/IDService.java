package tn.portfolio.reactive.common.service;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.project.domain.ProjectId;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.team.domain.TeamId;
import tn.portfolio.reactive.team.domain.TeamMemberId;
import tn.portfolio.reactive.team.domain.TeamTaskId;

import java.util.UUID;

@Component
public class IDService {    //in theory UUIDs could be fetched from external systems also

    public Mono<ProjectTaskId> newProjectTaskId() {
        return Mono.fromSupplier(UUID::randomUUID)
                .map(ProjectTaskId::new);
    }

    public Mono<ProjectId> newProjectId() {
        return Mono.fromSupplier(UUID::randomUUID)
                .map(ProjectId::new);
    }

    public Mono<TeamId> newTeamId() {
        return Mono.fromSupplier(UUID::randomUUID)
                .map(TeamId::new);
    }

    public Mono<TeamMemberId> newTeamMemberId() {
        return Mono.fromSupplier(UUID::randomUUID)
                .map(TeamMemberId::new);
    }

    public Mono<TeamTaskId> newTeamTaskId() {
        return Mono.fromSupplier(UUID::randomUUID)
                .map(TeamTaskId::new);
    }

}
