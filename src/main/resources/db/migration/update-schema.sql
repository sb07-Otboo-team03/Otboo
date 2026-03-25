CREATE TABLE binary_contents
(
    id         UUID         NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name       VARCHAR(255) NOT NULL,
    type       VARCHAR(255),
    size       BIGINT,
    CONSTRAINT pk_binary_contents PRIMARY KEY (id)
);

CREATE TABLE clothes
(
    id         UUID        NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name       VARCHAR(30) NOT NULL,
    type       VARCHAR(30) NOT NULL,
    owner_id   UUID,
    image_id   UUID,
    CONSTRAINT pk_clothes PRIMARY KEY (id)
);

CREATE TABLE clothes_attribute_defs
(
    id         UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name       VARCHAR(30),
    CONSTRAINT pk_clothes_attribute_defs PRIMARY KEY (id)
);

CREATE TABLE clothes_attribute_values
(
    id               UUID        NOT NULL,
    selectable_value VARCHAR(30) NOT NULL,
    is_active        BOOLEAN     NOT NULL,
    definition_id    UUID        NOT NULL,
    CONSTRAINT pk_clothes_attribute_values PRIMARY KEY (id)
);

CREATE TABLE clothes_feeds
(
    clothes_id UUID NOT NULL,
    feed_id    UUID NOT NULL
);

CREATE TABLE comments
(
    id         UUID         NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    content    VARCHAR(255) NOT NULL,
    author_id  UUID,
    feed_id    UUID         NOT NULL,
    CONSTRAINT pk_comments PRIMARY KEY (id)
);

CREATE TABLE direct_messages
(
    id          UUID         NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    content     VARCHAR(255) NOT NULL,
    sender_id   UUID,
    receiver_id UUID,
    CONSTRAINT pk_direct_messages PRIMARY KEY (id)
);

CREATE TABLE feeds
(
    id                                      UUID             NOT NULL,
    updated_at                              TIMESTAMP WITHOUT TIME ZONE,
    created_at                              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    content                                 VARCHAR(500)     NOT NULL,
    like_count                              BIGINT           NOT NULL,
    comment_count                           INTEGER          NOT NULL,
    author_id                               UUID             NOT NULL,
    feed_weather_id                         UUID             NOT NULL,
    feed_sky_status                         VARCHAR(30)      NOT NULL,
    feed_precipitation_type                 VARCHAR(30)      NOT NULL,
    feed_precipitation_amount               DOUBLE PRECISION NOT NULL,
    feed_precipitation_probability          DOUBLE PRECISION NOT NULL,
    feed_temperature_current                DOUBLE PRECISION NOT NULL,
    feed_temperature_compared_to_day_before DOUBLE PRECISION NOT NULL,
    feed_temperature_max                    DOUBLE PRECISION,
    feed_temperature_min                    DOUBLE PRECISION,
    CONSTRAINT pk_feeds PRIMARY KEY (id)
);

CREATE TABLE follows
(
    id          UUID NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    follower_id UUID NOT NULL,
    followee_id UUID NOT NULL,
    CONSTRAINT pk_follows PRIMARY KEY (id)
);

CREATE TABLE likes
(
    id      UUID NOT NULL,
    user_id UUID,
    feed_id UUID NOT NULL,
    CONSTRAINT pk_likes PRIMARY KEY (id)
);

CREATE TABLE location_name_map
(
    id                 UUID             NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    x                  INTEGER          NOT NULL,
    y                  INTEGER          NOT NULL,
    latitude           DOUBLE PRECISION NOT NULL,
    longitude          DOUBLE PRECISION NOT NULL,
    region_1depth_name VARCHAR(50),
    region_2depth_name VARCHAR(100),
    region_3depth_name VARCHAR(100),
    region_4depth_name VARCHAR(100),
    CONSTRAINT pk_location_name_map PRIMARY KEY (id)
);

CREATE TABLE notifications
(
    id          UUID         NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    title       VARCHAR(100) NOT NULL,
    content     VARCHAR(500) NOT NULL,
    level       VARCHAR(30)  NOT NULL,
    receiver_id UUID         NOT NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id)
);

