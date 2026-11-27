#!/usr/bin/env bash
#
# Build and start a local Docker environment so the application can be used
# manually. It does not run any automated tests.
#
# Usage:
#   ./run-app.sh                 build, start and wait for the application
#   ./run-app.sh --no-build      start using the existing JAR and frontend bundle
#   ./run-app.sh --logs          follow the container logs
#   ./run-app.sh --stop          stop the containers, keeping the database volume
#   ./run-app.sh --reset         stop the containers and delete the database volume
#   ./run-app.sh --help          show this help
#
# Environment variables:
#   APP_FRONTEND_PORT   host port for the UI (default 14200)
#   APP_BACKEND_PORT    host port for the API (default 18080)
#   APP_USERNAME        bootstrap administrator username (default admin@test.com)
#   APP_PASSWORD        bootstrap administrator password (default asd123)
#   APP_ENABLE_TENANCY  enable multi-organization support (default true)
#   APP_PROJECT_NAME    Docker Compose project name (default kendo-tournament-app)
#   APP_SEED_DATA       import demo CSVs: if-empty (default), always, never
#   APP_SEED_CLUBS_CSV  clubs CSV path (default core test resource)
#   APP_SEED_PARTICIPANTS_CSV  participants CSV path (default core test resource)

set -euo pipefail

module_directory="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
backend_directory="$(cd "${module_directory}/.." && pwd)"
workspace_directory="$(cd "${backend_directory}/.." && pwd)"
compose_file="${module_directory}/docker-compose.app.yml"
project_name="${APP_PROJECT_NAME:-kendo-tournament-app}"
frontend_port="${APP_FRONTEND_PORT:-14200}"
backend_port="${APP_BACKEND_PORT:-18080}"
app_username="${APP_USERNAME:-admin@test.com}"
app_password="${APP_PASSWORD:-asd123}"
app_enable_tenancy="${APP_ENABLE_TENANCY:-true}"
seed_mode="${APP_SEED_DATA:-if-empty}"
seed_mode="${seed_mode,,}"
seed_clubs_csv="${APP_SEED_CLUBS_CSV:-${backend_directory}/kendo-tournament-core/src/test/resources/csv/clubs.csv}"
seed_participants_csv="${APP_SEED_PARTICIPANTS_CSV:-${backend_directory}/kendo-tournament-core/src/test/resources/csv/participants.csv}"
build_images=true
api_base_url=""
auth_header=""
seed_files_available=true
temporary_files=()

