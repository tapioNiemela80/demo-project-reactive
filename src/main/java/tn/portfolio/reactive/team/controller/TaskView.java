package tn.portfolio.reactive.team.controller;

import java.util.UUID;

public record TaskView(
        UUID id,
        String name,
        String description,
        UUID projectTaskId,
        String status,
        UUID assigneeId,
        ActualSpentTime actualSpentTime
) {
    public boolean isCompleted() {
        return actualSpentTime != null;
    }
}