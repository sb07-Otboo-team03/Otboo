CREATE TABLE clothes_attribute_mappings (
                                              clothes_id	uuid		NOT NULL,
                                              attribute_value_id	uuid		NOT NULL
);



CREATE TABLE direct_messages (
                                   id	uuid		NOT NULL,
                                   created_at	timestamp		NOT NULL,
                                   content	varchar(255)		NOT NULL,
                                   sender_id	uuid		NULL,
                                   receiver_id	uuid		NULL
);



CREATE TABLE follows (
                           id	uuid		NOT NULL,
                           created_at	timestamp		NOT NULL,
                           follower_id	uuid		NULL,
                           followee_id	uuid		NULL
);



CREATE TABLE notifications (
                                 id	uuid		NOT NULL,
                                 created_at	timestamp		NOT NULL,
                                 title	varchar(100)		NOT NULL,
                                 content	varchar(500)		NOT NULL,
                                 level	varchar(30)		NOT NULL,
                                 receiver_id	uuid		NOT NULL
);




CREATE TABLE comments (
                            id	uuid		NOT NULL,
                            created_at	timestamp		NOT NULL,
                            content	varchar(255)		NOT NULL,
                            feed_id	uuid		NOT NULL,
                            author_id	uuid		NULL
);




CREATE TABLE feeds (
                         id	uuid		NOT NULL,
                         created_at	timestamp		NOT NULL,
                         updated_at	timestamp		NULL,
                         content	varchar(500)		NOT NULL,
                         like_count	integer	DEFAULT 0	NOT NULL,
                         comment_count	integer	DEFAULT 0	NOT NULL,
                         author_id	uuid		NULL,

                         feed_weather_id  uuid      NOT NULL,
                         feed_sky_status  varchar(30)    NOT NULL,
                         feed_precipitation_type  varchar(30)   NOT NULL,
                         feed_precipitation_amount  double precision     NOT NULL,
                         feed_precipitation_probability  double precision    NOT NULL,
                         feed_temperature_current  double precision      NOT NULL,
                         feed_temperature_compared_to_day_before  double precision   NOT NULL,
                         feed_temperature_max  double precision      NULL,
                         feed_temperature_min  double precision      NULL
);





CREATE TABLE clothes_attribute_values (
                                            id	uuid		NOT NULL,
                                            selectable_value	varchar(30)		NOT NULL,
                                            is_active	boolean	DEFAULT TRUE	NOT NULL,
                                            definition_id	uuid		NOT NULL
);



CREATE TABLE users (
                         id	uuid		NOT NULL,
                         created_at	timestamp		NOT NULL,
                         updated_at	timestamp		NULL,
                         email	varchar(255)		NOT NULL,
                         password	varchar(100)		NOT NULL,
                         role	varchar(30)		NOT NULL,
                         locked	boolean	DEFAULT FALSE	NOT NULL
);




CREATE TABLE clothes (
                           id	uuid		NOT NULL,
                           created_at	timestamp		NOT NULL,
                           name	varchar(30)		NOT NULL,
                           image_url	varchar(255)		NULL,
                           type	varchar(30)		NOT NULL,
                           owner_id	uuid		NULL
);


CREATE TABLE clothes_feeds (
                                 clothes_id	uuid		NOT NULL,
                                 feed_id	uuid		NOT NULL
);




CREATE TABLE clothes_attribute_defs (
                                          id	uuid		NOT NULL,
                                          created_at	timestamp		NOT NULL,
                                          name	varchar(30)		NULL
);

CREATE TABLE likes (
                         id	uuid		NOT NULL,
                         user_id	uuid		NULL,
                         feed_id	uuid		NOT NULL
);





CREATE TABLE weathers (
                          id	uuid		NOT NULL,
                          created_at	timestamp		NOT NULL,
                          forecasted_at	timestamp		NOT NULL,
                          forecast_at	timestamp		NOT NULL,
                          latitude	double precision		NOT NULL,
                          longitude	double precision		NOT NULL,
                          x	integer		NOT NULL,
                          y	integer		NOT NULL,
                          location_names	varchar(30)		NOT NULL,
                          temperature_current	double precision		NOT NULL,
                          temperature_min	double precision		NULL,
                          temperature_max	double precision		NULL,
                          wind_speed	double precision		NOT NULL,
                          wind_as_word	varchar(30)		NOT NULL,
                          sky_status	varchar(30)		NOT NULL,
                          precipitation_type	varchar(30)		NOT NULL,
                          precipitation_amount	double precision		NOT NULL,
                          precipitation_probability	double precision		NOT NULL,
                          humidity_current	double precision		NOT NULL
);




