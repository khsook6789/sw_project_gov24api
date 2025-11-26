-- V3__create_user_favorite.sql (MySQL 8+)

CREATE TABLE IF NOT EXISTS user_favorite (
                                             id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                             user_id BIGINT UNSIGNED NOT NULL,
                                             svc_id VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    -- 한 유저가 같은 svc_id를 중복으로 즐겨찾기 못 하게 유니크 키
    UNIQUE KEY uk_favorite_user_svc (user_id, svc_id),

    -- app_user.user_id랑 FK 연결
    CONSTRAINT fk_user_favorite_user
    FOREIGN KEY (user_id)
    REFERENCES app_user(user_id)
    ON DELETE CASCADE
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;
