package tn.portfolio.reactive.team.controller;

import java.util.List;
import java.util.UUID;

public record TeamView(
        UUID id,
        String name,
        List<MemberView> members,
        List<TaskView> tasks
) {

    public ActualSpentTime getActualWorkDone(){
        return tasks.stream()
                .filter(TaskView::isCompleted)
                .map(TaskView::actualSpentTime)
                .reduce(ActualSpentTime.zero(), ActualSpentTime::add);
    }

}
