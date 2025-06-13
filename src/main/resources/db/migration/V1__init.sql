CREATE SCHEMA IF NOT EXISTS project_demo_reactive;

CREATE TABLE project_demo_reactive.projects (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    status TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    planned_end_date DATE,
    version INTEGER NOT NULL,
    initial_estimated_time_hours INTEGER NOT NULL,
    initial_estimated_time_minutes INTEGER NOT NULL,
    contact_person_name TEXT NOT NULL,
    contact_person_email TEXT NOT NULL
);

CREATE TABLE project_demo_reactive.project_tasks (
    id UUID PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    estimated_time_hours INTEGER NOT NULL,
    estimated_time_minutes INTEGER NOT NULL,
    task_status TEXT NOT NULL,
    actual_time_spent_hours INTEGER,
    actual_time_spent_minutes INTEGER,
    project_id UUID REFERENCES project_demo_reactive.projects(id) ON DELETE CASCADE
);
