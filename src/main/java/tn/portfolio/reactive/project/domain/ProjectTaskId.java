package tn.portfolio.reactive.project.domain;

import java.util.Objects;
import java.util.UUID;

public record ProjectTaskId(UUID value) {
    public ProjectTaskId {
        Objects.requireNonNull(value, "ProjectTaskId value cannot be null");
    }
}