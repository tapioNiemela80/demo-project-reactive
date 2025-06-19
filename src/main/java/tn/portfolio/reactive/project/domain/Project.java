package tn.portfolio.reactive.project.domain;

import tn.portfolio.reactive.common.domain.ActualSpentTime;
import tn.portfolio.reactive.common.domain.AggregateRoot;
import tn.portfolio.reactive.project.infrastructure.ProjectDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@AggregateRoot
public final class Project {
    private final ProjectId id;
    private final String name;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDate plannedEndDate;
    private final ProjectStatus status;
    private final int version;
    private final TimeEstimation timeEstimation;
    private final List<ProjectTask> tasks;

    private final String contactPersonName;
    private final String contactPersonEmail;

    public Project(ProjectId id,
                   String name,
                   String description,
                   LocalDateTime createdAt,
                   LocalDate plannedEndDate,
                   ProjectStatus status,
                   int version,
                   TimeEstimation timeEstimation,
                   List<ProjectTask> tasks,
                   String contactPersonName,
                   String contactPersonEmail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.plannedEndDate = plannedEndDate;
        this.status = status;
        this.version = version;
        this.timeEstimation = timeEstimation;
        this.tasks = List.copyOf(tasks);
        this.contactPersonName = contactPersonName;
        this.contactPersonEmail = contactPersonEmail;
    }

    public static Project create(ProjectId id, String name, String description, LocalDateTime createdAt,
                                 LocalDate plannedEndDate,
                                 TimeEstimation timeEstimation, String contactPersonName, String contactPersonEmail) {
        return new Project(
                id,
                name,
                description,
                createdAt,
                plannedEndDate,
                ProjectStatus.PLANNED,
                0,
                timeEstimation,
                List.of(),
                contactPersonName,
                contactPersonEmail
        );
    }

    public Project addTask(ProjectTaskId taskId, String taskName, String taskDescription, TimeEstimation estimation) {
        if (isCompleted()) {
            throw new ProjectAlreadyCompletedException(id);
        }
        var currentTotalEstimation = getEstimationOfAllTasks();
        var newEstimation = currentTotalEstimation.add(estimation);
        if (newEstimation.exceedsOther(timeEstimation)) {
            throw new ProjectTimeEstimationWouldBeExceededException("Cannot add any more tasks, project estimation would be exceeded");
        }
        List<ProjectTask> existingTasks = new ArrayList<>(tasks);
        existingTasks.add(ProjectTask.create(taskId, taskName, taskDescription, estimation, id));
        return new Project(
                id, name, description, createdAt, plannedEndDate, status,
                version, timeEstimation, existingTasks, contactPersonName, contactPersonEmail
        );
    }

    public Project completeTask(ProjectTaskId projectTaskId, ActualSpentTime actualSpentTime) {
        verifyContainsTask(projectTaskId);

        var processedTasks = tasks.stream()
                .map(processTask(projectTaskId, actualSpentTime))
                .toList();
        var newStatus = areAllTasksCompleted(processedTasks)
                ? ProjectStatus.COMPLETED
                : status;
        return new Project(
                id, name, description, createdAt, plannedEndDate, newStatus,
                version, timeEstimation, processedTasks, contactPersonName, contactPersonEmail
        );
    }

    private static boolean areAllTasksCompleted(Collection<ProjectTask> tasks) {
        return tasks.stream()
                .allMatch(ProjectTask::isCompleted);
    }

    private Function<ProjectTask, ProjectTask> processTask(ProjectTaskId projectTaskId, ActualSpentTime actualSpentTime) {
        return task -> {
            if (task.hasId(projectTaskId)) {
                return task.complete(actualSpentTime);
            }
            return task;
        };
    }

    public Optional<ProjectTaskSnapshot> getTask(ProjectTaskId projectTaskId) {
        return tasks.stream()
                .filter(task -> task.hasId(projectTaskId))
                .map(task -> task.toSnapshot(id))
                .findFirst();
    }

    private void verifyContainsTask(ProjectTaskId projectTaskId) {
        tasks.stream()
                .filter(task -> task.hasId(projectTaskId))
                .findFirst()
                .orElseThrow(() -> new UnknownProjectTaskIdException(projectTaskId));
    }

    public TimeEstimation getEstimationOfAllTasks() {
        return tasks.stream()
                .map(ProjectTask::getEstimation)
                .reduce(TimeEstimation::add)
                .orElseGet(TimeEstimation::zeroEstimation);
    }

    public boolean isCompleted() {
        return status == ProjectStatus.COMPLETED;
    }

    public ProjectId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project other = (Project) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public boolean isNew() {
        return version == 0;
    }

    ProjectDto toDto() {
        return new ProjectDto(
                id.value(),
                name,
                description,
                createdAt,
                plannedEndDate,
                status,
                version,
                timeEstimation.getHours(),
                timeEstimation.getMinutes(),
                contactPersonName,
                contactPersonEmail,
                tasks.stream()
                        .map(ProjectTask::toDto)
                        .toList()
        );
    }

    public String getContactPersonEmail() {
        return contactPersonEmail;
    }
}
