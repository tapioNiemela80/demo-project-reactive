package tn.portfolio.reactive.team.domain;

import java.util.Objects;
import java.util.UUID;

public record TeamId(UUID value) {
    public TeamId {
        Objects.requireNonNull(value, "TeamId value cannot be null");
    }
}
