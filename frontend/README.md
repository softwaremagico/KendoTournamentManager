# Kendo Tournament Manager NG — Frontend

[![Powered by](https://img.shields.io/badge/powered%20by%20angular-red.svg?logo=angular&logoColor=white)]()
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-frontend&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-frontend)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-frontend&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-frontend)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-frontend&metric=bugs)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-frontend)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-frontend&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-frontend)
[![Docker Pulls](https://img.shields.io/docker/pulls/softwaremagico/kendo-tournament-manager-frontend)](https://hub.docker.com/r/softwaremagico/kendo-tournament-manager-frontend)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-brightgreen.svg)](https://github.com/softwaremagico/KendoTournamentManager/blob/main/LICENSE)

---

## Description

This is the **Angular frontend** for the [Kendo Tournament Manager NG](https://github.com/softwaremagico/KendoTournamentManager),
a comprehensive web application designed to efficiently manage kendo tournament data.
It provides a modern, responsive interface to handle tournaments of varying sizes and formats,
from small club events to large professional competitions.

Built with **Angular 15 LTS** and the **Wizardry Theme** (by BiiT Solutions), the frontend communicates with a Java Spring Boot backend via REST API
and supports real-time score updates through WebSockets (STOMP over SockJS).

Key features exposed through this UI include:

- **Tournament management**: create and manage tournaments in multiple formats (league, championship, etc.)
- **Participant & team management**: register competitors, assign them to teams and clubs
- **Fight tracking**: create fights, score duels in real time and handle tie-breaking
- **Statistics & rankings**: view detailed per-participant and per-tournament statistics
- **Achievements**: gamification system to encourage club member participation
- **QR codes**: generate QR codes for tournaments and participants
- **Role-based access control (RBAC)**: manage users with different roles and permissions
- **Internationalization (i18n)**: multi-language support via [Transloco](https://ngneat.github.io/transloco/) (Spanish, English, Italian and more)
- **PDF export**: export brackets and results to PDF using jsPDF

For further details on the application functionality, refer to the
[wiki](https://github.com/softwaremagico/KendoTournamentManager/wiki).

---

## Tech Stack

| Technology | Version |
|---|---|
| Angular | 15 (LTS) |
| Angular Material | ^15.2.9 |
| Transloco (i18n) | ^4.3.0 |
| RxJS | ~7.4.0 |
| STOMP / WebSockets | @stomp/rx-stomp ^2.0.0 |
| jsPDF | ^2.5.1 |
| TypeScript | ~4.9.4 |
| Karma + Jasmine | (unit tests) |

---

## Project Structure

```
src/
├── app/
│   ├── components/          # Reusable UI components
│   │   ├── achievement-tile/        # Single achievement display
│   │   ├── achievement-wall/        # Full achievements panel
│   │   ├── charts/                  # Statistical chart components
│   │   ├── competitors-ranking/     # Competitor ranking tables
│   │   ├── fight/                   # Fight display and scoring
│   │   ├── fight-creator/           # Fight generation wizard
│   │   ├── fight-statistics-panel/  # Fight stats overview
│   │   ├── navigation/              # Top navigation bar
│   │   ├── participant-picture/     # Participant photo component
│   │   ├── participant-qr-code/     # QR code for participants
│   │   ├── team-card/               # Team display card
│   │   ├── team-ranking/            # Team ranking table
│   │   ├── timer/                   # Match countdown timer
│   │   ├── tournament-brackets/     # Read-only bracket view
│   │   ├── tournament-brackets-editor/ # Editable bracket
│   │   ├── tournament-qr-code/      # QR code for tournaments
│   │   ├── untie-fight/             # Tie-breaking fight creator
│   │   └── ...                      # Other shared components
│   ├── forms/               # Reactive forms for data entry
│   │   ├── club-form/               # Club creation/edit form
│   │   ├── participant-form/        # Participant registration form
│   │   ├── tournament-form/         # Tournament setup form
│   │   └── ...
│   ├── interceptors/        # HTTP interceptors (auth headers, JWT, error handling)
│   ├── models/              # TypeScript interfaces and domain models
│   │   ├── tournament.ts            # Tournament model
│   │   ├── participant.ts           # Participant model
│   │   ├── fight.ts                 # Fight model
│   │   ├── duel.ts                  # Duel model
│   │   ├── team.ts                  # Team model
│   │   ├── achievement.model.ts     # Achievement model
│   │   └── ...                      # Other domain models
│   ├── pipes/               # Custom Angular pipes
│   ├── services/            # Services for API communication
│   │   ├── tournament.service.ts    # Tournament CRUD
│   │   ├── participant.service.ts   # Participant CRUD
│   │   ├── fight.service.ts         # Fight operations
│   │   ├── ranking.service.ts       # Rankings & scores
│   │   ├── statistics.service.ts    # Statistics retrieval
│   │   ├── login.service.ts         # Authentication
│   │   ├── achievements.service.ts  # Achievements
│   │   └── ...                      # Other domain services
│   ├── utils/               # Utility classes and helpers
│   ├── views/               # Routed page views
│   │   ├── login/                   # Login page
│   │   ├── tournament-list/         # Tournament list view
│   │   ├── participant-list/        # Participant management view
│   │   ├── club-list/               # Club management view
│   │   ├── fight-list/              # Fight list and scoring view
│   │   ├── tournament-statistics/   # Tournament statistics view
│   │   ├── participant-statistics/  # Participant statistics view
│   │   └── ...
│   └── websockets/          # WebSocket (STOMP) integration for real-time updates
├── assets/
│   ├── audio/               # Sound effects for scoring events
│   ├── i18n/                # Translation files (JSON)
│   ├── icons/               # Application icons
│   └── img/                 # Images and graphics
├── environments/            # Environment-specific configuration
│   ├── environment.ts            # Development configuration
│   ├── environment.prod.ts       # Production configuration
│   └── environment.docker.ts     # Docker deployment configuration
└── styles.scss              # Global styles
```

---

## Configure

In the [environment folder](src/environments), there are various files for storing configuration variables related to the server.
Ensure you set the backend server URL appropriately if your frontend and backend run on different hosts:

```typescript
backendUrl: "http://localhost:8080/kendo-tournament-backend"
```

| Environment file | Used for |
|-----------------|---------|
| `environment.ts` | Local development (`ng serve`) |
| `environment.prod.ts` | Production build from release binaries |
| `environment.docker.ts` | Docker deployment — variables are injected at runtime via `config.js` |

When deploying via Docker, the backend URL and WebSocket URL are configured through the `config.js` file that is
mounted into the container at runtime (see the [Docker documentation](../docker/README.md) for details).

---

## Development server

Run `ng serve` to start a local development server. Navigate to `http://localhost:4200/`.
The application will automatically reload on any source file change.

For a production configuration:

```bash
ng serve --configuration=production
```

---

## Build

```bash
ng build
```

For a production build:

```bash
ng build --configuration=production
```

The compiled artifacts will be placed in the `dist/` directory and can be served by any static web server (Apache, Nginx, etc.).

---

## Running unit tests

Run the following command to execute the unit test suite using [Karma](https://karma-runner.github.io):

```bash
ng test
```

### Check coverage

Run tests with code coverage report:

```bash
npm run test -- --browsers=ChromeHeadless --watch=false --code-coverage 2>&1 | tail -120
```

---

## Docker

A Docker image is available on Docker Hub:

```bash
docker pull softwaremagico/kendo-tournament-manager-frontend
```

For full deployment including backend and reverse proxy, refer to the
[Docker documentation](../docker/README.md) and the
[wiki](https://github.com/softwaremagico/KendoTournamentManager/wiki/Installation-using-docker-hub).

---

## Related modules

| Module | Description |
|--------|-------------|
| [Backend](../backend/README.md) | Java Spring Boot REST API that this frontend consumes |
| [Docker](../docker/README.md) | Docker Compose configuration for full-stack deployment |
| [docker-examples](../docker-examples/) | Ready-to-use `docker-compose.yml` examples for various scenarios |

---

> For full application documentation, installation guides, and usage examples, visit the
> [project wiki](https://github.com/softwaremagico/KendoTournamentManager/wiki).

| Module | Description |
|---|---|
| [Backend](../backend/README.md) | Java Spring Boot REST API |
| [Docker](../docker/README.md) | Docker Compose deployment |
| [Wiki](https://github.com/softwaremagico/KendoTournamentManager/wiki) | Full documentation |

---

## License

This project is licensed under the [AGPL License v3.0](https://github.com/softwaremagico/KendoTournamentManager/blob/main/LICENSE).
