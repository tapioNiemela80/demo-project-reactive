package tn.portfolio.reactive.project.infrastructure;

// ProjectRepositoryCustomImpl.java

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
                .map(tuple -> {
                    ProjectEntity entity = tuple.getT1();
                    List<ProjectTaskEntity> taskEntities = tuple.getT2();

                    ProjectDto dto = ProjectPersistenceMapper.fromEntity(entity, taskEntities);
                    return ProjectDomainMapper.fromDto(dto);
                });
    }

    @Override
    @Transactional
    public Mono<Project> save(Project project) {
        boolean isNew = project.isNew();

        // Domain → DTO
        ProjectDto dto = ProjectDomainMapper.toDto(project); // uusi mapperi
        ProjectEntity entity = ProjectPersistenceMapper.toEntity(dto, isNew);
        List<ProjectTaskEntity> taskEntities = dto.tasks().stream()
                .map(ProjectPersistenceMapper::toEntity)
                .toList();

        UUID projectId = project.getId().value();

        return taskEntityRepository.deleteByProjectId(projectId)
                .then(projectEntityRepository.save(entity))
                .flatMap(savedEntity ->
                        taskEntityRepository.saveAll(taskEntities)
                                .collectList()
                                .map(savedTasks -> {
                                    ProjectDto savedDto = ProjectPersistenceMapper.fromEntity(savedEntity, savedTasks);
                                    return ProjectDomainMapper.fromDto(savedDto);
                                })
                )
                .doOnError(e -> System.out.println("Saving project failed " + e));
    }

    @Override
    public Mono<Project> findByTaskId(ProjectTaskId taskId) {
        UUID id = taskId.value();
        return taskEntityRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Task not found with id: " + taskId)))
                .flatMap(taskEntity -> {
                    UUID projectId = taskEntity.getProjectId();

                    Mono<ProjectEntity> projectMono = projectEntityRepository.findById(projectId);
                    Mono<List<ProjectTaskEntity>> tasksMono = taskEntityRepository.findByProjectId(projectId).collectList();

                    return Mono.zip(projectMono, tasksMono)
                            .map(tuple -> {
                                ProjectDto dto = ProjectPersistenceMapper.fromEntity(
                                        tuple.getT1(),
                                        tuple.getT2()
                                );
                                return ProjectDomainMapper.fromDto(dto);
                            });
                });
    }
}