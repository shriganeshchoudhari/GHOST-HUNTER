# GHOST-HUNTER: Database Schema Documentation

**Version:** 1.0  
**Date:** March 16, 2026  
**Author:** Manus AI  
**Status:** Draft

---

## Overview

This document defines the complete database schema for the GHOST-HUNTER platform, including table definitions, relationships, indexes, and data models. The schema is designed for PostgreSQL 14+ with support for JSON data types, time-series extensions, and advanced indexing strategies.

---

## 1. Core Tables

### 1.1 Users Table

Stores user account information and authentication credentials.

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    avatar_url VARCHAR(512),
    subscription_tier VARCHAR(50) DEFAULT 'free' CHECK (subscription_tier IN ('free', 'premium', 'enterprise')),
    subscription_status VARCHAR(50) DEFAULT 'active' CHECK (subscription_status IN ('active', 'inactive', 'cancelled')),
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
    deleted_at TIMESTAMP,
    CONSTRAINT valid_email CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_subscription_tier ON users(subscription_tier);
CREATE INDEX idx_users_created_at ON users(created_at DESC);
```

**Column Descriptions:**

- `id`: Unique identifier for each user
- `email`: User's email address (unique)
- `username`: Display name (unique)
- `password_hash`: Bcrypt-hashed password
- `subscription_tier`: User's subscription level (free, premium, enterprise)
- `preferences`: JSON object storing user preferences (language, notifications, privacy settings)
- `deleted_at`: Soft delete timestamp for GDPR compliance

### 1.2 Authentication Tokens Table

Stores JWT tokens and refresh tokens for session management.

```sql
CREATE TABLE auth_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_token_hash VARCHAR(255) NOT NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    access_token_expires_at TIMESTAMP NOT NULL,
    refresh_token_expires_at TIMESTAMP NOT NULL,
    device_id VARCHAR(255),
    device_type VARCHAR(50) CHECK (device_type IN ('ios', 'android', 'web')),
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP
);

CREATE INDEX idx_auth_tokens_user_id ON auth_tokens(user_id);
CREATE INDEX idx_auth_tokens_access_token_hash ON auth_tokens(access_token_hash);
CREATE INDEX idx_auth_tokens_refresh_token_hash ON auth_tokens(refresh_token_hash);
```

### 1.3 WiFi Networks Table

Stores information about WiFi networks users have scanned.

```sql
CREATE TABLE wifi_networks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ssid VARCHAR(255) NOT NULL,
    bssid MACADDR NOT NULL,
    frequency INTEGER,
    band VARCHAR(20) CHECK (band IN ('2.4GHz', '5GHz', '6GHz')),
    security_type VARCHAR(50),
    channel INTEGER,
    max_data_rate INTEGER,
    first_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(ssid, bssid)
);

CREATE INDEX idx_wifi_networks_ssid ON wifi_networks(ssid);
CREATE INDEX idx_wifi_networks_bssid ON wifi_networks(bssid);
```

### 1.4 WiFi Telemetry Table

Stores individual WiFi signal measurements with spatial and temporal context.

```sql
CREATE TABLE wifi_telemetry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    wifi_network_id UUID REFERENCES wifi_networks(id),
    rssi INTEGER NOT NULL CHECK (rssi >= -100 AND rssi <= 0),
    noise_level INTEGER,
    latency_ms INTEGER CHECK (latency_ms >= 0),
    packet_loss_percent DECIMAL(5, 2) CHECK (packet_loss_percent >= 0 AND packet_loss_percent <= 100),
    throughput_mbps DECIMAL(10, 2),
    device_orientation JSONB,
    position_approximation JSONB,
    location_accuracy_meters DECIMAL(10, 2),
    device_id VARCHAR(255),
    session_id UUID,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (wifi_network_id) REFERENCES wifi_networks(id)
);

