package tn.portfolio.reactive.project.infrastructure;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.project.controller.TimeEstimation;
import tn.portfolio.reactive.project.view.ProjectView;
import tn.portfolio.reactive.project.view.TaskView;
import tn.portfolio.reactive.team.controller.ActualSpentTime;

import java.util.UUID;

@Service
public class ProjectViewService {
    private final ProjectViewRepository repository;

    public ProjectViewService(ProjectViewRepository repository) {
        this.repository = repository;
    }

    public Mono<ProjectView> getProjectView(UUID id) {
        return repository.findProjectWithTasks(id)
                .collectList()
                .flatMap(rows -> {
                    if (rows.isEmpty()) return Mono.empty(); //TODO
                    var first = rows.get(0);
                    var tasks = rows.stream()
                            .filter(r -> r.taskId() != null)
                            .map(this::from)
                            .toList();
                    return Mono.just(new ProjectView(
                            first.id(), first.name(), first.description(), first.projectStatus().equals("COMPLETED")
                            , first.contactPersonEmail(), new TimeEstimation(first.projectEstimateHours(), first.projectEstimateMinutes()), tasks));
                });
    }

    private TaskView from(ProjectTaskRow row) {
        return new TaskView(row.taskId(), row.taskTitle(),
                row.taskDescription(), row.taskStatus().equals("COMPLETE"), new TimeEstimation(row.taskEstimateHours(), row.taskEstimateMinutes()), timeSpent(row));
    }

    private ActualSpentTime timeSpent(ProjectTaskRow row) {
        if (row.actualHours() == null) {
            return null;
        }
        return new ActualSpentTime(row.actualHours(), row.actualMinutes());
    }
}