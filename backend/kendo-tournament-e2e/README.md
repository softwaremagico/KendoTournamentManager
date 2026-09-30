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

The supported entry point runs the full suite against the disposable Docker
environment:

```bash
bash ./backend/kendo-tournament-e2e/run-e2e.sh
```

The module defaults target `http://localhost:14200` and the temporary bootstrap
administrator. Tests are not skipped when credentials are absent: the runner
creates the account in the fresh database. If the environment is unavailable,
direct Maven execution fails rather than reporting an incomplete suite as
successful.

Tests run headlessly by default. To see the browser window, pass:

```bash
E2E_TESTS=LoginTest bash ./backend/kendo-tournament-e2e/run-e2e.sh
```

### Clean, ephemeral environment

`run-e2e.sh` is the recommended way to run the authenticated suite. It builds
the local backend and frontend, starts an isolated MySQL database, backend, and
frontend with Docker Compose, runs the tests, and always removes the containers
and database volume when it exits:

```bash
bash ./backend/kendo-tournament-e2e/run-e2e.sh
```

The environment uses `http://localhost:14200` for the frontend and
`http://localhost:18080` for the backend only while the script runs. It creates
the `e2e-admin@test.local` bootstrap administrator inside the disposable
database; these credentials are only for the local E2E environment. Override
them for a run without storing secrets in the repository:

```bash
E2E_USERNAME="e2e-admin@example.test" \
E2E_PASSWORD="a-local-random-password" \
bash ./backend/kendo-tournament-e2e/run-e2e.sh
```

The bootstrap properties are passed only to the temporary backend container.
They are not present in the application's default configuration or release
images, and the database volume is removed after every run. The temporary
account receives `super_admin`, `admin`, `editor`, and `viewer` roles so it can
exercise protected user workflows; normal bootstrap configuration defaults to
the `super_admin` role only.

To run one class while diagnosing a workflow, set `E2E_TESTS`:

```bash
E2E_TESTS=ClubParticipantTest bash ./backend/kendo-tournament-e2e/run-e2e.sh
```

The tenant-isolation scenario requires multi-organization support in its
temporary backend:

```bash
E2E_ENABLE_TENANCY=true \
E2E_TESTS=TenantIsolationTest \
bash ./backend/kendo-tournament-e2e/run-e2e.sh
```

Set `E2E_KEEP_ENVIRONMENT=true` only while diagnosing a failed run. It keeps
the temporary Compose project available for log inspection; clean it manually
with the project name printed by Docker Compose when finished.

### Authenticated workflows

The runner provisions an administrator with every role required by the browser
workflows. Its credentials are scoped to the disposable database and can be
overridden at execution time:

```bash
mvn -pl kendo-tournament-e2e test \
  -Dselenium.base-url=http://localhost:4200 \
  -Dselenium.username="operator@example.test" \
  -Dselenium.password="replace-with-a-secret" \
  -Dgpg.skip=true
```


## Configuration

| Property | Default | Description |
| --- | --- | --- |
| `selenium.base-url` | `http://localhost:14200` | Frontend URL of the disposable E2E environment. |
| `selenium.headless` | `true` | Runs Chromium without a visible window. |
| `selenium.username` | `e2e-admin@test.local` | Temporary E2E administrator. |
| `selenium.password` | `E2e-password-123` | Temporary E2E administrator password. |

## Implemented tests

| Test | Authentication | Coverage |
| --- | --- | --- |
| `LoginTest.loginFormRenders` | No | Opens `#/login`, checks that the login form renders, and verifies that typing in the username field is retained by the WizardryTheme input. |
| `ClubParticipantTest.createsClubAndParticipantsFromForms` | Yes | Logs in, creates a club through the club form, verifies it in the clubs table, then creates and verifies two participants through the participant form and club dropdown. |
| `LeagueTournamentTest.resolvesThreeMemberTeamLeagueAndRanksWinner` | Yes | Creates six participants, a league tournament with teams of three members, assigns competitors and teams through the UI, resolves its duels, and verifies `Team 1` leads the final ranking. |
| `LargeIndividualLeagueTournamentTest.resolvesTwelveTeamIndividualLeagueAndRanksWinner` | Yes | Creates twelve individual teams, verifies that the generated league contains 66 fights, resolves them all, and checks that `Team 1` has eleven wins and leads the final ranking. |
| `ChampionshipTournamentTest.resolvesEightTeamsChampionshipAndRanksWinner` | Yes | Creates eight teams of three participants, generates the championship bracket, resolves every round through the final, and verifies the seven-fight bracket and final team ranking. |
| `FifteenTeamTwoWinnersChampionshipTest.generatesChampionshipWithTwoFirstRoundWinners` | Yes | Creates fifteen individual teams, configures two winners from each first-round group, creates five initial groups, and verifies that first-round fights are generated. |
| `LoopTournamentTest.createsAndResolvesLoopTournament` | Yes | Creates a three-team loop, verifies its home-and-away fixture count, resolves the fights, and validates the leading team. |
| `KingOfTheMountainTournamentTest.createsNextFightAfterKingWins` | Yes | Creates a king-of-the-mountain tournament, resolves the first fight, and verifies that a new challenger fight is generated. |
| `BubbleSortTournamentTest.createsNextBubbleSortFightAfterResult` | Yes | Creates a bubble-sort tournament, resolves its first fight, and verifies that the next comparison is generated. |
| `SwissTournamentTest.createsInitialSwissPairings` | Yes | Creates four individual teams and verifies that the initial Swiss round contains two pairings. |
| `SwissByeTournamentTest.oddSwissFieldGeneratesPairingAndBye` | Yes | Creates three individual teams and verifies that the first Swiss round creates one pairing, leaving one team with a bye. |
| `CustomizedTournamentTest.opensManualFightCreatorForCustomizedTournament` | Yes | Creates a customized tournament and verifies access to its manual fight creator, because this format does not generate fights automatically. |
| `SenbatsuTournamentTest.opensSenbatsuChallengeCreator` | Yes | Creates a three-team Senbatsu tournament and verifies access to the challenge-constrained fight creator. |
| `TenantIsolationTest.tenantAdministratorsOnlySeeTheirOwnDataAndTournaments` | Yes, tenancy enabled | The super administrator creates two tenants and their administrators. Each tenant administrator creates a user, club, two participants, and resolves a league tournament. Both sessions verify rankings/statistics for their own tournament and that the other tenant's clubs, participants, and tournaments are absent. |
| `TournamentGuestQrTest.guestQrLinkShowsLiveTournamentScoreUpdates` | Yes | Generates a tournament guest QR link, opens it in a separate guest browser session, updates a score as administrator, and verifies the guest view receives the updated score. |
| `LockedTournamentTest.lockedTournamentDoesNotAllowScoreEdition` | Yes | Creates a minimal league, locks it from the tournament list, and verifies that its fight view no longer exposes editable score controls. |
| `TournamentCloneTest.clonedTournamentCanGenerateFreshFights` | Yes | Creates and starts a minimal league, clones it from the tournament list, then verifies that the copied teams can generate a fresh fight without copying the original fights. |

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
