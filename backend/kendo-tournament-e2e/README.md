# End-to-end tests

This Maven module contains browser-based end-to-end tests for Kendo Tournament
Manager. The tests use Selenium with Chromium/Chrome and interact with the
frontend through the same WizardryTheme components used by operators.

## Requirements

- Java 25 and Maven.
- Chromium or Google Chrome available on the machine executing the tests.
- A running frontend instance. The default URL is `http://localhost:4200`.
- A running backend and a user with club and participant management permissions
  for authenticated workflows.

Selenium Manager resolves the matching browser driver automatically.

## Running tests

From the `backend` directory, execute the login smoke test against a running
frontend:

```bash
mvn -pl kendo-tournament-e2e test \
  -Dselenium.base-url=http://localhost:4200 \
  -Dgpg.skip=true
```

Tests run headlessly by default. To see the browser window, add:

```bash
-Dselenium.headless=false
```

### Authenticated workflows

The club and participant workflow requires a user with permissions to create
clubs and participants. No credentials are stored in the repository. Supply
them at execution time:

```bash
mvn -pl kendo-tournament-e2e test \
  -Dselenium.base-url=http://localhost:4200 \
  -Dselenium.username="operator@example.test" \
  -Dselenium.password="replace-with-a-secret" \
  -Dgpg.skip=true
```

When `selenium.username` or `selenium.password` is absent, authenticated tests
are reported as skipped. This lets the public login smoke test run without a
backend or credentials.

## Configuration

| Property | Default | Description |
| --- | --- | --- |
| `selenium.base-url` | `http://localhost:4200` | Frontend base URL. |
| `selenium.headless` | `true` | Runs Chromium without a visible window. |
| `selenium.username` | Empty | User for authenticated test flows. |
| `selenium.password` | Empty | Password for authenticated test flows. |

## Implemented tests

| Test | Authentication | Coverage |
| --- | --- | --- |
| `LoginTest.loginFormRenders` | No | Opens `/login`, checks that the login form renders, and verifies that typing in the username field is retained by the WizardryTheme input. |
| `ClubParticipantTest.createsClubAndParticipantsFromForms` | Yes | Logs in, creates a club through the club form, verifies it in the clubs table, then creates and verifies two participants through the participant form and club dropdown. |
| `LeagueTournamentTest.resolvesThreeMemberTeamLeagueAndRanksWinner` | Yes | Creates six participants, a league tournament with teams of three members, assigns competitors and teams through the UI, resolves its duels, and verifies `Team 1` leads the final ranking. |

The club workflow uses values inspired by the core CSV fixtures, including
`Técnicos de Investigación Aeroterráquea`, `Mengano López`, and `Fulano
Férnandez`. It does not upload or import a CSV. Each run appends a timestamp to
the generated club name and participant identifiers so it can run repeatedly
without conflicting with existing data.

## Test support

Reusable component wrappers live under
`src/main/java/com/softwaremagico/kt/selenium/components`:

- `InputField` for WizardryTheme text inputs.
- `Dropdown` for filterable WizardryTheme dropdowns.
- `Popup` for modal lifecycle synchronization.
- `Notification` for snackbar messages.
- `Table` for WizardryTheme data-table actions, search, and content checks.

Page objects in `src/main/java/com/softwaremagico/kt/selenium/pages` compose
these wrappers for individual application screens.
