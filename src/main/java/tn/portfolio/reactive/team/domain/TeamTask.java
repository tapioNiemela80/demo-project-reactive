package tn.portfolio.reactive.team.domain;

import tn.portfolio.reactive.common.domain.ActualSpentTime;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.team.infrastructure.TeamTaskDto;

import java.util.Objects;
import java.util.UUID;

class TeamTask {
    private final TeamTaskId id;
    private final ProjectTaskId projectTaskId;
    private final String name;
    private final String description;
    private final TeamTaskStatus status;
    private final TeamMemberId assigneeId;
    private final ActualSpentTime actualSpentTime;

    TeamTask(TeamTaskId id, ProjectTaskId projectTaskId, String name, String description, TeamTaskStatus status, TeamMemberId assigneeId,ActualSpentTime actualSpentTime) {
        this.id = id;
        this.projectTaskId = projectTaskId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.assigneeId = assigneeId;
        this.actualSpentTime = actualSpentTime;
    }

    static TeamTask createNew(TeamTaskId id, ProjectTaskId projectTaskId, String name, String description){
        return new TeamTask(id, projectTaskId, name, description, TeamTaskStatus.NOT_ASSIGNED, null, null);
    }
    boolean canBeDeleted(){
        return status == TeamTaskStatus.NOT_ASSIGNED;
    }
    TeamTask assignTo(TeamMemberId assigneeId){
        if (this.status != TeamTaskStatus.NOT_ASSIGNED) {
            throw new IllegalArgumentException("Task already assigned or in progress.");
        }
        return new TeamTask(id, projectTaskId, name, description, TeamTaskStatus.ASSIGNED, assigneeId, actualSpentTime);
    }
    TeamTask markInProgress(){
        if (this.status != TeamTaskStatus.ASSIGNED) {
            throw new IllegalArgumentException("Task needs to be assigned before it can be put to in progress.");
        }
        return new TeamTask(id, projectTaskId, name, description, TeamTaskStatus.IN_PROGRESS, assigneeId, actualSpentTime);
    }
    TeamTask complete(ActualSpentTime actualTimeWasSpent) {
        if (this.status != TeamTaskStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("task not in progress");
        }
        return new TeamTask(id, projectTaskId, name, description, TeamTaskStatus.COMPLETED, assigneeId, actualTimeWasSpent);
    }
    TeamTask unassign() {
        if(this.status != TeamTaskStatus.ASSIGNED){
            throw new IllegalArgumentException("Task is not assigned");
        }
        return new TeamTask(id, projectTaskId, name, description, TeamTaskStatus.NOT_ASSIGNED, null, actualSpentTime);
    }
    boolean hasId(TeamTaskId expected) {
        return id.equals(expected);
    }

    boolean isAssignedTo(TeamMemberId memberId) {
        return assigneeId.equals(memberId.value());
    }
    ProjectTaskId getOriginalTaskId() {
        return projectTaskId;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamTask other = (TeamTask) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    TeamTaskDto toDto(TeamId teamId){
        return new TeamTaskDto(id.value(), name, description, projectTaskId.value(), status, getAssigned(),
                getTimeHours(), getTimeMinutes(), teamId.value());
    }

    private Integer getTimeHours() {
        return actualSpentTime == null ? null : actualSpentTime.getHours();
    }

    private Integer getTimeMinutes() {
        return actualSpentTime == null ? null : actualSpentTime.getMinutes();
    }

    private UUID getAssigned() {
        return assigneeId== null ? null : assigneeId.value();
    }

}