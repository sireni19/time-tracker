ALTER TABLE time_track.tasks
    ADD COLUMN status VARCHAR(20) DEFAULT 'UNASSIGNED';

CREATE INDEX idx_executor_id ON time_track.tasks (executor_id);