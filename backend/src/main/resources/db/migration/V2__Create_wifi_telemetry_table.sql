-- Create wifi_telemetry table
CREATE TABLE wifi_telemetry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rssi INTEGER NOT NULL,
    noise_level INTEGER,
    latency_ms INTEGER,
    packet_loss_percent DECIMAL(5,2),
    throughput_mbps DECIMAL(10,2),
    device_orientation JSONB,
    position_approximation JSONB,
    location_accuracy_meters DECIMAL(10,2),
    device_id VARCHAR(255),
    session_id UUID,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_wifi_telemetry_user_id ON wifi_telemetry(user_id);
CREATE INDEX idx_wifi_telemetry_timestamp ON wifi_telemetry(timestamp DESC);
CREATE INDEX idx_wifi_telemetry_session_id ON wifi_telemetry(session_id);
CREATE INDEX idx_wifi_telemetry_rssi ON wifi_telemetry(rssi);

-- Create function to update created_at timestamp
CREATE OR REPLACE FUNCTION update_created_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.created_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update created_at
CREATE TRIGGER update_wifi_telemetry_created_at 
    BEFORE INSERT ON wifi_telemetry 
    FOR EACH ROW 
    EXECUTE FUNCTION update_created_at_column();

-- Add constraint for RSSI range validation
ALTER TABLE wifi_telemetry ADD CONSTRAINT valid_rssi CHECK (rssi >= -100 AND rssi <= 0);

-- Create view for active sessions
CREATE OR REPLACE VIEW active_telemetry_sessions AS
SELECT 
    session_id,
    MIN(timestamp) as start_time,
    MAX(timestamp) as end_time,
    COUNT(*) as measurement_count,
    AVG(rssi) as avg_rssi
FROM wifi_telemetry 
WHERE session_id IS NOT NULL 
    AND timestamp >= CURRENT_TIMESTAMP - INTERVAL '24 hours'
GROUP BY session_id
HAVING COUNT(*) > 0;

-- Create function to find active sessions by user
CREATE OR REPLACE FUNCTION find_active_sessions_by_user(p_user_id UUID, p_cutoff_time TIMESTAMP)
RETURNS TABLE (
    session_id VARCHAR,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    measurement_count BIGINT,
    avg_rssi DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.session_id::VARCHAR,
        s.start_time,
        s.end_time,
        s.measurement_count,
        s.avg_rssi
    FROM active_telemetry_sessions s
    JOIN wifi_telemetry t ON s.session_id = t.session_id
    WHERE t.user_id = p_user_id
        AND s.start_time >= p_cutoff_time
    GROUP BY s.session_id, s.start_time, s.end_time, s.measurement_count, s.avg_rssi
    ORDER BY s.start_time DESC;
END;
$$ LANGUAGE plpgsql;

-- Create function to delete old telemetry data
CREATE OR REPLACE FUNCTION delete_old_telemetry_data(p_user_id UUID, p_cutoff_time TIMESTAMP)
RETURNS BIGINT AS $$
DECLARE
    deleted_count BIGINT;
BEGIN
    DELETE FROM wifi_telemetry 
    WHERE user_id = p_user_id 
        AND timestamp < p_cutoff_time;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;