CREATE TABLE profiles (
                            id	uuid		NOT NULL,
                            user_id	uuid		NOT NULL,
                            created_at	timestamp		NOT NULL,
                            updated_at	timestamp		NULL,
                            name	varchar(30)		NOT NULL,
                            gender	varchar(30)		NULL,
                            birth_date	date		NULL,
                            latitude	double precision		NULL,
                            longitude	double precision		NULL,
                            location_names varchar(30)          NULL,
                            x	integer		NULL,
                            y	integer		NULL,
                            temperature_sensitivity	integer		NOT NULL,
                            profile_image_url	varchar(255)		NULL
);

ALTER TABLE clothes_attribute_mappings ADD CONSTRAINT PK_CLOTHES_ATTRIBUTE_MAPPINGS PRIMARY KEY (
                                                                                                     clothes_id,
                                                                                                     attribute_value_id
    );

ALTER TABLE direct_messages ADD CONSTRAINT PK_DIRECT_MESSAGES PRIMARY KEY (
                                                                               id
    );

ALTER TABLE follows ADD CONSTRAINT PK_FOLLOWS PRIMARY KEY (
                                                               id
    );

ALTER TABLE notifications ADD CONSTRAINT PK_NOTIFICATIONS PRIMARY KEY (
                                                                           id
    );

ALTER TABLE comments ADD CONSTRAINT PK_COMMENTS PRIMARY KEY (
                                                                 id
    );

ALTER TABLE feeds ADD CONSTRAINT PK_FEEDS PRIMARY KEY (
                                                           id
    );

ALTER TABLE clothes_attribute_values ADD CONSTRAINT PK_CLOTHES_ATTRIBUTE_VALUES PRIMARY KEY (
                                                                                                 id
    );

ALTER TABLE users ADD CONSTRAINT PK_USERS PRIMARY KEY (
                                                           id
    );

ALTER TABLE clothes ADD CONSTRAINT PK_CLOTHES PRIMARY KEY (
                                                               id
    );

ALTER TABLE clothes_feeds ADD CONSTRAINT PK_CLOTHES_FEEDS PRIMARY KEY (
                                                                           clothes_id,
                                                                           feed_id
    );

ALTER TABLE clothes_attribute_defs ADD CONSTRAINT PK_CLOTHES_ATTRIBUTE_DEFS PRIMARY KEY (
                                                                                             id
    );

ALTER TABLE likes ADD CONSTRAINT PK_LIKES PRIMARY KEY (
                                                           id
    );

ALTER TABLE weathers ADD CONSTRAINT PK_WEATHERS PRIMARY KEY (
                                                                 id
    );

ALTER TABLE profiles ADD CONSTRAINT PK_PROFILES PRIMARY KEY (
                                                                 id
    );


ALTER TABLE clothes_attribute_mappings ADD CONSTRAINT fk_clothes_attribute_mappings_clothes FOREIGN KEY (clothes_id) REFERENCES clothes(id) ON DELETE CASCADE;
ALTER TABLE clothes_attribute_mappings ADD CONSTRAINT fk_clothes_attribute_mappings_clothes_attribute_values FOREIGN KEY (attribute_value_id) REFERENCES clothes_attribute_values(id) ON DELETE CASCADE;

