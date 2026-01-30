# Frontend

This project has been created using [Angular CLI](https://github.com/angular/angular-cli) version 15.2.9.

## Configure

In the [environment folder](src/environments), there are various files designated for storing configuration variables related to the server. 
Since it caters to multiple environments, you can adjust the necessary settings in files such as `environment.prod.ts`.

If required, ensure to specify the backend server URL. It's advisable to update this URL if your frontend and backend are operating on different hosts.

```
backendUrl: "http://localhost:8080/kendo-tournament-backend"
```

## Development server

To launch a development server, run `ng serve`. Access it through `http://localhost:4200/`. Any changes made to the source files will trigger an automatic reload of the application.

For a production server, execute `ng serve --configuration=production`.

## Build

Utilize `ng build` to compile the project (or `ng build --configuration=production`). 
The resulting artifacts will be saved in the `dist/` directory.

## Running unit tests

Execute `ng test` to perform unit tests using [Karma](https://karma-runner.github.io).

# 3rd party components