CREATE INDEX idx_wifi_telemetry_user_id ON wifi_telemetry(user_id);
CREATE INDEX idx_wifi_telemetry_timestamp ON wifi_telemetry(timestamp DESC);
CREATE INDEX idx_wifi_telemetry_session_id ON wifi_telemetry(session_id);
CREATE INDEX idx_wifi_telemetry_rssi ON wifi_telemetry(rssi);
```

**Column Descriptions:**

- `rssi`: Signal strength in dBm (-100 to 0, higher is better)
- `device_orientation`: JSON object with pitch, roll, yaw angles
- `position_approximation`: JSON object with x, y, z coordinates relative to starting point
- `location_accuracy_meters`: Estimated accuracy of position approximation

### 1.5 Heat Maps Table

Stores generated heat maps representing spatial WiFi coverage.

```sql
CREATE TABLE heat_maps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    location_name VARCHAR(255),
    bounds JSONB NOT NULL,
    grid_resolution DECIMAL(10, 2) DEFAULT 1.0,
    grid_data JSONB NOT NULL,
    statistics JSONB,
    generated_from_session_id UUID,
    is_public BOOLEAN DEFAULT FALSE,
    share_token VARCHAR(255) UNIQUE,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_heat_maps_user_id ON heat_maps(user_id);
CREATE INDEX idx_heat_maps_created_at ON heat_maps(created_at DESC);
CREATE INDEX idx_heat_maps_share_token ON heat_maps(share_token);
```

**Column Descriptions:**

- `bounds`: JSON object defining the geographic boundaries of the heat map
- `grid_data`: JSON array containing signal strength values for each grid cell
- `statistics`: JSON object with min, max, average RSSI values
- `share_token`: Unique token for sharing heat maps without authentication

### 1.6 Sessions Table

Tracks user hunting sessions for analytics and progress tracking.

```sql
CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_type VARCHAR(50) CHECK (session_type IN ('hunting', 'scanning', 'analysis')),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_seconds INTEGER,
    location_name VARCHAR(255),
    bounds JSONB,
    ghosts_encountered INTEGER DEFAULT 0,
    ghosts_captured INTEGER DEFAULT 0,
    safe_zones_discovered INTEGER DEFAULT 0,
    achievements_unlocked INTEGER DEFAULT 0,
    score INTEGER DEFAULT 0,
    heat_map_id UUID REFERENCES heat_maps(id),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_start_time ON sessions(start_time DESC);
```

---

## 2. Gamification Tables

### 2.1 Achievements Table

Stores achievement definitions and user progress.

```sql
CREATE TABLE achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon_url VARCHAR(512),
    category VARCHAR(50) CHECK (category IN ('exploration', 'optimization', 'social', 'milestone')),
    points INTEGER DEFAULT 0,
    rarity VARCHAR(50) CHECK (rarity IN ('common', 'uncommon', 'rare', 'epic', 'legendary')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_achievements_code ON achievements(code);
```

### 2.2 User Achievements Table

Tracks which achievements users have earned.

```sql
CREATE TABLE user_achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES achievements(id),
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    progress_percent INTEGER DEFAULT 100,
    UNIQUE(user_id, achievement_id)
);

CREATE INDEX idx_user_achievements_user_id ON user_achievements(user_id);
CREATE INDEX idx_user_achievements_earned_at ON user_achievements(earned_at DESC);
```

### 2.3 Leaderboard Table

Stores leaderboard rankings and scores.

```sql
CREATE TABLE leaderboard (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    leaderboard_type VARCHAR(50) CHECK (leaderboard_type IN ('global', 'regional', 'friends')),
    score INTEGER NOT NULL,
    rank INTEGER,
    region VARCHAR(100),
    period VARCHAR(50) CHECK (period IN ('all_time', 'monthly', 'weekly', 'daily')),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, leaderboard_type, region, period)
);

CREATE INDEX idx_leaderboard_user_id ON leaderboard(user_id);
CREATE INDEX idx_leaderboard_score ON leaderboard(score DESC);
CREATE INDEX idx_leaderboard_rank ON leaderboard(rank);
```

---

## 3. Social Features Tables

### 3.1 Friends Table

Manages user friendships and social connections.

```sql
CREATE TABLE friendships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    friend_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(50) DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'blocked')),
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    CHECK (user_id < friend_id)
);

