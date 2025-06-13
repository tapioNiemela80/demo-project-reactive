package tn.portfolio.reactive.team.infrastructure;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.team.domain.Team;
import tn.portfolio.reactive.team.domain.TeamDomainMapper;
import tn.portfolio.reactive.team.domain.TeamId;
import tn.portfolio.reactive.team.repository.TeamRepository;

import java.util.List;
import java.util.UUID;

@Repository
class TeamRepositoryImpl implements TeamRepository {
    private final TeamEntityRepository teamEntityRepository;
    private final TeamMemberEntityRepository teamMemberEntityRepository;
    private final TeamTaskEntityRepository teamTaskEntityRepository;

    TeamRepositoryImpl(TeamEntityRepository teamEntityRepository, TeamMemberEntityRepository teamMemberEntityRepository, TeamTaskEntityRepository teamTaskEntityRepository) {
        this.teamEntityRepository = teamEntityRepository;
        this.teamMemberEntityRepository = teamMemberEntityRepository;
        this.teamTaskEntityRepository = teamTaskEntityRepository;
    }

    @Override
    public Mono<Team> findById(TeamId teamId) {
        UUID id = teamId.value();
        Mono<TeamEntity> teamMono = teamEntityRepository.findById(id);
        Flux<TeamMemberEntity> memberFlux = teamMemberEntityRepository.findByTeamId(id);
        Flux<TeamTaskEntity> taskFlux = teamTaskEntityRepository.findByTeamId(id);

        return Mono.zip(teamMono, memberFlux.collectList(), taskFlux.collectList())
                .map(this::toTeam);
    }

    private Team toTeam(Tuple3<TeamEntity, List<TeamMemberEntity>, List<TeamTaskEntity>> tuple) {
        TeamEntity entity = tuple.getT1();
        List<TeamMemberEntity> memberEntities = tuple.getT2();
        List<TeamTaskEntity> taskEntities = tuple.getT3();
        TeamDto dto = TeamPersistenceMapper.fromEntity(entity, memberEntities, taskEntities);
        return TeamDomainMapper.fromDto(dto);
    }

    @Override
    @Transactional
    public Mono<Team> save(Team team) {
        boolean isNew = team.isNew();
        UUID teamId = team.getId().value();
        TeamDto dto = TeamDomainMapper.toDto(team);
        TeamEntity entity = TeamPersistenceMapper.toEntity(dto, isNew);
        List<TeamMemberEntity> members = dto.members().stream().map(TeamPersistenceMapper::toEntity).toList();
        List<TeamTaskEntity> tasks = dto.tasks().stream().map(TeamPersistenceMapper::toEntity).toList();

        return Mono.when(
                        teamMemberEntityRepository.deleteByTeamId(teamId),
                        teamTaskEntityRepository.deleteByTeamId(teamId)
                ).then(teamEntityRepository.save(entity))
                .flatMap(savedEntity -> toTeam(members, tasks, savedEntity));
    }

    private Mono<Team> toTeam(List<TeamMemberEntity> members, List<TeamTaskEntity> tasks, TeamEntity savedEntity) {
        return Mono.zip(
                teamMemberEntityRepository.saveAll(members).collectList(),
                teamTaskEntityRepository.saveAll(tasks).collectList()
        ).map(tuple -> {
            var savedMembers = tuple.getT1();
            var savedTasks = tuple.getT2();
            TeamDto savedDto = TeamPersistenceMapper.fromEntity(savedEntity, savedMembers, savedTasks);
            return TeamDomainMapper.fromDto(savedDto);
        });
    }

    @Override
    public Mono<Team> findByProjectTaskId(ProjectTaskId projectTaskId) {
        return teamTaskEntityRepository.findByProjectTaskId(projectTaskId.value())
                .flatMap(teamTask -> {
                    UUID teamId = teamTask.getTeamId();
                    Mono<TeamEntity> teamMono = teamEntityRepository.findById(teamId);
                    Flux<TeamMemberEntity> membersFlux = teamMemberEntityRepository.findByTeamId(teamId);
                    Flux<TeamTaskEntity> tasksFlux = teamTaskEntityRepository.findByTeamId(teamId);

                    return Mono.zip(teamMono, membersFlux.collectList(), tasksFlux.collectList())
                            .map(tuple -> toTeam(tuple));
                });
    }

}
