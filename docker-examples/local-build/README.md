This folder contains a script (localdockerbuild.sh) that will build locally the current code and docker images.
You can customise the `.env` file
You need nodejs 22 installed, angular 22 & maven in order to build locally frontend & backend.
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
