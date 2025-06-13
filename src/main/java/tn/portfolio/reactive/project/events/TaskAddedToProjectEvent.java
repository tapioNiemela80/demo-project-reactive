package tn.portfolio.reactive.project.events;

import tn.portfolio.reactive.project.domain.ProjectId;
import tn.portfolio.reactive.project.domain.ProjectTaskId;

public record TaskAddedToProjectEvent(ProjectId projectId, ProjectTaskId taskId) {}