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

Use a specific version tag (e.g., `v3.6.0`) for production deployments instead of `latest` to ensure
reproducibility.

---

## Quick start

### Option A — Official images (recommended)

The fastest way to get up and running. See the
[`docker-examples/official-docker-image/`](../docker-examples/official-docker-image/) directory for a
self-contained example that pulls all three images from Docker Hub.

1. Copy the example directory to a working location.
2. Generate secrets (`openssl rand -hex 32` for each secret, `htpasswd -nB -C 12 admin` for the proxy) and put them in `.env`.
3. Edit `.env` with your domain and email, and `application.properties` / `config.js` with non-secret settings.
4. Never commit the filled `.env`; the stack refuses to start while required secrets are empty.
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
| `version` | `v3.6.0` | Application version tag |
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
| `jwt_secret` | *(required)* | JWT signing secret. Generate with `openssl rand -hex 32`. There is no default; startup fails if it is empty |
| `jwt_expiration` | `1200000` | Standard user token lifetime in milliseconds (default: 20 min) |
| `jwt_guest_expiration` | `3600000` | Guest user token lifetime in milliseconds (default: 1 h) |
| `jwt_ip_check` | `false` | Bind token validation to the verified remote address. Enabling it invalidates tokens issued with a different IP source |
| `SERVER_FORWARD_HEADERS_STRATEGY` | `none` | Forwarded-header handling. Use `framework` only when the proxy overwrites `X-Forwarded-*`; changing it invalidates active tokens when `jwt_ip_check=true` |
| `enable_guest_user` | `false` | Allow unauthenticated read-only access via QR codes |
| `enable_tenancy` | `true` | Enable multi-organization support. Set to `false` so the whole installation uses a single organization (the `Legacy organization`): the login selector disappears, the tenant administration menu is hidden and all tenant management API endpoints return `404`. |
| `server_cors_domains` | *(empty)* | Exact frontend origin(s) allowed for CORS/WebSockets. Empty means same-origin only |
| `database_populate_default_data` | `always` | Controls SQL data initialization. The platform admin is created only from configured `bootstrap.super-admin.*` credentials, never by the first anonymous login |

### Backend — Database

| Variable | Default | Description |
|----------|---------|-------------|
| `database_type` | `postgresql` | Database driver: `postgresql`, `mysql`, `h2`, `hsqldb`, `oracle`, … |
| `database_name` | `postgres` | Database / schema name |
| `database_user` | `myuser` | Database username |
| `database_password` | *(required)* | Database password. Generate with `openssl rand -hex 32`; startup fails if empty |
| `database_port` | `5432` | Database port |
| `database_encryption_key` | *(required)* | Master key for field-level encryption of PII at rest. Generate with `openssl rand -hex 32` and keep it stable across restarts |

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

To use a different engine, update the `database_type`, and `database_port` variables in `.env`
and replace the `kendo-tournament-database` service image in `docker-compose.yml`.

| Engine | `database_type` | `database_port` |
|--------|-----------------|-----------------|
| PostgreSQL 14 (default) | `postgresql` | `5432` |
| MySQL 8 | `mysql` | `3306` |

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

The Traefik dashboard and Swagger UI are accessible behind HTTP Basic Auth. There are **no default credentials**:
the stack refuses to start until you generate your own hash and set `TRAEFIK_BASIC_AUTH_USERS` in `.env`.

```bash
htpasswd -nB admin
# Copy the whole "admin:$2y$..." output into .env as TRAEFIK_BASIC_AUTH_USERS=...
```

Use a high cost factor (`-C 12` or higher) and a unique password. To rotate the credential, update `.env` and restart:

```bash
docker-compose up -d kendo-tournament-rproxy
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

- [ ] `jwt_secret` is set to a strong random value (`openssl rand -hex 32`)
- [ ] `database_encryption_key` is set to a long random secret known only to you and is backed up
- [ ] `database_password` is a strong random value, not a default from an example
- [ ] `TRAEFIK_BASIC_AUTH_USERS` contains a freshly generated htpasswd hash
- [ ] `bootstrap.super-admin.*` credentials were used only for initial setup and then removed
- [ ] No secrets were passed as Docker build arguments or committed in `.env`/image layers
- [ ] Ports 80 and 443 are the only ports exposed to the internet; the database is not published on the host
- [ ] `SERVER_FORWARD_HEADERS_STRATEGY` is explicitly set in `.env` for this environment (`none` unless the reverse proxy sanitises `X-Forwarded-*`)
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

If this update changes `SERVER_FORWARD_HEADERS_STRATEGY` (or moves the backend behind a proxy), tokens issued with the previous IP source are rejected when `jwt.ip.check=true`. Users must log in again; guest users just need to scan their QR code again.

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
- A Java version mismatch (the backend requires Java 25+).

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
database_user=myuser
database_port=5432
# database_password must be generated and kept only in .env
```

## Security passwords

Variable `jwt_secret` signs the JWT tokens used by the REST API. Set it to a long random value
(`openssl rand -hex 32`). The Compose stack requires it to be non-empty; the application can otherwise generate a
random secret on each start, which invalidates all sessions on restart. Never use a short, guessable value.

Variable `database_encryption_key` encrypts sensitive fields (participant names, IDs, etc.) at rest. Set it to a long
random value that only you know and keep it stable across restarts or the stored data will no longer be readable.
Leaving it blank stores those fields unencrypted — only do this for local development.

The backend only accepts cross-origin requests from the exact origins listed in `server_cors_domains` /
`SERVER_CORS_DOMAINS`. Keep it empty when the frontend is served from the same domain by the reverse proxy.

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

Traefik comes with a dashboard where information about the services is shown. It is protected by HTTP Basic Auth, but
there is no committed default password: set `TRAEFIK_BASIC_AUTH_USERS` in `.env` to a hash you generate yourself:

```
htpasswd -nB admin -C 12
# TRAEFIK_BASIC_AUTH_USERS=admin:$2y$12$...
```

Restart the proxy with `docker-compose up -d kendo-tournament-rproxy` after changing it.

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
