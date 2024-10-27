CREATE TABLE time_track.records
(
    id          BIGSERIAL PRIMARY KEY,
    description TEXT,
    user_id     UUID NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    created_by  VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES time_track.users (id)
);