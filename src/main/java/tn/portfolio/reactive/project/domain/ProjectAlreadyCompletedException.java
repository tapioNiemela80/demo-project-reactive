package tn.portfolio.reactive.project.domain;

public class ProjectAlreadyCompletedException extends RuntimeException {

    private final ProjectId projectId;
    public ProjectAlreadyCompletedException(ProjectId projectId) {
        super("Project %s already completed".formatted(projectId));
        this.projectId = projectId;
    }
}