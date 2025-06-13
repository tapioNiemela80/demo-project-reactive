package tn.portfolio.reactive.project.domain;

import java.util.Objects;
import java.util.UUID;

public record ProjectId(UUID value) {
    public ProjectId {
        Objects.requireNonNull(value, "ProjectId value cannot be null");
    }
}