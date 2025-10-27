# Camera Cloud - AI Picture Book Platform

A Spring Boot application providing camera view endpoints with rate limiting and an admin portal for platform management.

## Features

### Camera View API
- **Public endpoint**: `GET /view?camera_id=<id>` - renders HTML page for camera stream
- **Rate limiting**: First request per camera_id per 60 seconds allowed, subsequent requests return HTTP 429
- **Input validation**: Camera ID must match regex `^[A-Za-z0-9_-]{3,128}$`
- **Security headers**: Anti-caching and anti-embedding headers included

### Admin Portal
- **Platform management**: Create, update, test streaming platforms
- **Camera management**: Assign cameras to platforms, enable/disable redirects
- **Bulk import**: Excel/CSV import for camera data
- **Role-based access**: MAIN_ADMIN and PLATFORM_ADMIN roles

## Tech Stack

- **Runtime**: Java 21, Spring Boot 3.2
- **Database**: PostgreSQL 16
- **Cache**: Redis 7 (for rate limiting)
- **Build**: Maven
- **Container**: Docker, Docker Compose

## Quick Start

### Prerequisites
- Java 21
- Maven 3.9+
- Docker & Docker Compose (optional)

### Local Development

1. **Start dependencies**:
   ```bash
   docker-compose up -d postgres redis
   ```

2. **Run the application**:
   ```bash
   make run
   # or
   ./mvnw spring-boot:run
   ```

3. **Test the camera view**:
   ```bash
   curl "http://localhost:8080/view?camera_id=CAM_001"
   ```

### Docker Deployment

1. **Start all services**:
   ```bash
   make docker-up
   # or
   docker-compose up -d
   ```

2. **Check health**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

## API Endpoints

### Public Endpoints

- `GET /view?camera_id=<id>` - Camera view page (rate limited)
- `GET /actuator/health` - Health check

### Admin Endpoints (Authentication Required)

#### Platform Management
- `GET /api/v1/admin/platforms` - List platforms
- `POST /api/v1/admin/platforms` - Create platform (MAIN_ADMIN only)
- `PATCH /api/v1/admin/platforms/{code}` - Update platform (MAIN_ADMIN only)
- `POST /api/v1/admin/platforms/{code}/test` - Test platform connectivity

#### Camera Management
- `GET /api/v1/admin/cameras` - List cameras with filtering
- `POST /api/v1/admin/cameras/{id}/assign-platform` - Assign camera to platform
- `PATCH /api/v1/admin/cameras/{id}/redirect` - Enable/disable camera redirect
- `POST /api/v1/admin/cameras/import` - Import cameras from Excel/CSV (MAIN_ADMIN only)
- `GET /api/v1/admin/cameras/import/{jobId}` - Get import job status

## Authentication & Account Management

### 🔐 Web Login Page
Access the admin portal through the modern login page:
- **URL**: `http://localhost:8080/login`
- **Design**: Beautiful UI inspired by [Insight Software](https://www.insight-software.com/)
- **Features**: 
  - Modern gradient design
  - Responsive layout
  - Remember me functionality
  - Error message handling

### 📊 Dashboard
After successful login, users are directed to the dashboard:
- **URL**: `http://localhost:8080/dashboard`
- **Features**:
  - System statistics overview
  - Quick action buttons
  - User information display
  - Navigation menu

### ⚙️ Account Settings
Manage account information and password:
- **URL**: `http://localhost:8080/account/settings`
- **Features**:
  - View account information
  - Change password
  - Security recommendations
  - Role and permission display

### Default Credentials
- **Main Admin**: `admin` / `admin123`
  - Full access to all features
  - Can manage platforms and cameras
  - Can import bulk data
  
- **Platform Admin**: `platform_admin` / `platform123`
  - Limited access to authorized platforms
  - Can manage assigned cameras
  - Cannot create/modify platforms

### API Authentication
Admin APIs also support HTTP Basic Authentication for direct API access.

For detailed information, see [ACCOUNT_MANAGEMENT.md](ACCOUNT_MANAGEMENT.md)

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | `camera_user` | Database username |
| `DB_PASSWORD` | `camera_pass` | Database password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `ADMIN_PASSWORD` | `admin123` | Admin password |
| `APP_RATE_WINDOW_SECONDS` | `60` | Rate limit window |

### Profiles

- **default**: Production mode with PostgreSQL and Redis
- **dev**: Development mode with H2 in-memory database

## Database Schema

The application uses the following main entities:

- **platforms**: Streaming platforms (YouTube, Twitch, etc.)
- **cameras**: Camera devices with platform assignments
- **users**: Admin users with role-based access
- **import_jobs**: Background import job tracking
- **audit_logs**: Security and operation audit trail

## Rate Limiting

The camera view endpoint implements a rolling 60-second rate limit per camera_id:

- **Redis-based**: Production-ready, multi-instance safe
- **In-memory fallback**: Development mode only
- **Atomic operations**: Uses Redis `SET NX EX` for race condition safety

## Development

### Available Commands

```bash
make help          # Show all available commands
make build         # Build the application
make run           # Run locally
make run-dev       # Run with H2 database
make test          # Run tests
make docker-up     # Start all services
make docker-down   # Stop all services
```

### Testing

```bash
# Test camera view (first request should succeed)
curl "http://localhost:8080/view?camera_id=TEST_001"

# Test rate limiting (second request within 60s should fail)
curl "http://localhost:8080/view?camera_id=TEST_001"

# Test admin API
curl -u admin:admin123 "http://localhost:8080/api/v1/admin/platforms"
```

## Monitoring

- **Health checks**: `/actuator/health`
- **Metrics**: `/actuator/prometheus`
- **Application info**: `/actuator/info`

## Security Features

- **Rate limiting**: Prevents abuse of camera view endpoints
- **Input validation**: Strict camera ID format validation
- **Security headers**: Anti-caching, anti-framing headers
- **Role-based access**: Granular admin permissions
- **Audit logging**: All admin operations logged

## Performance

- **Target response time**: < 50ms p50 for camera view endpoint
- **Rate limit window**: 60 seconds per camera_id
- **Concurrent safety**: Redis-based atomic operations
- **Caching**: Redis for rate limit state

## Troubleshooting

### Common Issues

1. **Database connection failed**:
   ```bash
   docker-compose up -d postgres
   ```

2. **Redis connection failed**:
   ```bash
   docker-compose up -d redis
   ```

3. **Rate limit not working**:
   - Check Redis connection
   - Verify Redis keys: `redis-cli keys "rate:view:*"`

4. **Admin API returns 401**:
   - Check authentication credentials
   - Verify user roles in database

### Logs

```bash
# Application logs
docker-compose logs -f app

# Database logs
docker-compose logs -f postgres

# Redis logs
docker-compose logs -f redis
```