cleanup() {
  if (( ${#temporary_files[@]} > 0 )); then
    rm -f "${temporary_files[@]}"
  fi
}
trap cleanup EXIT

seed_warning() {
  printf 'WARNING: %s\n' "$1" >&2
}

compose() {
  docker compose --project-name "${project_name}" --file "${compose_file}" "$@"
}

usage() {
  sed -n '2,23p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
}

new_temp_file() {
  mktemp
}

json_escape() {
  local value="$1"
  value="${value//\\/\\\\}"
  value="${value//\"/\\\"}"
  printf '%s' "${value}"
}

authenticate() {
  local body_file headers_file status escaped_username escaped_password token

  if ! command -v mktemp >/dev/null 2>&1; then
    seed_warning "mktemp is not available; demo data will not be imported."
    return 1
  fi

  body_file="$(new_temp_file)"
  temporary_files+=("${body_file}")
  headers_file="$(new_temp_file)"
  temporary_files+=("${headers_file}")

  escaped_username="$(json_escape "${app_username}")"
  escaped_password="$(json_escape "${app_password}")"

  if ! status="$(curl --silent --max-time 30 \
    -o "${body_file}" -D "${headers_file}" -w '%{http_code}' \
    -X POST "${api_base_url}/auth/public/login" \
    -H 'Content-Type: application/json' \
    --data "{\"username\":\"${escaped_username}\",\"password\":\"${escaped_password}\"}" 2>/dev/null)"; then
    seed_warning "Could not reach the login endpoint; demo data will not be imported."
    return 1
  fi

  if [[ "${status}" != "200" ]]; then
    seed_warning "Could not authenticate as ${app_username} (HTTP ${status}); demo data will not be imported."
    return 1
  fi

  token="$(awk 'tolower($1)=="authorization:" { sub(/^[^:]*:[[:space:]]*/, ""); print }' \
    "${headers_file}" | tr -d '\r' | tail -n 1)"
  if [[ -z "${token}" ]]; then
    seed_warning "The login response did not include an Authorization token; demo data will not be imported."
    return 1
  fi
  if [[ "${token,,}" == bearer\ * ]]; then
    auth_header="${token}"
  else
    auth_header="Bearer ${token}"
  fi
}

api_get() {
  local path="$1" body_file status
  body_file="$(new_temp_file)"
  temporary_files+=("${body_file}")

  if ! status="$(curl --silent --max-time 30 \
    -o "${body_file}" -w '%{http_code}' \
    -H "Authorization: ${auth_header}" "${api_base_url}${path}" 2>/dev/null)"; then
    seed_warning "GET ${path} could not be completed; demo data will not be imported."
    return 1
  fi

  if [[ "${status}" != "200" ]]; then
    seed_warning "GET ${path} failed (HTTP ${status}); demo data will not be imported."
    return 1
  fi
  cat "${body_file}"
}

entity_count() {
  local value
  if ! value="$(api_get "$1")"; then
    return 1
  fi
  if [[ ! "${value}" =~ ^[0-9]+$ ]]; then
    seed_warning "Unexpected count from $1: ${value}. Demo data will not be imported."
    return 1
  fi
  printf '%s' "${value}"
}

import_csv() {
  local label="$1" path="$2" file="$3" body_file status
  printf 'Importing %s from %s...\n' "${label}" "${file}"
  body_file="$(new_temp_file)"
  temporary_files+=("${body_file}")

  if ! status="$(curl --silent --max-time 120 \
    -o "${body_file}" -w '%{http_code}' \
    -H "Authorization: ${auth_header}" \
    -F "file=@${file}" "${api_base_url}${path}" 2>/dev/null)"; then
    seed_warning "Could not import ${label}; demo data will not be imported."
    return 1
  fi

  if [[ "${status}" != "200" && "${status}" != "201" && "${status}" != "204" ]]; then
    seed_warning "Could not import ${label} (HTTP ${status}); demo data will not be imported."
    return 1
  fi
}

seed_database() {
  if [[ "${seed_mode}" == "never" || "${seed_files_available}" == "false" ]]; then
    return 0
  fi

  printf 'Seeding demo data (%s)...\n' "${seed_mode}"
  authenticate || return 1

  if [[ "${seed_mode}" == "if-empty" ]]; then
    local clubs_count participants_count
    if ! clubs_count="$(entity_count "/clubs/count")"; then
      return 1
    fi
    if [[ "${clubs_count}" -gt 0 ]]; then
      printf 'Skipping clubs: %s already present.\n' "${clubs_count}"
    else
      import_csv "clubs" "/csv/clubs" "${seed_clubs_csv}" || return 1
    fi

    if ! participants_count="$(entity_count "/participants/count")"; then
      return 1
    fi
    if [[ "${participants_count}" -gt 0 ]]; then
      printf 'Skipping participants: %s already present.\n' "${participants_count}"
    else
      import_csv "participants" "/csv/participants" "${seed_participants_csv}" || return 1
    fi
  else
    import_csv "clubs" "/csv/clubs" "${seed_clubs_csv}" || return 1
    import_csv "participants" "/csv/participants" "${seed_participants_csv}" || return 1
  fi

  printf 'Demo data seeding finished.\n'
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-build)
      build_images=false
      shift
      ;;
    --logs)
      compose logs --follow
      exit 0
      ;;
    --stop)
      compose down --remove-orphans
      printf 'Application stopped. The database volume has been kept.\n'
      exit 0
      ;;
    --reset)
      compose down --volumes --remove-orphans
      printf 'Application stopped and the database volume has been deleted.\n'
      exit 0
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      printf 'Unknown option: %s\n\n' "$1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

case "${seed_mode}" in
  if-empty|always|never) ;;
  *)
    printf 'Invalid APP_SEED_DATA=%s. Use if-empty, always or never.\n' "${seed_mode}" >&2
    exit 2
    ;;