CREATE INDEX idx_friendships_user_id ON friendships(user_id);
CREATE INDEX idx_friendships_friend_id ON friendships(friend_id);
CREATE INDEX idx_friendships_status ON friendships(status);
```

### 3.2 Shared Content Table

Stores shared heat maps, gameplay clips, and achievements.

```sql
CREATE TABLE shared_content (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_type VARCHAR(50) CHECK (content_type IN ('heat_map', 'gameplay_clip', 'achievement', 'session_summary')),
    content_id UUID NOT NULL,
    title VARCHAR(255),
    description TEXT,
    thumbnail_url VARCHAR(512),
    view_count INTEGER DEFAULT 0,
    like_count INTEGER DEFAULT 0,
    share_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

CREATE INDEX idx_shared_content_user_id ON shared_content(user_id);
CREATE INDEX idx_shared_content_created_at ON shared_content(created_at DESC);
```

---

## 4. Community Data Tables

### 4.1 Anonymized Telemetry Table

Stores aggregated, anonymized telemetry data for community insights.

```sql
CREATE TABLE anonymized_telemetry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    region VARCHAR(100),
    grid_cell JSONB,
    average_rssi DECIMAL(10, 2),
    median_rssi DECIMAL(10, 2),
    std_dev_rssi DECIMAL(10, 2),
    measurement_count INTEGER,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(region, grid_cell)
);

CREATE INDEX idx_anonymized_telemetry_region ON anonymized_telemetry(region);
```

### 4.2 Regional Statistics Table

Stores computed regional statistics for analytics dashboards.

```sql
CREATE TABLE regional_statistics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    region VARCHAR(100) NOT NULL,
    period_start DATE,
    period_end DATE,
    total_users INTEGER,
    active_users INTEGER,
    average_signal_strength DECIMAL(10, 2),
    coverage_percentage DECIMAL(5, 2),
    common_issues JSONB,
    recommendations JSONB,
    computed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(region, period_start, period_end)
);

CREATE INDEX idx_regional_statistics_region ON regional_statistics(region);
```

---

## 5. Analytics and Logging Tables

### 5.1 User Activity Log Table

Tracks user actions for analytics and debugging.

```sql
CREATE TABLE user_activity_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id UUID,
    details JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_activity_log_user_id ON user_activity_log(user_id);
CREATE INDEX idx_user_activity_log_action ON user_activity_log(action);
CREATE INDEX idx_user_activity_log_created_at ON user_activity_log(created_at DESC);
```

### 5.2 API Request Log Table

Logs API requests for performance monitoring and debugging.

```sql
CREATE TABLE api_request_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    endpoint VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    status_code INTEGER,
    response_time_ms INTEGER,
    request_size_bytes INTEGER,
    response_size_bytes INTEGER,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_api_request_log_user_id ON api_request_log(user_id);
CREATE INDEX idx_api_request_log_endpoint ON api_request_log(endpoint);
CREATE INDEX idx_api_request_log_created_at ON api_request_log(created_at DESC);
```

---

## 6. Subscription and Billing Tables

### 6.1 Subscriptions Table

Manages user subscription information.

```sql
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    tier VARCHAR(50) NOT NULL CHECK (tier IN ('free', 'premium', 'enterprise')),
    status VARCHAR(50) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'cancelled', 'suspended')),
    billing_cycle VARCHAR(50) CHECK (billing_cycle IN ('monthly', 'annual')),
    current_period_start DATE,
    current_period_end DATE,
    next_billing_date DATE,
    cancel_at_period_end BOOLEAN DEFAULT FALSE,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_tier ON subscriptions(tier);
```

### 6.2 Billing History Table

Tracks billing transactions and invoices.

```sql
CREATE TABLE billing_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID NOT NULL REFERENCES subscriptions(id),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(50) CHECK (status IN ('pending', 'completed', 'failed', 'refunded')),
    invoice_number VARCHAR(50) UNIQUE,
    payment_method VARCHAR(50),
    transaction_id VARCHAR(255),
    billing_date DATE,
    due_date DATE,
    paid_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_billing_history_subscription_id ON billing_history(subscription_id);
