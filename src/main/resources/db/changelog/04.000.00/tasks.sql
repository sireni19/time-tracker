CREATE TABLE time_track.tasks (
                      id BIGSERIAL PRIMARY KEY,
                      title VARCHAR(255) NOT NULL,
                      description TEXT,
                      expected_hours SMALLINT,
                      actual_hours SMALLINT DEFAULT 0,
                      executor_id UUID,
                      FOREIGN KEY (executor_id) REFERENCES time_track.users(id)
);
