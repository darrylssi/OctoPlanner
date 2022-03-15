CREATE TABLE IF NOT EXISTS users
(
    id                INT          NOT NULL,
    username          VARCHAR(255) NOT NULL UNIQUE,
    password          VARCHAR(255) NOT NULL,
    first_name        VARCHAR(255) NOT NULL,
    middle_name       VARCHAR(255),
    last_name         VARCHAR(255) NOT NULL,
    nick_name         VARCHAR(255),
    bio               VARCHAR(255),
    personal_pronouns VARCHAR(255),
    email             VARCHAR(255) NOT NULL UNIQUE,
    register_date     TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id)
);