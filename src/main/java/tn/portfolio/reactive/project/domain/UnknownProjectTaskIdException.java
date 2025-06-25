package tn.portfolio.reactive.project.domain;

public class UnknownProjectTaskIdException extends RuntimeException {
    private final ProjectTaskId taskId;

    public UnknownProjectTaskIdException(ProjectTaskId taskId) {
        super("Unknown task %s".formatted(taskId));
        this.taskId = taskId;
    }
}