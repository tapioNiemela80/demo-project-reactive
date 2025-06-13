package tn.portfolio.reactive.project.infrastructure;

import org.springframework.data.annotation.*;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import tn.portfolio.reactive.project.domain.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table("projects")
class ProjectEntity implements Persistable<UUID> {
    @Id
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDate plannedEndDate;
    private ProjectStatus status;
    @Version
    private int version;
    @Column("initial_estimated_time_hours")
    private int initialEstimatedTimeHours;
    @Column("initial_estimated_time_minutes")
    private int initialEstimatedTimeMinutes;
    @Transient
    private transient boolean isNew;
    private String contactPersonName;
    private String contactPersonEmail;

    @PersistenceConstructor
    ProjectEntity(UUID id, String name, String description,
                         LocalDateTime createdAt, LocalDate plannedEndDate,
                         ProjectStatus status, int version,
                         int initialEstimatedTimeHours, int initialEstimatedTimeMinutes,
                         String contactPersonName, String contactPersonEmail) {
        this(id, name, description, createdAt, plannedEndDate, status,
                version, initialEstimatedTimeHours, initialEstimatedTimeMinutes, false, contactPersonName, contactPersonEmail);
    }

    ProjectEntity(UUID id, String name, String description,
                         LocalDateTime createdAt, LocalDate plannedEndDate,
                         ProjectStatus status, int version,
                         int initialEstimatedTimeHours, int initialEstimatedTimeMinutes,
                         boolean isNew, String contactPersonName, String contactPersonEmail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.plannedEndDate = plannedEndDate;
        this.status = status;
        this.version = version;
        this.initialEstimatedTimeHours = initialEstimatedTimeHours;
        this.initialEstimatedTimeMinutes = initialEstimatedTimeMinutes;
        this.isNew = isNew;
        this.contactPersonName = contactPersonName;
        this.contactPersonEmail = contactPersonEmail;
    }
    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    ProjectDto toDto(List<ProjectTaskEntity> taskEntities) {
        List<ProjectTaskDto> taskDtos = taskEntities.stream()
                .map(ProjectTaskEntity::toDto)
                .toList();

        return new ProjectDto(
                id,
                name,
                description,
                createdAt,
                plannedEndDate,
                status,
                version,
                initialEstimatedTimeHours,
                initialEstimatedTimeMinutes,
                contactPersonName,
                contactPersonEmail,
                taskDtos
        );
    }
}