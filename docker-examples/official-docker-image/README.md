Generate unique values for `jwt_secret`, `database_password`, `database_encryption_key` and
`TRAEFIK_BASIC_AUTH_USERS` (see the comments in `.env`). Secrets are injected into the backend container at runtime;
do not write them into `application.properties` or commit the filled `.env` file. Update the machine domain in
`.env`, `application.properties` and `config.js`.

These images use the `latest` published tags, which may be older than the
current source tree. Rebuilding the local Compose configuration does not rebuild
the published images; a new image must be published to include later fixes.

Subsequently, execute the following command:

```
docker compose up -d
```

After configuring a domain and TLS for Traefik, open the configured domain:

```
https://<machine-domain>/
```

The frontend container's port 4200 is internal; it is not published as
`localhost:4200` by this Compose file. An example demonstrating how to configure these files can be
found  [here](https://github.com/softwaremagico/KendoTournamentManager/wiki/Installation-using-docker-hub)
