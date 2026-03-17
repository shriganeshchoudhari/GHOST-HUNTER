# GHOST-HUNTER: API Documentation

**Version:** 1.0  
**Date:** March 16, 2026  
**Author:** Manus AI  
**Base URL:** `https://api.ghosthunter.app/api/v1`  
**Status:** Draft

---

## Overview

The GHOST-HUNTER API provides RESTful endpoints for mobile clients to interact with the backend services. All requests require HTTPS and authentication (except for public endpoints). The API uses JSON for request and response payloads.

---

## Authentication

### JWT Authentication

All authenticated endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <access_token>
```

Access tokens expire after 1 hour. Use the refresh endpoint to obtain new tokens.

### Token Refresh

**Endpoint:** `POST /auth/refresh`

**Request:**
```json
{
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 3600
}
```

---

## 1. Authentication Endpoints

### 1.1 User Registration

**Endpoint:** `POST /auth/register`

**Description:** Create a new user account.

**Request:**
```json
{
  "email": "user@example.com",
  "username": "ghosthunter123",
  "password": "SecurePassword123!",
  "full_name": "John Doe"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "username": "ghosthunter123",
  "full_name": "John Doe",
  "created_at": "2026-03-16T12:00:00Z"
}
```

**Error Responses:**
- `400 Bad Request`: Invalid email or password format
- `409 Conflict`: Email or username already exists

---

### 1.2 User Login

**Endpoint:** `POST /auth/login`

**Description:** Authenticate user and obtain tokens.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "device_id": "device-12345",
  "device_type": "ios"
}
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 3600,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "username": "ghosthunter123",
    "subscription_tier": "free"
  }
}
```

**Error Responses:**
- `401 Unauthorized`: Invalid credentials
- `429 Too Many Requests`: Too many login attempts

---

### 1.3 User Logout

**Endpoint:** `POST /auth/logout`

**Description:** Invalidate user tokens and end session.

**Request:**
```json
{
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "message": "Logged out successfully"
}
```

---

## 2. User Endpoints

### 2.1 Get User Profile

**Endpoint:** `GET /users/profile`

**Description:** Retrieve current user's profile information.

