CREATE SCHEMA IF NOT EXISTS time_track;
--changeSet Prokopovich: create table users
CREATE TABLE IF NOT EXISTS time_track.users
(
    id        UUID PRIMARY KEY,
    firstname VARCHAR(255)        NOT NULL,
    lastname  VARCHAR(255)        NOT NULL,
    email     VARCHAR(255) UNIQUE NOT NULL,
    password  VARCHAR(255)        NOT NULL,
    role      VARCHAR(20)         NOT NULL
);