ALTER TABLE direct_messages ADD CONSTRAINT fk_direct_messages_senders FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE direct_messages ADD CONSTRAINT fk_direct_messages_receivers FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE follows ADD CONSTRAINT fk_follows_followers FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE follows ADD CONSTRAINT fk_follows_followees FOREIGN KEY (followee_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE notifications ADD CONSTRAINT fk_notifications_receivers FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE cascade;
ALTER TABLE notifications ADD CONSTRAINT chk_notifications_level CHECK (level IN ('INFO', 'WARN', 'ERROR'));

ALTER TABLE comments ADD CONSTRAINT fk_comments_feeds FOREIGN KEY (feed_id) REFERENCES feeds (id) ON DELETE cascade;
ALTER TABLE comments ADD CONSTRAINT fk_comments_authors FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE feeds ADD CONSTRAINT fk_feeds_authors FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE SET NULL;
ALTER TABLE feeds ADD CONSTRAINT chk_feeds_precipitation_type CHECK (feed_precipitation_type IN ('NONE', 'RAIN', 'RAIN_SNOW', 'SNOW', 'SHOWER'));
ALTER TABLE feeds ADD CONSTRAINT chk_feeds_sky_status CHECK (feed_sky_status IN ('CLEAR', 'MOSTLY_CLOUDY', 'CLOUDY'));

ALTER TABLE clothes_attribute_values ADD CONSTRAINT fk_clothes_attribute_values_clothes_attribute_defs FOREIGN KEY (definition_id) REFERENCES clothes_attribute_defs (id) ON DELETE CASCADE;

ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'));

ALTER TABLE clothes ADD CONSTRAINT fk_clothes_owners FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE SET NULL;
ALTER TABLE clothes ADD CONSTRAINT chk_clothes_type CHECK (type IN ('TOP', 'BOTTOM', 'DRESS','OUTER','UNDERWEAR', 'ACCESSORY','SHOES','SOCKS','HAT','BAG','SCARF','ETC'));

ALTER TABLE clothes_feeds ADD CONSTRAINT fk_clothes_feeds_clothes FOREIGN KEY (clothes_id) REFERENCES clothes (id) ON DELETE CASCADE;
ALTER TABLE clothes_feeds ADD CONSTRAINT fk_clothes_feeds_feeds FOREIGN KEY (feed_id) REFERENCES feeds (id) ON DELETE CASCADE;

ALTER TABLE likes ADD CONSTRAINT fk_likes_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL;
ALTER TABLE likes ADD CONSTRAINT fk_likes_feeds FOREIGN KEY (feed_id) REFERENCES feeds (id) ON DELETE CASCADE;

ALTER TABLE weathers ADD CONSTRAINT chk_weathers_precipitation_type CHECK (precipitation_type IN ('NONE', 'RAIN', 'RAIN_SNOW', 'SNOW', 'SHOWER'));
ALTER TABLE weathers ADD CONSTRAINT chk_weathers_wind_as_word CHECK (wind_as_word IN ('WEAK', 'MODERATE', 'STRONG'));
ALTER TABLE weathers ADD CONSTRAINT chk_weathers_sky_status CHECK (sky_status IN ('CLEAR', 'MOSTLY_CLOUDY', 'CLOUDY'));

ALTER TABLE profiles ADD CONSTRAINT chk_profiles_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'));
ALTER TABLE profiles ADD CONSTRAINT chk_profiles_temperature_sensitivity CHECK (temperature_sensitivity BETWEEN 1 AND 5);
ALTER TABLE profiles ADD CONSTRAINT fk_profiles_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE likes
    ADD CONSTRAINT uk_likes_user_feed
        UNIQUE (user_id, feed_id);

ALTER TABLE follows
    ADD CONSTRAINT uk_follows
        UNIQUE (follower_id, followee_id);

ALTER TABLE profiles
    ADD CONSTRAINT uk_profiles_user
        UNIQUE (user_id);

CREATE INDEX idx_feeds_author ON feeds(author_id);
CREATE INDEX idx_comments_feed ON comments(feed_id);
CREATE INDEX idx_likes_feed ON likes(feed_id);
CREATE INDEX idx_likes_user ON likes(user_id);
CREATE INDEX idx_follows_follower ON follows(follower_id);
CREATE INDEX idx_follows_followee ON follows(followee_id);
CREATE INDEX idx_feeds_like_count ON feeds(like_count DESC);
CREATE INDEX idx_feeds_comment_count ON feeds(comment_count DESC);

-- follow 트리거 함수 생성
-- 둘 다 NULL → row 삭제
-- 하나라도 존재 → 유지
CREATE OR REPLACE FUNCTION cleanup_follow_row()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.follower_id IS NULL AND NEW.followee_id IS NULL THEN
DELETE FROM follows WHERE id = NEW.id;
RETURN NULL;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- follow 트리거 생성
CREATE TRIGGER trg_cleanup_follow
    AFTER UPDATE ON follows
    FOR EACH ROW
    EXECUTE FUNCTION cleanup_follow_row();

-- direct message 트리거 함수 생성
-- sender_id = NULL
-- receiver_id = NULL
-- → row 삭제
CREATE OR REPLACE FUNCTION cleanup_direct_message_row()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.sender_id IS NULL AND NEW.receiver_id IS NULL THEN
DELETE FROM direct_messages WHERE id = NEW.id;
RETURN NULL;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- direct message 트리거 생성
CREATE TRIGGER trg_cleanup_direct_message
    AFTER UPDATE ON direct_messages
    FOR EACH ROW
    EXECUTE FUNCTION cleanup_direct_message_row();

-- binaryContent 테이블 변경
CREATE TABLE binary_contents (
    id uuid NOT NULL,
    created_at	timestamp	default NOW()	NOT NULL,
    name varchar(255) NOT NULL,
    size integer NULL,
    type varchar(255) NULL
);

-- profiles 테이블에 칼럼 추가 및 참조 연결
ALTER TABLE profiles ADD COLUMN profile_image_id uuid;
ALTER TABLE profiles ADD CONSTRAINT fk_profiles_profile_image FOREIGN KEY (profile_image_id) REFERENCES binary_contents(id) ON DELETE SET NULL;

-- clothes 테이블에 칼럼 추가 및 참조 연결
ALTER TABLE clothes ADD COLUMN image_id uuid;
ALTER TABLE clothes ADD CONSTRAINT fk_clothes_image FOREIGN KEY (image_id) REFERENCES binary_contents(id) ON DELETE SET NULL;