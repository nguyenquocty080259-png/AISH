-- =========================================
-- AUTH MODULE
-- =========================================

CREATE TABLE auth_users (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    avatar_url TEXT,
    deleted_at TIMESTAMPTZ,
    dob DATE,
    full_name VARCHAR(255),
    phone_number VARCHAR(20),
    status VARCHAR(20)
);

CREATE TABLE auth_accounts (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    failed_login_attempts INTEGER DEFAULT 0,
    identifier VARCHAR(255),
    is_primary BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMPTZ,
    password_hash TEXT,
    provider VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL
);

CREATE TABLE auth_email_verifications (
    id BIGINT PRIMARY KEY,
    attempt_count INTEGER,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ,
    is_used BOOLEAN DEFAULT FALSE,
    verification_code VARCHAR(10),
    verified_at TIMESTAMPTZ,
    auth_account_id BIGINT NOT NULL
);

CREATE TABLE auth_refresh_tokens (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    device_info VARCHAR(100),
    expires_at TIMESTAMPTZ,
    ip_address TEXT,
    revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMPTZ,
    token TEXT,
    user_agent VARCHAR(100),
    auth_account_id BIGINT NOT NULL
);

CREATE TABLE auth_sms_verifications (
    id BIGINT PRIMARY KEY,
    attempt_count INTEGER,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ,
    is_used BOOLEAN DEFAULT FALSE,
    otp_code VARCHAR(10),
    verified_at TIMESTAMPTZ,
    auth_account_id BIGINT NOT NULL
);

-- =========================================
-- DOCUMENT MODULE
-- =========================================

CREATE TABLE doc_subjects (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    description TEXT,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE doc_documents (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    average_rating NUMERIC(3,2) DEFAULT 0.00,
    deleted_at TIMESTAMPTZ,
    description TEXT,
    download_count INTEGER DEFAULT 0,
    status VARCHAR(20),
    title VARCHAR(255) NOT NULL,
    view_count INTEGER DEFAULT 0,
    visibility VARCHAR(20),
    deleted_by BIGINT,
    subject_id BIGINT,
    user_id BIGINT NOT NULL
);

CREATE TABLE doc_files (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(100),
    is_current BOOLEAN DEFAULT TRUE,
    storage_url TEXT NOT NULL,
    upload_status VARCHAR(20),
    version_number INTEGER DEFAULT 1,
    document_id BIGINT NOT NULL
);

CREATE TABLE doc_previews (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    preview_url TEXT,
    thumbnail_url TEXT,
    document_id BIGINT UNIQUE NOT NULL
);

CREATE TABLE doc_comments (
    id BIGINT PRIMARY KEY,
    content TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    document_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL
);

CREATE TABLE doc_ratings (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    rating INTEGER NOT NULL,
    document_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL
);

CREATE TABLE doc_downloads (
    id BIGINT PRIMARY KEY,
    downloaded_at TIMESTAMPTZ NOT NULL,
    document_id BIGINT NOT NULL,
    user_id BIGINT
);

CREATE TABLE doc_embeddings (
    id BIGINT PRIMARY KEY,
    chunk_index INTEGER,
    chunk_text TEXT,
    chunk_token_count INTEGER,
    created_at TIMESTAMPTZ NOT NULL,
    embedding_model VARCHAR(100),
    embedding_provider VARCHAR(100),
    embedding_reference TEXT,
    embedding_status VARCHAR(100),
    document_id BIGINT NOT NULL
);

CREATE TABLE doc_tags (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE doc_document_tags (
    document_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY(document_id, tag_id)
);

CREATE TABLE doc_collections (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    color VARCHAR(20),
    description TEXT,
    icon VARCHAR(100),
    name VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    visibility VARCHAR(20),
    owner_id BIGINT NOT NULL
);

CREATE TABLE doc_collection_documents (
    collection_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    added_at TIMESTAMPTZ NOT NULL,
    added_by BIGINT,
    PRIMARY KEY(collection_id, document_id)
);

CREATE TABLE doc_favorites (
    document_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY(document_id, user_id)
);

-- =========================================
-- AI MODULE
-- =========================================

CREATE TABLE ai_models (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    max_context_tokens INTEGER,
    model_name VARCHAR(100) NOT NULL,
    model_type VARCHAR(50),
    model_version VARCHAR(50),
    provider VARCHAR(50)
);

CREATE TABLE ai_prompts (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    prompt_name VARCHAR(255) NOT NULL,
    system_prompt TEXT,
    version INTEGER
);

CREATE TABLE ai_conversations (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20),
    ai_model_id BIGINT,
    prompt_id BIGINT,
    related_document_id BIGINT,
    user_id BIGINT NOT NULL
);

CREATE TABLE ai_messages (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    message TEXT,
    response_time_ms INTEGER,
    role VARCHAR(20),
    token_count INTEGER,
    conversation_id BIGINT NOT NULL,
    related_document_id BIGINT
);

CREATE TABLE ai_usage_logs (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    estimated_cost NUMERIC(10,6),
    prompt_tokens INTEGER,
    provider_response_status VARCHAR(50),
    response_time_ms INTEGER,
    response_tokens INTEGER,
    total_tokens INTEGER,
    ai_model_id BIGINT,
    conversation_id BIGINT NOT NULL
);

-- =========================================
-- STORAGE MODULE
-- =========================================

CREATE TABLE stor_providers (
    id BIGINT PRIMARY KEY,
    api_endpoint TEXT,
    base_url TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    provider_name VARCHAR(100) NOT NULL,
    provider_type VARCHAR(100)
);

CREATE TABLE stor_quotas (
    id BIGINT PRIMARY KEY,
    max_storage_mb BIGINT,
    updated_at TIMESTAMPTZ NOT NULL,
    used_storage_mb BIGINT,
    auth_account_id BIGINT UNIQUE NOT NULL
);

CREATE TABLE stor_upload_logs (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    log_level VARCHAR(20),
    message TEXT,
    document_file_id BIGINT NOT NULL
);