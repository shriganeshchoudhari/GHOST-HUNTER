# GHOST-HUNTER: Technical Design Document (TDD)

**Version:** 1.0  
**Date:** March 16, 2026  
**Author:** Manus AI  
**Status:** Draft

---

## Executive Summary

This Technical Design Document provides a comprehensive overview of the GHOST-HUNTER system architecture, design patterns, and implementation strategies. The document outlines the mobile application design, backend microservices architecture, data pipeline, AR system implementation, and DevOps infrastructure required to build a scalable, production-grade WiFi optimization platform.

---

## 1. System Architecture Overview

### 1.1 High-Level Architecture

The GHOST-HUNTER platform follows a modern distributed architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                         Mobile Clients                          │
│                  (iOS/Android via React Native)                 │
└────────────────────────────┬────────────────────────────────────┘
                             │
                    ┌────────▼────────┐
                    │  API Gateway    │
                    │   (Nginx/ALB)   │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
   ┌────▼────┐         ┌────▼────┐         ┌────▼────┐
   │  User   │         │  WiFi   │         │ Analytics│
   │ Service │         │Telemetry│         │ Service  │
   └────┬────┘         │ Service │         └────┬────┘
        │              └────┬────┘              │
        │                   │                   │
   ┌────▼───────────────────▼───────────────────▼────┐
   │          PostgreSQL Database                    │
   │  (Users, Telemetry, Heat Maps, Analytics)       │
   └──────────────────────┬──────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
   ┌────▼────┐      ┌────▼────┐      ┌────▼────┐
   │  Redis  │      │ Message │      │  Time-  │
   │ Cache   │      │  Queue  │      │ Series  │
   └─────────┘      │(RabbitMQ)      │  DB    │
                    └─────────┘      └────────┘
