Update the passwords on the `.env` file and replace the content of the `docker` folder with these files. After it, execute:

```
docker compose build && docker compose up -d
```

For accessing to the tool, please open a browser and access to:

```
http://localhost:4200
```

`machine_domain` must be `localhost:8080` so that the generated QR codes and the frontend backend URL point to the
published backend port. Since the frontend (`:4200`) and the backend (`:8080`) are different origins, the Compose file
sets `SERVER_CORS_DOMAINS=http://localhost:4200` on the backend for REST and WebSocket requests.
