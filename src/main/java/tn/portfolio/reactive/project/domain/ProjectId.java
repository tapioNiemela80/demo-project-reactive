package tn.portfolio.reactive.project.domain;

import tn.portfolio.reactive.common.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

@ValueObject
public record ProjectId(UUID value) {
    public ProjectId {
        Objects.requireNonNull(value, "ProjectId value cannot be null");
    }
}