# In-Memory APIs - Reckless Bank

A Spring Boot application implementing core banking operations with an in-memory repository. This project demonstrates clean architecture principles with domain-driven design for a simple banking system.

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [Running the Application](#running-the-application)
- [Docker Deployment](#docker-deployment)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Architecture](#architecture)
- [Contributing](#contributing)

## Overview

The Fast & Reckless Bank application provides core banking functionalities including:

- Account creation and management
- Deposit and withdrawal operations
- Money transfers between accounts
- Account balance inquiries
- Exception handling for various banking scenarios

## Tech Stack

- **Java 21** - Programming language
- **Spring Boot 3.5.7** - Application framework
- **Spring Web** - REST API development
- **Spring Actuator** - Application monitoring
- **Spring TX** - Transaction management
- **Maven** - Build tool and dependency management
- **JUnit 5** - Testing framework
- **Docker** - Containerization platform
- **Docker Compose** - Multi-container orchestration

## Project Structure

The project follows Clean Architecture principles with clear separation of concerns:

```text
src/
├── main/
│   ├── java/com/reckless_bank/in_memory_apis/
│   │   ├── InMemoryApisApplication.java              # Main application entry point
│   │   ├── account/                                  # Account bounded context
│   │   │   ├── application/                          # Application layer
│   │   │   │   ├── dto/                             # Data Transfer Objects
│   │   │   │   │   ├── CreateAccountRequest.java
│   │   │   │   │   ├── TransactionRequest.java
│   │   │   │   │   ├── TransferRequest.java
│   │   │   │   │   └── TransferResult.java
│   │   │   │   └── usecase/                         # Use cases (business logic)
│   │   │   │       ├── CreateAccountUseCase.java
│   │   │   │       ├── DepositUseCase.java
│   │   │   │       ├── GetAccountBalanceUseCase.java
│   │   │   │       ├── GetAccountUseCase.java
│   │   │   │       ├── GetAllAccountsUseCase.java
│   │   │   │       ├── TransferUseCase.java
│   │   │   │       └── WithdrawUseCase.java
│   │   │   ├── domain/                              # Domain layer
│   │   │   │   ├── model/
│   │   │   │   │   └── Account.java                 # Domain entity
│   │   │   │   └── repository/
│   │   │   │       ├── IAccountRepository.java     # Repository interface
│   │   │   │       └── AccountRepository.java      # In-memory implementation
│   │   │   └── infrastructure/                      # Infrastructure layer
│   │   │       └── rest/controller/
│   │   │           └── AccountController.java      # REST endpoints
│   │   └── common/                                  # Shared components
│   │       ├── dto/
│   │       │   └── ErrorResponse.java
│   │       └── exception/                           # Exception handling
│   │           ├── GlobalExceptionHandler.java
│   │           ├── AccountNotFoundException.java
│   │           ├── InsufficientFundsException.java
│   │           ├── InvalidTransactionException.java
│   │           └── RepositoryException.java
│   └── resources/
│       └── application.properties                   # Application configuration
└── test/                                           # Test files
    └── java/com/reckless_bank/in_memory_apis/
        ├── InMemoryApisApplicationTests.java
        ├── account/
        │   ├── application/usecase/
        │   │   ├── DepositUseCaseTest.java
        │   │   ├── TransferUseCaseTest.java
        │   │   └── WithdrawUseCaseTest.java
        │   └── domain/repository/
        │       ├── AccountRepositorySingletonTest.java
        │       └── IAccountRepositoryContractTest.java
        └── common/exception/
            └── GlobalExceptionHandlerTest.java
```

## Prerequisites

Before running this application, ensure you have the following installed:

- **Java 21** or higher
- **Maven 3.6+** (or use the included Maven wrapper)
- **Git** (for version control)
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code recommended)
- **Docker** (for containerized deployment - optional)
- **Docker Compose** (for orchestrated deployment - optional)

### Verify Java Installation

```bash
java -version
```

Should output Java 21 or higher.

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd in-memory-apis
```

### 2. Build the Project

Using Maven wrapper (recommended):

```bash
# Windows
.\mvnw clean compile

# Linux/macOS
./mvnw clean compile
```

Or using system Maven:

```bash
mvn clean compile
```

### 3. Run Tests

```bash
# Windows
.\mvnw test

# Linux/macOS
./mvnw test
```

### 4. Package the Application

```bash
# Windows
.\mvnw clean package

# Linux/macOS
./mvnw clean package
```

## Running the Application

### Development Mode

```bash
# Windows
.\mvnw spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

### Production Mode

```bash
# Build the JAR
.\mvnw clean package

# Run the JAR
java -jar target/in-memory-apis-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### Health Check

Once running, verify the application is healthy:

```bash
curl http://localhost:8080/actuator/health
```

## Docker Deployment

The application is fully containerized with Docker support for easy deployment across different environments.

### Docker Prerequisites

- **Docker** 20.10+ installed and running
- **Docker Compose** 2.0+ (optional, for orchestrated deployment)

### Docker Files Structure

The project includes the following Docker-related files:

```text
├── Dockerfile                    # Multi-stage build configuration
├── docker-compose.yml           # Orchestration configuration  
├── .dockerignore                # Files excluded from build context
├── docker-build.ps1             # Windows PowerShell build script
├── docker-build.sh              # Unix/Linux build script
└── src/main/resources/
    └── application-docker.properties  # Docker-specific configuration
```

### Quick Start with Docker

#### Option 1: Using Build Scripts (Recommended)

**For Windows (PowerShell):**
```powershell
# Set execution policy if needed
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Build and run
.\docker-build.ps1
```

**For Linux/macOS (Bash):**
```bash
# Make script executable
chmod +x docker-build.sh

# Build and run
./docker-build.sh
```

#### Option 2: Using Docker Compose
```bash
# Build and start services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

#### Option 3: Manual Docker Commands
```bash
# Build the image
docker build -t reckless-bank/in-memory-apis:latest .

# Run the container
docker run -d \
  --name reckless-bank-api \
  -p 8080:8080 \
  --restart unless-stopped \
  reckless-bank/in-memory-apis:latest
```

### Docker Configuration Details

#### Multi-Stage Dockerfile Features

- **Build Stage**: Uses `eclipse-temurin:21-jdk-alpine` for compilation
- **Runtime Stage**: Uses `eclipse-temurin:21-jre-alpine` for smaller image size
- **Security**: Runs as non-root user (`appuser:appgroup`)
- **Health Checks**: Built-in health monitoring using Actuator endpoints
- **Optimizations**: Layered builds for better caching and faster rebuilds

#### Docker Environment Configuration

The application uses a Docker-specific profile (`application-docker.properties`) with:

```properties
# Enhanced Actuator endpoints for containerized monitoring
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
management.prometheus.metrics.export.enabled=true

# Container-optimized logging
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# Graceful shutdown for container environments
server.shutdown=graceful
spring.lifecycle.timeout-per-shutdown-phase=20s
```

### Docker Operations

#### Container Management
```bash
# View running containers
docker ps

# View container logs
docker logs -f reckless-bank-api

# Stop container
docker stop reckless-bank-api

# Remove container
docker rm reckless-bank-api

# Remove image
docker rmi reckless-bank/in-memory-apis:latest
```

#### Health Monitoring in Docker
```bash
# Check health status
docker inspect --format='{{.State.Health.Status}}' reckless-bank-api

# View health check logs
docker inspect --format='{{range .State.Health.Log}}{{.Output}}{{end}}' reckless-bank-api

# Access health endpoint
curl http://localhost:8080/actuator/health
```

### Docker Production Considerations

#### Environment Variables
```bash
# Run with custom environment
docker run -d \
  --name reckless-bank-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e JAVA_OPTS="-Xms512m -Xmx1024m" \
  reckless-bank/in-memory-apis:latest
```

#### Volume Mounts for Logs
```bash
# Mount logs directory
docker run -d \
  --name reckless-bank-api \
  -p 8080:8080 \
  -v /host/logs:/app/logs \
  reckless-bank/in-memory-apis:latest
```

#### Registry Deployment
```bash
# Tag for registry
docker tag reckless-bank/in-memory-apis:latest your-registry.com/reckless-bank/in-memory-apis:1.0.0

# Push to registry
docker push your-registry.com/reckless-bank/in-memory-apis:1.0.0
```

### Docker Troubleshooting

#### Common Docker Issues

1. **Port already in use**:
   ```bash
   # Use different port mapping
   docker run -d -p 8081:8080 reckless-bank/in-memory-apis:latest
   ```

2. **Container not starting**:
   ```bash
   # Check logs for errors
   docker logs reckless-bank-api
   
   # Run interactively for debugging
   docker run -it --rm reckless-bank/in-memory-apis:latest /bin/sh
   ```

3. **Build context too large**:
   ```bash
   # Clean Maven target directory first
   .\mvnw clean
   docker build -t reckless-bank/in-memory-apis:latest .
   ```

4. **Health check failing**:
   ```bash
   # Check if application is responding
   docker exec reckless-bank-api curl -f http://localhost:8080/actuator/health
   ```

### Docker Best Practices Implemented

- ✅ **Multi-stage builds** for smaller production images
- ✅ **Non-root user** for security
- ✅ **Health checks** for monitoring
- ✅ **Graceful shutdown** handling
- ✅ **Layered caching** for faster builds
- ✅ **Minimal base images** (Alpine Linux)
- ✅ **Build scripts** for different platforms
- ✅ **Environment-specific configuration**

## API Endpoints

The application exposes the following REST endpoints under `/api/accounts`:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/accounts` | Get all accounts |
| GET | `/api/accounts/{accountId}` | Get specific account |
| GET | `/api/accounts/{accountId}/balance` | Get account balance |
| POST | `/api/accounts` | Create new account |
| POST | `/api/accounts/{accountId}/deposit` | Deposit money |
| POST | `/api/accounts/{accountId}/withdraw` | Withdraw money |
| POST | `/api/accounts/transfer` | Transfer money between accounts |

### Example API Calls

#### Create Account

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountId": "ACC001", "accountHolder": "John Doe"}'
```

#### Deposit Money

```bash
curl -X POST http://localhost:8080/api/accounts/ACC001/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00}'
```

#### Transfer Money

```bash
curl -X POST http://localhost:8080/api/accounts/transfer \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId": "ACC001", "toAccountId": "ACC002", "amount": 50.00}'
```

## Testing

### Run All Tests

```bash
.\mvnw test
```

### Run Specific Test Class

```bash
.\mvnw test -Dtest=DepositUseCaseTest
```

### Run Tests with Coverage

```bash
.\mvnw clean test jacoco:report
```

### Test Categories

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **Contract Tests**: Test repository contract compliance

## Architecture

### Clean Architecture Layers

1. **Domain Layer** (`domain/`): Contains business entities and repository interfaces
2. **Application Layer** (`application/`): Contains use cases and DTOs
3. **Infrastructure Layer** (`infrastructure/`): Contains external concerns like REST controllers
4. **Common Layer** (`common/`): Contains shared utilities and exceptions

### Key Design Patterns

- **Repository Pattern**: Abstract data access
- **Use Case Pattern**: Encapsulate business logic
- **Dependency Injection**: Loose coupling between components
- **Exception Handling**: Centralized error management
- **Immutable Entities**: Using Java records for domain models

### In-Memory Storage

The application uses an in-memory repository implementation with thread-safe operations using:

- `ConcurrentHashMap` for data storage
- `ReentrantReadWriteLock` for read/write synchronization
- Singleton pattern for repository instance

## Development Guidelines

### Code Style

- Follow Java naming conventions
- Use immutable objects where possible
- Implement proper exception handling
- Write comprehensive tests

### Adding New Features

1. Start with domain models
2. Define repository interfaces
3. Implement use cases
4. Add REST endpoints
5. Write tests for all layers

## Monitoring

The application includes Spring Actuator for monitoring:

- Health: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`
- Metrics: `http://localhost:8080/actuator/metrics`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Ensure all tests pass
6. Submit a pull request

## Troubleshooting

### Common Issues

1. **Port 8080 already in use**:

   ```bash
   # Change port in application.properties
   server.port=8081
   ```

2. **Java version mismatch**:
   - Ensure Java 21 is installed and JAVA_HOME is set correctly

3. **Maven wrapper not executable**:

   ```bash
   # Make executable (Linux/macOS)
   chmod +x mvnw
   ```

### Logs

Application logs are available in the console output. For file logging, add to `application.properties`:

```properties
logging.file.name=logs/application.log
logging.level.com.reckless_bank=DEBUG
```

## License

This project is part of the Reckless Bank implementation exercise.