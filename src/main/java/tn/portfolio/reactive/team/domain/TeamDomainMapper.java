package tn.portfolio.reactive.team.domain;

import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.team.infrastructure.TeamDto;
import tn.portfolio.reactive.team.infrastructure.TeamMemberDto;
import tn.portfolio.reactive.team.infrastructure.TeamTaskDto;

public final class TeamDomainMapper {
    public static Team fromDto(TeamDto teamDto){
        return new Team(new TeamId(teamDto.id()), teamDto.name(), teamDto.version(),
                teamDto.members().stream().map(TeamDomainMapper::memberFromDto).toList(),
                teamDto.tasks().stream().map(TeamDomainMapper::taskFromDto).toList());
    }

    private static TeamMember memberFromDto(TeamMemberDto dto){
        return new TeamMember(new TeamMemberId(dto.id()), dto.name(), dto.profession());
    }

    private static TeamTask taskFromDto(TeamTaskDto dto){
        return new TeamTask(new TeamTaskId(dto.id()), new ProjectTaskId(dto.projectTaskId()), dto.name(), dto.description(), dto.status(),
                dto.assignedTo().orElse(null), dto.actualSpentTime().orElse(null));
    }

    public static TeamDto toDto(Team team){
        return team.toDto();
    }

}
