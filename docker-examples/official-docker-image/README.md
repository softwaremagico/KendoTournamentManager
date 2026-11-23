Generate unique values for `jwt_secret`, `database_password`, `database_encryption_key` and
`TRAEFIK_BASIC_AUTH_USERS` (see the comments in `.env`). Secrets are injected into the backend container at runtime;
do not write them into `application.properties` or commit the filled `.env` file. Update the machine domain in
`.env`, `application.properties` and `config.js`.

Subsequently, execute the following command:

```
docker-compose build && docker-compose up -d
```

To access the tool, open a web browser and navigate to:

```
http://localhost:4200
```

An example demonstrating how to configure these files can be
found  [here](https://github.com/softwaremagico/KendoTournamentManager/wiki/Installation-using-docker-hub)
