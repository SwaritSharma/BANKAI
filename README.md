# BANKAI

## Backend environment configuration

The backend is configured to read environment variables for database, Redis, CORS, and server settings.

Required env vars:

- `SPRING_DATASOURCE_URL` – JDBC connection URL for MySQL
- `SPRING_DATASOURCE_USERNAME` – database username
- `SPRING_DATASOURCE_PASSWORD` – database password
- `SPRING_REDIS_HOST` – Redis host
- `SPRING_REDIS_PORT` – Redis port
- `SERVER_PORT` – backend HTTP port (default: `8081`)
- `APP_CORS_ALLOWED_ORIGIN_PATTERNS` – allowed CORS origin patterns (default: `http://localhost:3000`)

### Running locally

1. Create or copy `Backend/.env.example` to `Backend/.env`, or place a `.env` file at the repo root.
2. Update the environment variables in the file.
3. Run the backend with Maven:

```bash
cd Backend
./mvnw spring-boot:run
```

### Building the backend

```bash
cd Backend
./mvnw clean package -DskipTests
```

The existing GitHub Actions workflow at `.github/workflows/backend-ci.yml` already builds and packages the backend on `dev` and `main`.

### Deployment with GitHub Actions

A separate GitHub Actions workflow is available at `.github/workflows/backend-deploy.yml`.

Required GitHub Secrets for deployment:

- `DEPLOY_HOST` – remote server hostname or IP
- `DEPLOY_USER` – SSH username
- `DEPLOY_PORT` – SSH port (default: `22`)
- `DEPLOY_KEY` – SSH private key for the remote user
- `DEPLOY_PATH` – remote target directory for the JAR file

You can trigger deployment manually from the Actions tab or by pushing to `main`.

