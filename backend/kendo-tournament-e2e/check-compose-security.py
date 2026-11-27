#!/usr/bin/env python3
"""Validate security-related environment propagation in all shipped Compose files."""
import json
import os
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[2]
CASES = (
    ("docker/docker-compose.yml", "http://localhost"),
    ("docker-examples/local-build/docker-compose.yml", "http://localhost:4200"),
    ("docker-examples/localhost-without-reverse-proxy/docker-compose.yml", "http://localhost:4200"),
    ("docker-examples/localhost-with-reverse-proxy/docker-compose.yml", "http://localhost"),
    ("docker-examples/official-docker-image/docker-compose.yml", "https://example.test"),
)


def render(path, overrides, service="kendo-tournament-backend", field="environment"):
    env = os.environ.copy()
    env.update({
        "jwt_secret": "test-jwt",
        "database_password": "test-password",
        "database_encryption_key": "test-key",
        "TRAEFIK_BASIC_AUTH_USERS": "test-user",
        "BOOTSTRAP_SUPER_ADMIN_USERNAME": "admin@example.test",
        "BOOTSTRAP_SUPER_ADMIN_PASSWORD": "test-bootstrap",
    })
    env.pop("SERVER_CORS_DOMAINS", None)
    env.update(overrides)
    result = subprocess.run(
        ["docker", "compose", "-f", str(ROOT / path), "config", "--format", "json"],
        cwd=ROOT / path.parent, env=env, check=True, capture_output=True, text=True,
    )
    return json.loads(result.stdout)["services"][service][field]


def main():
    for name, expected in CASES:
        path = Path(name)
        overrides = {"machine_domain": "example.test"} if "official-docker-image" in name else {}
        settings = render(path, overrides)
        assert settings["SERVER_CORS_DOMAINS"] == expected, (name, settings["SERVER_CORS_DOMAINS"])
        assert settings["JWT_SECRET"] == "test-jwt", name
        assert settings["SPRING_KENDO_DATASOURCE_PASSWORD"] == "test-password", name
        assert settings["DATABASE_ENCRYPTION_KEY"] == "test-key", name
        assert settings["BOOTSTRAP_SUPER_ADMIN_USERNAME"] == "admin@example.test", name
        assert settings["BOOTSTRAP_SUPER_ADMIN_PASSWORD"] == "test-bootstrap", name
        if "official-docker-image" not in name:
            health = render(path, overrides, service="kendo-tournament-database", field="healthcheck")
            assert "POSTGRES_USER" in health["test"][1] and "POSTGRES_DB" in health["test"][1], name
        settings = render(path, {**overrides, "SERVER_CORS_DOMAINS": "https://one.test,https://two.test"})
        assert settings["SERVER_CORS_DOMAINS"] == "https://one.test,https://two.test", name
        print(f"OK {name}")
    for name, domain, protocol in (
        ("docker/docker-compose.yml", "tournament.example.test", "https"),
        ("docker-examples/localhost-with-reverse-proxy/docker-compose.yml", "tournament.example.test", "https"),
    ):
        settings = render(Path(name), {"machine_domain": domain, "protocol": protocol})
        assert settings["SERVER_CORS_DOMAINS"] == f"{protocol}://{domain}", name
        print(f"OK {name} (HTTPS domain)")
    ports = render(Path("docker-examples/localhost-without-reverse-proxy/docker-compose.yml"),
                   {}, service="kendo-tournament-frontend", field="ports")
    assert any(port["target"] == 4200 and port["published"] == "4200" for port in ports), ports
    networks = render(Path("docker-examples/localhost-without-reverse-proxy/docker-compose.yml"),
                      {}, service="kendo-tournament-frontend", field="networks")
    assert "external" in networks, networks
    print("OK no-proxy frontend port 4200")


if __name__ == "__main__":
    main()
