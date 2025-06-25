package tn.portfolio.reactive.project.infrastructure;

import tn.portfolio.reactive.common.domain.ActualSpentTime;
import tn.portfolio.reactive.project.domain.TaskStatus;

import java.util.Optional;
import java.util.UUID;

public record ProjectTaskDto(
        UUID id,
        String title,
        String description,
        int estimatedTimeHours,
        int estimatedTimeMinutes,
        TaskStatus taskStatus,
        Integer actualTimeSpentHours,
        Integer actualTimeSpentMinutes,
        UUID projectId
) {
    public Optional<ActualSpentTime> actualTimeSpent() {
        if (actualTimeSpentHours == null) {
            return Optional.empty();
        }
        return Optional.of(new ActualSpentTime(actualTimeSpentHours, actualTimeSpentMinutes));
    }
}