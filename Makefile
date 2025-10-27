.PHONY: help build run test clean docker-build docker-up docker-down

help: ## Show this help message
	@echo "Camera Cloud - Available commands:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build the application
	./mvnw clean package -DskipTests

run: ## Run the application locally
	./mvnw spring-boot:run

run-dev: ## Run the application in development mode (with H2 database)
	./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

test: ## Run tests
	./mvnw test

clean: ## Clean build artifacts
	./mvnw clean

docker-build: ## Build Docker image
	docker build -t camera-cloud:latest .

docker-up: ## Start services with Docker Compose
	docker-compose up -d

docker-down: ## Stop services with Docker Compose
	docker-compose down

docker-logs: ## Show logs from Docker Compose services
	docker-compose logs -f

setup: ## Initial setup - create directories and download dependencies
	./mvnw dependency:go-offline

# Development helpers
redis-cli: ## Connect to Redis CLI
	docker exec -it camera-cloud-redis redis-cli

postgres-cli: ## Connect to PostgreSQL CLI
	docker exec -it camera-cloud-postgres psql -U camera_user -d camera_cloud

# Testing endpoints
test-view: ## Test camera view endpoint
	curl -v "http://localhost:8080/view?camera_id=CAM_001"

test-admin: ## Test admin endpoint (requires authentication)
	curl -v -u admin:admin123 "http://localhost:8080/api/v1/admin/platforms"

# Health checks
health: ## Check application health
	curl -s "http://localhost:8080/actuator/health" | jq .