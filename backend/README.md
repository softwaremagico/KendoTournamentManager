# Backend

This project is based on Java and Maven.

## Compiling the project.

As is based on maven, simply run `mvn clean install` inside the `backend` folder.

## Run project.

We use Spring Boot as a framework for developing the backend. For running the project, first access to the folder `backend/kendo-tournament-rest` and execute `mvn spring-boot:run`

## Check API Endpoints

This project is using Swagger. You can check all available endpoints on `http://localhost:8080/kendo-tournament-backend/` or using your server domain instead of localhost if is in production.

## Running unit tests

Run `mvn test` on the `backend` folder to execute all tests.