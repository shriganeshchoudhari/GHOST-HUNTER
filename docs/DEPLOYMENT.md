# GHOST-HUNTER: Deployment Guide

**Version:** 1.0  
**Date:** March 16, 2026  
**Author:** Manus AI  
**Status:** Draft

---

## Overview

This guide provides comprehensive instructions for deploying the GHOST-HUNTER platform to staging and production environments. The deployment process includes backend services, mobile applications, database migrations, and infrastructure setup.

---

## 1. Prerequisites

### 1.1 Required Tools

The deployment process requires the following tools to be installed and configured:

- **Docker:** Version 20.10 or later for containerization
- **Docker Compose:** Version 1.29 or later for local development
- **Kubernetes:** Version 1.24 or later for production orchestration (optional)
- **kubectl:** Command-line tool for Kubernetes management
- **Git:** Version 2.30 or later for version control
- **Node.js:** Version 18 or later for mobile app builds
- **Java:** Version 17 or later for backend builds
- **Maven:** Version 3.8 or later for Java dependency management

### 1.2 Infrastructure Requirements

**Staging Environment:**
- Minimum 2 CPU cores
- 4 GB RAM
- 20 GB storage
- Single-region deployment

**Production Environment:**
- Minimum 8 CPU cores
- 16 GB RAM
- 100 GB storage
- Multi-region deployment with failover

### 1.3 Cloud Provider Setup

The deployment supports multiple cloud providers:

- **AWS:** EC2, RDS, ElastiCache, S3, CloudFront
- **Google Cloud:** Compute Engine, Cloud SQL, Memorystore, Cloud Storage
- **Azure:** Virtual Machines, Azure Database, Azure Cache, Blob Storage
- **DigitalOcean:** Droplets, Managed Databases, Spaces

---

## 2. Local Development Deployment

### 2.1 Setup Local Environment

Start by cloning the repository and setting up the development environment:

```bash
# Clone repository
git clone https://github.com/your-org/ghost-hunter.git
cd GHOST-HUNTER

# Copy environment file
cp .env.example .env

# Update .env with local values
# DB_HOST=localhost
# REDIS_HOST=localhost
# API_BASE_URL=http://localhost:8080
```

### 2.2 Start Development Services

Use Docker Compose to start all required services:

```bash
# Start all services
docker-compose -f devops/docker-compose.yml up -d

# Verify services are running
docker-compose -f devops/docker-compose.yml ps

# View logs
docker-compose -f devops/docker-compose.yml logs -f
```

### 2.3 Access Development Services

After starting services, access them at:

| Service | URL | Credentials |
|---|---|---|
| Backend API | http://localhost:8080/api/v1 | - |
| Nginx | http://localhost | - |
| PostgreSQL | localhost:5432 | postgres/postgres |
| Redis | localhost:6379 | redis_password |
| RabbitMQ | http://localhost:15672 | guest/guest |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin/admin |
| Kibana | http://localhost:5601 | - |

---

## 3. Staging Environment Deployment

### 3.1 Prepare Staging Environment

Configure staging environment variables:

```bash
# Create staging configuration
cat > .env.staging << EOF
ENVIRONMENT=staging
DB_HOST=staging-postgres.example.com
DB_PORT=5432
DB_NAME=ghost_hunter_staging
DB_USER=postgres
DB_PASSWORD=$(openssl rand -base64 32)

REDIS_HOST=staging-redis.example.com
REDIS_PORT=6379
REDIS_PASSWORD=$(openssl rand -base64 32)

JWT_SECRET=$(openssl rand -base64 32)
API_BASE_URL=https://staging-api.ghosthunter.app

CORS_ALLOWED_ORIGINS=https://staging.ghosthunter.app,https://staging-web.ghosthunter.app

LOG_LEVEL=INFO
EOF

# Save credentials securely
# Store in secrets manager (AWS Secrets Manager, HashiCorp Vault, etc.)
```

### 3.2 Build and Push Docker Images

Build Docker images and push to container registry:

```bash
# Set registry URL
REGISTRY=your-registry.azurecr.io
IMAGE_NAME=ghost-hunter-backend
TAG=staging-$(date +%Y%m%d-%H%M%S)

# Build backend image
docker build -f devops/Dockerfile.backend \
    -t $REGISTRY/$IMAGE_NAME:$TAG \
    -t $REGISTRY/$IMAGE_NAME:staging-latest \
    .

# Push to registry
docker push $REGISTRY/$IMAGE_NAME:$TAG
docker push $REGISTRY/$IMAGE_NAME:staging-latest
```

### 3.3 Deploy to Staging

Deploy using Docker Compose on staging server:

```bash
# SSH into staging server
ssh ubuntu@staging-server.example.com

# Clone repository
git clone https://github.com/your-org/ghost-hunter.git
cd GHOST-HUNTER

# Load environment variables
export $(cat .env.staging | xargs)

# Pull latest images
docker-compose -f devops/docker-compose.yml pull

# Start services
docker-compose -f devops/docker-compose.yml up -d

# Run database migrations
docker-compose exec backend mvn flyway:migrate

# Verify deployment
curl -f https://staging-api.ghosthunter.app/api/v1/actuator/health
```

