package tn.portfolio.reactive.team.infrastructure;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.team.controller.ActualSpentTime;
import tn.portfolio.reactive.team.controller.MemberView;
import tn.portfolio.reactive.team.controller.TaskView;
import tn.portfolio.reactive.team.controller.TeamView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TeamViewService {

    private final TeamViewRepository repository;

    public TeamViewService(TeamViewRepository repository) {
        this.repository = repository;
    }

    public Mono<TeamView> findOne(UUID teamId) {
        return repository.findTeamViewByTeamId(teamId)
                .collectList()
                .flatMap(rows -> {
                    if (rows.isEmpty()) return Mono.empty();

                    TeamViewRow first = rows.get(0);
                    List<MemberView> members = getMembers(rows);
                    List<TaskView> tasks = getTasks(rows);

                    return Mono.just(new TeamView(
                            first.teamId(),
                            first.teamName(),
                            members,
                            tasks
                    ));
                });
    }

    private List<TaskView> getTasks(List<TeamViewRow> rows) {
        return rows.stream()
                .filter(row -> row.taskId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                TeamViewRow::taskId,
                                row -> getTaskView(row),
                                (a, b) -> a
                        ),
                        map -> new ArrayList<>(map.values())
                ));
    }

    private TaskView getTaskView(TeamViewRow row) {
        return new TaskView(
                row.taskId(),
                row.taskName(),
                row.taskDescription(),
                row.projectTaskId(),
                row.taskStatus(),
                row.taskAssigneeId(),
                actualTimeSpent(row.actualTimeSpentHours(), row.actualTimeSpentMinutes())
        );
    }

    private List<MemberView> getMembers(List<TeamViewRow> rows) {
        return rows.stream()
                .filter(row -> row.memberId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                TeamViewRow::memberId,
                                row -> new MemberView(
                                        row.memberId(),
                                        row.memberName(),
                                        row.memberProfession()
                                ),
                                (a, b) -> a // ignore duplicates
                        ),
                        map -> new ArrayList<>(map.values())
                ));
    }

    private ActualSpentTime actualTimeSpent(Integer actualTimeSpentHours, Integer actualTimeSpentMinutes) {
        if(actualTimeSpentHours == null){
            return null;
        }
        return new ActualSpentTime(actualTimeSpentHours, actualTimeSpentMinutes);
    }
}