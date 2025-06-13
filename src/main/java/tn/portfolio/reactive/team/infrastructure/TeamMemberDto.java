package tn.portfolio.reactive.team.infrastructure;

import java.util.UUID;

public record TeamMemberDto(UUID id, String name, String profession, UUID teamId) {
}
