package tn.portfolio.reactive.team.infrastructure;

import java.util.List;
import java.util.UUID;

public record TeamDto(UUID id, String name, int version, List<TeamMemberDto> members, List<TeamTaskDto> tasks) {
}
