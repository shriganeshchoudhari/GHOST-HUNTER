-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    avatar_url VARCHAR(512),
    subscription_tier VARCHAR(50) DEFAULT 'FREE' CHECK (subscription_tier IN ('FREE', 'PREMIUM', 'ENTERPRISE')),
    subscription_status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (subscription_status IN ('ACTIVE', 'INACTIVE', 'CANCELLED')),
    subscription_started_at TIMESTAMP,
    subscription_expires_at TIMESTAMP,
    email_verified BOOLEAN DEFAULT FALSE,
    email_verified_at TIMESTAMP,
    phone_number VARCHAR(20),
    country VARCHAR(100),
    region VARCHAR(100),
    bio TEXT,
    preferences JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_subscription_tier ON users(subscription_tier);
CREATE INDEX idx_users_created_at ON users(created_at DESC);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Add constraint for email format validation
ALTER TABLE users ADD CONSTRAINT valid_email CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$');

-- Insert sample data for development
INSERT INTO users (email, username, password_hash, full_name, subscription_tier, subscription_status, email_verified, country, region, bio)
VALUES 
    ('admin@ghosthunter.app', 'admin', '$2a$12$Djy5QZqy5QZqy5QZqy5QZuVfZqy5QZqy5QZqy5QZqy5QZqy5QZqy5', 'Admin User', 'PREMIUM', 'ACTIVE', true, 'USA', 'California', 'Ghost Hunter Admin'),
    ('demo@ghosthunter.app', 'demo_user', '$2a$12$Djy5QZqy5QZqy5QZqy5QZuVfZqy5QZqy5QZqy5QZqy5QZqy5QZqy5', 'Demo User', 'FREE', 'ACTIVE', true, 'USA', 'New York', 'Demo account for testing');