CREATE INDEX idx_billing_history_status ON billing_history(status);
```

---

## 7. Data Retention Policies

| Table | Free Tier | Premium Tier | Enterprise Tier |
|---|---|---|---|
| wifi_telemetry | 30 days | 6 months | 24 months |
| heat_maps | 30 days | 6 months | Unlimited |
| sessions | 90 days | 1 year | Unlimited |
| user_activity_log | 7 days | 30 days | 90 days |
| api_request_log | 7 days | 30 days | 90 days |

---

## 8. Backup and Recovery

**Backup Strategy:**
- Daily full backups at 02:00 UTC
- Hourly incremental backups
- 30-day backup retention
- Cross-region backup replication

**Recovery Procedures:**
- Recovery Time Objective (RTO): 1 hour
- Recovery Point Objective (RPO): 15 minutes
- Automated backup verification and integrity checks

---

## 9. Performance Optimization

### 9.1 Indexing Strategy

All frequently queried columns have indexes to optimize query performance. Composite indexes are used for common query patterns.

### 9.2 Partitioning

The `wifi_telemetry` table is partitioned by date to improve query performance and enable efficient data archival:

```sql
CREATE TABLE wifi_telemetry_2026_03 PARTITION OF wifi_telemetry
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');
```

### 9.3 Query Optimization

- Use EXPLAIN ANALYZE to identify slow queries
- Maintain statistics with ANALYZE command
- Regular VACUUM to prevent table bloat

---

## 10. Data Migration Strategy

**Version 1.0 to 1.1 Migration Example:**

```sql
-- Add new column with default value
ALTER TABLE users ADD COLUMN preferences JSONB DEFAULT '{}';

-- Backfill existing data
UPDATE users SET preferences = '{"language": "en", "notifications": true}' 
WHERE preferences IS NULL;

-- Add constraint
ALTER TABLE users ALTER COLUMN preferences SET NOT NULL;
```

---

## Appendix: SQL Setup Script

Complete SQL script to initialize the database:

```sql
-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create tables (as defined above)
-- Run all CREATE TABLE statements

-- Create views for common queries
CREATE VIEW user_stats AS
SELECT 
    u.id,
    u.username,
    COUNT(DISTINCT s.id) as total_sessions,
    COUNT(DISTINCT hm.id) as total_heat_maps,
    COUNT(DISTINCT ua.achievement_id) as achievements_earned,
    COALESCE(l.score, 0) as current_score
FROM users u
LEFT JOIN sessions s ON u.id = s.user_id
LEFT JOIN heat_maps hm ON u.id = hm.user_id
LEFT JOIN user_achievements ua ON u.id = ua.user_id
LEFT JOIN leaderboard l ON u.id = l.user_id AND l.period = 'all_time'
GROUP BY u.id, u.username, l.score;

-- Create stored procedures for common operations
CREATE OR REPLACE FUNCTION update_leaderboard()
RETURNS void AS $$
BEGIN
    -- Update global leaderboard
    DELETE FROM leaderboard WHERE leaderboard_type = 'global' AND period = 'all_time';
    INSERT INTO leaderboard (user_id, leaderboard_type, score, rank, period)
    SELECT 
        u.id,
        'global',
        COALESCE(SUM(s.score), 0),
        ROW_NUMBER() OVER (ORDER BY COALESCE(SUM(s.score), 0) DESC),
        'all_time'
    FROM users u
    LEFT JOIN sessions s ON u.id = s.user_id
    GROUP BY u.id;
END;
$$ LANGUAGE plpgsql;
```

---

## 11. Security Considerations

- All passwords are hashed with bcrypt (cost factor: 12)
- Sensitive data (API keys, tokens) are encrypted at rest
- Row-level security policies prevent unauthorized data access
- Regular security audits and penetration testing
- GDPR compliance with data minimization and right to be forgotten
