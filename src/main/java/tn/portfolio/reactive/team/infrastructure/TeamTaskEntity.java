package tn.portfolio.reactive.team.infrastructure;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import tn.portfolio.reactive.team.domain.TeamTaskStatus;

import java.util.Objects;
import java.util.UUID;

@Table("team_tasks")
class TeamTaskEntity implements Persistable<UUID> {
    @Id
    private final UUID id;
    private final UUID projectTaskId;
    private final String name;
    private final String description;
    private final TeamTaskStatus status;
    private final UUID assigneeId;
    @Column("actual_time_spent_hours")
    private final Integer actualTimeSpentHours;
    @Column("actual_time_spent_minutes")
    private final Integer actualTimeSpentMinutes;
    @Column
    private final UUID teamId;

    @PersistenceCreator
    TeamTaskEntity(UUID id, UUID projectTaskId, String name, String description, TeamTaskStatus status, UUID assigneeId, Integer actualTimeSpentHours, Integer actualTimeSpentMinutes, UUID teamId){
        this.id = id;
        this.projectTaskId = projectTaskId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.assigneeId = assigneeId;
        this.actualTimeSpentHours = actualTimeSpentHours;
        this.actualTimeSpentMinutes = actualTimeSpentMinutes;
        this.teamId = teamId;
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
        TeamTaskEntity other = (TeamTaskEntity) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    UUID getTeamId() {
        return teamId;
    }

    TeamTaskDto toDto(){
        return new TeamTaskDto(id, name, description, projectTaskId, status, assigneeId, actualTimeSpentHours, actualTimeSpentMinutes, teamId);
    }
}