package tn.portfolio.reactive.project.infrastructure;

import java.util.List;

final class ProjectPersistenceMapper {
    static ProjectDto fromEntity(ProjectEntity entity, List<ProjectTaskEntity> taskEntities) {
        return entity.toDto(taskEntities);
    }

    static ProjectEntity toEntity(ProjectDto dto, boolean isNew) {
        return new ProjectEntity(dto.id(),
                dto.name(), dto.description(), dto.createdAt(),
                dto.plannedEndDate(), dto.status(), dto.version(),
                dto.initialEstimatedTimeHours(), dto.initialEstimatedTimeMinutes(),
                isNew,
                dto.contactPersonName(), dto.contactPersonEmail());
    }

    static ProjectTaskEntity toEntity(ProjectTaskDto dto) {
        return new ProjectTaskEntity(dto.id(), dto.title(), dto.description(), dto.estimatedTimeHours(),
                dto.estimatedTimeMinutes(), dto.taskStatus(), dto.projectId(), dto.actualTimeSpentHours(), dto.actualTimeSpentMinutes());
    }
}