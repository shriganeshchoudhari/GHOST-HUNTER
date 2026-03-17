#!/bin/bash

# GHOST-HUNTER Deployment Script
# Usage: ./deploy.sh [staging|production]

set -e

ENVIRONMENT=${1:-staging}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Validate environment
if [[ "$ENVIRONMENT" != "staging" && "$ENVIRONMENT" != "production" ]]; then
    log_error "Invalid environment: $ENVIRONMENT"
    echo "Usage: ./deploy.sh [staging|production]"
    exit 1
fi

log_info "Starting deployment to $ENVIRONMENT environment..."

# Load environment variables
if [ -f "$PROJECT_DIR/.env.$ENVIRONMENT" ]; then
    log_info "Loading environment variables from .env.$ENVIRONMENT"
    export $(cat "$PROJECT_DIR/.env.$ENVIRONMENT" | grep -v '^#' | xargs)
else
    log_warn "Environment file .env.$ENVIRONMENT not found"
fi

# Backend deployment
log_info "Building backend Docker image..."
cd "$PROJECT_DIR/backend"
mvn clean package -DskipTests -q

log_info "Building Docker image..."
docker build -f "$PROJECT_DIR/devops/Dockerfile.backend" \
    -t "ghost-hunter-backend:$ENVIRONMENT" \
    -t "ghost-hunter-backend:latest" \
    "$PROJECT_DIR"

# Push to registry if configured
if [ -n "$DOCKER_REGISTRY" ]; then
    log_info "Pushing Docker image to registry..."
    docker tag "ghost-hunter-backend:$ENVIRONMENT" "$DOCKER_REGISTRY/ghost-hunter-backend:$ENVIRONMENT"
    docker push "$DOCKER_REGISTRY/ghost-hunter-backend:$ENVIRONMENT"
fi

# Database migrations
log_info "Running database migrations..."
cd "$PROJECT_DIR/backend"
mvn flyway:migrate \
    -Dflyway.url="$DB_URL" \
    -Dflyway.user="$DB_USER" \
    -Dflyway.password="$DB_PASSWORD"

# Deploy with Docker Compose
if [ "$ENVIRONMENT" = "staging" ]; then
    log_info "Deploying to staging with docker-compose..."
    docker-compose -f "$PROJECT_DIR/devops/docker-compose.yml" \
        -p "ghost-hunter-staging" \
        up -d
elif [ "$ENVIRONMENT" = "production" ]; then
    log_info "Deploying to production with Kubernetes..."
    kubectl apply -f "$PROJECT_DIR/devops/kubernetes/" \
        --namespace=ghost-hunter-prod
    
    # Wait for rollout
    kubectl rollout status deployment/ghost-hunter-backend \
        --namespace=ghost-hunter-prod \
        --timeout=5m
fi

# Health checks
log_info "Running health checks..."
sleep 10

HEALTH_CHECK_URL="${API_BASE_URL}/actuator/health"
RETRIES=5
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $RETRIES ]; do
    if curl -f "$HEALTH_CHECK_URL" > /dev/null 2>&1; then
        log_info "Health check passed!"
        break
    fi
    
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -lt $RETRIES ]; then
        log_warn "Health check failed, retrying... ($RETRY_COUNT/$RETRIES)"
        sleep 5
    fi
done

if [ $RETRY_COUNT -eq $RETRIES ]; then
    log_error "Health check failed after $RETRIES attempts"
    exit 1
fi

# Smoke tests
log_info "Running smoke tests..."
cd "$PROJECT_DIR/tests"
npm install > /dev/null 2>&1
npm run test:smoke || log_warn "Smoke tests failed, but deployment continues"

log_info "Deployment to $ENVIRONMENT completed successfully!"
