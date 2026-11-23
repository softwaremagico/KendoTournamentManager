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
#   APP_USERNAME        bootstrap administrator username (default admin@test.local)
#   APP_PASSWORD        bootstrap administrator password (default Admin-password-123)
#   APP_ENABLE_TENANCY  enable multi-organization support (default true)
#   APP_PROJECT_NAME    Docker Compose project name (default kendo-tournament-app)

set -euo pipefail

module_directory="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
backend_directory="$(cd "${module_directory}/.." && pwd)"
workspace_directory="$(cd "${backend_directory}/.." && pwd)"
compose_file="${module_directory}/docker-compose.app.yml"
project_name="${APP_PROJECT_NAME:-kendo-tournament-app}"
frontend_port="${APP_FRONTEND_PORT:-14200}"
backend_port="${APP_BACKEND_PORT:-18080}"
app_username="${APP_USERNAME:-admin@test.local}"
app_password="${APP_PASSWORD:-Admin-password-123}"
app_enable_tenancy="${APP_ENABLE_TENANCY:-true}"
build_images=true

compose() {
  docker compose --project-name "${project_name}" --file "${compose_file}" "$@"
}

usage() {
  sed -n '2,20p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
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

printf 'Starting the application containers...\n'
compose up --detach --wait

# Compose already waits for the declared health checks; these loops give a
# clearer message if a service becomes unhealthy after startup.
for attempt in $(seq 1 120); do
  if curl --silent --show-error --fail --max-time 5 \
    "http://localhost:${backend_port}/kendo-tournament-backend/info/health-check" >/dev/null 2>&1; then
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

cat <<EOF

The application is ready.

  Frontend:  http://localhost:${frontend_port}
  API:       http://localhost:${backend_port}/kendo-tournament-backend
  Swagger:   http://localhost:${backend_port}/kendo-tournament-backend/swagger-ui/index.html

  Username:  ${app_username}
  Password:  ${app_password}

The database is stored in the "${project_name}" Compose volume and survives
restarts.

  Follow logs:  ${BASH_SOURCE[0]} --logs
  Stop:         ${BASH_SOURCE[0]} --stop
  Reset data:   ${BASH_SOURCE[0]} --reset
EOF
