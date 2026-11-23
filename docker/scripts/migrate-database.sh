#!/usr/bin/env bash
# Migrate the Kendo Tournament Manager database to the current major versions:
#   PostgreSQL 14 -> 19.0
#   MySQL       8.4 -> 9.7 LTS (8.0 is staged through 8.4 automatically)
#
# The script stops the application, dumps the old database, keeps a physical
# backup of the old volume, recreates the volume with the new engine version,
# restores the dump and verifies the number of tables before finishing.
# See docker/README.md ("Database major-version upgrade") for rollback steps.

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

COMPOSE_FILE="${COMPOSE_FILE:-${REPO_ROOT}/docker/docker-compose.yml}"
ENV_FILE="${ENV_FILE:-${REPO_ROOT}/docker/.env}"
BACKUP_DIR="${BACKUP_DIR:-${REPO_ROOT}/.db-backups}"
SOURCE_IMAGE=""
SOURCE_VOLUME=""
ASSUME_YES=0

DB_SERVICE="kendo-tournament-database"
DB_CONTAINER="kendo-tournament-database" # container_name in every compose file
APP_SERVICES=("kendo-tournament-backend" "kendo-tournament-frontend" "kendo-tournament-rproxy")
PG_TARGET_IMAGE="postgres:19.0"
MYSQL_TARGET_IMAGE="mysql:9.7.4"
MYSQL_STAGING_IMAGE="mysql:8.4.13"
MIGRATE_CONTAINER="kendo-db-migrate-source"
STAGING_CONTAINER="kendo-db-migrate-84"
STAGING_VOLUME="kendo-db-migrate-mysql84"
MIGRATE_NETWORK="kendo-db-migrate"

log() { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
info() { printf '    %s\n' "$*"; }
die() { printf '\nERROR: %s\n' "$*" >&2; exit 1; }

usage() {
  cat <<EOF
Usage: $0 [options]

Options:
  --compose-file FILE   Compose file of the deployment (default: docker/docker-compose.yml)
  --env-file FILE       Environment file of the deployment (default: docker/.env)
  --backup-dir DIR      Directory for dumps and volume backups (default: .db-backups)
  --source-image IMAGE  Old database image (autodetected from the running container)
  --source-volume NAME  Old database volume (autodetected when only one matches)
  --yes                 Do not ask for confirmation
  -h, --help            Show this help

The Compose file must already reference the target image (postgres:19.0 or
mysql:9.7.4). The application is stopped during the migration and must be
started again with 'docker compose up -d' afterwards.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --compose-file) COMPOSE_FILE="$2"; shift 2 ;;
    --env-file) ENV_FILE="$2"; shift 2 ;;
    --backup-dir) BACKUP_DIR="$2"; shift 2 ;;
    --source-image) SOURCE_IMAGE="$2"; shift 2 ;;
    --source-volume) SOURCE_VOLUME="$2"; shift 2 ;;
    --yes) ASSUME_YES=1; shift ;;
    -h|--help) usage; exit 0 ;;
    *) die "Unknown option: $1" ;;
  esac
done

read_env() {
  local key="$1"
  awk -v key="$key" '
    /^[[:space:]]*#/ || /^[[:space:]]*$/ { next }
    index($0, "=") {
      name = substr($0, 1, index($0, "=") - 1)
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", name)
      if (name == key) {
        value = substr($0, index($0, "=") + 1)
        gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
        print value
        exit
      }
    }
  ' "$ENV_FILE"
}

confirm() {
  [[ "$ASSUME_YES" -eq 1 ]] && return 0
  local answer
  read -r -p "$* [y/N] " answer
  [[ "$answer" =~ ^[Yy]$ ]] || die "Migration cancelled."
}

SANITIZED_ENV=""
cleanup() {
  [[ -n "$SANITIZED_ENV" && -f "$SANITIZED_ENV" ]] && rm -f "$SANITIZED_ENV" || true
  docker rm -f "$MIGRATE_CONTAINER" >/dev/null 2>&1 || true
  docker rm -f "$STAGING_CONTAINER" >/dev/null 2>&1 || true
  if docker network inspect "$MIGRATE_NETWORK" >/dev/null 2>&1; then
    docker network rm "$MIGRATE_NETWORK" >/dev/null 2>&1 || true
  fi
}

