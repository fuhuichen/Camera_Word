-- Initial schema for camera cloud system

-- Platforms table
CREATE TABLE platforms (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    test_endpoint TEXT,
    description TEXT,
    website_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- User platform authorizations (for PLATFORM_ADMIN)
CREATE TABLE user_platforms (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    platform_code VARCHAR(50) REFERENCES platforms(code) ON DELETE CASCADE,
    PRIMARY KEY (user_id, platform_code)
);

-- Cameras table
CREATE TABLE cameras (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    public_id VARCHAR(128) UNIQUE NOT NULL,
    model VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    target_platform_code VARCHAR(50) REFERENCES platforms(code),
    redirect_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_test_device BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT camera_id_format CHECK (public_id ~ '^[A-Za-z0-9_-]{3,128}$')
);

-- Import jobs table
CREATE TABLE import_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    uploader_user_id UUID REFERENCES users(id) NOT NULL,
    file_name VARCHAR(255),
    total_rows INTEGER,
    success_rows INTEGER,
    failed_rows INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Import job errors table
CREATE TABLE import_job_errors (
    id BIGSERIAL PRIMARY KEY,
    job_id UUID REFERENCES import_jobs(id) ON DELETE CASCADE NOT NULL,
    row_no INTEGER,
    camera_id_in_file VARCHAR(128),
    error_message TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Audit logs table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(128) NOT NULL,
    details JSONB,
    ip_address INET,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_cameras_public_id ON cameras(public_id);
CREATE INDEX idx_cameras_target_platform ON cameras(target_platform_code);
CREATE INDEX idx_cameras_status ON cameras(status);
CREATE INDEX idx_cameras_redirect_enabled ON cameras(redirect_enabled);
CREATE INDEX idx_import_jobs_status ON import_jobs(status);
CREATE INDEX idx_import_job_errors_job_id ON import_job_errors(job_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- Insert default platforms
INSERT INTO platforms (code, name, test_endpoint, description, website_url) VALUES 
('dk', 'DK', 'https://dk.example.com/test', 'DK 平台 - 专业直播平台', 'https://dk.example.com'),
('duixin', '兑心', 'https://duixin.example.com/test', '兑心平台 - 智能监控平台', 'https://duixin.example.com');

-- Insert default admin user
INSERT INTO users (email, display_name, role) VALUES 
('admin@example.com', 'System Administrator', 'MAIN_ADMIN'),
('platform@example.com', 'Platform Administrator', 'PLATFORM_ADMIN');

-- Insert test cameras for platforms (hidden from device list)
INSERT INTO cameras (public_id, model, target_platform_code, status, is_test_device) VALUES 
('DK_TEST_CAM', 'DK 测试相机', 'dk', 'ACTIVE', true),
('DUIXIN_TEST_CAM', '兑心测试相机', 'duixin', 'ACTIVE', true);
