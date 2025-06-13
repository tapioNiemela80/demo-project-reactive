package tn.portfolio.reactive.team.domain;

public class UnknownTeamIdException extends RuntimeException{
    private final TeamId teamId;

    public UnknownTeamIdException(TeamId teamId) {
        super("Unknown team %s".formatted(teamId));
        this.teamId = teamId;
    }
}