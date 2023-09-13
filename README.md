<img src="./documents/logo.svg" width="800" alt="Kendo Tournament Manager v2" align="middle"> 

***REMOVED***

[![Languages](https://img.shields.io/badge/languages-%F0%9F%87%AA%F0%9F%87%B8%20%F0%9F%87%AC%F0%9F%87%A7%20%F0%9F%87%AE%F0%9F%87%B9%20-blue.svg)]()
[![GNU GPL 3.0 License](https://img.shields.io/badge/license-GNU_GPL_3.0-brightgreen.svg)](https://github.com/softwaremagico/KendoTournamentManager/blob/main/LICENSE)
[![Issues](https://img.shields.io/github/issues/softwaremagico/KendoTournamentManager.svg)](https://github.com/softwaremagico/KendoTournamentManager/issues)
[![GitHub commit activity](https://img.shields.io/github/commit-activity/y/softwaremagico/KendoTournamentManager)](https://github.com/softwaremagico/KendoTournamentManager)
[![GitHub last commit](https://img.shields.io/github/last-commit/softwaremagico/KendoTournamentManager)](https://github.com/softwaremagico/KendoTournamentManager)
[![CircleCI](https://circleci.com/gh/softwaremagico/KendoTournamentManager.svg?style=shield)](https://circleci.com/gh/softwaremagico/KendoTournamentManager)
[![Time](https://img.shields.io/badge/development-495h-blueviolet.svg)]()

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

Kendo Tournament Manager v2 is the final tool for handling all your kendo tournament information in one single place.
Designed for small to big size tournaments, it allows you to handle any kind of event that you can imagine for your
kendo club. Including experience from several fighting structures that we had experienced in the last 15 years of use
of the previous version of this software. The goal is to have enough flexibility to be at the service of your club, and
not the other way around.

This tool is a complete rebuild from the old
tool Kendo Tournament Generator [[1]](https://sourceforge.net/projects/kendotournament/files/) [[2]](https://github.com/softwaremagico/KendoTournament) with the
effort of adapting it to more modern technologies. The new architecture allows the deployment of this tool as a web
application -rather than a desktop application- allowing some advantages such as better compatibility between devices,
as now can be used in any Android/iOS through a browser. Also improves the scoring synchronization between multiple
devices as now everything can be centralized into the cloud.

If you are looking for a desktop application, please go to [Kendo Tournament Generator](https://github.com/softwaremagico/KendoTournament) that maybe suites better to your needs. But if you want some professional tool that is accessible online, this is the project you are looking for. 

## Installation

### From a release version

Download the release you want to use from this git project. You will find two different files, one for the frontend and
one for the backend.

#### Deploy the frontend

For the frontend you can use any web server you are familiar to (Apache, Nginx, ...). Simply uncompress the
file `kendo-tournament-frontend.zip` on the server as a standard web page.

#### Deploy the backend

The backend is released as a standalone JAR file. You need Java JRE 11 or superior to run this application. You can
execute it manually by typing the next command:

```
java -jar kendo-tournament-backend.jar 
```

Note that probably you need to configure the application. For this purpose, you can use any ZIP tool to open the jar
file, and edit the file `BOOT-INF/classes/application.properties` as described on this [Readme](./backend/README.md).
Any change must be put inside the JAR file again to be used by the application.

#### Database

You will need to install a database server. By default, the application is configured to use a MySQL Server, but you can
install anyone you desire. If you want to use any other database engine, you must include the needed connector
on `/BOOT-INF/lib/` inside the backend JAR. After choosing your database server, you must configure
the `application.properties` as described on this [Readme](./backend/README.md) to match your database properties.

For example, to use it with a PostgresSQL server the settings will be:

```
spring.kendo.datasource.platform=postgresql
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.kendo.datasource.jdbc-url=jdbc:postgresql://kendo-tournament-database:5432/postgres
spring.kendo.datasource.username=<your user>
spring.kendo.datasource.password=<your password>
``` 

And search and download the
connector [postgresql-X.X.X.jar](https://mvnrepository.com/artifact/org.postgresql/postgresql) and deploy it
on `/BOOT-INF/lib/` folder inside the JAR file.

If the backend is connected successfully to the database, it will generate all required database structure for you.

### From the source code

Please, download the complete project from [here](https://github.com/softwaremagico/KendoTournamentManager). The project
is divided in two parts `frontend` and `backend`. Each one must be run separately.

#### Frontend Component

The frontend is developed using Angular. All information related to the use and execution of the frontend can be
found [here](./frontend/README.md).

#### Backend Component

The backend component is developed in Java. You can find all the information of compiling and running the backend on
this [Readme](./backend/README.md) file. Be sure you read the documentation, as there are some default keys that must be
changed for security reasons.

##### Database Storage

Backend needs a database to persist all data. You can use easily any of your preferred database providers. Please,
check [the documentation](./backend/README.md) about how to configure your preferred database engine.

### Using Docker

The application can also be deployed as a [docker](https://www.docker.com/) container. If you want to deploy it as a
docker, please read the [docker guide](./docker/README.md) of this project. This is the preferred way of deploying the
application and probably the easiest way if you feel comfortable using docker.

## Hardware Requirements

### Hosting

The application has been tested in a Raspberry Pi 3 model B with 1GB of RAM, with a very good response for a small to
medium size event. For a bigger events, please consider to hire some more specific hardware hosted on the cloud.

### Client Side

As a designed as a web application, any device with a browser can act as a client. The UX is designed for desktop
environment and tablets. Has a responsive design that fits well in almost all devices of the market. The use of mobile
devices is not recommended due to the physical size of the screen.

# Using the application

## Default credentials.

The default user is `admin@test.com` with password `asd123`. This user is an admin user that can create new users.
Remember
to change the password or remove this account.

# Contributing to Kendo Tournament Manager v2

You can contribute to this project in different way: the most basic way to show your support is to star the project, or
to raise issues. But as a programmer you can also include new features or fixing bugs. If you speak a different
language, you can translate the application to your own language. Also, any way of improving the design is welcome.

To contribute to the code of Kendo Tournament Manager v2, follow these steps:

    Fork this repository.
    Create a branch: git checkout -b <branch_name>.
    Make your changes and commit them: git commit -m '<commit_message>'
    Push to the original branch: git push origin <project_name>/<location>
    Create the pull request.

Alternatively see the GitHub documentation
on [creating a pull request](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request)

### Contributors

<a href="https://github.com/softwaremagico/KendoTournamentManager/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=softwaremagico/KendoTournamentManager" alt="contributors"/>
</a>

# Contact

If you want to contact me you can reach me at  ![email address image](./documents/email-address.gif)

# License

This project uses the following
license: [AGPL License v3.0](https://github.com/softwaremagico/KendoTournamentManager/blob/main/LICENSE). Please take a
look on it before using this application. 
