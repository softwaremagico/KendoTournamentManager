# Kendo Tournament Manager NG — Docker Deployment

[![Docker Pulls (backend)](https://img.shields.io/docker/pulls/softwaremagico/kendo-tournament-manager-backend)](https://hub.docker.com/r/softwaremagico/kendo-tournament-manager-backend)
[![Docker Pulls (frontend)](https://img.shields.io/docker/pulls/softwaremagico/kendo-tournament-manager-frontend)](https://hub.docker.com/r/softwaremagico/kendo-tournament-manager-frontend)
[![Docker Pulls (proxy)](https://img.shields.io/docker/pulls/softwaremagico/kendo-tournament-manager-rproxy)](https://hub.docker.com/r/softwaremagico/kendo-tournament-manager-rproxy)

This directory contains everything you need to run **Kendo Tournament Manager NG** as a Docker Compose stack.
The stack comprises three services: a Traefik reverse proxy, the Angular frontend, and the Java Spring Boot backend,
plus a PostgreSQL database volume.

---

## Table of contents

- [Official Docker Hub images](#official-docker-hub-images)
- [Quick start](#quick-start)
  - [Option A — Official images (recommended)](#option-a--official-images-recommended)
  - [Option B — Build from source](#option-b--build-from-source)
- [Directory layout](#directory-layout)
- [Configuration reference](#configuration-reference)
- [Domain setup](#domain-setup)
  - [Using localhost](#using-localhost)
  - [Using a custom domain with HTTPS](#using-a-custom-domain-with-https)
- [Database options](#database-options)
- [Reverse proxy (Traefik)](#reverse-proxy-traefik)
  - [Traefik dashboard](#traefik-dashboard)
  - [Disabling the reverse proxy](#disabling-the-reverse-proxy)
- [Security checklist](#security-checklist)
- [Updating to a new version](#updating-to-a-new-version)
- [Troubleshooting](#troubleshooting)

---

## Official Docker Hub images

Three pre-built images are published on Docker Hub and updated with every release:

| Service | Image | Docker Hub |
|---------|-------|------------|
| Backend | `softwaremagico/kendo-tournament-manager-backend` | [View →](https://hub.docker.com/r/softwaremagico/kendo-tournament-manager-backend) |
| Frontend | `softwaremagico/kendo-tournament-manager-frontend` | [View →](https://hub.docker.com/r/softwaremagico/kendo-tournament-manager-frontend) |
| Reverse proxy | `softwaremagico/kendo-tournament-manager-rproxy` | [View →](https://hub.docker.com/r/softwaremagico/kendo-tournament-manager-rproxy) |

Use a specific version tag (e.g., `v3.2.3`) for production deployments instead of `latest` to ensure
reproducibility.

---

## Quick start

### Option A — Official images (recommended)

The fastest way to get up and running. See the
[`docker-examples/official-docker-image/`](../docker-examples/official-docker-image/) directory for a
self-contained example that pulls all three images from Docker Hub.

1. Copy the example directory to a working location.
2. Edit the `.env` file with your domain, passwords, and email.
3. Edit `application.properties` with your database and JWT settings.
4. Edit `config.js` with your backend URL.
5. Run:

```bash
docker-compose up -d
```

For full details, see the
[wiki — Installing using Docker Hub images](https://github.com/softwaremagico/KendoTournamentManager/wiki/Installation-using-docker-hub).

### Option B — Build from source

Build the images locally from the source code in this repository:

```bash
# From the repository root
cd docker
docker-compose build && docker-compose up -d
```

This builds all three images using the Dockerfiles in `docker/backend/`, `docker/frontend/`, and `docker/traefik/`.
Build arguments are automatically read from the `.env` file.

---

## Directory layout

```
docker/
├── .env                    # Main configuration file — edit this before deploying
├── docker-compose.yml      # Full stack definition (reverse proxy + frontend + backend + database)
├── backend/                # Dockerfile and resources for the backend image
├── frontend/               # Dockerfile and resources for the frontend image
└── traefik/                # Dockerfile and Traefik configuration files
```

The [`docker-examples/`](../docker-examples/) directory at the repository root contains additional ready-to-use
configurations:

| Example | Description |
|---------|-------------|
| `official-docker-image/` | Uses the pre-built Docker Hub images with a custom `application.properties` and `config.js` |
| `localhost-with-reverse-proxy/` | Runs the full stack locally with Traefik (no SSL) |
| `localhost-without-reverse-proxy/` | Runs frontend and backend directly on ports 4200/8080, no Traefik |
| `local-build/` | Builds all images from source for local testing |

---

## Configuration reference

All runtime settings live in the `.env` file. The table below describes every variable:

### General

| Variable | Default | Description |
|----------|---------|-------------|
| `machine_domain` | `localhost` | Domain name (or `localhost`) for the deployment |
| `email` | `myemail@domain.com` | Email used for Let's Encrypt TLS certificate registration |
| `timezone` | `Europe/Madrid` | Server timezone (used in logs and date handling) |
| `version` | `v3.2.3` | Application version tag |
| `release` | `${version}` | Release label (usually the same as `version`) |

### Frontend

| Variable | Default | Description |
|----------|---------|-------------|
| `protocol` | `http` | `http` for localhost; `https` for production with SSL |
| `websocket_protocol` | `http` | `http` or `https` — must match `protocol` |
| `achievements_enabled` | `true` | Enable the achievements/gamification system |
| `check_new_version_enabled` | `true` | Show a notification when a new version is available |

### Backend — Security

| Variable | Default | Description |
|----------|---------|-------------|
| `jwt_secret` | *(empty)* | Secret used to sign JWT tokens. Leave empty to auto-generate a random secret on each startup (more secure, but all sessions expire on restart) |
| `jwt_expiration` | `1200000` | Standard user token lifetime in milliseconds (default: 20 min) |
| `jwt_guest_expiration` | `3600000` | Guest user token lifetime in milliseconds (default: 1 h) |
| `jwt_ip_check` | `false` | Bind token validation to the client IP address |
| `enable_guest_user` | `false` | Allow unauthenticated read-only access via QR codes |
| `database_populate_default_data` | `always` | `always` creates a default admin user on fresh install; `never` requires manual first-login creation |

### Backend — Database

| Variable | Default | Description |
|----------|---------|-------------|
| `database_type` | `postgresql` | Database driver: `postgresql`, `mysql`, `h2`, `hsqldb`, `oracle`, … |
| `database_dialect` | `org.hibernate.dialect.PostgreSQLDialect` | Hibernate dialect class |
| `database_name` | `postgres` | Database / schema name |
| `database_user` | `myuser` | Database username |
| `database_password` | `mypass` | Database password — **change this** |
| `database_port` | `5432` | Database port |
| `database_encryption_key` | *(empty)* | Key for field-level encryption of PII at rest. Leave empty to disable encryption. **Change before production use** |

---

## Domain setup

### Using localhost

Suitable for local testing. SSL certificates are not available for `localhost`, so HTTPS must be disabled.

In `.env`:

```env
machine_domain=localhost
protocol=http
websocket_protocol=http
```

Then follow the instructions in the
[wiki — Using localhost](https://github.com/softwaremagico/KendoTournamentManager/wiki/Installation-using-docker#using-localhost)
to disable TLS in the Traefik configuration.

> **Note:** When running on `localhost`, features that require network access from external devices
> (QR codes for guests, multiple-shiaijo devices) will not work because external devices cannot reach your machine.

### Using a custom domain with HTTPS

For production deployments with a public domain, Traefik automatically obtains and renews a TLS certificate via
**Let's Encrypt**. No manual certificate management is needed.

In `.env`:

```env
machine_domain=mydomain.com
email=myemail@mydomain.com
protocol=https
websocket_protocol=https
```

Make sure port 80 and port 443 are open in your server's firewall and that your DNS A record points to the server's
public IP before starting the stack for the first time.

---

## Database options

The default database is **PostgreSQL 14**, included in the Compose stack as a service named
`kendo-tournament-database`.

To use a different engine, update the `database_type`, `database_dialect`, and `database_port` variables in `.env`
and replace the `kendo-tournament-database` service image in `docker-compose.yml`.

| Engine | `database_type` | `database_dialect` | `database_port` |
|--------|-----------------|-------------------|-----------------|
| PostgreSQL 14 (default) | `postgresql` | `org.hibernate.dialect.PostgreSQLDialect` | `5432` |
| MySQL 8 | `mysql` | `org.hibernate.dialect.MySQL8Dialect` | `3306` |

The backend images ship with JDBC drivers for both PostgreSQL and MySQL. For other engines, you will need to add the
appropriate driver JAR to the image.

### Persistent data

The database data is stored in a named Docker volume (`kendo-tournament-database`) so it persists across
container restarts and upgrades. **Back up this volume** before performing major version upgrades.

---

## Reverse proxy (Traefik)

Since version `2.13.7`, the reverse proxy is **Traefik**. It handles:

- HTTP → HTTPS redirection
- Automatic TLS certificate issuance and renewal via Let's Encrypt
- Routing requests to the correct backend/frontend container
- Basic-auth protection of the Swagger UI and the Traefik dashboard

### Traefik dashboard

The Traefik dashboard is accessible at `https://<your-domain>/dashboard/` and is protected by HTTP Basic Auth.

**Default credentials:** `admin` / `0b186336d5` — **change these immediately.**

To generate a new password hash:

```bash
echo $(htpasswd -nB user) | sed -e s/\\$/\\$\\$/g
```

The `sed` step is required because Docker Compose needs every `$` character doubled to `$$`.

Update `docker-compose.yml` with the output by replacing the line:

```yaml
- "traefik.http.middlewares.auth.basicauth.users=admin:<<put here your hashed password>>"
```

Then rebuild and restart the proxy:

```bash
docker-compose build --no-cache kendo-tournament-rproxy && docker-compose up -d kendo-tournament-rproxy
```

### Disabling the reverse proxy

If you already have a shared reverse proxy (Nginx, Caddy, another Traefik instance, etc.), you can prevent the
built-in proxy from starting by adding the `donotstart` profile in `docker-compose.yml`:

```yaml
kendo-tournament-rproxy:
  # ...
  profiles:
    - donotstart
```

Configure your external proxy to forward:
- `/<any path>/` → `kendo-tournament-frontend:4200`
- `/kendo-tournament-backend/` → `kendo-tournament-backend:8080`

---

## Security checklist

Before going to production, verify the following:

- [ ] `jwt_secret` is set to a strong random value (or left empty for auto-generation)
- [ ] `database_encryption_key` is set to a secret value known only to you
- [ ] `database_password` has been changed from the default `mypass`
- [ ] Traefik dashboard password has been changed from the default
- [ ] `database_populate_default_data=always` has been reviewed — the default admin credentials (`admin@test.com` / `asd123`) must be changed immediately after first login
- [ ] Ports 80 and 443 are the only ports exposed to the internet; database port 5432 is internal only
- [ ] Email is set correctly in `.env` for Let's Encrypt registration

---

## Updating to a new version

1. Pull the latest source code (or download the new release).
2. Update the `version` and `release` variables in `.env`.
3. Rebuild and restart the stack:

```bash
docker-compose build && docker-compose up -d
```

Hibernate's `ddl-auto=update` will automatically apply any required schema changes on startup.

For **breaking schema changes** between specific versions, refer to the
[Updating Versions](https://github.com/softwaremagico/KendoTournamentManager/wiki/Updating-Versions)
wiki page for manual migration scripts.

> **Tip:** Back up the database volume before any upgrade.

---

## Troubleshooting

### "Error connecting to the backend service" after reinstalling containers

Open your browser's developer tools → Application → Local Storage and delete the `jwt` key.
This forces a new login and clears the stale token.

### The backend container keeps restarting

Check the backend logs:

```bash
docker-compose logs kendo-tournament-backend
```

Common causes:
- The database container is not yet healthy when the backend tries to connect. The `depends_on` + `healthcheck` in
  `docker-compose.yml` should handle this, but some systems need a longer `start_period`.
- Incorrect database credentials in `.env`.
- A Java version mismatch (the backend requires Java 17+).

### Let's Encrypt certificate not issued

- Ensure your domain's DNS A record points to the server's public IP.
- Ensure ports 80 and 443 are open in the firewall.
- Check Traefik logs: `docker-compose logs kendo-tournament-rproxy`.
- Let's Encrypt has rate limits; if you have hit them, wait before retrying.

### Frontend cannot connect to the backend (WebSocket errors)

- Verify that `protocol` and `websocket_protocol` are both set correctly in `.env`.
- For HTTPS deployments, ensure `websocket_protocol=https` (not `http`).
- In `docker-examples/official-docker-image/config.js`, make sure `websocketsUrl` uses the correct scheme and domain.


## Domain

### Using localhost

You can deploy the application in your desktop computer for testing it. It is not recommended for a real environment but
is an easy way to check the state of the art of this application and if it fits to your needs or not.

On `docker/.env` set:

```
machine_domain=localhost
[...]
#Frontend
protocol=http
``` 

And deploy the application using `docker-compose` command as usual. Ignore any information about SSL certificates as
they are not available for localhost domain.

### Using a custom domain

By default, I am assuming that you are deploying this application as a docker container on a server on the cloud. That
means that probably you already have a domain that you must set on the application. Please, update `machine_domain`
variable on `docker/.env` to match your existing domain.

```
machine_domain=mydomain.com
[...]
#Frontend
protocol=https
``` 

Parameter `protocol` is assuming that you have also an SSL certificate. If is not the case, please change to `http`.

## Using a different database engine.

If you want to use a different database engine, add the correct jar dependency with the jdbc connector
in `backend/libraries`. Configure these specific variables in the `.env` file:

```
database_type=postgresql  (hsqldb, h2, oracle, mysql, postgresql, ...)
database_name=database
database_password=mypass
database_user=myuser
database_port=5432
```

## Security passwords

Variable `jwt_secret` is used for encrypting JWT token related to the REST API authorization. Please change it and avoid
using the default one. If `jwt_secret` is left empty, the system will generate a random one on start. Random is more
secure, but any user will be forced to log in into the system again if the server is restarted.

Variable `database_encryption_key` will encrypt the database content, to ensure a higher level of privacy. If you want
to check the content of your database using any other external software, please leave this variable with a blank value.
Otherwise, change the default value to any other that only you know.

# Using REVERSE PROXY and SSL certificates generation

Since version 2.13.7, the reverse proxy engine has been changed from Nginx to Traefik. Traefik is more automated than
Nginx, and it will handle all _Let's encrypt_ certificate generation automatically.

Remember to change `email` value on the `docker/.env` file as it is used for the certificates' registration:

```
email=myemail@domain.com
```

Regenerate again the proxy if needed with the next command:

```
docker-compose build --no-cache kendo-tournament-rproxy && docker-compose up -d kendo-tournament-rproxy
```

## Traefik Dashboard

Traefik comes with a dashboard where information about the services is shown. Can be interesting for troubleshooting,
specially if you want to customize any setting. By default, is protected by user `admin` and password `0b186336d5`. I
recommend to change it as soon as possible. For this purpose, you can use `htpasswd` tool:

```
echo $(htpasswd -nB user) | sed -e s/\\$/\\$\\$/g
```

The `sed` command is due to the docker-compose needs to scape any `$` character to `$$`. Ensure that is the case in your
output.

Finally, update the `docker-compose.yml` file with the obtained password, replacing the next line with the content
obtained from the value obtained on the previous step:

```
  - "traefik.http.middlewares.auth.basicauth.users=admin:<<put here your hashed password>>"
```

Regenerate the docker container with a `docker-compose build --no-cache kendo-tournament-rproxy && docker-compose up -d kendo-tournament-rproxy` and the new password will be used. 

## Disable the REVERSE PROXY

If you have your custom reverse proxy that is shared between other applications, maybe you need to disable the provided
one with this application, as some ports will collide. The best approach would be to force to not start the container in
the `docker-compose.yml` file. Search for the `kendo-tournament-rproxy` section and set the `donotstart` profile. You
can uncomment the lines that are already prepared for this purpose:

```
  kendo-tournament-rproxy:
      [...]
      # profiles:
      #      - donotstart
```

Uncomment to

```
  kendo-tournament-rproxy:
      [...]
       profiles:
            - donotstart
```

And the container will not be started automatically. Remember to configure your reverse proxy to include the `backend`
and `frontend` of this application or will not work properly. 