on_error() {
  local code=$?
  printf '\nERROR: migration failed (exit %s).\n' "$code" >&2
  cat >&2 <<EOF

Nothing has been deleted from the backups in: ${BACKUP_DIR}
To roll back:
  1. Restore the old compose file (git checkout -- $(basename "$COMPOSE_FILE")).
  2. Remove the recreated volume and recreate it from the *.volume.tar.gz backup:
       docker volume rm <volume>
       docker volume create <volume>
       docker run --rm -v <volume>:/volume -v ${BACKUP_DIR}:/backup alpine:3.22 \\
         tar xzf /backup/<backup-prefix>.volume.tar.gz -C /volume
  3. Start the stack again: docker compose -f ${COMPOSE_FILE} up -d
EOF
  cleanup
  exit "$code"
}
trap on_error ERR
trap cleanup EXIT

# ---------------------------------------------------------------- preflight

[[ -f "$COMPOSE_FILE" ]] || die "Compose file not found: $COMPOSE_FILE"
[[ -f "$ENV_FILE" ]] || die "Environment file not found: $ENV_FILE"
command -v docker >/dev/null 2>&1 || die "docker is required"
docker compose version >/dev/null 2>&1 || die "docker compose v2 is required"

mkdir -p "$BACKUP_DIR"
chmod 700 "$BACKUP_DIR"
SANITIZED_ENV="$(mktemp "${BACKUP_DIR}/compose-env.XXXXXX")"
cp "$ENV_FILE" "$SANITIZED_ENV"
# Compose refuses to parse some stacks when optional secrets are blank. They are
# only placeholders for stop/start of the database service; the app is not
# restarted by this script.
for key in TRAEFIK_BASIC_AUTH_USERS jwt_secret database_encryption_key; do
  if [[ -z "$(read_env "$key")" ]]; then
    if grep -q "^${key}=" "$SANITIZED_ENV"; then
      sed -i "s|^${key}=.*|${key}=migration-placeholder|" "$SANITIZED_ENV"
    else
      printf '%s=migration-placeholder\n' "$key" >> "$SANITIZED_ENV"
    fi
  fi
done
chmod 600 "$SANITIZED_ENV"

compose() { docker compose --env-file "$SANITIZED_ENV" -f "$COMPOSE_FILE" "$@"; }

DB_TYPE="$(read_env database_type || true)"
if [[ -z "$DB_TYPE" ]]; then
  if grep -qE 'image:[[:space:]]*["'"'"']?mysql:' "$COMPOSE_FILE"; then
    DB_TYPE="mysql"
  elif grep -qE 'image:[[:space:]]*["'"'"']?postgres' "$COMPOSE_FILE"; then
    DB_TYPE="postgresql"
  else
    die "Cannot detect the database engine from $COMPOSE_FILE"
  fi
fi
case "$DB_TYPE" in
  postgres|postgresql) ENGINE="postgresql" ;;
  mysql) ENGINE="mysql" ;;
  *) die "Unsupported database_type: $DB_TYPE" ;;
esac

TARGET_IMAGE="$(grep -E '^[[:space:]]+image:[[:space:]]*["'"'"']?(postgres|mysql):' "$COMPOSE_FILE" \
  | head -1 | sed -E 's/.*image:[[:space:]]*//; s/["'"'"']//g')"
EXPECTED_IMAGE="$([[ "$ENGINE" == "postgresql" ]] && echo "$PG_TARGET_IMAGE" || echo "$MYSQL_TARGET_IMAGE")"
[[ "$TARGET_IMAGE" == "$EXPECTED_IMAGE" ]] || die "$COMPOSE_FILE must use ${EXPECTED_IMAGE} (found: ${TARGET_IMAGE:-none}). Update the compose file first."

DB_USER="$(read_env database_user)"
DB_NAME="$(read_env database_name)"
DB_PASSWORD="$(read_env database_password)"
[[ -n "$DB_USER" ]] || die "database_user is empty in $ENV_FILE"
[[ -n "$DB_NAME" ]] || die "database_name is empty in $ENV_FILE"
[[ -n "$DB_PASSWORD" ]] || die "database_password is empty in $ENV_FILE"

