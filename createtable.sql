-- Auth DB
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ToDo DB
CREATE TABLE todos (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       user_id BIGINT NOT NULL,
                       title VARCHAR(255) NOT NULL,
                       due_date DATE NOT NULL,
                       priority TINYINT NOT NULL DEFAULT 1, -- 0:低, 1:中, 2:高
                       is_completed BOOLEAN NOT NULL DEFAULT FALSE,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       INDEX idx_user_id (user_id),
                       INDEX idx_due_date (due_date)
);