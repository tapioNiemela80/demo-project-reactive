package tn.portfolio.reactive.team.domain;

public class UnknownTeamMemberIdException extends RuntimeException {
    private final TeamMemberId teamMemberId;

    public UnknownTeamMemberIdException(TeamMemberId teamMemberId) {
        super("Unknown team member %s".formatted(teamMemberId));
        this.teamMemberId = teamMemberId;
    }

    public TeamMemberId getTeamMemberId() {
        return teamMemberId;
    }
}