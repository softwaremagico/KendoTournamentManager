# Kendo Tournament Manager v2

[![Languages](https://img.shields.io/badge/languages-%F0%9F%87%AA%F0%9F%87%B8%20%F0%9F%87%AC%F0%9F%87%A7%20%F0%9F%87%B3%F0%9F%87%B1%20%F0%9F%87%AE%F0%9F%87%B9%20%F0%9F%87%A9%F0%9F%87%AA%20-blue.svg)]()
[![GNU GPL 3.0 License](https://img.shields.io/badge/license-GNU_GPL_3.0-brightgreen.svg)](https://github.com/softwaremagico/KendoTournamentManager/blob/main/LICENSE)
[![Issues](https://img.shields.io/github/issues/softwaremagico/KendoTournamentManager.svg)](https://github.com/softwaremagico/KendoTournamentManager/issues)
[![GitHub commit activity](https://img.shields.io/github/commit-activity/y/softwaremagico/KendoTournamentManager)](https://github.com/softwaremagico/KendoTournamentManager)
[![GitHub last commit](https://img.shields.io/github/last-commit/softwaremagico/KendoTournamentManager)](https://github.com/softwaremagico/KendoTournamentManager)
[![CircleCI](https://circleci.com/gh/softwaremagico/KendoTournamentManager.svg?style=shield)](https://circleci.com/gh/softwaremagico/KendoTournamentManager)
[![Time](https://img.shields.io/badge/development-188h-blueviolet.svg)]()

[![Powered by](https://img.shields.io/badge/powered%20by%20java-orange.svg?logo=OpenJDK&logoColor=white)]()
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-backend&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-backend)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-backend&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-backend)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-backend&metric=bugs)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-backend)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-backend&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-backend)

[![Powered by](https://img.shields.io/badge/powered%20by%20angular-red.svg?logo=angular&logoColor=white)]()
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-frontend&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-frontend)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-frontend&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-frontend)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-frontend&metric=bugs)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-frontend)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=kendo-tournament-frontend&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=kendo-tournament-frontend)


Kendo Tournament Manager v2 is a complete rebuild of the old
tool [Kendo Tournament Generator](https://sourceforge.net/projects/kendotournament/files/) with the effort of adapting
it to the modern technologies. The new architecture allows the deployment of this tool as a web application -rather than
a desktop application- allowing some advantages such as better compatibility between devices, as now can be used in any
Android/iOS system as a standard webpage, or better scoring synchronization between multiple devices as now everything
is centralized in one server.

## Installation

### Compiling from the sourcecode

Please, download the complete project from [here](https://github.com/softwaremagico/KendoTournamentManager). The project
is divided in two parts `frontend` and `backend`. Each one must be run separately.

#### Frontend Component

The frontend is developed using Angular. All information related to the use and execution of the frontend can be
found [here](./frontend/README.md).

#### Backend Component

The backend component is developed in Java. You can find all the information of compiling and running the backend on
this [Readme](./backend/README.md) file. Be sure you read the documentation, as there are some default keys that must be
changed.

##### Database Storage

Backend needs a database to persist all data. You can fit easily any of your preferred database providers. Please,
check [the documentation](./backend/README.md) about how to configure the database.

### Using Docker

The application can also be deployed as a [docker](https://www.docker.com/) container. If you want to deploy it as a
docker, please read the [docker guide](./docker/README.md) of this project. This is the preferred way of deploying the
application and probably the easiest way if you feel comfortable using docker
