package tn.portfolio.reactive.team.infrastructure;

import java.util.List;

final class TeamPersistenceMapper {
    static TeamDto fromEntity(TeamEntity entity, List<TeamMemberEntity> members, List<TeamTaskEntity> tasks){
        return entity.toDto(members, tasks);
    }

    static TeamEntity toEntity(TeamDto dto, boolean isNew){
        return new TeamEntity(dto.id(), dto.name(), dto.version(), isNew);
    }

    static TeamMemberEntity toEntity(TeamMemberDto dto){
        return new TeamMemberEntity(dto.id(), dto.name(), dto.profession(), dto.teamId());
    }

    static TeamTaskEntity toEntity(TeamTaskDto dto){
        return new TeamTaskEntity(dto.id(), dto.projectTaskId(), dto.name(),
                dto.description(), dto.status(), dto.assigneeId(),
                dto.actualTimeSpentHours(), dto.actualTimeSpentMinutes(), dto.teamId());
    }
}
