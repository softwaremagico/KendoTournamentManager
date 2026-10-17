#!/usr/bin/env bash
set -euo pipefail

module_directory="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
backend_directory="$(cd "${module_directory}/.." && pwd)"
workspace_directory="$(cd "${backend_directory}/.." && pwd)"
project_name="kendo-tournament-e2e-$RANDOM"
e2e_username="${E2E_USERNAME:-e2e-admin@test.local}"
e2e_password="${E2E_PASSWORD:-E2e-password-123}"
e2e_tests="${E2E_TESTS:-}"
e2e_enable_tenancy="${E2E_ENABLE_TENANCY:-true}"
e2e_headless="${E2E_HEADLESS:-true}"

stop_previous_environments() {
  local previous_project
  while IFS= read -r previous_project; do
    [[ -z "${previous_project}" ]] && continue
    docker compose --project-name "${previous_project}" --file "${module_directory}/docker-compose.e2e.yml" \
      down --volumes --remove-orphans
  done < <(docker compose ls --all --format json | jq -r '.[].Name' | grep '^kendo-tournament-e2e-' || true)
}

case "${1:-}" in
  --headed)
    e2e_headless=false
    ;;
  --stop)
    stop_previous_environments
    exit 0
    ;;
  '')
    ;;
  *)
    printf 'Usage: %s [--headed|--stop]\n' "${BASH_SOURCE[0]}" >&2
    exit 2
    ;;
esac

cleanup() {
  if [[ "${E2E_KEEP_ENVIRONMENT:-false}" == "true" ]]; then
    return
  fi
  docker compose --project-name "${project_name}" --file "${module_directory}/docker-compose.e2e.yml" down --volumes --remove-orphans
}
trap cleanup EXIT

for port in 14200 18080; do
  if [[ -z "$(ss -ltnH "sport = :${port}")" ]]; then
    continue
  fi
  printf 'Port %s is already in use. Stop the previous E2E environment before starting a new one.\n' "${port}" >&2
  exit 1
done

cd "${backend_directory}"
mvn --projects kendo-tournament-rest --also-make package -DskipTests -Dgpg.skip=true
npm --prefix "${workspace_directory}/frontend" run build

docker compose --project-name "${project_name}" --file "${module_directory}/docker-compose.e2e.yml" up --detach --wait
for attempt in $(seq 1 30); do
  if curl --silent --show-error --fail --max-time 2 "http://localhost:18080/kendo-tournament-backend/info/health-check" >/dev/null \
    && curl --silent --show-error --fail --max-time 2 "http://localhost:14200/" >/dev/null; then
    break
  fi
  if [[ "${attempt}" -eq 30 ]]; then
    docker compose --project-name "${project_name}" --file "${module_directory}/docker-compose.e2e.yml" logs
    exit 1
  fi
  sleep 2
done
maven_arguments=(--projects kendo-tournament-e2e test
  -Dselenium.base-url=http://localhost:14200
  -Dselenium.headless="${e2e_headless}"
  -Dselenium.username="${e2e_username}"
  -Dselenium.password="${e2e_password}"
  -Dgpg.skip=true)
if [[ -n "${e2e_tests}" ]]; then
  maven_arguments+=("-Dtest=${e2e_tests}")
fi
mvn "${maven_arguments[@]}"