CREATE TABLE profiles
(
    id                      UUID         NOT NULL,
    updated_at              TIMESTAMP WITHOUT TIME ZONE,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id                 UUID         NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    gender                  VARCHAR(255),
    birth_date              date,
    temperature_sensitivity INTEGER      NOT NULL,
    profile_image_id        UUID,
    latitude                DOUBLE PRECISION,
    longitude               DOUBLE PRECISION,
    x                       INTEGER,
    y                       INTEGER,
    location_names          VARCHAR(255),
    CONSTRAINT pk_profiles PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         UUID         NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(255) NOT NULL,
    locked     BOOLEAN      NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE weather
(
    id                        UUID             NOT NULL,
    updated_at                TIMESTAMP WITHOUT TIME ZONE,
    created_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    forecasted_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    forecast_at               TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    x                         INTEGER          NOT NULL,
    y                         INTEGER          NOT NULL,
    temperature_current       DOUBLE PRECISION NOT NULL,
    temperature_max           DOUBLE PRECISION,
    temperature_min           DOUBLE PRECISION,
    wind_speed                DOUBLE PRECISION NOT NULL,
    wind_as_word              VARCHAR(30)      NOT NULL,
    sky_status                VARCHAR(30)      NOT NULL,
    precipitation_type        VARCHAR(30)      NOT NULL,
    precipitation_amount      DOUBLE PRECISION NOT NULL,
    precipitation_probability DOUBLE PRECISION NOT NULL,
    humidity_current          DOUBLE PRECISION NOT NULL,
    CONSTRAINT pk_weather PRIMARY KEY (id)
);

CREATE TABLE yesterday_hourly_weather
(
    id          UUID             NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    x           INTEGER          NOT NULL,
    y           INTEGER          NOT NULL,
    date        date             NOT NULL,
    hour        time WITHOUT TIME ZONE      NOT NULL,
    temperature DOUBLE PRECISION NOT NULL,
    humidity    DOUBLE PRECISION NOT NULL,
    CONSTRAINT pk_yesterday_hourly_weather PRIMARY KEY (id)
);

ALTER TABLE likes
    ADD CONSTRAINT uc_a8e9726de66afc980dfd72c44 UNIQUE (user_id, feed_id);

ALTER TABLE location_name_map
    ADD CONSTRAINT uc_c992c2ffcb8740841fc0c27a0 UNIQUE (latitude, longitude);

ALTER TABLE clothes
    ADD CONSTRAINT uc_clothes_image UNIQUE (image_id);

ALTER TABLE profiles
    ADD CONSTRAINT uc_profiles_profile_image UNIQUE (profile_image_id);

ALTER TABLE profiles
    ADD CONSTRAINT uc_profiles_user UNIQUE (user_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE clothes_attribute_values
    ADD CONSTRAINT FK_CLOTHES_ATTRIBUTE_VALUES_ON_DEFINITION FOREIGN KEY (definition_id) REFERENCES clothes_attribute_defs (id);

ALTER TABLE clothes
    ADD CONSTRAINT FK_CLOTHES_ON_IMAGE FOREIGN KEY (image_id) REFERENCES binary_contents (id);

ALTER TABLE clothes
    ADD CONSTRAINT FK_CLOTHES_ON_OWNER FOREIGN KEY (owner_id) REFERENCES users (id);

ALTER TABLE comments
    ADD CONSTRAINT FK_COMMENTS_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES users (id);

ALTER TABLE comments
    ADD CONSTRAINT FK_COMMENTS_ON_FEED FOREIGN KEY (feed_id) REFERENCES feeds (id);

ALTER TABLE direct_messages
    ADD CONSTRAINT FK_DIRECT_MESSAGES_ON_RECEIVER FOREIGN KEY (receiver_id) REFERENCES users (id);

ALTER TABLE direct_messages
    ADD CONSTRAINT FK_DIRECT_MESSAGES_ON_SENDER FOREIGN KEY (sender_id) REFERENCES users (id);

ALTER TABLE feeds
    ADD CONSTRAINT FK_FEEDS_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES users (id);

ALTER TABLE follows
    ADD CONSTRAINT FK_FOLLOWS_FOLLOWEES FOREIGN KEY (followee_id) REFERENCES users (id);

ALTER TABLE follows
    ADD CONSTRAINT FK_FOLLOWS_FOLLOWERS FOREIGN KEY (follower_id) REFERENCES users (id);

ALTER TABLE likes
    ADD CONSTRAINT FK_LIKES_ON_FEED FOREIGN KEY (feed_id) REFERENCES feeds (id);

ALTER TABLE likes
    ADD CONSTRAINT FK_LIKES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE notifications
    ADD CONSTRAINT FK_NOTIFICATIONS_RECEIVERS FOREIGN KEY (receiver_id) REFERENCES users (id);

ALTER TABLE profiles
    ADD CONSTRAINT FK_PROFILES_ON_PROFILE_IMAGE FOREIGN KEY (profile_image_id) REFERENCES binary_contents (id);

ALTER TABLE profiles
    ADD CONSTRAINT FK_PROFILES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE clothes_feeds
    ADD CONSTRAINT fk_clofee_on_clothes FOREIGN KEY (clothes_id) REFERENCES clothes (id);

ALTER TABLE clothes_feeds
    ADD CONSTRAINT fk_clofee_on_feed FOREIGN KEY (feed_id) REFERENCES feeds (id);