**Authentication:** Required

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "username": "ghosthunter123",
  "full_name": "John Doe",
  "avatar_url": "https://cdn.ghosthunter.app/avatars/550e8400.jpg",
  "subscription_tier": "premium",
  "subscription_status": "active",
  "subscription_expires_at": "2026-04-16T12:00:00Z",
  "country": "United States",
  "region": "California",
  "bio": "WiFi enthusiast and ghost hunter!",
  "created_at": "2026-03-16T12:00:00Z",
  "updated_at": "2026-03-16T12:00:00Z"
}
```

---

### 2.2 Update User Profile

**Endpoint:** `PUT /users/profile`

**Description:** Update user profile information.

**Authentication:** Required

**Request:**
```json
{
  "full_name": "John Smith",
  "avatar_url": "https://cdn.ghosthunter.app/avatars/new-avatar.jpg",
  "bio": "Updated bio",
  "country": "Canada",
  "region": "Ontario"
}
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "full_name": "John Smith",
  "avatar_url": "https://cdn.ghosthunter.app/avatars/new-avatar.jpg",
  "bio": "Updated bio",
  "country": "Canada",
  "region": "Ontario",
  "updated_at": "2026-03-16T13:00:00Z"
}
```

---

### 2.3 Get User Statistics

**Endpoint:** `GET /users/stats`

**Description:** Retrieve user's gameplay statistics and achievements.

**Authentication:** Required

**Response (200 OK):**
```json
{
  "user_id": "550e8400-e29b-41d4-a716-446655440000",
  "total_sessions": 42,
  "total_hunting_time_minutes": 1260,
  "ghosts_encountered": 523,
  "ghosts_captured": 487,
  "safe_zones_discovered": 156,
  "achievements_earned": 18,
  "current_score": 4750,
  "global_rank": 245,
  "regional_rank": 12,
  "friends_rank": 3,
  "last_session_date": "2026-03-16T10:30:00Z"
}
```

---

## 3. Telemetry Endpoints

### 3.1 Submit Batch Telemetry

**Endpoint:** `POST /telemetry/batch`

**Description:** Submit multiple WiFi signal measurements in a single request.

**Authentication:** Required

**Rate Limit:** 100 requests per minute

**Request:**
```json
{
  "session_id": "session-12345",
  "measurements": [
    {
      "rssi": -65,
      "latency_ms": 45,
      "packet_loss_percent": 2.5,
      "device_orientation": {
        "pitch": 15.5,
        "roll": -5.2,
        "yaw": 120.3
      },
      "position_approximation": {
        "x": 2.5,
        "y": 1.8,
        "z": 0.0
      },
      "location_accuracy_meters": 0.5,
      "timestamp": "2026-03-16T12:00:00Z"
    },
    {
      "rssi": -62,
      "latency_ms": 42,
      "packet_loss_percent": 1.8,
      "device_orientation": {
        "pitch": 16.2,
        "roll": -4.9,
        "yaw": 122.1
      },
      "position_approximation": {
        "x": 2.7,
        "y": 1.9,
        "z": 0.0
      },
      "location_accuracy_meters": 0.5,
      "timestamp": "2026-03-16T12:00:01Z"
    }
  ]
}
```

**Response (202 Accepted):**
```json
{
  "message": "Telemetry received",
  "measurements_accepted": 2,
  "session_id": "session-12345"
}
```

**Error Responses:**
- `400 Bad Request`: Invalid measurement data
- `413 Payload Too Large`: Batch size exceeds limit (max 1000 measurements)

---

### 3.2 Get Recent Measurements

**Endpoint:** `GET /telemetry/recent`

**Description:** Retrieve user's recent WiFi measurements.

**Authentication:** Required

**Query Parameters:**
- `limit` (optional): Number of measurements to return (default: 100, max: 1000)
- `session_id` (optional): Filter by specific session

**Response (200 OK):**
```json
{
  "measurements": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "rssi": -65,
      "latency_ms": 45,
      "packet_loss_percent": 2.5,
      "timestamp": "2026-03-16T12:00:00Z"
    }
  ],
  "total_count": 1,
  "has_more": false
}
```

---

## 4. Heat Map Endpoints

### 4.1 List Heat Maps

**Endpoint:** `GET /heatmaps`

**Description:** Retrieve user's heat maps.

**Authentication:** Required

**Query Parameters:**
- `limit` (optional): Number of heat maps to return (default: 20, max: 100)
- `offset` (optional): Pagination offset (default: 0)
- `sort_by` (optional): Sort field (created_at, updated_at, name)

**Response (200 OK):**
```json
{
  "heat_maps": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Living Room",
      "location_name": "Home - Living Room",
      "bounds": {
        "min_x": 0,
        "max_x": 10,
        "min_y": 0,
        "max_y": 8
      },
      "statistics": {
        "min_rssi": -85,
        "max_rssi": -45,
        "average_rssi": -65,
        "coverage_percentage": 92.5
      },
      "is_public": false,
      "view_count": 0,
      "created_at": "2026-03-16T12:00:00Z",
      "updated_at": "2026-03-16T12:00:00Z"
    }
  ],
  "total_count": 5,
  "has_more": false
}
```

---

### 4.2 Get Specific Heat Map

**Endpoint:** `GET /heatmaps/{id}`

**Description:** Retrieve a specific heat map with full grid data.

**Authentication:** Optional (required if private)

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Living Room",
  "location_name": "Home - Living Room",
  "bounds": {
    "min_x": 0,
    "max_x": 10,
    "min_y": 0,
    "max_y": 8
  },
  "grid_resolution": 1.0,
  "grid_data": [
    [-65, -64, -63, -62, -61, -60, -59, -58, -57, -56],
    [-66, -65, -64, -63, -62, -61, -60, -59, -58, -57],
    ...
  ],
  "statistics": {
    "min_rssi": -85,
    "max_rssi": -45,
    "average_rssi": -65,
    "coverage_percentage": 92.5,
    "dead_zone_count": 3,
    "optimal_zone_count": 25
  },
  "is_public": false,
  "share_token": null,
  "view_count": 0,
  "created_at": "2026-03-16T12:00:00Z"
}
```

---

### 4.3 Create Heat Map

