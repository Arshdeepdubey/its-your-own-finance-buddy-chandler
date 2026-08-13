# Its Your Own Finance Buddy — Project Overview

This repository implements the "Finance Buddy" application — a simple Java backend with a TypeScript frontend and related build artifacts. This README documents the purpose and contents of each folder at the repository root so contributors can quickly understand the codebase layout.

## Repository layout

- `Dockerfile` — Docker build instructions for containerizing the application.
- `pom.xml` — Maven project descriptor: dependencies, build plugins, packaging instructions.
- `dev-sql-notebook.sqlnb` — Developer SQL notebook used for exploring or prototyping database queries.
- `sql-worksheet.sql` — Ad-hoc SQL worksheet with example queries or schema snippets.

Top-level directories

- `src/` — Source code for the project.
  - `frontend/` — TypeScript frontend application (likely a single-page app or UI assets).
    - `package.json` — npm metadata and scripts for building and running the frontend.
    - `tsconfig.json` — TypeScript compiler configuration for the frontend.
    - `ts/` — Frontend TypeScript source files (UI components, services, build entry points).
    - Role: This folder contains the client-side application, build scripts, and types. It is responsible for the user interface that interacts with the Java backend.
  - `main/` — Java application source files and resources used by the backend.
    - `java/` — Java source root.
      - `com/finance/HelloContainer.java` — Example or containerized entry point (may register endpoints or start a server).
      - `com/finance/Main.java` — Main application class (application startup logic).
      - Role: Backend application code in Java — controllers, services, domain models and startup.
    - `resources/` — Runtime resources packaged with the application.
      - `application.properties` — Configuration properties used by the Java application (server port, datasource settings, etc.).
      - `static/` — Static resources that may be served by the backend (e.g., prebuilt frontend assets or static pages).
      - Role: Holds configuration and static assets required at runtime.

- `test/` — Test sources (JUnit or other Java test classes).
  - `java/` — Java test classes that exercise the backend logic.
  - Role: Unit and integration tests to verify application behavior.

- `target/` — Maven build output (generated during `mvn package` or similar).
  - `tech-1.0-SNAPSHOT.jar.original` — Original JAR produced by the build process.
  - `classes/` — Compiled `.class` files and copied resources packaged into the application.
  - `generated-sources/` — Files produced by annotation processors or build-time code generation.
  - `maven-archiver/` and `maven-status/` — Build metadata used by Maven.
  - Role: Contains build artifacts and intermediate files; not committed but included here when present.

- `generated-test-sources/` — Test-time generated sources (annotation processors, test helpers).
- `test-classes/` — Compiled test classes produced by the build.

- `.github/workflows/` — CI configuration for GitHub Actions.
  - `ci.yml` — Continuous integration workflow used by the repository to build and/or test on push and pull requests.
  - Role: Automates builds, tests and other checks for pull requests and branches.

- `pom.xml` (root) — (already listed) Defines modules/plugins used for building the Java application with Maven.

## How the pieces fit together

- Development flow:
  - The frontend in `src/frontend` is built with Node/npm. The produced static assets may live in `frontend/dist` (or similar) and can be served by the Java backend from `src/main/resources/static`.
  - The Java backend in `src/main/java` is built by Maven (`pom.xml`). It reads configuration from `src/main/resources/application.properties` and can include the frontend's built files as static resources.
  - Tests live under `test/` and are executed by Maven's test phase.

- Build and run (typical commands):

```bash
# Build frontend (from repo root)
cd src/frontend
npm install
npm run build

# Build backend
cd ../../
mvn clean package

# Run backend jar (from target/)
java -jar target/tech-1.0-SNAPSHOT.jar
```

(Adjust commands to match actual `package.json` scripts and `pom.xml` artifact names.)

## API Testing - Using curl

Before testing, set the required environment variables:

```bash
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY_ID=AKIA****
export AWS_SECRET_ACCESS_KEY=wFHx****
```

Then start the application:
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

### Sample curl Commands for All REST APIs

#### 1. Health Check
```bash
curl -X GET http://localhost:8080/
```

#### 2. Actuator Health
```bash
curl -X GET http://localhost:8080/actuator/health
```

#### 3. Create a Secret (POST)
```bash
curl -X POST http://localhost:8080/api/v1/secrets/createSecret \
  -H "Content-Type: application/json" \
  -d '{"name": "db_password", "value": "my_secret_password"}'
```

#### 4. Get a Secret (GET)
```bash
curl -X GET http://localhost:8080/api/v1/secrets/db_password
```

#### 5. Update a Secret (PUT)
```bash
curl -X PUT http://localhost:8080/api/v1/secrets/updateSecret \
  -H "Content-Type: application/json" \
  -d '{"name": "db_password", "value": "updated_password"}'
```

#### 6. Delete a Secret (DELETE)
```bash
curl -X DELETE http://localhost:8080/api/v1/secrets/db_password
```

### Testing Multiple Secrets

Create multiple secrets:
```bash
curl -X POST http://localhost:8080/api/v1/secrets/createSecret \
  -H "Content-Type: application/json" \
  -d '{"name": "api_key", "value": "key_123456"}'

curl -X POST http://localhost:8080/api/v1/secrets/createSecret \
  -H "Content-Type: application/json" \
  -d '{"name": "jwt_token", "value": "token_abcdef"}'
```

Retrieve secrets:
```bash
curl -X GET http://localhost:8080/api/v1/secrets/api_key
curl -X GET http://localhost:8080/api/v1/secrets/jwt_token
```

Update secrets:
```bash
curl -X PUT http://localhost:8080/api/v1/secrets/updateSecret \
  -H "Content-Type: application/json" \
  -d '{"name": "api_key", "value": "key_updated_123"}'
```

## Notes for contributors

- Where to look first:
  - Backend startup and REST endpoints: `src/main/java/com/finance/Main.java` and nearby classes.
  - Frontend entry points and UI logic: `src/frontend/ts/`.
  - Maven build customizations: top-level `pom.xml`.
  - CI jobs: `.github/workflows/ci.yml`.

- Cleaning up build artifacts: run `mvn clean` to remove `target/` contents.

- If you add new frontend build steps, ensure the CI workflow in `.github/workflows/ci.yml` installs Node and runs the frontend build before packaging the backend (if backend packaging depends on frontend assets).

## Contact / Maintainers

If you're contributing or need context about design decisions, open an issue or a PR describing the change. The repository owner and branch information is in the repository metadata.

---
Generated: concise folder overview for maintainers and contributors.
