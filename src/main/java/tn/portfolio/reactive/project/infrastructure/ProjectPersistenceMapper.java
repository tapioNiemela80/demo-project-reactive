package tn.portfolio.reactive.project.infrastructure;

import java.util.List;

public final class ProjectPersistenceMapper {
    public static ProjectDto fromEntity(ProjectEntity entity, List<ProjectTaskEntity> taskEntities) {
        return entity.toDto(taskEntities);
    }

    public static ProjectEntity toEntity(ProjectDto dto, boolean isNew) {
        ProjectEntity entity = new ProjectEntity(dto.id(),
                dto.name(), dto.description(), dto.createdAt(),
                dto.plannedEndDate(), dto.status(), dto.version(),
                dto.initialEstimatedTimeHours(), dto.initialEstimatedTimeMinutes(),
                isNew,
                dto.contactPersonName(), dto.contactPersonEmail());
        return entity;
    }

    public static ProjectTaskEntity toEntity(ProjectTaskDto dto) {
        ProjectTaskEntity entity = new ProjectTaskEntity(dto.id(), dto.title(), dto.description(), dto.estimatedTimeHours(),
                dto.estimatedTimeMinutes(), dto.taskStatus(), dto.projectId(), dto.actualTimeSpentHours(), dto.actualTimeSpentMinutes());
        return entity;
    }
}