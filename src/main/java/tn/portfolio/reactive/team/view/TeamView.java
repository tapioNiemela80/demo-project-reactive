package tn.portfolio.reactive.team.view;

import java.util.List;
import java.util.UUID;

public record TeamView(
        UUID id,
        String name,
        List<MemberView> members,
        List<TaskView> tasks
) {

    public ActualTimeSpent getActualWorkDone(){
        return tasks.stream()
                .filter(TaskView::isCompleted)
                .map(TaskView::actualSpentTime)
                .reduce(ActualTimeSpent.zero(), ActualTimeSpent::add);
    }

}
