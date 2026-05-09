# Kendo Tournament Manager — Backend

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-brightgreen.svg)](https://github.com/softwaremagico/KendoTournamentManager/blob/main/LICENSE)
[![Powered by](https://img.shields.io/badge/powered%20by%20java-orange.svg?logo=OpenJDK&logoColor=white)]()
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-backend&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-backend)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-backend&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-backend)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-backend&metric=bugs)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-backend)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-backend&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-backend)
[![Docker Pulls](https://img.shields.io/docker/pulls/softwaremagico/kendo-tournament-manager-backend)](https://hub.docker.com/r/softwaremagico/kendo-tournament-manager-backend)
[![Docker Image Version](https://img.shields.io/docker/v/softwaremagico/kendo-tournament-manager-backend?sort=semver)](https://hub.docker.com/r/softwaremagico/kendo-tournament-manager-backend)

This is the backend component of the [Kendo Tournament Manager](https://github.com/softwaremagico/KendoTournamentManager),
a comprehensive tool designed to manage all aspects of kendo tournaments: participants, clubs, teams, fights, scores,
rankings, statistics, and achievements.

The backend exposes a RESTful API secured by JWT authentication, consumed by the Angular frontend and any third-party
integrations. It is built with **Java 17+**, **Spring Boot**, and **Maven**, and uses **Hibernate** for database
abstraction.

---

## Table of contents

- [Project structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Building the project](#building-the-project)
- [Running the application](#running-the-application)
- [Running the tests](#running-the-tests)
- [Configuration](#configuration)
  - [Security](#security)
  - [Database](#database)
- [API documentation (Swagger / OpenAPI)](#api-documentation-swagger--openapi)
- [Authentication](#authentication)
- [3rd party components](#3rd-party-components)

---

## Project structure

The backend is organised as a **multi-module Maven project**. Each module has its own responsibility:

```
backend/
├── kendo-tournament-logger/       # Cross-cutting logging utilities and custom exceptions
├── kendo-tournament-persistence/  # JPA entities, repositories and database configuration
├── kendo-tournament-core/         # Business logic: tournaments, fights, scores, rankings, statistics…
├── kendo-tournament-pdf/          # PDF report generation (score sheets, diplomas, etc.)
└── kendo-tournament-rest/         # Spring Boot application: REST controllers, security, Swagger
```

| Module | Description |
|--------|-------------|
| `kendo-tournament-logger` | Centralised logging facade, `LoggedException` hierarchy and `ExceptionType` |
| `kendo-tournament-persistence` | All JPA entities (`Tournament`, `Participant`, `Fight`, `Duel`, …), Spring Data repositories and Hibernate configuration |
| `kendo-tournament-core` | Service layer and domain controllers: fight generation algorithms, ranking calculation, statistics, achievements, CSV import/export |
| `kendo-tournament-pdf` | iText-based PDF builder for score sheets, diplomas, and tournament summaries |
| `kendo-tournament-rest` | Spring Boot entry point, REST controllers, JWT security filter chain, Swagger/OpenAPI configuration |

The build order is determined by Maven's module dependency graph; `rest` is always built last as it aggregates all the
other modules.

---

## Prerequisites

| Tool | Minimum version |
|------|----------------|
| Java (JDK) | 17 |
| Maven | 3.8 |
| Database server | MySQL 8 (default) or any Hibernate-compatible engine |

---

## Building the project

From the `backend` directory, a standard Maven lifecycle command is enough:

```bash
cd backend
mvn clean install
```

To skip tests during compilation (not recommended for production builds):

```bash
mvn clean install -DskipTests
```

The final deployable artefact is produced inside `kendo-tournament-rest/target/` as a self-contained Spring Boot JAR:

```
kendo-tournament-rest/target/kendo-tournament-rest-<version>.jar
```

---

## Running the application

### From compiled sources

After building, navigate to `kendo-tournament-rest` and start the Spring Boot application:

```bash
cd backend/kendo-tournament-rest
mvn spring-boot:run
```

Or execute the JAR directly:

```bash
java -jar kendo-tournament-rest/target/kendo-tournament-rest-<version>.jar
```

The server listens on **port 8080** by default, under the context path `/kendo-tournament-backend`.

Base URL: `http://localhost:8080/kendo-tournament-backend`

### Using Docker

The easiest way to run the full stack is with Docker Compose. Official images are available on Docker Hub:

- **Backend**: [`softwaremagico/kendo-tournament-manager-backend`](https://hub.docker.com/r/softwaremagico/kendo-tournament-manager-backend)

Refer to the [docker documentation](../docker/README.md) and the
[docker-examples](../docker-examples/) folder for ready-to-use `docker-compose.yml` files.

---

## Running the tests

Each module contains its own TestNG suite located at `src/test/resources/testng.xml`.

### Run all tests across every module

```bash
cd backend
mvn test
```

### Run tests for a single module

```bash
# Example: only the logger module
cd backend/kendo-tournament-logger
mvn test

# Example: only the REST module
cd backend/kendo-tournament-rest
mvn test
```

### Run a specific test class

```bash
mvn test -Dtest=JwtTokenUtilTest
```

### Skip static analysis during test runs (useful locally)

```bash
mvn test -Dspotbugs.skip=true -Dcheckstyle.skip=true
```

---

## Configuration

All runtime configuration lives in:

```
kendo-tournament-rest/src/main/resources/application.properties
```

The most important settings are described below.

### Security

#### JWT secret key

Used to sign and verify all JWT tokens exchanged between the frontend and the backend.
Any secret value is valid as long as it remains confidential.
If left blank, a random secret is generated at startup — note that this means **all active sessions are invalidated
every time the server restarts**.

```properties
jwt.secret=change-me-before-deploying
```

Token expiration values (milliseconds):

```properties
jwt.expiration=1200000          # Standard authenticated user (20 min)
jwt.guest.expiration=14400000   # Guest user (4 h)
jwt.participant.expiration=317098000000  # Participant access (≈ 10 years)
```

#### Database encryption key

The property `database.encryption.key` encrypts personally identifiable data at rest.
It is crucial that this value remains confidential.
Leave it empty to disable field-level encryption (not recommended in production).

```properties
database.encryption.key=change-me-before-deploying
```

#### CORS

Restrict cross-origin requests to specific domains (comma-separated list):

```properties
server.cors.domains=https://my.tournament.site
```

In development you can use `*` to allow all origins.

#### Optional access features

```properties
# Allow unauthenticated read-only access to a specific tournament via QR code
enable.guest.user=true

# Allow participants to view their own statistics via a long-lived personal QR code
enable.participant.access=true

# Bind JWT token validation to the client's IP address (adds extra security,
# but breaks access for users with dynamic IPs or VPNs)
jwt.ip.check=false
```

**Guest access** generates a temporary JWT for each QR code scan. The token expiration is controlled by
`jwt.guest.expiration` (milliseconds; default: 14 400 000 ms = 4 hours). Guests can only view the scores of
the specific tournament for which the QR code was generated; all other views redirect to the login screen.
To disable guest access globally, set `enable.guest.user=false`.

**Participant access** generates a long-lived personal JWT for each registered participant. The token expiration is
controlled by `jwt.participant.expiration` (milliseconds; default: 317 098 000 000 ms ≈ 10 years). Participants
can only view their own statistics and fight history. To disable participant access globally, set
`enable.participant.access=false`.

#### Initial administrator user

On first startup with an empty database, the application can create a default administrator account automatically.
This behaviour is controlled by the `database_populate_default_data` property (available via the Docker environment
variable of the same name):

| Value | Effect |
|-------|--------|
| `always` | A default admin user (`admin@test.com` / `asd123`) is created on every fresh schema. **Not compatible with database encryption.** |
| `never` | No default data is created. On first login attempt the application prompts you to create the first administrator account interactively. |

**Change the default credentials immediately** if you use `always`. Passwords are stored in **BCrypt** format.
You can generate a BCrypt hash using an online tool such as [bcrypt-generator.com](https://bcrypt-generator.com/).

The default admin SQL script is located at:
```
kendo-tournament-rest/src/main/resources/database/default-authenticated-users.sql
```

### Database

The project uses Hibernate for managing database connections, allowing easy interchange between different database
providers. The default setup targets **MySQL 8**:

```properties
spring.kendo.datasource.platform=mysql
spring.kendo.datasource.jdbc-url=jdbc:mysql://localhost:3306/kendotournament?useSSL=false&autoReconnect=true
spring.kendo.datasource.username=user
spring.kendo.datasource.password=asd123
```

Hibernate will automatically create and update the schema on startup (`ddl-auto=update`), so no manual migration
scripts are needed on first run.

#### Using PostgreSQL

1. Add the PostgreSQL connector JAR to `/BOOT-INF/lib/` inside the packaged JAR (or declare it as a Maven dependency).
2. Update `application.properties`:

```properties
spring.kendo.datasource.platform=postgresql
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.kendo.datasource.jdbc-url=jdbc:postgresql://localhost:5432/kendotournament
spring.kendo.datasource.username=<your user>
spring.kendo.datasource.password=<your password>
```

---

## CSV import / export

The backend supports bulk import of **clubs** and **participants** via CSV files uploaded through the REST API (and
exposed in the frontend). The CSV format is:

**Clubs**
```
#Name; Country; City; Address; Phone; Email; Web;
```

**Participants**
```
#Name; Lastname; idCard; Club; ClubCity
```

Existing records are skipped (clubs are de-duplicated by name + city; participants are de-duplicated by ID card).

---

## Achievements

The achievement engine runs automatically in the background after each tournament update. It evaluates each
participant against a set of hidden goals and awards badges at four levels: **normal**, **bronze**, **silver**, and
**gold**.

Achievements can be **disabled** globally:

```properties
# In application.properties — set via the achievements_enabled Docker variable
achievements.enabled=false
```

When disabled, no achievement calculations are performed and the achievement wall is hidden in the frontend.

---

## API documentation (Swagger / OpenAPI)

The backend ships with a **Swagger UI** powered by SpringDoc / OpenAPI 3.

Once the application is running, open the following URL in your browser:

```
http://localhost:8080/kendo-tournament-backend/swagger-ui/index.html
```

Or, if using a custom domain:

```
https://<your-domain>/kendo-tournament-backend/swagger-ui/index.html
```

The raw OpenAPI JSON spec is available at:

```
http://localhost:8080/kendo-tournament-backend/v3/api-docs
```

Tags are sorted alphabetically and all endpoints are grouped under the `kendo-tournament-public` group.

### Available endpoint groups

| Domain | Description |
|--------|-------------|
| `auth` | Login, token refresh, guest and participant access |
| `authenticated-users` | Administrator user management |
| `clubs` | Club creation, update and deletion |
| `participants` | Participant registration, photo upload, roles |
| `teams` | Team composition |
| `tournaments` | Tournament lifecycle: create, configure, start, finish |
| `groups` | Draw groups within a tournament |
| `fights` | Fight scheduling and result entry |
| `duels` | Individual duel score entry |
| `ranking` | Live ranking per group and tournament |
| `statistics` | Participant and tournament statistics |
| `achievements` | Badge and achievement tracking |
| `pdf` | Download PDF score sheets and summary reports |

---

## Authentication

All protected endpoints require a **Bearer JWT token** in the `Authorization` header:

```
Authorization: Bearer <token>
```

### Obtaining a token

Call the login endpoint with valid credentials:

```http
POST /kendo-tournament-backend/auth/public/login
Content-Type: application/json

{
  "username": "admin",
  "password": "yourpassword"
}
```

The response body contains:
- `jwt` — the access token to include in all subsequent requests
- `expiration` — token expiry timestamp (milliseconds)

### Using the token in Swagger UI

1. Open the Swagger UI.
2. Click the **Authorize** button (padlock icon, top right).
3. Paste your JWT token (without the `Bearer ` prefix) into the `bearerAuth` field.
4. Click **Authorize** — all subsequent requests from the UI will carry the token automatically.

---

## 3rd party components

- **ArchitectsDaughter** font by Kimberly Geswein, used in PDF reports —
  [Google Fonts](https://fonts.google.com/specimen/Architects+Daughter/about)
- **OpenPDF** — PDF generation library (LGPL-2.1)
- **JJWT** — JWT creation and validation
- **SpringDoc / OpenAPI 3** — interactive API documentation
- **EhCache** — second-level Hibernate cache
- **Apache Batik** — SVG/image manipulation for accreditation generation
- **Qr Code with logo** — QR code generation for guest and participant access

---

> For full application documentation, installation guides, and usage examples, visit the
> [project wiki](https://github.com/softwaremagico/KendoTournamentManager/wiki).
