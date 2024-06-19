<img src="./documents/logo.svg" width="800" alt="Kendo Tournament Manager v2" align="middle"> 

---

[![Languages](https://img.shields.io/badge/languages-%F0%9F%87%AA%F0%9F%87%B8%20%F0%9F%87%AC%F0%9F%87%A7%20%F0%9F%87%AE%F0%9F%87%B9%20-blue.svg)]()
[![GNU GPL 3.0 License](https://img.shields.io/badge/license-GNU_GPL_3.0-brightgreen.svg)](https://github.com/softwaremagico/KendoTournamentManager/blob/main/LICENSE)
[![Issues](https://img.shields.io/github/issues/softwaremagico/KendoTournamentManager.svg)](https://github.com/softwaremagico/KendoTournamentManager/issues)
[![GitHub commit activity](https://img.shields.io/github/commit-activity/y/softwaremagico/KendoTournamentManager)](https://github.com/softwaremagico/KendoTournamentManager)
[![GitHub last commit](https://img.shields.io/github/last-commit/softwaremagico/KendoTournamentManager)](https://github.com/softwaremagico/KendoTournamentManager)
[![CircleCI](https://circleci.com/gh/softwaremagico/KendoTournamentManager.svg?style=shield)](https://circleci.com/gh/softwaremagico/KendoTournamentManager)
[![Time](https://img.shields.io/badge/development-625.5h-blueviolet.svg)]()

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

The Kendo Tournament Manager v2 is a [comprehensive tool](https://github.com/softwaremagico/KendoTournamentManager/wiki)
designed to efficiently manage all your kendo tournament data in
one convenient location.
It caters to tournaments of varying sizes, offering versatility to accommodate any type of
event for your kendo club.
Drawing on 15 years of experience with different fighting structures, this tool aims to
provide flexibility that serves your club's needs effectively.

<p align="center">
    <img src="https://github.com/softwaremagico/KendoTournamentManager/wiki/images/Scores-Example.png" width="500" alt="Scores example">
</p>

A significant upgrade from the previous Kendo Tournament
Generator [[1]](https://sourceforge.net/projects/kendotournament/files/) [[2]](https://github.com/softwaremagico/KendoTournament),
this tool has been rebuilt entirely with a focus on modern technologies.
The new architecture allows for web deployment instead of being limited to desktop applications.
This shift brings advantages like enhanced compatibility across different devices, enabling access from any Android or
iOS device through a web browser.
Moreover, it enhances scoring synchronization among multiple devices by centralizing
all data in the cloud.

If you are seeking a desktop application, please visit
the [Kendo Tournament Generator page](https://sourceforge.net/projects/kendotournament/files/) for more information.

But if you are in need of a professional online tool, the project described here may be exactly what you are searching
for. If you are interested in discovering the capabilities of this tool for your tournaments, you can explore the 
[wiki](https://github.com/softwaremagico/KendoTournamentManager/wiki/Tournament-definition) for details on its features 
and customization choices. 
Alternatively, for practical illustrations, you can directly check out a few description 
[examples](https://github.com/softwaremagico/KendoTournamentManager/wiki/Full-Examples) provided.

## Installation

For detailed guidance on the installation process, please refer to
the [wiki](https://github.com/softwaremagico/KendoTournamentManager/wiki/Installation).
But for your convenience, here is an overview:

### From a release version

To begin, download your desired release from this git project.
You will receive two distinct files, one for the frontend and another for the backend.

#### Deploy the frontend

For the frontend component, any familiar web server such as Apache or Nginx can be used.
Simply extract the file `kendo-tournament-frontend.zip` onto your server as a standard web page.

#### Deploy the backend

The backend is distributed as a standalone JAR file.
Running this application requires Java JRE 17 or higher.
You can manually execute it by entering the following command:

```
java -jar kendo-tournament-backend.jar 
```

It's likely that some configuration adjustments may be necessary for the application.
To do so, utilize any ZIP tool to access the jar file and modify 'BOOT-INF/classes/application.properties' according to
instructions provided in this [Readme](./backend/README.md).
Remember to repackage any changes back into the JAR file for them to take effect.

#### Database

A database server installation is essential.
While MySQL Server is set as default, you have the option to install any preferred database system.
If opting for a different database engine, ensure to include its required connector within `/BOOT-INF/lib/` in the
backend JAR.
Once your choice of database server is installed, configure `application.properties`
based on guidelines listed in this [Readme](./backend/README.md) to align with your specific database properties.
Remember again to repackage any changes back into the JAR file for them to take effect.

For example, to configure the settings for using a PostgresSQL server, you will need to set up these steps:

1. Download the connector [postgresql-X.X.X.jar](https://mvnrepository.com/artifact/org.postgresql/postgresql) and place
   it in the `/BOOT-INF/lib/` folder within the JAR file.

2. Configure the settings according to your database preferences.

```
spring.kendo.datasource.platform=postgresql
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.kendo.datasource.jdbc-url=jdbc:postgresql://kendo-tournament-database:5432/postgres
spring.kendo.datasource.username=<your user>
spring.kendo.datasource.password=<your password>
``` 

3. Upon successful connection of the backend to the database, all necessary database structures will be automatically
   generated.

### From the source code

For the complete project, please access it [here](https://github.com/softwaremagico/KendoTournamentManager).
The project is divided into two parts: `frontend` and `backend`, each requiring separate execution.

#### Frontend Component

The frontend is created using Angular.
Detailed instructions on how to use and run the frontend can be found [here](./frontend/README.md).

#### Backend Component

The backend component is written in Java.
Instructions for compiling and running the backend are available in this [Readme](./backend/README.md) file.
Make sure to carefully review the documentation as there are default keys that should be modified for security purposes.

##### Database Storage

The backend requires a database for data persistence.
You have flexibility in choosing your preferred database provider.
Refer to [the documentation](./backend/README.md) for guidance on configuring your chosen database engine.

### Using Docker

Furthermore, deploying the application as a [docker](https://www.docker.com/) container is an option.
If you prefer deploying it as a Docker container, refer to the guidelines provided in this
project's [documentation](./docker/README.md).
Using Docker is recommended for deployment due to its ease of use once you are familiar with it.

## Hardware Requirements

### Hosting

The application has been tested on a Raspberry Pi 3 model B with 1GB of RAM and has shown excellent performance for
small to medium-sized events.
For larger events, it is recommended to consider using more specialized hardware hosted in the cloud.

### Client Side

Designed as a web application, it can be accessed by any device with a browser.
The user experience is optimized for desktop environments and tablets, featuring a responsive design that adapts well to
most devices in the market.
However, using mobile devices is not advised due to screen size limitations.

# Using the application

For detailed information on how to use this application, you can refer to
the  [wiki](https://github.com/softwaremagico/KendoTournamentManager/wiki/).

## Default credentials.

The default user credentials are as follows: Username: `admin@test.com`, Password: `asd123`.
This user has administrative privileges and can create new accounts.
It is important to change the password or delete this account for security reasons.

# Contributing to Kendo Tournament Manager v2

There are various ways to contribute to this project:

- You can show support by starring the project or reporting issues.
- If you are a programmer, you can add new features or fix bugs.
- Translating the application into different languages is also appreciated.
- Any suggestions for improving the design are welcome.

To contribute code to Kendo Tournament Manager v2, please follow these steps:

    Fork this repository.
    Create a branch: git checkout -b <branch_name>.
    Make your changes and commit them: git commit -m '<commit_message>'
    Push to the original branch: git push origin <project_name>/<location>
    Create the pull request.

Alternatively, refer to GitHub's documentation
on [creating a pull request](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request)

## Translations

If you are not a programmer but would like to have this software available in your language,
you can easily contribute to the translations through Weblate.
Weblate allows you to enhance existing translations, address issues, or even introduce new languages if desired.
No programming skills are required, only a willingness to assist.
To begin translating, please visit this [link](https://hosted.weblate.org/projects/kendotournamentmanager/) for more
information.

### Contributors

<a href="https://github.com/softwaremagico/KendoTournamentManager/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=softwaremagico/KendoTournamentManager" alt="contributors"/>
</a>

# Contact

Should you wish to contact me, feel free to reach out at the following email
address: ![email address image](./documents/email-address.gif)

# License

Please note that this project is licensed under
the [AGPL License v3.0](https://github.com/softwaremagico/KendoTournamentManager/blob/main/LICENSE).
Kindly review it before using the application.
