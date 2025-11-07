package tn.portfolio.reactive.project.domain;

import tn.portfolio.reactive.project.infrastructure.ProjectDto;
import tn.portfolio.reactive.project.infrastructure.ProjectTaskDto;

import java.util.List;

public final class ProjectDomainMapper {
    public static Project fromDto(ProjectDto dto) {
        List<ProjectTask> tasks = dto.tasks().stream()
                .map(ProjectDomainMapper::fromDto)
                .toList();

        return new Project(
                new ProjectId(dto.id()),
                dto.name(),
                dto.description(),
                dto.createdAt(),
                dto.plannedEndDate(),
                dto.status(),
                dto.version(),
                new TimeEstimation(dto.initialEstimatedTimeHours(), dto.initialEstimatedTimeMinutes()),
                tasks,
                toContactPerson(dto)
        );
    }

    private static ContactPerson toContactPerson(ProjectDto dto) {
        return ContactPerson.rehydrate(dto.name(), dto.contactPersonEmail());
    }

    private static ProjectTask fromDto(ProjectTaskDto dto) {
        return new ProjectTask(
                new ProjectTaskId(dto.id()),
                dto.title(),
                dto.description(),
                new TimeEstimation(dto.estimatedTimeHours(), dto.estimatedTimeMinutes()),
                dto.taskStatus(),
                dto.actualTimeSpent().orElse(null),
                new ProjectId(dto.projectId())
        );
    }

    public static ProjectDto toDto(Project domain) {
        return domain.toDto();
    }
}