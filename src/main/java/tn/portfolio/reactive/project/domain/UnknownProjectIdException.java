package tn.portfolio.reactive.project.domain;
public class UnknownProjectIdException extends RuntimeException{
    private final ProjectId givenId;

    public UnknownProjectIdException(ProjectId givenId) {
        super("Unknown project id %s".formatted(givenId));
        this.givenId = givenId;
    }
}