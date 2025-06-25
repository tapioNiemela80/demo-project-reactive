package tn.portfolio.reactive.team.domain;

public class UnknownTeamTaskIdException extends RuntimeException {
    private final TeamTaskId taskId;

    public UnknownTeamTaskIdException(TeamTaskId taskId) {
        super("Unknown task %s".formatted(taskId));
        this.taskId = taskId;
    }

    public TeamTaskId getTaskId() {
        return taskId;
    }
}