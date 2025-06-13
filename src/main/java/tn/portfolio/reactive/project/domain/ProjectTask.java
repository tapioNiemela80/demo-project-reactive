package tn.portfolio.reactive.project.domain;

// ProjectTask.java

import tn.portfolio.reactive.common.domain.ActualSpentTime;
import tn.portfolio.reactive.project.infrastructure.ProjectTaskDto;

import java.util.Objects;
import java.util.Optional;

final class ProjectTask {
    private final ProjectTaskId id;
    private final String title;
    private final String description;
    private final TimeEstimation timeEstimation;
    private final TaskStatus taskStatus;
    private final ActualSpentTime actualSpentTime;

    private final ProjectId projectId;

    ProjectTask(ProjectTaskId id, String title, String description,
                       TimeEstimation timeEstimation,
                       TaskStatus taskStatus,
                        ActualSpentTime actualSpentTime, ProjectId projectId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timeEstimation = timeEstimation;
        this.taskStatus = taskStatus;
        this.actualSpentTime = actualSpentTime;
        this.projectId = projectId;
    }

    static ProjectTask create(ProjectTaskId id, String title, String description,
                                     TimeEstimation timeEstimation, ProjectId projectId) {
        return new ProjectTask(
                id,
                title,
                description,
                timeEstimation,
                TaskStatus.INCOMPLETE,
                null,
                projectId
        );
    }

    TimeEstimation getEstimation() {
        return timeEstimation;
    }

    ProjectTask complete(ActualSpentTime actualSpentTime){
        return new ProjectTask(id, title, description, timeEstimation, TaskStatus.COMPLETE, actualSpentTime, projectId);
    }

    boolean hasId(ProjectTaskId expected){
        return expected.equals(id);
    }

    ProjectTaskSnapshot toSnapshot(ProjectId projectId) {
        return new ProjectTaskSnapshot(id, projectId, title, description, getEstimation());
    }

    public boolean isCompleted() {
        return taskStatus == TaskStatus.COMPLETE;
    }

    @Override
    public String toString() {
        return "ProjectTask{" +
                "id=" + id +
                ", name='" + title + '\'' +
                ", description='" + description + '\'' +
                ", estimation=" + timeEstimation +
                ", taskStatus=" + taskStatus +
                ", actualSpentTime=" + actualSpentTime +
                ", projectId=" + projectId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectTask other = (ProjectTask) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    ProjectTaskDto toDto() {
        return new ProjectTaskDto(
                id.value(),
                title,
                description,
                getEstimation().getHours(),
                getEstimation().getMinutes(),
                taskStatus,
                actualTimeSpent().map(ActualSpentTime::getHours).orElse(null),
                actualTimeSpent().map(ActualSpentTime::getMinutes).orElse(null),
                projectId.value()
        );
    }


    private Optional<ActualSpentTime> actualTimeSpent(){
        return Optional.ofNullable(actualSpentTime);
    }
}
