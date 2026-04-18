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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    wifi_ssid VARCHAR(255),
    wifi_bssid VARCHAR(17),
    wifi_frequency INTEGER,
    wifi_channel INTEGER,
    wifi_bandwidth INTEGER,
    wifi_link_speed INTEGER,
    wifi_rssi INTEGER,
    wifi_security VARCHAR(50),
    device_model VARCHAR(100),
    device_manufacturer VARCHAR(100),
    device_os VARCHAR(50),
    device_os_version VARCHAR(50),
    app_version VARCHAR(20),
    signal_quality VARCHAR(50),
    connection_status VARCHAR(50),
    network_type VARCHAR(50),
    optimal_positioning VARCHAR(100),
    measurement_type VARCHAR(50),
    measurement_mode VARCHAR(50),
    measurement_accuracy VARCHAR(50),
    measurement_confidence VARCHAR(50),
    measurement_environment VARCHAR(100),
    measurement_purpose VARCHAR(100),
    measurement_tags JSONB,
    measurement_metadata JSONB,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    altitude DECIMAL(10,2),
    accuracy DECIMAL(10,2)
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
