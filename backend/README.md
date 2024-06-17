# Backend

This project is built on Java and Maven. It is essential to have both tools properly installed on your system.

## Compiling the project

Given that Maven is the foundation of this project, executing `mvn clean install` within the `backend` directory
suffices.

## Configuration

Certain aspects of the project are configurable, with some requiring customization for security purposes.
Within the backend component, all configuration settings can be found in the
file [application.properties](kendo-tournament-rest/src/main/resources/application.properties).

### Security

Regarding security measures, it is imperative to undertake the following actions to ensure a secure deployment:

#### Update the database encryption key

The property `database.encryption.key` serves the purpose of encrypting personal data.
It is crucial that this value remains confidential.
If left empty, no encryption will be applied to the database.
Examples of usage include:

```
database.encryption.key=mypassword
```

#### JWT secret key

This setting defines the encryption token utilized for communication between the frontend and backend.
Any value may be used here as long as it remains confidential.
Should `jwt_secret` be left blank, a random token will be generated upon initialization.
While randomness enhances security, users will need to log back into the system if the server restarts.

```
jwt.secret=anotherpassword
```

#### Initial user

A user is automatically added to the database to enable an administrator to log in to the application for the initial
time.
This user is intended for connecting from the frontend to the backend endpoints and retrieving the necessary data
required by the application.
The script responsible for generating the admin user can be
accessed [here](kendo-tournament-rest/src/main/resources/database/default-authenticated-users.sql).
It is highly advisable to modify the user credentials within this script before initiating the software.
The password should be encrypted using the BCrypt algorithm.
An online tool such as [this one](https://bcrypt-generator.com/) can be utilized for this purpose.

### Database configuration

The project utilizes Hibernate for managing database connections, allowing easy interchange between different database
providers.
Currently, for developmental purposes, it is configured to operate with MySQL database (v8.3), and this configuration
can be located in the file [application.properties](kendo-tournament-rest/src/main/resources/application.properties):

```
spring.kendo.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.kendo.datasource.platform=mysql
spring.kendo.datasource.jdbc-url=jdbc:mysql://localhost:3306/kendotournament?useSSL=false
spring.kendo.datasource.username=user
spring.kendo.datasource.password=asd123
```

Adjust the configuration according to your requirements.
If opting for a different database provider, ensure inclusion of the corresponding `jar` dependency into the project or
manual transfer of the `jar` into your server.

## Run the project

Spring Boot serves as our framework for backend development.
To run the project, navigate first to `backend/kendo-tournament-rest/target` directory post-compilation, and
execute `mvn spring-boot:run`.

### Check API Endpoints

This project utilizes OpenAPI Documentation.
To review all available endpoints, please visit `http://localhost:8080/kendo-tournament-backend/` or substitute
`localhost` with your server domain if in a production environment.
The presence of endpoint descriptions indicates proper functionality of the backend.

Kindly note that the endpoints require a Bearer Auth Token for execution.
This token can be obtained through the login service at the `auth/public/login` endpoint, using the admin user
credentials specified earlier in this document.

## Running unit tests

For testing purposes, to execute all tests, navigate to the `backend` folder and run `mvn test`.

# 3rd party components

The font used in the PDF is ArchitectsDaughter by Kimberly Geswein; further details can be
found [here](https://fonts.google.com/specimen/Architects+Daughter/about).
