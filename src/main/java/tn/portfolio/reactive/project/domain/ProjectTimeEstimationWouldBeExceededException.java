package tn.portfolio.reactive.project.domain;

public class ProjectTimeEstimationWouldBeExceededException extends RuntimeException {
    public ProjectTimeEstimationWouldBeExceededException(String message) {
        super(message);
    }
}
