package tn.portfolio.reactive.team.events;

import tn.portfolio.reactive.common.domain.ActualSpentTime;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.team.domain.TeamTaskId;

public record TeamTaskCompletedEvent(TeamTaskId taskId, ProjectTaskId projectTaskId, ActualSpentTime actualSpentTime) {
}