esac

if [[ "${seed_mode}" != "never" ]]; then
  for seed_file in "${seed_clubs_csv}" "${seed_participants_csv}"; do
    if [[ ! -f "${seed_file}" ]]; then
      seed_warning "Seed CSV not found: ${seed_file}. Demo data will not be imported."
      seed_files_available=false
    fi
  done
fi

for command in docker curl ss; do
  if ! command -v "${command}" >/dev/null 2>&1; then
    printf 'Required command not found: %s\n' "${command}" >&2
    exit 1
  fi
done

if [[ "${build_images}" == "true" ]]; then
  for command in mvn npm; do
    if ! command -v "${command}" >/dev/null 2>&1; then
      printf 'Required command not found: %s (use --no-build to skip building)\n' "${command}" >&2
      exit 1
    fi
  done
fi

project_running=false
if [[ -n "$(compose ps --status running --quiet 2>/dev/null || true)" ]]; then
  project_running=true
fi

if [[ "${project_running}" == "false" ]]; then
  for port in "${frontend_port}" "${backend_port}"; do
    if [[ -n "$(ss -ltnH "sport = :${port}")" ]]; then
      printf 'Port %s is already in use. Stop the process using it or set APP_FRONTEND_PORT/APP_BACKEND_PORT.\n' "${port}" >&2
      exit 1
    fi
  done
fi

if [[ "${build_images}" == "true" ]]; then
  printf 'Building the backend...\n'
  (cd "${backend_directory}" && mvn --projects kendo-tournament-rest --also-make package -DskipTests -Dgpg.skip=true)

  printf 'Building the frontend...\n'
  npm --prefix "${workspace_directory}/frontend" run build
else
  if [[ ! -f "${backend_directory}/kendo-tournament-rest/target/kendo-tournament-backend.jar" ]]; then
    printf 'Backend JAR not found. Run without --no-build first.\n' >&2
    exit 1
  fi
  if [[ ! -d "${workspace_directory}/frontend/dist/frontend" ]]; then
    printf 'Frontend bundle not found. Run without --no-build first.\n' >&2
    exit 1
  fi
fi

export APP_FRONTEND_PORT="${frontend_port}"
export APP_BACKEND_PORT="${backend_port}"
export APP_USERNAME="${app_username}"
export APP_PASSWORD="${app_password}"
export APP_ENABLE_TENANCY="${app_enable_tenancy}"
api_base_url="http://localhost:${backend_port}/kendo-tournament-backend"

printf 'Starting the application containers...\n'
compose up --detach --wait

# Compose already waits for the declared health checks; these loops give a
# clearer message if a service becomes unhealthy after startup.
for attempt in $(seq 1 120); do
  if curl --silent --show-error --fail --max-time 5 \
    "${api_base_url}/info/health-check" >/dev/null 2>&1; then
    break
  fi
  if [[ "${attempt}" -eq 120 ]]; then
    printf 'The backend did not become ready within 300 seconds.\n' >&2
    compose logs >&2
    exit 1
  fi
  sleep 2
done

for attempt in $(seq 1 120); do
  if curl --silent --show-error --fail --max-time 5 "http://localhost:${frontend_port}/" >/dev/null 2>&1; then
    break
  fi
  if [[ "${attempt}" -eq 120 ]]; then
    printf 'The frontend did not become ready within 300 seconds.\n' >&2
    compose logs >&2
    exit 1
  fi
  sleep 2
done

if ! seed_database; then
  printf 'Continuing without demo data. The application can still be used normally.\n' >&2
fi

cat <<EOF

The application is ready.

  Frontend:  http://localhost:${frontend_port}
  API:       ${api_base_url}
  Swagger:   ${api_base_url}/swagger-ui/index.html

  Username:  ${app_username}
  Password:  ${app_password}

The database is stored in the "${project_name}" Compose volume and survives
restarts.

  Follow logs:  ${BASH_SOURCE[0]} --logs
  Stop:         ${BASH_SOURCE[0]} --stop
  Reset data:   ${BASH_SOURCE[0]} --reset
EOF
