package tn.portfolio.reactive.project.infrastructure;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.project.view.*;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectViewService {
    private final ProjectViewRepository repository;

    public ProjectViewService(ProjectViewRepository repository) {
        this.repository = repository;
    }

    public Flux<ProjectsView> findAll(){
        return repository.findAllProjectsViewRows()
                .map(data -> new ProjectsView(data.id(), data.name(), data.description()));
    }

    public Mono<ProjectView> getProjectView(UUID id) {
        return repository.findProjectWithTasks(id)
                .collectList()
                .flatMap(rows -> toProjectView(rows));
    }

    private Mono<ProjectView> toProjectView(List<ProjectTaskRow> rows) {
        if (rows.isEmpty()) {
            return Mono.empty();
        }
        var first = rows.get(0);
        var tasks = rows.stream()
                .filter(r -> r.taskId() != null)
                .map(this::from)
                .toList();
        return Mono.just(new ProjectView(
                first.id(), first.name(), first.description(), first.projectStatus().equals("COMPLETED")
                , first.contactPersonEmail(), new TimeEstimate(first.projectEstimateHours(), first.projectEstimateMinutes()), tasks));
    }

    private TaskView from(ProjectTaskRow row) {
        return new TaskView(row.taskId(), row.taskTitle(),
                row.taskDescription(), row.taskStatus().equals("COMPLETE"), new TimeEstimate(row.taskEstimateHours(), row.taskEstimateMinutes()), timeSpent(row));
    }

    private ActualTimeSpent timeSpent(ProjectTaskRow row) {
        if (row.actualHours() == null) {
            return null;
        }
        return new ActualTimeSpent(row.actualHours(), row.actualMinutes());
    }
}