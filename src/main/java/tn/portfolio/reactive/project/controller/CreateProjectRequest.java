package tn.portfolio.reactive.project.controller;

import java.time.LocalDate;

public record CreateProjectRequest(
        String name,
        String description,
        LocalDate estimatedEndDate,
        TimeEstimation estimation,
        ContactPersonInput contactPersonInput
) {}