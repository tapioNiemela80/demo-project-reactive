package tn.portfolio.reactive.team.domain;

import tn.portfolio.reactive.common.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

@ValueObject
public record TeamTaskId(UUID value) {
    public TeamTaskId {
        Objects.requireNonNull(value, "TeamTaskId value cannot be null");
    }
}
