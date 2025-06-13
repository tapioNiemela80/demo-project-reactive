package tn.portfolio.reactive.team.view;

import java.util.UUID;

public record TaskView(
        UUID id,
        String name,
        String description,
        UUID projectTaskId,
        String status,
        UUID assigneeId,
        ActualTimeSpent actualSpentTime
) {
    public boolean isCompleted() {
        return actualSpentTime != null;
    }
}