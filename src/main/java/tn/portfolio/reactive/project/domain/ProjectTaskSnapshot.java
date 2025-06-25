package tn.portfolio.reactive.project.domain;

public record ProjectTaskSnapshot(ProjectTaskId projectTaskId, ProjectId projectId, String title, String description,
                                  TimeEstimation timeEstimation) {
}