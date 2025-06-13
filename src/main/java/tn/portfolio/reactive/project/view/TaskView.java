package tn.portfolio.reactive.project.view;

import tn.portfolio.reactive.project.controller.TimeEstimation;
import tn.portfolio.reactive.team.controller.ActualSpentTime;

import java.util.UUID;

public record TaskView(UUID id, String title, String description, boolean isCompleted, TimeEstimation timeEstimation, ActualSpentTime actualSpentTime) { }
