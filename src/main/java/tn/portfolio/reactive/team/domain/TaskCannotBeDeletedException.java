package tn.portfolio.reactive.team.domain;

public class TaskCannotBeDeletedException extends RuntimeException {
    private final TeamTaskId taskId;
    public TaskCannotBeDeletedException(TeamTaskId taskId) {
        super("Task cannot be deleted %s".formatted(taskId));
        this.taskId = taskId;
    }
}