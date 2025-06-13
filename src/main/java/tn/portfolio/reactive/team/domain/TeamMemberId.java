package tn.portfolio.reactive.team.domain;

import tn.portfolio.reactive.common.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

@ValueObject
public record TeamMemberId(UUID value) {
    public TeamMemberId{
        Objects.requireNonNull(value, "TeamMemberId value cannot be null");
    }
}
