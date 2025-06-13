package tn.portfolio.reactive.team.infrastructure;

import java.util.UUID;

public record TeamViewRow(
        UUID teamId,
        String teamName,

        UUID memberId,
        String memberName,
        String memberProfession,

        UUID taskId,
        String taskName,
        String taskDescription,
        UUID projectTaskId,
        String taskStatus,
        UUID taskAssigneeId,
        Integer actualTimeSpentHours,
        Integer actualTimeSpentMinutes
) {}