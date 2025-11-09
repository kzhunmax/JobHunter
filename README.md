# JobHunter 🎯

A comprehensive job search and application management platform built with Spring Boot, featuring OAuth2 authentication, Elasticsearch-powered search, and real-time notifications.

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Environment Configuration](#environment-configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Architecture](#architecture)
- [Database Migrations](#database-migrations)
- [Testing](#testing)
- [Contributing](#contributing)

## ✨ Features

- **User Management**
  - OAuth2 authentication (Google & GitHub)
  - Email verification
  - User profiles and resumes
  - Role-based access control (RBAC)

- **Job Management**
  - Post and manage job listings
  - Advanced search with Elasticsearch
  - Company profiles
  - Application deadlines

- **Application Tracking**
  - Submit applications with cover letters and resumes
  - Track application status
  - CV/Resume storage (AWS S3/Supabase)

- **Payment Integration**
  - Stripe payment processing
  - API key management
  - Subscription plans

- **Communication**
  - Email notifications via Kafka
  - Real-time updates

- **Rate Limiting**
  - API rate limiting with Bucket4j
  - Redis-backed rate limit storage

## 🛠 Tech Stack

### Backend
- **Java 25** with Spring Boot 3.5.7
- **Spring Security** with OAuth2 Client
- **Spring Data JPA** with Hibernate
- **PostgreSQL** (v18) - Primary database
- **Redis** (v8.2) - Caching and rate limiting
- **Elasticsearch** (v8.19) - Full-text search
- **Apache Kafka** (v4.0) - Event streaming and email notifications

### Additional Technologies
- **Flyway** - Database migration management
- **MapStruct** - Object mapping
- **Lombok** - Code generation
- **JWT** (JJWT) - Token-based authentication
- **Stripe API** - Payment processing
- **AWS SDK** - S3 storage integration
- **SpringDoc OpenAPI** - API documentation
- **Testcontainers** - Integration testing

### DevOps
- **Docker** & **Docker Compose** - Containerization
- **Gradle** - Build automation

## 📦 Prerequisites

- **Java 25** or higher
- **Docker** and **Docker Compose**
- **Gradle** (or use the included wrapper)
- **Git**

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/kzhunmax/JobHunter.git
cd JobHunter
```

### 2. Configure Environment Variables

Create a `.env` file in the project root directory:

```bash
cp .env.example .env
```

Edit the `.env` file with your configuration (see [Environment Configuration](#environment-configuration)).

### 3. Build the Application

#### Using Gradle Wrapper (Recommended)

**Windows:**
```bash
cd backend
gradlew.bat clean build
```

**Linux/Mac:**
```bash
cd backend
./gradlew clean build
```

### 4. Run with Docker Compose

```bash
docker-compose up -d
```

The application will be available at `http://localhost:8080`

## 🔧 Environment Configuration

Create a `.env` file with the following variables:

```env
# Database Configuration
DB_NAME=jobhunter
DB_USER=postgres
DB_PASSWORD=your_secure_password

# JWT Configuration
SECRET_KEY=your_jwt_secret_key_here_minimum_256_bits
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=86400000

# OAuth2 - Google
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# OAuth2 - GitHub
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

# Email Configuration (Gmail SMTP)
SPRING_KAFKA_EMAIL=your_email@gmail.com
SPRING_KAFKA_PASSWORD=your_app_password

# AWS S3 / Supabase Storage
SUPABASE_S3_ACCESS_KEY=your_s3_access_key
SUPABASE_S3_SECRET_KEY=your_s3_secret_key

# Stripe Payment (Optional)
STRIPE_API_KEY=your_stripe_api_key
```

### OAuth2 Setup Instructions

#### Google OAuth2:
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable Google+ API
4. Create OAuth 2.0 credentials
5. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`

#### GitHub OAuth2:
1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Create a new OAuth App
3. Set Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`

## 🎮 Running the Application

### Using Docker Compose (Recommended)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f backend

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### Running Locally (Without Docker)

1. **Start required services:**
   ```bash
   # PostgreSQL
   docker run -d -p 5432:5432 -e POSTGRES_DB=jobhunter -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=password postgres:18-alpine
   
   # Redis
   docker run -d -p 6379:6379 redis:8.2.2-alpine
   
   # Elasticsearch
   docker run -d -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" elasticsearch:8.19.5
   
   # Kafka
   docker run -d -p 9092:9092 apache/kafka:4.0.1
   ```

2. **Run the application:**
   ```bash
   cd backend
   ./gradlew bootRun
   ```

## 📚 API Documentation

Once the application is running, visit:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

## 🏗 Architecture

```
JobHunter/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/github/kzhunmax/jobsearch/
│   │   │   │   ├── company/        # Company management
│   │   │   │   ├── config/         # Spring configuration
│   │   │   │   ├── event/          # Event handling (Kafka)
│   │   │   │   ├── exception/      # Exception handling
│   │   │   │   ├── job/            # Job listings & applications
│   │   │   │   ├── payment/        # Stripe payment integration
│   │   │   │   ├── security/       # Security & authentication
│   │   │   │   ├── user/           # User management
│   │   │   │   └── shared/         # Shared utilities
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/   # Flyway migrations
│   │   └── test/                   # Test files
│   ├── build.gradle
│   └── Dockerfile
└── docker-compose.yml
```

### Key Components

- **Controllers**: REST API endpoints
- **Services**: Business logic layer
- **Repositories**: Data access layer (JPA & Elasticsearch)
- **Security**: JWT authentication & OAuth2 integration
- **Events**: Kafka producers/consumers for async processing
- **Mappers**: MapStruct DTOs for clean API contracts

## 🗄 Database Migrations

Database migrations are managed by Flyway. Migration files are located in `backend/src/main/resources/db/migration/`.

### Key Migrations

- `V1__initial_schema.sql` - Initial database schema
- `V5__add_resume_table.sql` - Resume management
- `V7__add_user_profile_table.sql` - User profiles
- `V10__add_email_verification_to_users.sql` - Email verification
- `V11__add_api_key_and_plan_to_users.sql` - API keys & plans
- `V12__create_company_table.sql` - Company management
- `V13__link_jobs_and_profiles_to_company.sql` - Company relationships

### Running Migrations

Migrations run automatically on application startup. To run manually:

```bash
./gradlew flywayMigrate
```

## 🧪 Testing

### Run All Tests

```bash
cd backend
./gradlew test
```

### Run Specific Test

```bash
./gradlew test --tests "com.github.kzhunmax.jobsearch.job.mapper.JobMapperTest"
```

### View Test Reports

After running tests, open:
```
backend/build/reports/tests/test/index.html
```

### Test Technologies

- **JUnit 5** - Testing framework
- **Testcontainers** - Integration testing with real services
- **Spring Boot Test** - Spring context testing
- **Spring Security Test** - Security testing

## 📊 Service Ports

| Service       | Port  | Description                    |
|---------------|-------|--------------------------------|
| Backend API   | 8080  | Spring Boot application        |
| PostgreSQL    | 5433  | Database (mapped from 5432)    |
| Redis         | 6379  | Cache & rate limiting          |
| Elasticsearch | 9200  | Search engine                  |
| Kafka         | 9092  | Event streaming                |

## 🔒 Security

- JWT token-based authentication
- OAuth2 integration (Google & GitHub)
- BCrypt password hashing
- Role-based access control (RBAC)
- Rate limiting with Bucket4j
- Email verification
- Secure credential management

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 👤 Author

**kzhunmax**
- GitHub: [@kzhunmax](https://github.com/kzhunmax)

## 🐛 Known Issues

- None at the moment

## 📮 Support

For support, email korshunmax82@gmail.com or open an issue in the repository.

---

**Built with ❤️ using Spring Boot and modern technologies**

