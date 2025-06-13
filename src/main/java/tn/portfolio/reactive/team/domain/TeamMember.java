package tn.portfolio.reactive.team.domain;

import tn.portfolio.reactive.team.infrastructure.TeamMemberDto;

import java.util.Objects;

public class TeamMember {
    private final TeamMemberId id;

    private final String name;

    private final String profession;

    TeamMember(TeamMemberId id, String name, String profession){
        this.id = id;
        this.name = name;
        this.profession = profession;
    }

    static TeamMember createNew(TeamMemberId memberId, String name, String profession){
        return new TeamMember(memberId, name, profession);
    }

    boolean hasId(TeamMemberId expected) {
        return id.equals(expected);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamMember other = (TeamMember) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    TeamMemberDto toDto(TeamId teamId){
        return new TeamMemberDto(id.value(), name, profession, teamId.value());
    }
}