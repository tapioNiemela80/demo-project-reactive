package tn.portfolio.reactive.team.view;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface TeamViewRepository extends R2dbcRepository<TeamViewRow, UUID> {

    @Query("""
        SELECT
            t.id AS team_id,
            t.name AS team_name,
            m.id AS member_id,
            m.name AS member_name,
            m.profession AS member_profession,
            tt.id AS task_id,
            tt.name AS task_name,
            tt.description AS task_description,
            tt.project_task_id AS project_task_id,
            tt.status AS task_status,
            tt.assignee_id AS task_assignee_id,
            tt.actual_time_spent_hours AS actual_time_spent_hours,
            tt.actual_time_spent_minutes AS actual_time_spent_minutes
        FROM project_demo_reactive.teams t
        LEFT JOIN project_demo_reactive.team_members m ON t.id = m.team_id
        LEFT JOIN project_demo_reactive.team_tasks tt ON t.id = tt.team_id
        WHERE t.id = :teamId
    """)
    Flux<TeamViewRow> findTeamViewByTeamId(UUID teamId);

    @Query("""
        SELECT
            t.id AS team_id,
            t.name AS team_name
            FROM project_demo_reactive.teams t
            """)
    Flux<TeamsViewRow> findAllTeamsViewRows();
}