if [[ -z "$SOURCE_VOLUME" ]]; then
  mapfile -t FOUND_VOLUMES < <(docker volume ls -q --filter "name=kendo-tournament-database")
  [[ ${#FOUND_VOLUMES[@]} -eq 1 ]] || die "Expected exactly one kendo-tournament-database volume; pass --source-volume (found ${#FOUND_VOLUMES[@]})."
  SOURCE_VOLUME="${FOUND_VOLUMES[0]}"
fi
docker volume inspect "$SOURCE_VOLUME" >/dev/null 2>&1 || die "Volume not found: $SOURCE_VOLUME"

if [[ -z "$SOURCE_IMAGE" ]]; then
  EXISTING_IMAGE="$(docker inspect -f '{{.Config.Image}}' "$DB_CONTAINER" 2>/dev/null || true)"
  if [[ -n "$EXISTING_IMAGE" && "$EXISTING_IMAGE" != "$TARGET_IMAGE" ]]; then
    SOURCE_IMAGE="$EXISTING_IMAGE"
  else
    SOURCE_IMAGE="$([[ "$ENGINE" == "postgresql" ]] && echo 'postgres:14' || echo 'mysql:8.4.6')"
  fi
fi

STAMP="$(date +%Y%m%d-%H%M%S)"
PREFIX="${ENGINE}-${STAMP}"
DUMP_FILE="${BACKUP_DIR}/${PREFIX}.sql"
VOLUME_BACKUP="${BACKUP_DIR}/${PREFIX}.volume.tar.gz"

docker image inspect "$SOURCE_IMAGE" >/dev/null 2>&1 || docker pull "$SOURCE_IMAGE"
docker image inspect alpine:3.22 >/dev/null 2>&1 || docker pull alpine:3.22

SERVICES="$(compose config --services)" || die "Could not parse $COMPOSE_FILE (check $ENV_FILE)"
has_service() { printf '%s\n' "$SERVICES" | grep -qx "$1"; }

log "Migration plan"
info "Engine:        $ENGINE"
info "Source image:  $SOURCE_IMAGE"
info "Target image:  $TARGET_IMAGE"
info "Volume:        $SOURCE_VOLUME"
info "Dump:          $DUMP_FILE"
info "Volume backup: $VOLUME_BACKUP"
confirm "The application will be stopped and the volume recreated. Continue?"

# ---------------------------------------------------------------- stop stack

log "Stopping application services"
for service in "${APP_SERVICES[@]}"; do
  if has_service "$service"; then compose stop "$service" >/dev/null 2>&1 || true; fi
done
if has_service "$DB_SERVICE"; then
  compose stop "$DB_SERVICE" >/dev/null 2>&1 || true
  compose rm -f -s "$DB_SERVICE" >/dev/null 2>&1 || true
fi
docker rm -f "$MIGRATE_CONTAINER" >/dev/null 2>&1 || true
docker network inspect "$MIGRATE_NETWORK" >/dev/null 2>&1 || docker network create "$MIGRATE_NETWORK" >/dev/null

# ------------------------------------------------------------ source database

log "Starting old database ($SOURCE_IMAGE) from volume $SOURCE_VOLUME"
if [[ "$ENGINE" == "postgresql" ]]; then
  docker run -d --name "$MIGRATE_CONTAINER" --network "$MIGRATE_NETWORK" \
    -e POSTGRES_USER="$DB_USER" -e POSTGRES_PASSWORD="$DB_PASSWORD" -e POSTGRES_DB="$DB_NAME" \
    -v "${SOURCE_VOLUME}:/var/lib/postgresql/data" "$SOURCE_IMAGE" >/dev/null
  for _ in $(seq 1 60); do
    docker exec -e PGPASSWORD="$DB_PASSWORD" "$MIGRATE_CONTAINER" \
      pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1 && break
    sleep 2
  done
  docker exec -e PGPASSWORD="$DB_PASSWORD" "$MIGRATE_CONTAINER" \
    pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null || die "Old PostgreSQL did not become ready"
  SOURCE_TABLES="$(docker exec -e PGPASSWORD="$DB_PASSWORD" "$MIGRATE_CONTAINER" \
    psql -U "$DB_USER" -d "$DB_NAME" -Atc \
    "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public'")"
else
  docker run -d --name "$MIGRATE_CONTAINER" --network "$MIGRATE_NETWORK" \
    -e MYSQL_ROOT_PASSWORD="$DB_PASSWORD" -e MYSQL_DATABASE="$DB_NAME" \
    -e MYSQL_USER="$DB_USER" -e MYSQL_PASSWORD="$DB_PASSWORD" \
    -v "${SOURCE_VOLUME}:/var/lib/mysql" "$SOURCE_IMAGE" >/dev/null
  for _ in $(seq 1 90); do
    docker exec -e MYSQL_PWD="$DB_PASSWORD" "$MIGRATE_CONTAINER" \
      mysql -u root -N -B -e 'SELECT 1' >/dev/null 2>&1 && break
    sleep 2
  done
  docker exec -e MYSQL_PWD="$DB_PASSWORD" "$MIGRATE_CONTAINER" \
    mysql -u root -N -B -e 'SELECT 1' >/dev/null || die "Old MySQL did not become ready"
  SOURCE_VERSION="$(docker exec -e MYSQL_PWD="$DB_PASSWORD" "$MIGRATE_CONTAINER" \
    mysql -u root -N -B -e 'SELECT VERSION()' | head -1)"
  SOURCE_TABLES="$(docker exec -e MYSQL_PWD="$DB_PASSWORD" "$MIGRATE_CONTAINER" \
    mysql -u root -N -B -e \
    "SELECT count(*) FROM information_schema.tables WHERE table_schema = '${DB_NAME}'")"
fi
info "Source tables in ${DB_NAME}: ${SOURCE_TABLES}"

# ------------------------------------------------------------------- backups

log "Dumping database (logical backup)"
if [[ "$ENGINE" == "postgresql" ]]; then
  # Use the target version of pg_dumpall: newer dump tools can read older servers.
  docker run --rm --network "$MIGRATE_NETWORK" -e PGPASSWORD="$DB_PASSWORD" "$TARGET_IMAGE" \
    pg_dumpall -h "$MIGRATE_CONTAINER" -U "$DB_USER" > "$DUMP_FILE"
  grep -q 'PostgreSQL database cluster dump complete' "$DUMP_FILE" \
    || die "Dump looks incomplete: $DUMP_FILE"
else
  docker exec -e MYSQL_PWD="$DB_PASSWORD" "$MIGRATE_CONTAINER" \
    mysqldump --user="$DB_USER" --databases "$DB_NAME" --single-transaction \
    --set-gtid-purged=OFF --no-tablespaces > "$DUMP_FILE"
  grep -q 'CREATE DATABASE' "$DUMP_FILE" || die "Dump looks incomplete: $DUMP_FILE"
fi
[[ -s "$DUMP_FILE" ]] || die "Empty dump: $DUMP_FILE"
chmod 600 "$DUMP_FILE"
info "Logical backup: $DUMP_FILE ($(du -h "$DUMP_FILE" | cut -f1))"

log "Backing up the old volume (physical backup)"
docker run --rm -v "${SOURCE_VOLUME}:/volume:ro" -v "${BACKUP_DIR}:/backup" alpine:3.22 \
  tar czf "/backup/${PREFIX}.volume.tar.gz" -C /volume .
[[ -s "$VOLUME_BACKUP" ]] || die "Empty volume backup: $VOLUME_BACKUP"

# MySQL 8.0 cannot jump directly to 9.7 LTS; stage through 8.4 first.
if [[ "$ENGINE" == "mysql" && "$SOURCE_VERSION" == 8.0.* ]]; then
  log "MySQL 8.0 detected: staging through ${MYSQL_STAGING_IMAGE}"
  docker image inspect "$MYSQL_STAGING_IMAGE" >/dev/null 2>&1 || docker pull "$MYSQL_STAGING_IMAGE"
  docker volume rm -f "$STAGING_VOLUME" >/dev/null 2>&1 || true
  docker volume create "$STAGING_VOLUME" >/dev/null
  docker run -d --name "$STAGING_CONTAINER" --network "$MIGRATE_NETWORK" \
    -e MYSQL_ROOT_PASSWORD="$DB_PASSWORD" -e MYSQL_DATABASE="$DB_NAME" \
    -e MYSQL_USER="$DB_USER" -e MYSQL_PASSWORD="$DB_PASSWORD" \
    -v "${STAGING_VOLUME}:/var/lib/mysql" "$MYSQL_STAGING_IMAGE" >/dev/null
  for _ in $(seq 1 90); do
    docker exec -e MYSQL_PWD="$DB_PASSWORD" "$STAGING_CONTAINER" \
      mysql -u root -N -B -e 'SELECT 1' >/dev/null 2>&1 && break
    sleep 2
  done
  docker exec -e MYSQL_PWD="$DB_PASSWORD" "$STAGING_CONTAINER" \
    mysql -u root -N -B -e 'SELECT 1' >/dev/null || die "Staging MySQL 8.4 did not become ready"
  docker exec -i -e MYSQL_PWD="$DB_PASSWORD" "$STAGING_CONTAINER" mysql -u root < "$DUMP_FILE"
  docker exec -e MYSQL_PWD="$DB_PASSWORD" "$MIGRATE_CONTAINER" \
    mysql -u root -N -B -e 'SELECT 1' >/dev/null
  mv "$DUMP_FILE" "${DUMP_FILE%.sql}-8.0.sql"
  docker exec -e MYSQL_PWD="$DB_PASSWORD" "$STAGING_CONTAINER" \
    mysqldump --user="$DB_USER" --databases "$DB_NAME" --single-transaction \
    --set-gtid-purged=OFF --no-tablespaces > "$DUMP_FILE"
  chmod 600 "$DUMP_FILE"
  docker rm -f "$STAGING_CONTAINER" >/dev/null
  docker volume rm -f "$STAGING_VOLUME" >/dev/null
fi

# ------------------------------------------------------------- target volume

log "Recreating volume with $TARGET_IMAGE"
docker rm -f "$MIGRATE_CONTAINER" >/dev/null
docker volume rm "$SOURCE_VOLUME" >/dev/null
# Compose creates a fresh volume with the same name when the service starts.
compose up -d --no-deps "$DB_SERVICE" >/dev/null

if [[ "$ENGINE" == "postgresql" ]]; then
  for _ in $(seq 1 60); do
    docker exec -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" \
      pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1 && break
    sleep 2
  done
  docker exec -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" \
    pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null || die "New PostgreSQL did not become ready"
else
  for _ in $(seq 1 90); do
    docker exec -e MYSQL_PWD="$DB_PASSWORD" "$DB_CONTAINER" \
      mysql -u root -N -B -e 'SELECT 1' >/dev/null 2>&1 && break
    sleep 2
  done
  docker exec -e MYSQL_PWD="$DB_PASSWORD" "$DB_CONTAINER" \
    mysql -u root -N -B -e 'SELECT 1' >/dev/null || die "New MySQL did not become ready"
fi

log "Restoring logical backup"
if [[ "$ENGINE" == "postgresql" ]]; then
  # Roles/databases created by the fresh image already exist; those errors are
  # harmless and psql continues with the rest of the dump.
  docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" \
    psql -U "$DB_USER" -d postgres -f - < "$DUMP_FILE" >/dev/null
else
  docker exec -i -e MYSQL_PWD="$DB_PASSWORD" "$DB_CONTAINER" \
    mysql -u root < "$DUMP_FILE"
fi

log "Verifying restore"
if [[ "$ENGINE" == "postgresql" ]]; then
  TARGET_TABLES="$(docker exec -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" \
    psql -U "$DB_USER" -d "$DB_NAME" -Atc \
    "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public'")"
  docker exec -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" \
    vacuumdb -U "$DB_USER" -d "$DB_NAME" --analyze >/dev/null
else
  TARGET_TABLES="$(docker exec -e MYSQL_PWD="$DB_PASSWORD" "$DB_CONTAINER" \
    mysql -u root -N -B -e \
    "SELECT count(*) FROM information_schema.tables WHERE table_schema = '${DB_NAME}'")"
fi
info "Target tables in ${DB_NAME}: ${TARGET_TABLES}"
[[ "$SOURCE_TABLES" == "$TARGET_TABLES" ]] || die "Table count mismatch (${SOURCE_TABLES} -> ${TARGET_TABLES})"

docker exec "$DB_CONTAINER" sh -c 'true' >/dev/null
compose ps "$DB_SERVICE"

log "Migration completed"
cat <<EOF
Database upgraded to ${TARGET_IMAGE}.
Backups kept in ${BACKUP_DIR} (remove them only after verifying the application).
Start the application with:
  docker compose --env-file ${ENV_FILE} -f ${COMPOSE_FILE} up -d
EOF
