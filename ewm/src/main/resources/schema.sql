DROP TABLE IF EXISTS compilation_events;
DROP TABLE IF EXISTS compilations;
DROP TABLE IF EXISTS requests;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(254)                        NOT NULL,
    name VARCHAR(250)                         NOT NULL,
    CONSTRAINT uq_user_email UNIQUE (email));

CREATE TABLE IF NOT EXISTS categories (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50)                           NOT NULL,
    CONSTRAINT uq_cat_name UNIQUE (NAME));

CREATE TABLE IF NOT EXISTS events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annotation VARCHAR(2000)                  NOT NULL,
    category_id INTEGER                       NOT NULL,
    created_on TIMESTAMP WITHOUT TIME ZONE    NOT NULL,
    description VARCHAR(7000)                 NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE    NOT NULL,
    initiator_id BIGINT                       NOT NULL,
    is_paid BOOLEAN                           NOT NULL,
    title VARCHAR(120)                        NOT NULL,
    loc_lat DOUBLE PRECISION                  NOT NULL,
    loc_lon DOUBLE PRECISION                  NOT NULL,
    participant_limit INTEGER                 NOT NULL,
    request_moderation BOOLEAN                NOT NULL,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    state VARCHAR(30)                         NOT NULL,
    confirmed_requests INTEGER,
    CONSTRAINT fk_events_to_categories FOREIGN KEY(category_id) REFERENCES categories(id),
    CONSTRAINT fk_events_to_users FOREIGN KEY(initiator_id) REFERENCES users(id));

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created_at TIMESTAMP                      NOT NULL,
    event_id BIGINT                           NOT NULL,
    requester_id BIGINT                       NOT NULL,
    status VARCHAR(15)                        NOT NULL,
    CONSTRAINT fk_requests_to_events FOREIGN KEY(event_id) REFERENCES events(id),
    CONSTRAINT fk_requests_to_users FOREIGN KEY(requester_id) REFERENCES users(id));

CREATE TABLE IF NOT EXISTS compilations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    is_pinned BOOLEAN                         NOT NULL,
    title VARCHAR(50)                         NOT NULL);

CREATE TABLE IF NOT EXISTS compilation_events (
    compilation_id BIGINT                     NOT NULL,
    event_id BIGINT                           NOT NULL,
    CONSTRAINT compilation_events_pk PRIMARY KEY (compilation_id, event_id),
    CONSTRAINT fk_comp_events_to_comps FOREIGN KEY(compilation_id) REFERENCES compilations(id),
    CONSTRAINT fk_comp_events_to_events FOREIGN KEY(event_id) REFERENCES events(id));
