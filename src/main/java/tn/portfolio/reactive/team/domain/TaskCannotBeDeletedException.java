package tn.portfolio.reactive.team.domain;

public class TaskCannotBeDeletedException extends RuntimeException {
    private final TeamTaskId taskId;
    public TaskCannotBeDeletedException(TeamTaskId taskId) {
        super("Task %s needs to be unassigned before it can be deleted".formatted(taskId));
        this.taskId = taskId;
    }
}