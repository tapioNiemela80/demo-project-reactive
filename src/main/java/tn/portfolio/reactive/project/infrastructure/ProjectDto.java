package tn.portfolio.reactive.project.infrastructure;

import tn.portfolio.reactive.project.domain.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProjectDto(
        UUID id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDate plannedEndDate,
        ProjectStatus status,
        int version,
        int initialEstimatedTimeHours,
        int initialEstimatedTimeMinutes,
        String contactPersonName,
        String contactPersonEmail,
        List<ProjectTaskDto> tasks
) {}
