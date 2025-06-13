package tn.portfolio.reactive.team.domain;

import java.util.Objects;
import java.util.UUID;

public record TeamMemberId(UUID value) {
    public TeamMemberId{
        Objects.requireNonNull(value, "TeamMemberId value cannot be null");
    }
}
