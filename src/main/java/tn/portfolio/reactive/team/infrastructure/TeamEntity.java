package tn.portfolio.reactive.team.infrastructure;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Table("teams")
public class TeamEntity implements Persistable<UUID> {
    @Id
    private final UUID id;
    private final String name;
    @Version
    private final int version;
    @Transient
    private final transient boolean isNew;

    @PersistenceCreator
    TeamEntity(UUID id, String name, int version){
        this(id, name, version, false);
    }

    TeamEntity(UUID id, String name, int version, boolean isNew) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.isNew = isNew;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamEntity other = (TeamEntity) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    TeamDto toDto(List<TeamMemberEntity> members, List<TeamTaskEntity> tasks){
        return new TeamDto(id,
                name,
                version,
                members.stream().map(TeamMemberEntity::toDto).toList(),
                tasks.stream().map(TeamTaskEntity::toDto).toList());
    }
}