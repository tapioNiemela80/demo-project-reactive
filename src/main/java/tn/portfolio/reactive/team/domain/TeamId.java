package tn.portfolio.reactive.team.domain;

import tn.portfolio.reactive.common.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

@ValueObject
public record TeamId(UUID value) {
    public TeamId {
        Objects.requireNonNull(value, "TeamId value cannot be null");
    }
}
