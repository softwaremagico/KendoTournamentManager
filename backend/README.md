# Backend

This project is based on Java and Maven. You need to have both tools installed on your system.

## Compiling the project

As is based on Maven, simply run `mvn clean install` inside the `backend` folder.

## Configuration

Some properties of the project are customizable and for security reason, in some case must be customized. On the backend
side you can see all configuration settings inside the
file [application.properties](kendo-tournament-rest/src/main/resources/application.properties)

### Security

On the security level, you must perform the next actions to ensure that your deployment is safe:

#### Update the database encryption key

The property `database.encryption.key` is used for database encryption purpose. Obviously this value must be keep in
secret. If the property is left empty, no encryption is applied on the database. Some examples of use:

```
database.encryption.key=mypassword
```

#### JWT secret key

This setting configures the encryption token used for the communication between the frontend and the backend. Any value
is fine here meanwhile is kept secret. If `jwt_secret` is left empty, the system will generate a random one on start.
Random is more secure, but any user will be forced to log in into the system again if the server is restarted.

```
jwt.secret=anotherpassword
```

#### Initial user

A user is automatically inserted into the database to allow an admin to login into the application for the first time.
This user will be used to connect from the frontend to the backend endpoints and retrieve the needed data needed by the
application. You can find the script that generates the admin user
[here](kendo-tournament-rest/src/main/resources/database/default-authenticated-users.sql). It is strongly recommended
changing the user credentials on this script before running the software for the first time. The password must be
encrypted using BCrypt algorithm. You can use an online tool like [this](https://bcrypt-generator.com/) for this
purpose.

### Database configuration

The project is based on Hibernate for handling the connections to the database, that means that you can easily switch
between different databases providers. Currently, for development purposes, is configured to use MySQL database (v5.7),
and the configuration can be found on the
file [application.properties](kendo-tournament-rest/src/main/resources/application.properties) file:

```
spring.kendo.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5Dialect
spring.kendo.datasource.platform=mysql
spring.kendo.datasource.jdbc-url=jdbc:mysql://localhost:3306/kendotournament?useSSL=false
spring.kendo.datasource.username=user
spring.kendo.datasource.password=asd123
```

Update the configuration according to your needs. If you want to use a different database provider, remember to include
the corresponding `jar` dependency into the project or copy the `jar` into your server manually.

## Run the project

We use Spring Boot as a framework for developing the backend. For running the project, first access to the
folder `backend/kendo-tournament-rest/target` after compiling and execute `mvn spring-boot:run`

### Check API Endpoints

This project is using OpenAPI Documentation. You can check all available endpoints
on `http://localhost:8080/kendo-tournament-backend/` or using your server domain instead of localhost if is in
production. If you can see the endpoints description here, probably the backend is working correctly.

Note that the endpoints need the Bearer Auth Token to be executed. You can obtain it using the login service
on `auth/public/login` path, and the admin user credentials described previously on this document.

## Running unit tests

In the case you want to run the tests, run `mvn test` on the `backend` folder to execute all tests.

# 3rd party components

Font used on pdf is: ArchitectsDaughter
by [Kimberly Geswein](https://fonts.google.com/specimen/Architects+Daughter/about). 
