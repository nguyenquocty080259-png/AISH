
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255),
    dob DATE,
    phone_number VARCHAR(20),
    avatar_url TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- AUTH MODULE
-- =========================================

CREATE TABLE auth_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    identifier VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    is_verified BOOLEAN DEFAULT FALSE,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_auth_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE email_verifications (
    id BIGSERIAL PRIMARY KEY,
    auth_account_id BIGINT NOT NULL,
    verification_code VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP,
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_email_verification_auth
        FOREIGN KEY(auth_account_id)
        REFERENCES auth_accounts(id)
        ON DELETE CASCADE
);

CREATE TABLE sms_verifications (
    id BIGSERIAL PRIMARY KEY,
    auth_account_id BIGINT NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP,
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sms_verification_auth
        FOREIGN KEY(auth_account_id)
        REFERENCES auth_accounts(id)
        ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    auth_account_id BIGINT NOT NULL,
    token TEXT NOT NULL,
    expires_at TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_auth
        FOREIGN KEY(auth_account_id)
        REFERENCES auth_accounts(id)
        ON DELETE CASCADE
);

-- =========================================
-- DOCUMENT MODULE
-- =========================================

CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    subject_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    storage_url TEXT NOT NULL,
    visibility VARCHAR(20) DEFAULT 'PRIVATE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_document_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_document_subject
        FOREIGN KEY(subject_id)
        REFERENCES subjects(id)
        ON DELETE SET NULL
);

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE document_tags (
    document_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,

    PRIMARY KEY(document_id, tag_id),

    CONSTRAINT fk_document_tag_document
        FOREIGN KEY(document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_document_tag_tag
        FOREIGN KEY(tag_id)
        REFERENCES tags(id)
        ON DELETE CASCADE
);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_comment_document
        FOREIGN KEY(document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE
);

CREATE TABLE favorites (
    user_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY(user_id, document_id),

    CONSTRAINT fk_favorite_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_favorite_document
        FOREIGN KEY(document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE
);

CREATE TABLE downloads (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    downloaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_download_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_download_document
        FOREIGN KEY(document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE
);

CREATE TABLE ratings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_rating_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_rating_document
        FOREIGN KEY(document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE
);

CREATE TABLE document_embeddings (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    chunk_index INTEGER NOT NULL,
    chunk_text TEXT NOT NULL,
    embedding_vector TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_embedding_document
        FOREIGN KEY(document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE
);

-- =========================================
-- CLOUD STORAGE MODULE
-- =========================================

CREATE TABLE storage_providers (
    id BIGSERIAL PRIMARY KEY,
    provider_name VARCHAR(100) NOT NULL UNIQUE,
    provider_type VARCHAR(50),
    api_endpoint TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE file_uploads (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    upload_status VARCHAR(20) DEFAULT 'PENDING',
    external_file_id TEXT,
    external_url TEXT,
    upload_progress INTEGER DEFAULT 0,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_upload_document
        FOREIGN KEY(document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_upload_provider
        FOREIGN KEY(provider_id)
        REFERENCES storage_providers(id)
        ON DELETE RESTRICT
);

CREATE TABLE upload_logs (
    id BIGSERIAL PRIMARY KEY,
    file_upload_id BIGINT NOT NULL,
    log_level VARCHAR(20),
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_upload_log
        FOREIGN KEY(file_upload_id)
        REFERENCES file_uploads(id)
        ON DELETE CASCADE
);

CREATE TABLE storage_quotas (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    max_storage_mb BIGINT,
    used_storage_mb BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_storage_quota_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE file_previews (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    thumbnail_url TEXT,
    preview_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_preview_document
        FOREIGN KEY(document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE
);

-- =========================================
-- AI SYSTEM MODULE
-- =========================================

CREATE TABLE ai_models (
    id BIGSERIAL PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    provider VARCHAR(50),
    model_version VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_prompts (
    id BIGSERIAL PRIMARY KEY,
    prompt_name VARCHAR(255) NOT NULL,
    system_prompt TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ai_model_id BIGINT NOT NULL,
    prompt_id BIGINT NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_conversation_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ai_conversation_model
        FOREIGN KEY(ai_model_id)
        REFERENCES ai_models(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_ai_conversation_prompt
        FOREIGN KEY(prompt_id)
        REFERENCES ai_prompts(id)
        ON DELETE RESTRICT
);

CREATE TABLE ai_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender VARCHAR(10) NOT NULL,
    message TEXT NOT NULL,
    related_document_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_message_conversation
        FOREIGN KEY(conversation_id)
        REFERENCES ai_conversations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ai_message_document
        FOREIGN KEY(related_document_id)
        REFERENCES documents(id)
        ON DELETE SET NULL
);

CREATE TABLE ai_usage_logs (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    ai_model_id BIGINT NOT NULL,
    prompt_tokens INTEGER,
    response_tokens INTEGER,
    total_tokens INTEGER,
    estimated_cost DECIMAL(10,4),
    response_time_ms INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_usage_conversation
        FOREIGN KEY(conversation_id)
        REFERENCES ai_conversations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ai_usage_model
        FOREIGN KEY(ai_model_id)
        REFERENCES ai_models(id)
        ON DELETE RESTRICT
);

-- =========================================
-- INDEXES
-- =========================================

CREATE INDEX idx_documents_user_id ON documents(user_id);
CREATE INDEX idx_documents_subject_id ON documents(subject_id);
CREATE INDEX idx_comments_document_id ON comments(document_id);
CREATE INDEX idx_downloads_document_id ON downloads(document_id);
CREATE INDEX idx_ai_messages_conversation_id ON ai_messages(conversation_id);
CREATE INDEX idx_document_embeddings_document_id ON document_embeddings(document_id);

-- =========================================
-- SEED DATA
-- =========================================

INSERT INTO users(full_name, dob, phone_number, avatar_url)
VALUES
('Nguyen Van A', '2003-05-12', '0901234567', 'https://example.com/avatar1.png'),
('Tran Thi B', '2002-11-21', '0912345678', 'https://example.com/avatar2.png');

INSERT INTO auth_accounts(user_id, provider, identifier, password_hash, is_verified, is_primary)
VALUES
(1, 'EMAIL', 'vana@gmail.com', '$2a$10$hashedpassword1', TRUE, TRUE),
(1, 'GOOGLE', 'google_sub_123456', NULL, TRUE, FALSE),
(2, 'PHONE', '0912345678', NULL, TRUE, TRUE);

INSERT INTO refresh_tokens(auth_account_id, token, expires_at)
VALUES
(1, 'refresh_token_demo_1', NOW() + INTERVAL '7 days');

INSERT INTO subjects(name, description)
VALUES
('Software Engineering', 'SE learning materials'),
('Database Systems', 'DBMS documents and notes');

INSERT INTO documents(
    user_id,
    subject_id,
    title,
    description,
    file_name,
    file_type,
    file_size,
    storage_url,
    visibility
)
VALUES
(
    1,
    1,
    'SWP391 Requirement Document',
    'Startup AI Study Hub requirement file',
    'swp391.pdf',
    'application/pdf',
    5242880,
    'https://storage.example.com/swp391.pdf',
    'PUBLIC'
),
(
    2,
    2,
    'Database Normalization Notes',
    'Learning SQL normalization',
    'normalization.docx',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    1048576,
    'https://storage.example.com/normalization.docx',
    'PUBLIC'
);

INSERT INTO tags(name)
VALUES
('AI'),
('Database'),
('Spring Boot');

INSERT INTO document_tags(document_id, tag_id)
VALUES
(1, 1),
(1, 3),
(2, 2);

INSERT INTO comments(user_id, document_id, content)
VALUES
(2, 1, 'This requirement document is very detailed.'),
(1, 2, 'Normalization examples are helpful.');

INSERT INTO favorites(user_id, document_id)
VALUES
(1, 2),
(2, 1);

INSERT INTO downloads(user_id, document_id)
VALUES
(1, 1),
(2, 1),
(2, 2);

INSERT INTO ratings(user_id, document_id, rating)
VALUES
(1, 1, 5),
(2, 2, 4);

INSERT INTO document_embeddings(document_id, chunk_index, chunk_text, embedding_vector)
VALUES
(
    1,
    1,
    'SWP391 project startup architecture and AI module discussion.',
    '[0.123,0.456,0.789]'
);

INSERT INTO storage_providers(provider_name, provider_type, api_endpoint)
VALUES
('AWS S3', 'CLOUD', 'https://s3.amazonaws.com'),
('Firebase Storage', 'CLOUD', 'https://firebase.google.com');

INSERT INTO file_uploads(
    document_id,
    provider_id,
    upload_status,
    external_file_id,
    external_url,
    upload_progress,
    started_at,
    completed_at
)
VALUES
(
    1,
    1,
    'COMPLETED',
    'aws_file_001',
    'https://s3.amazonaws.com/files/swp391.pdf',
    100,
    NOW(),
    NOW()
);

INSERT INTO upload_logs(file_upload_id, log_level, message)
VALUES
(1, 'INFO', 'Upload completed successfully');

INSERT INTO storage_quotas(user_id, max_storage_mb, used_storage_mb)
VALUES
(1, 10240, 5120),
(2, 5120, 1024);

INSERT INTO file_previews(document_id, thumbnail_url, preview_url)
VALUES
(
    1,
    'https://preview.example.com/thumb1.png',
    'https://preview.example.com/preview1.pdf'
);

INSERT INTO ai_models(model_name, provider, model_version)
VALUES
('GPT-4o', 'OpenAI', '4o'),
('Gemini 1.5 Pro', 'Google', '1.5');

INSERT INTO ai_prompts(prompt_name, system_prompt)
VALUES
(
    'Study Assistant',
    'You are an academic AI assistant helping students learn from documents.'
),
(
    'Quiz Generator',
    'Generate quizzes based on uploaded study materials.'
);

INSERT INTO ai_conversations(user_id, ai_model_id, prompt_id, title)
VALUES
(
    1,
    1,
    1,
    'SWP391 AI Discussion'
);

INSERT INTO ai_messages(conversation_id, sender, message, related_document_id)
VALUES
(
    1,
    'USER',
    'Can you summarize the SWP391 requirement document?',
    1
),
(
    1,
    'AI',
    'The document describes an AI-powered learning platform startup.',
    1
);

INSERT INTO ai_usage_logs(
    conversation_id,
    ai_model_id,
    prompt_tokens,
    response_tokens,
    total_tokens,
    estimated_cost,
    response_time_ms
)
VALUES
(
    1,
    1,
    120,
    240,
    360,
    0.0125,
    1500
);

