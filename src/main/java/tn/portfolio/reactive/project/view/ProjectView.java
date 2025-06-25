package tn.portfolio.reactive.project.view;

import java.util.List;
import java.util.UUID;

public record ProjectView(UUID id,
                          String name,
                          String description,
                          boolean isCompleted,
                          String contactPersonEmail,
                          TimeEstimate initialEstimation,
                          List<TaskView> tasks) {

    public TimeEstimate getRemainingEstimation() {
        return initialEstimation.subtract(getCompletedEstimation());
    }

    public TimeEstimate getCompletedEstimation() {
        return tasks.stream()
                .filter(TaskView::isCompleted)
                .map(TaskView::timeEstimate)
                .reduce(TimeEstimate.zeroEstimation(), TimeEstimate::add);
    }

    public ActualTimeSpent getActualTimeSpent() {
        return tasks.stream()
                .filter(TaskView::isCompleted)
                .map(TaskView::actualTimeSpent)
                .reduce(ActualTimeSpent.zero(), ActualTimeSpent::add);
    }

}
