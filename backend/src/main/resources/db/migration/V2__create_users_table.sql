CREATE TABLE users (
    id                            BIGINT       NOT NULL AUTO_INCREMENT,
    google_id                     VARCHAR(255) NOT NULL,
    email                         VARCHAR(255) NOT NULL,
    name                          VARCHAR(100) NOT NULL,
    department_id                 BIGINT       NULL,
    role                          ENUM('EMPLOYEE','MANAGER','CEO','ADMIN')
                                               NOT NULL DEFAULT 'EMPLOYEE',
    position                      VARCHAR(100) NULL     COMMENT '직책 (예: 과장)',
    phone                         VARCHAR(20)  NULL,
    profile_image_url             VARCHAR(500) NULL,
    google_calendar_refresh_token TEXT         NULL     COMMENT '암호화 저장',
    is_active                     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at                    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                               ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_google_id (google_id),
    UNIQUE KEY uq_users_email     (email),
    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id) REFERENCES departments (id)
        ON DELETE SET NULL
);
