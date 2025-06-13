package tn.portfolio.reactive.project.domain;

import tn.portfolio.reactive.common.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

@ValueObject
public record ProjectTaskId(UUID value) {
    public ProjectTaskId {
        Objects.requireNonNull(value, "ProjectTaskId value cannot be null");
    }
}