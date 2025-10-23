package tn.portfolio.reactive.project.infrastructure;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import tn.portfolio.reactive.project.domain.Project;
import tn.portfolio.reactive.project.domain.ProjectDomainMapper;
import tn.portfolio.reactive.project.domain.ProjectId;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.project.repository.ProjectRepository;

import java.util.List;
import java.util.UUID;

@Repository
class ProjectRepositoryImpl implements ProjectRepository {
    private final ProjectEntityRepository projectEntityRepository;
    private final ProjectTaskEntityRepository taskEntityRepository;

    ProjectRepositoryImpl(ProjectEntityRepository projectEntityRepository,
                          ProjectTaskEntityRepository taskEntityRepository) {
        this.projectEntityRepository = projectEntityRepository;
        this.taskEntityRepository = taskEntityRepository;
    }

    @Override
    public Mono<Project> findById(ProjectId projectId) {
        UUID id = projectId.value();
        Mono<ProjectEntity> projectMono = projectEntityRepository.findById(id);
        Flux<ProjectTaskEntity> taskFlux = taskEntityRepository.findByProjectId(id);

        return Mono.zip(projectMono, taskFlux.collectList())
                .map(this::astProject);
    }

    private Project astProject(Tuple2<ProjectEntity, List<ProjectTaskEntity>> tuple) {
        ProjectEntity entity = tuple.getT1();
        List<ProjectTaskEntity> taskEntities = tuple.getT2();

        ProjectDto dto = ProjectPersistenceMapper.fromEntity(entity, taskEntities);
        return ProjectDomainMapper.fromDto(dto);
    }

    @Override
    @Transactional
    public Mono<Project> save(Project project) {
        boolean isNew = project.isNew();

        ProjectDto dto = ProjectDomainMapper.toDto(project);
        ProjectEntity entity = ProjectPersistenceMapper.toEntity(dto, isNew);
        List<ProjectTaskEntity> taskEntities = dto.tasks().stream()
                .map(ProjectPersistenceMapper::toEntity)
                .toList();

        UUID projectId = project.getId().value();

        return taskEntityRepository.deleteByProjectId(projectId)
                .then(projectEntityRepository.save(entity))
                .flatMap(savedEntity -> toProject(taskEntities, savedEntity));
    }

    private Mono<Project> toProject(List<ProjectTaskEntity> taskEntities, ProjectEntity savedEntity) {
        return taskEntityRepository.saveAll(taskEntities)
                .collectList()
                .map(savedTasks -> {
                    ProjectDto savedDto = ProjectPersistenceMapper.fromEntity(savedEntity, savedTasks);
                    return ProjectDomainMapper.fromDto(savedDto);
                });
    }

    @Override
    public Mono<Project> findByTaskId(ProjectTaskId taskId) {
        UUID id = taskId.value();
        return taskEntityRepository.findById(id)
                .flatMap(this::toProject);
    }

    private Mono<Project> toProject(ProjectTaskEntity taskEntity) {
        UUID projectId = taskEntity.getProjectId();

        Mono<ProjectEntity> projectMono = projectEntityRepository.findById(projectId);
        Mono<List<ProjectTaskEntity>> tasksMono = taskEntityRepository.findByProjectId(projectId)
                .collectList();

        return Mono.zip(projectMono, tasksMono)
                .map(tuple -> {
                    ProjectDto dto = ProjectPersistenceMapper.fromEntity(
                            tuple.getT1(),
                            tuple.getT2()
                    );
                    return ProjectDomainMapper.fromDto(dto);
                });
    }
}