**Endpoint:** `POST /heatmaps`

**Description:** Generate a new heat map from recent telemetry data.

**Authentication:** Required

**Request:**
```json
{
  "name": "Bedroom",
  "location_name": "Home - Bedroom",
  "bounds": {
    "min_x": 0,
    "max_x": 5,
    "min_y": 0,
    "max_y": 4
  },
  "grid_resolution": 0.5,
  "session_id": "session-12345"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Bedroom",
  "location_name": "Home - Bedroom",
  "bounds": {
    "min_x": 0,
    "max_x": 5,
    "min_y": 0,
    "max_y": 4
  },
  "grid_data": [...],
  "statistics": {
    "min_rssi": -75,
    "max_rssi": -50,
    "average_rssi": -62,
    "coverage_percentage": 85.0
  },
  "created_at": "2026-03-16T12:00:00Z"
}
```

---

### 4.4 Share Heat Map

**Endpoint:** `POST /heatmaps/{id}/share`

**Description:** Generate a shareable link for a heat map.

**Authentication:** Required

**Request:**
```json
{
  "is_public": true,
  "expiry_days": 30
}
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "share_token": "abc123def456",
  "share_url": "https://ghosthunter.app/share/abc123def456",
  "is_public": true,
  "expires_at": "2026-04-15T12:00:00Z"
}
```

---

### 4.5 Delete Heat Map

**Endpoint:** `DELETE /heatmaps/{id}`

**Description:** Delete a heat map.

**Authentication:** Required

**Response (204 No Content)**

---

## 5. Leaderboard Endpoints

### 5.1 Get Global Leaderboard

**Endpoint:** `GET /leaderboard/global`

**Description:** Retrieve global leaderboard rankings.

**Query Parameters:**
- `limit` (optional): Number of entries to return (default: 100, max: 1000)
- `period` (optional): Time period (all_time, monthly, weekly, daily)
- `offset` (optional): Pagination offset

**Response (200 OK):**
```json
{
  "leaderboard": [
    {
      "rank": 1,
      "user_id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "WiFiMaster",
      "score": 15000,
      "avatar_url": "https://cdn.ghosthunter.app/avatars/550e8400.jpg"
    },
    {
      "rank": 2,
      "user_id": "550e8400-e29b-41d4-a716-446655440001",
      "username": "GhostHunter99",
      "score": 14500,
      "avatar_url": "https://cdn.ghosthunter.app/avatars/550e8400.jpg"
    }
  ],
  "period": "all_time",
  "total_entries": 50000
}
```

---

### 5.2 Get Regional Leaderboard

**Endpoint:** `GET /leaderboard/regional`

**Description:** Retrieve leaderboard for user's region.

**Authentication:** Required

**Query Parameters:**
- `region` (optional): Override user's region
- `limit` (optional): Number of entries to return (default: 100)

**Response (200 OK):**
```json
{
  "leaderboard": [...],
  "region": "California",
  "period": "all_time",
  "total_entries": 5000
}
```

---

### 5.3 Get Friends Leaderboard

**Endpoint:** `GET /leaderboard/friends`

**Description:** Retrieve leaderboard among user's friends.

**Authentication:** Required

**Response (200 OK):**
```json
{
  "leaderboard": [...],
  "total_entries": 42
}
```

---

## 6. Social Endpoints

### 6.1 Send Friend Request

**Endpoint:** `POST /social/friends/request`

**Description:** Send a friend request to another user.

**Authentication:** Required

**Request:**
```json
{
  "username": "ghosthunter123"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "user_id": "550e8400-e29b-41d4-a716-446655440000",
  "friend_id": "550e8400-e29b-41d4-a716-446655440001",
  "status": "pending",
  "requested_at": "2026-03-16T12:00:00Z"
}
```

---

### 6.2 Accept Friend Request

**Endpoint:** `POST /social/friends/{id}/accept`

**Description:** Accept a pending friend request.

