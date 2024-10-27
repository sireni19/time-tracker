ALTER TABLE time_track.records
    ADD COLUMN task_id BIGINT,
    ADD CONSTRAINT task_record_relation
        FOREIGN KEY (task_id) REFERENCES time_track.tasks (id) ON DELETE CASCADE ;

CREATE INDEX idx_user_id ON time_track.records (user_id);
CREATE INDEX idx_task_id ON time_track.records (task_id);