### 3.4 Staging Verification

Verify staging deployment is working correctly:

```bash
# Check service health
curl https://staging-api.ghosthunter.app/api/v1/actuator/health

# Check metrics
curl https://staging-api.ghosthunter.app/api/v1/actuator/prometheus

# Run smoke tests
cd tests
npm install
npm run test:smoke

# Monitor logs
docker-compose logs -f backend
```

---

## 4. Production Environment Deployment

### 4.1 Production Infrastructure Setup

Set up production infrastructure using Infrastructure as Code:

```bash
# Initialize Terraform
cd devops/terraform
terraform init

# Plan infrastructure
terraform plan -var-file=production.tfvars

# Apply infrastructure
terraform apply -var-file=production.tfvars

# Output infrastructure details
terraform output
```

### 4.2 Kubernetes Deployment

Deploy to Kubernetes cluster:

```bash
# Create namespace
kubectl create namespace ghost-hunter-prod

# Create secrets
kubectl create secret generic ghost-hunter-secrets \
    --from-literal=DB_PASSWORD=$(openssl rand -base64 32) \
    --from-literal=REDIS_PASSWORD=$(openssl rand -base64 32) \
    --from-literal=JWT_SECRET=$(openssl rand -base64 32) \
    -n ghost-hunter-prod

# Apply Kubernetes manifests
kubectl apply -f devops/kubernetes/ -n ghost-hunter-prod

# Verify deployment
kubectl get pods -n ghost-hunter-prod
kubectl get services -n ghost-hunter-prod

# Wait for rollout
kubectl rollout status deployment/ghost-hunter-backend -n ghost-hunter-prod
```

### 4.3 Database Setup

Initialize production database:

```bash
# Connect to production database
psql -h prod-postgres.example.com -U postgres -d ghost_hunter

# Run migrations
flyway -url=jdbc:postgresql://prod-postgres.example.com/ghost_hunter \
       -user=postgres \
       -password=$DB_PASSWORD \
       -locations=classpath:db/migration \
       migrate

# Verify schema
\dt
```

### 4.4 SSL/TLS Configuration

Configure SSL certificates for HTTPS:

```bash
# Generate self-signed certificate (for testing)
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365

# Or use Let's Encrypt with Certbot
certbot certonly --standalone -d ghosthunter.app -d www.ghosthunter.app

# Copy certificates to Nginx
sudo cp /etc/letsencrypt/live/ghosthunter.app/fullchain.pem devops/ssl/cert.pem
sudo cp /etc/letsencrypt/live/ghosthunter.app/privkey.pem devops/ssl/key.pem

# Update Nginx configuration
sudo systemctl reload nginx
```

### 4.5 Production Verification

Verify production deployment:

```bash
# Health check
curl -f https://api.ghosthunter.app/api/v1/actuator/health

# Performance check
ab -n 1000 -c 10 https://api.ghosthunter.app/api/v1/actuator/health

# SSL certificate check
curl -I https://api.ghosthunter.app

# DNS verification
dig api.ghosthunter.app
```

---

## 5. Mobile App Deployment

### 5.1 iOS Deployment

Build and submit iOS app to App Store:

```bash
# Install EAS CLI
npm install -g eas-cli

# Configure EAS
eas build:configure

# Build for iOS
cd mobile
eas build --platform ios --auto-submit

# Or build locally
eas build --platform ios --local

# Submit to App Store
eas submit --platform ios
```

### 5.2 Android Deployment

Build and submit Android app to Google Play:

```bash
# Build for Android
cd mobile
eas build --platform android --auto-submit

# Or build locally
eas build --platform android --local

# Submit to Google Play
eas submit --platform android
```

### 5.3 App Configuration

Update app configuration for production:

```json
{
  "expo": {
    "extra": {
      "apiBaseUrl": "https://api.ghosthunter.app/api/v1",
      "environment": "production"
    }
  }
}
```

---

## 6. Monitoring and Observability

### 6.1 Prometheus Setup

Configure Prometheus for metrics collection:

```bash
# Access Prometheus
curl http://localhost:9090

# Query metrics
curl 'http://localhost:9090/api/v1/query?query=http_requests_total'

# Create alert rules
kubectl apply -f devops/kubernetes/prometheus-rules.yaml
```

### 6.2 Grafana Dashboards

Import Grafana dashboards:

```bash
# Access Grafana
# http://localhost:3000 (admin/admin)

# Import dashboard
# - Go to Dashboards → Import
# - Enter dashboard ID or JSON
# - Select Prometheus data source
# - Save dashboard
```

### 6.3 Centralized Logging

Configure ELK Stack for logging:

```bash
# Verify Elasticsearch
curl http://localhost:9200/_cluster/health

# Create Kibana index pattern
# - Go to Stack Management → Index Patterns
# - Create index pattern: logstash-*
# - Set @timestamp as time field

# View logs
# - Go to Discover
# - Select logstash-* index pattern
# - Search and filter logs
```

---

## 7. Backup and Disaster Recovery

