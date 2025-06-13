package tn.portfolio.reactive.team.domain;
public class TeamMemberHasAssignedTasksException extends RuntimeException {
    private final TeamMemberId memberId;
    public TeamMemberHasAssignedTasksException(TeamMemberId memberId) {
        super("Team member %s has assigned tasks ".formatted(memberId));
        this.memberId = memberId;
    }
}