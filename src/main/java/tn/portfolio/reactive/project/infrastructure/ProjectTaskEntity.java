package tn.portfolio.reactive.project.infrastructure;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import tn.portfolio.reactive.project.domain.TaskStatus;

import java.util.Objects;
import java.util.UUID;

@Table("project_tasks")
class ProjectTaskEntity implements Persistable<UUID> {
    @Id
    private UUID id;

    private String title;

    private String description;

    @Column("estimated_time_hours")
    private int estimatedTimeHours;

    @Column("estimated_time_minutes")
    private int estimatedTimeMinutes;

    private TaskStatus taskStatus;

    @Column("actual_time_spent_hours")
    private Integer actualTimeSpentHours;

    @Column("actual_time_spent_minutes")
    private Integer actualTimeSpentMinutes;

    private UUID projectId;

    @PersistenceCreator
    ProjectTaskEntity(UUID id, String title, String description, int estimatedTimeHours, int estimatedTimeMinutes, TaskStatus taskStatus, UUID projectId, Integer actualTimeSpentHours, Integer actualTimeSpentMinutes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.estimatedTimeHours = estimatedTimeHours;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.taskStatus = taskStatus;
        this.projectId = projectId;
        this.actualTimeSpentHours = actualTimeSpentHours;
        this.actualTimeSpentMinutes = actualTimeSpentMinutes;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectTaskEntity other = (ProjectTaskEntity) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    ProjectTaskDto toDto() {
        return new ProjectTaskDto(
                id,
                title,
                description,
                estimatedTimeHours,
                estimatedTimeMinutes,
                taskStatus,
                actualTimeSpentHours,
                actualTimeSpentMinutes,
                projectId
        );
    }

    UUID getProjectId() {
        return projectId;
    }
}