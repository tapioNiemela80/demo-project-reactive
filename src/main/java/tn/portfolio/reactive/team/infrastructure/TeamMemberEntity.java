package tn.portfolio.reactive.team.infrastructure;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("team_members")
class TeamMemberEntity implements Persistable<UUID> {
    @Id
    private final UUID id;

    private final String name;

    private final String profession;

    private final UUID teamId;

    @PersistenceCreator
    TeamMemberEntity(UUID id, String name, String profession, UUID teamId){
        this.id = id;
        this.name = name;
        this.profession = profession;
        this.teamId = teamId;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamMemberEntity other = (TeamMemberEntity) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    TeamMemberDto toDto(){
        return new TeamMemberDto(id, name, profession, teamId);
    }
}