```

### 1.2 Component Overview

The system consists of the following major components:

**Mobile Application:** React Native/Expo-based iOS and Android app providing AR visualization and user interface.

**API Gateway:** Nginx or AWS Application Load Balancer handling request routing, load balancing, and SSL termination.

**Microservices:** Independent Spring Boot services handling specific business domains (user management, telemetry processing, analytics).

**Data Layer:** PostgreSQL for persistent storage, Redis for caching and sessions, and time-series database for metrics.

**Message Queue:** RabbitMQ or Apache Kafka for asynchronous communication between services.

---

## 2. Mobile Application Architecture

### 2.1 Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| Framework | React Native | Cross-platform iOS/Android development |
| Build Tool | Expo | Simplified React Native development and deployment |
| Language | TypeScript | Type-safe JavaScript development |
| State Management | Redux Toolkit | Centralized application state |
| Navigation | React Navigation | Screen navigation and routing |
| AR Engine | React Native AR | Cross-platform AR capabilities |
| HTTP Client | Axios | API communication with retry logic |
| Local Storage | AsyncStorage | Persistent device storage |
| Testing | Jest, React Native Testing Library | Unit and component testing |

### 2.2 Application Structure

```
mobile/
├── src/
│   ├── screens/
│   │   ├── HomeScreen.tsx           # Main navigation hub
│   │   ├── ARHuntingScreen.tsx      # AR ghost hunting interface
│   │   ├── HeatMapScreen.tsx        # Heat map visualization
│   │   ├── ProfileScreen.tsx        # User profile and stats
│   │   ├── LeaderboardScreen.tsx    # Global rankings
│   │   └── SettingsScreen.tsx       # App configuration
│   ├── components/
│   │   ├── ARView.tsx               # AR camera and rendering
│   │   ├── GhostEntity.tsx          # Ghost animation component
│   │   ├── HeatMapRenderer.tsx      # Heat map visualization
│   │   ├── SignalIndicator.tsx      # Real-time signal display
│   │   └── BottomSheet.tsx          # Reusable bottom sheet
│   ├── services/
│   │   ├── wifiService.ts           # WiFi signal measurement
│   │   ├── apiService.ts            # Backend API communication
│   │   ├── telemetryService.ts      # Data collection and upload
│   │   ├── arService.ts             # AR engine management
│   │   └── authService.ts           # Authentication logic
│   ├── ar/
│   │   ├── ghostSystem.ts           # Ghost behavior logic
│   │   ├── heatMapGenerator.ts      # Heat map algorithm
│   │   ├── arEngine.ts              # AR rendering engine
│   │   └── spatialAnchors.ts        # AR spatial tracking
│   ├── store/
│   │   ├── slices/
│   │   │   ├── userSlice.ts         # User state
│   │   │   ├── telemetrySlice.ts    # Telemetry state
│   │   │   ├── arSlice.ts           # AR state
│   │   │   └── uiSlice.ts           # UI state
│   │   └── store.ts                 # Redux store configuration
│   ├── utils/
│   │   ├── constants.ts             # App constants
│   │   ├── validators.ts            # Input validation
│   │   ├── formatters.ts            # Data formatting
│   │   └── logger.ts                # Logging utility
│   ├── hooks/
│   │   ├── useWiFiSignal.ts         # WiFi signal hook
│   │   ├── useARTracking.ts         # AR tracking hook
│   │   └── useHeatMap.ts            # Heat map generation hook
│   └── App.tsx                      # Root component
├── app.json                         # Expo configuration
├── package.json                     # Dependencies
└── tsconfig.json                    # TypeScript configuration
```

### 2.3 WiFi Signal Measurement

The mobile app continuously measures WiFi signal strength using platform-specific APIs:

**iOS Implementation:**
- Uses `NEHotspotHelper` framework for WiFi network information
- Accesses `CNCopyCurrentNetworkInfo()` for SSID and BSSID
- Measures signal strength via `NENetworkStatus`

**Android Implementation:**
- Uses `WifiManager` API for WiFi network scanning
- Accesses `WifiInfo` for current connection details
- Measures RSSI via `ScanResult` objects

**Measurement Frequency:** Every 100ms to provide smooth AR updates

**Data Collected per Measurement:**
- RSSI (Received Signal Strength Indicator) in dBm
- Network latency via ICMP ping
- Packet loss percentage
- Device orientation (pitch, roll, yaw)
- Approximate position (accelerometer-based)
- Timestamp

### 2.4 AR System Design

The AR system creates an immersive ghost-hunting experience by overlaying digital content on the real-world camera view.

**Ghost Behavior System:**

Ghosts respond dynamically to WiFi signal strength:

| Signal Strength | RSSI Range | Ghost State | Visual Behavior |
|---|---|---|---|
| Very Weak | < -85 dBm | Aggressive | Rapid movement, intense animations |
| Weak | -85 to -70 dBm | Active | Moderate movement, flickering |
| Medium | -70 to -60 dBm | Weakening | Slow movement, transparency increasing |
| Strong | -60 to -50 dBm | Fading | Minimal movement, high transparency |
| Very Strong | > -50 dBm | Vanished | Ghost disappears, safe zone appears |

**Safe Zone Visualization:**

When signal strength exceeds -50 dBm, ghosts vanish and safe zones appear with:

- Glowing circular aura indicating coverage area
- Particle effects suggesting protection
- Audio cue confirming safe zone entry
- Achievement notification for zone discovery

**Spatial Anchors:**

The AR system uses spatial anchors to place ghosts and safe zones in 3D space:

- Anchors persist across app sessions using device-relative positioning
- Anchors update as user moves to maintain spatial accuracy
- Multiple anchors create a ghost map of the entire environment

### 2.5 Heat Map Generation Algorithm

The heat map is generated using kriging interpolation to estimate signal strength across the environment:

1. **Data Collection:** Gather signal measurements with associated positions
2. **Grid Creation:** Divide environment into 1-meter grid cells
3. **Kriging Interpolation:** Estimate signal strength for unmeasured cells using surrounding measurements
4. **Color Mapping:** Assign colors based on signal strength (red for weak, yellow for medium, green for strong)
5. **Rendering:** Display heat map as semi-transparent overlay on camera view

**Performance Optimization:**
- Update heat map incrementally as new data arrives
- Cache interpolation results for unchanged grid cells
- Use GPU-accelerated rendering for smooth visualization

---

## 3. Backend Services Architecture

### 3.1 Microservices Design

The backend implements a microservices architecture with the following services:

**User Service**
- User registration and authentication
- Profile management
- Subscription tier management
- Social features (friends, followers)

**WiFi Telemetry Service**
- Receives signal strength measurements from mobile clients
- Validates and sanitizes telemetry data
- Stores raw measurements in time-series database
- Publishes events for downstream processing

**Heat Map Service**
- Consumes telemetry events
- Generates spatial heat maps using kriging algorithm
- Caches heat maps for fast retrieval
- Provides heat map visualization data to mobile clients

**Analytics Service**
- Analyzes telemetry data to identify patterns
- Generates optimization recommendations
- Computes user statistics and achievements
- Produces regional analytics reports

**Community Data Service**
- Aggregates anonymized telemetry data
- Computes regional signal strength statistics
- Identifies common network issues
- Provides community insights to users

### 3.2 Service Communication

Services communicate through multiple mechanisms:

**Synchronous Communication (REST APIs):**
- Used for request-response interactions
- Examples: User queries, profile updates, heat map retrieval

**Asynchronous Communication (Message Queue):**
- Used for event-driven workflows
- Examples: Telemetry ingestion, heat map generation, analytics computation

**Data Sharing:**
- Shared PostgreSQL database for consistency
- Redis cache for frequently accessed data
- Time-series database for metrics

### 3.3 Spring Boot Configuration

Each microservice is a Spring Boot application with:

- Spring Web for REST API endpoints
- Spring Data JPA for database access
- Spring Security for authentication and authorization
- Spring Cloud for service discovery and configuration
- Spring Kafka/RabbitMQ for message-driven architecture

---

## 4. Data Architecture

### 4.1 Database Schema Overview

**Core Tables:**

```sql
-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    subscription_tier VARCHAR(50) DEFAULT 'free',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- WiFi telemetry measurements
CREATE TABLE wifi_telemetry (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    rssi INTEGER NOT NULL,
    latency_ms INTEGER,
    packet_loss_percent DECIMAL(5, 2),
    device_orientation JSONB,
    position_approximation JSONB,
    network_identifier VARCHAR(255),
    timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Heat maps
CREATE TABLE heat_maps (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255),
    grid_data JSONB NOT NULL,
    bounds JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Achievements
CREATE TABLE achievements (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    achievement_type VARCHAR(100) NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Leaderboard entries
CREATE TABLE leaderboard (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    score INTEGER NOT NULL,
    rank INTEGER,
    region VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 4.2 Time-Series Data Storage

Signal strength measurements are stored in a time-series database optimized for high-volume metric ingestion:

- **Database:** InfluxDB or TimescaleDB
- **Retention Policy:** 30 days for free tier, 6 months for premium
- **Aggregation:** Hourly, daily, and weekly rollups for historical analysis
- **Indexing:** Optimized for time-range queries

### 4.3 Caching Strategy

Redis is used for caching to improve performance:

- **Session Cache:** User authentication tokens (TTL: 24 hours)
- **Heat Map Cache:** Recently generated heat maps (TTL: 1 hour)
- **User Cache:** User profiles and preferences (TTL: 1 hour)
- **Leaderboard Cache:** Top 100 users (TTL: 15 minutes)

---

## 5. API Design

### 5.1 REST API Endpoints

**Authentication Endpoints:**
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh` - Token refresh
- `POST /api/v1/auth/logout` - User logout

**User Endpoints:**
- `GET /api/v1/users/profile` - Get user profile
- `PUT /api/v1/users/profile` - Update user profile
- `GET /api/v1/users/stats` - Get user statistics

**Telemetry Endpoints:**
- `POST /api/v1/telemetry/batch` - Submit batch telemetry data
- `GET /api/v1/telemetry/recent` - Get recent measurements

**Heat Map Endpoints:**
- `GET /api/v1/heatmaps` - List user's heat maps
- `GET /api/v1/heatmaps/{id}` - Get specific heat map
- `POST /api/v1/heatmaps` - Create new heat map
- `DELETE /api/v1/heatmaps/{id}` - Delete heat map

**Leaderboard Endpoints:**
- `GET /api/v1/leaderboard/global` - Global leaderboard
- `GET /api/v1/leaderboard/regional` - Regional leaderboard
- `GET /api/v1/leaderboard/friends` - Friends leaderboard

### 5.2 WebSocket API

Real-time communication for live data streaming:

**Channels:**
- `/ws/telemetry/{userId}` - Real-time telemetry updates
- `/ws/heatmap/{heatmapId}` - Live heat map updates
- `/ws/leaderboard` - Real-time leaderboard changes

---

## 6. Security Architecture

### 6.1 Authentication and Authorization

**JWT-Based Authentication:**
- Access tokens valid for 1 hour
- Refresh tokens valid for 30 days
- Tokens stored securely in device keychain (iOS) and KeyStore (Android)

**Authorization Levels:**
- Public endpoints (registration, login)
- Authenticated endpoints (profile, heat maps)
- Premium-only endpoints (advanced analytics)

### 6.2 Data Protection

**Encryption:**
- HTTPS/TLS 1.3 for all network communication
- AES-256 encryption for sensitive data at rest
- Salted password hashing using bcrypt

**Data Privacy:**
- WiFi passwords never captured
- Network traffic never inspected
- Telemetry data anonymized before sharing
- User consent required for data collection

### 6.3 Rate Limiting

- API rate limiting: 1000 requests per hour per user
- Telemetry ingestion: 100 measurements per second per user
- Prevents abuse and ensures fair resource allocation

---

## 7. Scalability and Performance

### 7.1 Horizontal Scaling

**Stateless Services:**
- All microservices are stateless and can be scaled horizontally
- Session state stored in Redis, not in-memory
- Database connections pooled and managed centrally

**Load Balancing:**
- API Gateway distributes requests across service instances
- Database read replicas for scaling read operations
- Redis cluster for distributed caching

### 7.2 Performance Optimization

**Mobile App:**
- AR rendering optimized for 60 FPS
- Heat map generation completed in <2 seconds
- Telemetry batching reduces network overhead

**Backend:**
- Database query optimization with proper indexing
- Caching layer reduces database load
- Asynchronous processing for long-running operations
- Connection pooling for efficient resource utilization

### 7.3 Monitoring and Observability

**Metrics Collection:**
- Prometheus for metrics collection
- Custom metrics for business logic (heat map generation time, telemetry ingestion rate)
- Application performance monitoring (APM) with New Relic or DataDog

**Logging:**
- Centralized logging with ELK Stack (Elasticsearch, Logstash, Kibana)
- Structured logging with correlation IDs for request tracing
- Log retention: 30 days for free tier, 90 days for premium

**Alerting:**
- Alert on service unavailability
- Alert on high error rates (>1%)
- Alert on performance degradation (API response time >500ms)

---

## 8. DevOps and Deployment

### 8.1 Containerization

**Docker:**
- Each microservice packaged as Docker container
- Multi-stage builds for optimized image size
- Container registry: Docker Hub or Amazon ECR

### 8.2 Orchestration

**Kubernetes:**
- Services deployed as Kubernetes deployments
- Horizontal Pod Autoscaling based on CPU and memory metrics
- Service mesh (Istio) for advanced traffic management

### 8.3 CI/CD Pipeline

**GitHub Actions Workflow:**
1. Code push triggers automated tests
2. Unit tests, integration tests, and linting
3. Docker image build and push to registry
4. Deployment to staging environment
5. Manual approval for production deployment
6. Blue-green deployment strategy for zero-downtime updates

---

## 9. Testing Strategy

### 9.1 Unit Testing

- Test individual functions and components in isolation
- Target: 80%+ code coverage
- Framework: Jest (mobile), JUnit (backend)

### 9.2 Integration Testing

- Test interactions between components and services
- Test API endpoints with mock data
- Test database operations

### 9.3 End-to-End Testing

- Test complete user workflows
- Test AR functionality on real devices
- Test heat map generation with realistic data

### 9.4 Performance Testing

- Load testing with 100,000+ concurrent users
- Stress testing to identify breaking points
- Latency testing for AR and heat map operations

---

## 10. Deployment Architecture

### 10.1 Infrastructure Components

**Load Balancer:** AWS Application Load Balancer or Nginx
**API Gateway:** Kong or AWS API Gateway
**Compute:** Kubernetes cluster with auto-scaling
**Database:** Managed PostgreSQL (RDS or equivalent)
**Cache:** Managed Redis (ElastiCache or equivalent)
**Message Queue:** Managed Kafka or RabbitMQ
**Storage:** S3 for user-generated content and backups
**CDN:** CloudFront for static asset distribution

### 10.2 Disaster Recovery

- Automated daily database backups
- Multi-region replication for high availability
- Recovery Time Objective (RTO): 1 hour
- Recovery Point Objective (RPO): 15 minutes

---

## 11. Future Enhancements

**Phase 2 Enhancements:**
- Multiplayer ghost hunting with real-time synchronization
- 3D environment mapping using LiDAR
- Advanced AR features (ghost interactions, environmental effects)

**Phase 3 Enhancements:**
- Machine learning for predictive signal strength modeling
- Integration with smart home systems
- ISP partnership APIs for professional network diagnostics

---

## Appendix: Technology Justification

**React Native:** Enables code sharing between iOS and Android, reducing development time and maintenance burden while maintaining native performance.

**Spring Boot:** Provides a mature, battle-tested framework for building scalable microservices with excellent tooling and community support.

**PostgreSQL:** Offers ACID compliance, advanced data types (JSONB), and excellent performance for both transactional and analytical workloads.

**Kubernetes:** Provides industry-standard container orchestration with automatic scaling, self-healing, and rolling updates.

**Redis:** Offers sub-millisecond latency for caching and session management, critical for real-time AR performance.
