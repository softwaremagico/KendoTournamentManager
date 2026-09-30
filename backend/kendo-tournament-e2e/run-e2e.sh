#!/usr/bin/env bash
set -euo pipefail

module_directory="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
backend_directory="$(cd "${module_directory}/.." && pwd)"
workspace_directory="$(cd "${backend_directory}/.." && pwd)"
project_name="kendo-tournament-e2e-$RANDOM"
e2e_username="${E2E_USERNAME:-e2e-admin@test.local}"
e2e_password="${E2E_PASSWORD:-E2e-password-123}"
e2e_tests="${E2E_TESTS:-}"

cleanup() {
  if [[ "${E2E_KEEP_ENVIRONMENT:-false}" == "true" ]]; then
    return
  fi
  docker compose --project-name "${project_name}" --file "${module_directory}/docker-compose.e2e.yml" down --volumes --remove-orphans
}
trap cleanup EXIT

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
  -Dselenium.username="${e2e_username}"
  -Dselenium.password="${e2e_password}"
  -Dgpg.skip=true)
if [[ -n "${e2e_tests}" ]]; then
  maven_arguments+=("-Dtest=${e2e_tests}")
fi
mvn "${maven_arguments[@]}"
