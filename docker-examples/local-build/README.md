This folder contains a script (`localdockerbuild.sh`) that will build the current code and Docker images:

```bash
bash docker-examples/local-build/localdockerbuild.sh
```

The command above runs from the repository root; the script can also be run from its own directory.
You can customise the `.env` file
You need a Node.js version supported by the project's Angular CLI, npm, Maven, Java 25 and Docker Compose.
The script installs the project's local Angular CLI; a global `ng` installation is not required.
This is a totally unsafe way to run the tournament, should be used only for testing purposes.

After it, execute:

```
docker-compose up -d
```

For accessing to the tool, please open a browser and access to:

```
http://localhost:4200
```

The frontend on port 4200 calls the backend on `http://localhost:8080/kendo-tournament-backend`, which is a different
origin. The Compose file already sets `SERVER_CORS_DOMAINS=http://localhost:4200` on the backend for REST and WebSocket
requests; update it if you change the frontend port.
