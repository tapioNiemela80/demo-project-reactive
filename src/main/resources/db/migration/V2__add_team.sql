CREATE TABLE project_demo_reactive.teams (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    version INTEGER NOT NULL
);

CREATE TABLE project_demo_reactive.team_members (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL REFERENCES project_demo_reactive.teams(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    profession TEXT NOT NULL
);

CREATE TABLE project_demo_reactive.team_tasks (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL REFERENCES project_demo_reactive.teams(id) ON DELETE CASCADE,
    project_task_id UUID NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    status TEXT NOT NULL,
    assignee_id UUID,
    actual_time_spent_hours INTEGER,
    actual_time_spent_minutes INTEGER
);