**Authentication:** Required

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "accepted",
  "accepted_at": "2026-03-16T12:00:00Z"
}
```

---

### 6.3 Get Friends List

**Endpoint:** `GET /social/friends`

**Description:** Retrieve user's friends list.

**Authentication:** Required

**Response (200 OK):**
```json
{
  "friends": [
    {
      "user_id": "550e8400-e29b-41d4-a716-446655440001",
      "username": "GhostHunter99",
      "avatar_url": "https://cdn.ghosthunter.app/avatars/550e8400.jpg",
      "current_score": 12000,
      "friend_since": "2026-02-16T12:00:00Z"
    }
  ],
  "total_count": 15
}
```

---

## 7. Achievements Endpoints

### 7.1 Get User Achievements

**Endpoint:** `GET /achievements`

**Description:** Retrieve user's earned achievements.

**Authentication:** Required

**Response (200 OK):**
```json
{
  "achievements": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "code": "first_hunt",
      "name": "First Hunt",
      "description": "Complete your first ghost hunting session",
      "icon_url": "https://cdn.ghosthunter.app/icons/first-hunt.png",
      "category": "milestone",
      "points": 10,
      "rarity": "common",
      "earned_at": "2026-03-16T12:00:00Z"
    }
  ],
  "total_count": 18
}
```

---

## 8. Error Handling

### Standard Error Response

All error responses follow this format:

```json
{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Invalid request parameters",
    "details": {
      "field": "email",
      "reason": "Invalid email format"
    }
  }
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|---|---|---|
| INVALID_REQUEST | 400 | Invalid request parameters |
| UNAUTHORIZED | 401 | Missing or invalid authentication |
| FORBIDDEN | 403 | Insufficient permissions |
| NOT_FOUND | 404 | Resource not found |
| CONFLICT | 409 | Resource already exists |
| RATE_LIMITED | 429 | Too many requests |
| INTERNAL_ERROR | 500 | Internal server error |

---

## 9. Rate Limiting

API requests are rate limited per user:

- **General endpoints:** 1000 requests per hour
- **Telemetry endpoints:** 100 requests per minute
- **Authentication endpoints:** 10 requests per minute

Rate limit information is included in response headers:

```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1647432000
```

---

## 10. Pagination

List endpoints support pagination with `limit` and `offset` parameters:

```
GET /heatmaps?limit=20&offset=0
```

Response includes pagination metadata:

```json
{
  "data": [...],
  "pagination": {
    "limit": 20,
    "offset": 0,
    "total_count": 150,
    "has_more": true
  }
}
```

---

## 11. Versioning

The API uses URL versioning. Current version is `v1`. Future versions will be available at `/api/v2`, etc.

---

## 12. WebSocket API

Real-time communication for live updates.

**Connection URL:** `wss://api.ghosthunter.app/ws`

**Authentication:** Include access token in connection URL: `wss://api.ghosthunter.app/ws?token=<access_token>`

### Channels

**Telemetry Updates:**
```json
{
  "type": "telemetry_update",
  "data": {
    "rssi": -65,
    "latency_ms": 45,
    "timestamp": "2026-03-16T12:00:00Z"
  }
}
```

**Leaderboard Updates:**
```json
{
  "type": "leaderboard_update",
  "data": {
    "user_id": "550e8400-e29b-41d4-a716-446655440000",
    "new_rank": 245,
    "new_score": 4750
  }
}
```

---

## Appendix: Example Integration

**JavaScript/TypeScript Example:**

```typescript
import axios from 'axios';

const API_BASE_URL = 'https://api.ghosthunter.app/api/v1';

class GhostHunterAPI {
  private accessToken: string | null = null;

  async login(email: string, password: string) {
    const response = await axios.post(`${API_BASE_URL}/auth/login`, {
      email,
      password,
      device_id: 'device-12345',
      device_type: 'ios'
    });
    this.accessToken = response.data.access_token;
    return response.data;
  }

  async submitTelemetry(sessionId: string, measurements: any[]) {
    return axios.post(
      `${API_BASE_URL}/telemetry/batch`,
      { session_id: sessionId, measurements },
      { headers: { Authorization: `Bearer ${this.accessToken}` } }
    );
  }

  async createHeatMap(name: string, bounds: any) {
    return axios.post(
      `${API_BASE_URL}/heatmaps`,
      { name, bounds },
      { headers: { Authorization: `Bearer ${this.accessToken}` } }
    );
  }
}
```
