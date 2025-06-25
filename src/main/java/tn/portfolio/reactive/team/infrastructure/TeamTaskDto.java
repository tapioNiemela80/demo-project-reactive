package tn.portfolio.reactive.team.infrastructure;

import tn.portfolio.reactive.common.domain.ActualSpentTime;
import tn.portfolio.reactive.team.domain.TeamMemberId;
import tn.portfolio.reactive.team.domain.TeamTaskStatus;

import java.util.Optional;
import java.util.UUID;

public record TeamTaskDto(UUID id, String name, String description, UUID projectTaskId, TeamTaskStatus status,
                          UUID assigneeId, Integer actualTimeSpentHours, Integer actualTimeSpentMinutes, UUID teamId) {
    public Optional<TeamMemberId> assignedTo() {
        return assigneeId == null ? Optional.empty() : Optional.of(new TeamMemberId(assigneeId));
    }

    public Optional<ActualSpentTime> actualSpentTime() {
        return actualTimeSpentHours == null ? Optional.empty() : Optional.of(new ActualSpentTime(actualTimeSpentHours, actualTimeSpentMinutes));
    }
}
