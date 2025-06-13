package tn.portfolio.reactive.team.infrastructure;

import java.util.List;

public final class TeamPersistenceMapper {
    public static TeamDto fromEntity(TeamEntity entity, List<TeamMemberEntity> members, List<TeamTaskEntity> tasks){
        return entity.toDto(members, tasks);
    }

    public static TeamEntity toEntity(TeamDto dto, boolean isNew){
        return new TeamEntity(dto.id(), dto.name(), dto.version(), isNew);
    }

    public static TeamMemberEntity toEntity(TeamMemberDto dto){
        return new TeamMemberEntity(dto.id(), dto.name(), dto.profession(), dto.teamId());
    }

    public static TeamTaskEntity toEntity(TeamTaskDto dto){
        return new TeamTaskEntity(dto.id(), dto.projectTaskId(), dto.name(),
                dto.description(), dto.status(), dto.assigneeId(),
                dto.actualTimeSpentHours(), dto.actualTimeSpentMinutes(), dto.teamId());
    }
}
