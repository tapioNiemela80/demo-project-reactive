package tn.portfolio.reactive.project.controller;

public record AddTaskRequest(
        String name,
        String description,
        TimeEstimation estimation
) {
}