### 7.1 Database Backups

Configure automated database backups:

```bash
# Create backup script
cat > scripts/backup-db.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/backups/postgres"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/ghost_hunter_$TIMESTAMP.sql.gz"

pg_dump -h $DB_HOST -U $DB_USER $DB_NAME | gzip > $BACKUP_FILE

# Upload to S3
aws s3 cp $BACKUP_FILE s3://ghost-hunter-backups/

# Keep only last 30 days
find $BACKUP_DIR -name "ghost_hunter_*.sql.gz" -mtime +30 -delete
EOF

# Schedule daily backups
0 2 * * * /home/ubuntu/GHOST-HUNTER/scripts/backup-db.sh
```

### 7.2 Disaster Recovery Testing

Test disaster recovery procedures:

```bash
# Restore from backup
gunzip < ghost_hunter_20260316_020000.sql.gz | psql -h localhost -U postgres -d ghost_hunter

# Verify data integrity
psql -h localhost -U postgres -d ghost_hunter << EOF
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM wifi_telemetry;
SELECT COUNT(*) FROM heat_maps;
EOF
```

---

## 8. Scaling and Performance Tuning

### 8.1 Horizontal Scaling

Scale backend services horizontally:

```bash
# Scale Kubernetes deployment
kubectl scale deployment ghost-hunter-backend --replicas=5 -n ghost-hunter-prod

# Verify scaling
kubectl get pods -n ghost-hunter-prod

# Monitor load
kubectl top nodes
kubectl top pods -n ghost-hunter-prod
```

### 8.2 Database Optimization

Optimize database performance:

```bash
# Analyze query performance
EXPLAIN ANALYZE SELECT * FROM wifi_telemetry WHERE user_id = '...';

# Create indexes
CREATE INDEX idx_telemetry_user_timestamp ON wifi_telemetry(user_id, timestamp DESC);

# Vacuum and analyze
VACUUM ANALYZE;
```

### 8.3 Caching Strategy

Optimize caching with Redis:

```bash
# Monitor Redis memory
redis-cli INFO memory

# Configure eviction policy
redis-cli CONFIG SET maxmemory-policy allkeys-lru

# Monitor cache hit rate
redis-cli INFO stats
```

---

## 9. Rollback Procedures

### 9.1 Kubernetes Rollback

Rollback to previous deployment:

```bash
# View rollout history
kubectl rollout history deployment/ghost-hunter-backend -n ghost-hunter-prod

# Rollback to previous version
kubectl rollout undo deployment/ghost-hunter-backend -n ghost-hunter-prod

# Rollback to specific revision
kubectl rollout undo deployment/ghost-hunter-backend --to-revision=2 -n ghost-hunter-prod
```

### 9.2 Database Rollback

Rollback database migrations:

```bash
# List migration history
flyway info

# Rollback to previous version
flyway undo

# Or restore from backup
gunzip < ghost_hunter_backup.sql.gz | psql -h localhost -U postgres -d ghost_hunter
```

---

## 10. Troubleshooting

### 10.1 Common Issues

| Issue | Solution |
|---|---|
| Pod not starting | Check logs: `kubectl logs <pod-name> -n ghost-hunter-prod` |
| Database connection error | Verify credentials and network connectivity |
| High memory usage | Scale horizontally or optimize queries |
| API timeout | Check backend logs and increase timeout values |
| Certificate error | Renew SSL certificate with Certbot |

### 10.2 Debug Commands

```bash
# Check pod status
kubectl describe pod <pod-name> -n ghost-hunter-prod

# View pod logs
kubectl logs <pod-name> -n ghost-hunter-prod --tail=100

# Execute command in pod
kubectl exec -it <pod-name> -n ghost-hunter-prod -- /bin/bash

# Port forward for debugging
kubectl port-forward <pod-name> 8080:8080 -n ghost-hunter-prod

# Check resource usage
kubectl top pods -n ghost-hunter-prod
```

---

## 11. Security Hardening

### 11.1 Network Security

Configure network policies:

```bash
# Apply network policies
kubectl apply -f devops/kubernetes/network-policies.yaml

# Verify policies
kubectl get networkpolicies -n ghost-hunter-prod
```

### 11.2 RBAC Configuration

Configure Role-Based Access Control:

```bash
# Create service account
kubectl create serviceaccount ghost-hunter-app -n ghost-hunter-prod

# Create role
kubectl create role ghost-hunter-role \
    --verb=get,list,watch \
    --resource=pods \
    -n ghost-hunter-prod

# Bind role to service account
kubectl create rolebinding ghost-hunter-binding \
    --role=ghost-hunter-role \
    --serviceaccount=ghost-hunter-prod:ghost-hunter-app \
    -n ghost-hunter-prod
```

---

## Appendix: Deployment Checklist

- [ ] Environment variables configured
- [ ] Database migrations completed
- [ ] SSL certificates installed
- [ ] Backups configured
- [ ] Monitoring setup verified
- [ ] Health checks passing
- [ ] Performance tests completed
- [ ] Security scan passed
- [ ] Disaster recovery tested
- [ ] Team notified of deployment
