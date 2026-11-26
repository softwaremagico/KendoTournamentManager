# End-to-end tests

This Maven module contains browser-based end-to-end tests for Kendo Tournament
Manager. The tests use Selenium with Chromium/Chrome and interact with the
frontend through the same WizardryTheme components used by operators.

## Requirements

- Java 25 and Maven.
- Chromium or Google Chrome available on the machine executing the tests.
- Docker with Compose for the disposable frontend, backend, and database.
- A running backend and a user with club and participant management permissions
  for authenticated workflows.

Selenium Manager resolves the matching browser driver automatically.

## Running tests

### GitHub Actions (Gitflow branches)

The **E2E Browser Tests** workflow (`.github/workflows/e2e.yml`) runs automatically
on changes to `backend/` or `frontend/` in pull requests and pushes to `main`,
`master`, `develop`, `feature/**`, `release/**`, and `hotfix/**`.

It installs Java 25 and Node.js 22, uses Chrome on the Ubuntu runner, installs the
locked frontend dependencies with `npm ci`, and executes `run-e2e.sh` headlessly
against its disposable Docker environment. No application credentials or database
secrets need to be configured in GitHub: the runner creates its temporary account.

You can also select **Actions → E2E Browser Tests → Run workflow**, choose a branch,
and optionally enter a Surefire class filter such as `SwissProgressionTest`.
Leave the filter empty to run all scenarios. The manual workflow becomes available
in the Actions list once its definition exists on the default branch.

Each run uploads Surefire/TestNG reports, the runner output, and Docker logs as
an artifact retained for 14 days, including failed runs. Cleanup runs regardless
of test outcome. A newer run on the same branch/PR cancels the older run.
The job has a 45-minute timeout.

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
bash ./backend/kendo-tournament-e2e/run-e2e.sh --headed
```

The visible browser runs on the host machine while MySQL, backend, and frontend
remain in the disposable Docker environment. To inspect one workflow:

```bash
E2E_TESTS=WebSocketScoreUpdateTest \
bash ./backend/kendo-tournament-e2e/run-e2e.sh --headed
```

The equivalent environment-variable form is:

```bash
E2E_HEADLESS=false bash ./backend/kendo-tournament-e2e/run-e2e.sh
```

The browser scenarios execute sequentially. Several workflows deliberately use
two simultaneous browser profiles to validate guest and WebSocket behavior, so
running all classes in parallel would exhaust local Chromium resources.

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

Multi-organization support is enabled by default in the temporary backend, so
the full run also executes the tenant-isolation scenario. To run it alone:

```bash
E2E_TESTS=TenantIsolationTest \
bash ./backend/kendo-tournament-e2e/run-e2e.sh
```

Set `E2E_KEEP_ENVIRONMENT=true` only while diagnosing a failed run. It keeps
the temporary Compose project available for log inspection; clean it manually
with the project name printed by Docker Compose when finished.

The runner uses host ports `14200` and `18080`. It aborts before starting if a
previous E2E environment is still using either port. Remove that environment
with its printed Compose project name before starting another run.

To stop and remove any environments left by prior E2E runs, use:

```bash
bash ./backend/kendo-tournament-e2e/run-e2e.sh --stop
```

### Authenticated workflows

E2E tests are skipped by default, including during `mvn clean install` from the
backend root. `run-e2e.sh` explicitly enables them with `-DskipE2E=false`, while
the other modules' tests continue to run normally. When invoking Maven directly,
pass the same property to enable the browser tests.

The runner provisions an administrator with every role required by the browser
workflows. Its credentials are scoped to the disposable database and can be
overridden at execution time:

```bash
mvn -pl kendo-tournament-e2e test \
  -DskipE2E=false \
  -Dselenium.base-url=http://localhost:4200 \
  -Dselenium.username="operator@example.test" \
  -Dselenium.password="replace-with-a-secret" \
  -Dgpg.skip=true
```


## Configuration

| Property | Default | Description |
| --- | --- | --- |
| `skipE2E` | `true` | Skips E2E tests unless explicitly set to `false`; the runner sets it automatically. |
| `selenium.base-url` | `http://localhost:14200` | Frontend URL of the disposable E2E environment. |
| `selenium.headless` | `true` | Runs Chromium without a visible window. |
| `selenium.username` | `e2e-admin@test.local` | Temporary E2E administrator. |
| `selenium.password` | `E2e-password-123` | Temporary E2E administrator password. |

## Implemented tests

### Draw resolution

- `ResultCorrectionTest.tiedTeamLeagueCreatesDaihyosenAndAttributesDecidingHitToRepresentative`
  completes a tied league, selects representatives, resolves the deciding duel,
  and checks the ranking and participant's MEN statistic.
- `ChampionshipDrawTest.tiedChampionshipFinalRequiresDaihyosenAndRanksItsWinner`
  finishes a two-member championship final with one 2–0 victory per team (MEN
  versus KOTE), verifies equal ranking before the tiebreak, selects representatives
  through the UI, and checks the deciding MEN, final winner/runner-up ordering,
  persistence after reload, and absence of a duplicated final.

Run both draw regressions with:

```bash
E2E_TESTS=ResultCorrectionTest,ChampionshipDrawTest bash ./backend/kendo-tournament-e2e/run-e2e.sh
```

| Test | Authentication | Coverage |
| --- | --- | --- |
| `LoginTest.loginFormRenders` | No | Opens `#/login`, checks that the login form renders, and verifies that typing in the username field is retained by the WizardryTheme input. |
| `ClubParticipantTest.createsClubAndParticipantsFromForms` | Yes | Logs in, creates a club through the club form, verifies it in the clubs table, then creates and verifies two participants through the participant form and club dropdown. |
| `LeagueTournamentTest.resolvesThreeMemberTeamLeagueAndRanksWinner` | Yes | Creates six participants, a league tournament with teams of three members, assigns competitors and teams through the UI, resolves its duels, and verifies `Team 1` leads the final ranking. |
| `LargeIndividualLeagueTournamentTest.resolvesTwelveTeamIndividualLeagueAndRanksWinner` | Yes | Creates twelve individual teams, verifies 66 fights, resolves them all for a selected competitor, and checks that competitor leads the ranking with eleven wins. |
| `ChampionshipTournamentTest.resolvesEightTeamsChampionshipAndRanksWinner` | Yes | Creates eight teams of three participants, generates the championship bracket, resolves every round through the final, and verifies the seven-fight bracket and final team ranking. |
| `FifteenTeamTwoWinnersChampionshipTest.generatesChampionshipWithTwoFirstRoundWinners` | Yes | Creates fifteen individual teams, configures two winners from each first-round group, creates five initial groups, and verifies that first-round fights are generated. |
| `TwoTeamTwoWinnersChampionshipTest.twoTeamsWithTwoWinnersStillGenerateFinalFight` | Yes | Configures the minimum two-team championship with two first-round qualifiers and verifies that its only final fight remains generated. |
| `LoopTournamentTest.createsAndResolvesLoopTournament` | Yes | Creates a three-team loop, verifies the initial three-fight round, resolves the fights, and validates the leading team. |
| `KingOfTheMountainTournamentTest.createsNextFightAfterKingWins` | Yes | Creates a king-of-the-mountain tournament, resolves the first fight, and verifies that a new challenger fight is generated. |
| `BubbleSortTournamentTest.createsNextBubbleSortFightAfterResult` | Yes | Creates a bubble-sort tournament, resolves its first fight, and verifies that the next comparison is generated. |
| `SwissTournamentTest.createsInitialSwissPairings` | Yes | Creates four individual teams and verifies that the initial Swiss round contains two pairings. |
| `SwissByeTournamentTest.oddSwissFieldGeneratesPairingAndBye` | Yes | Creates three individual teams and verifies that the first Swiss round creates one pairing, leaving one team with a bye. |
| `CustomizedTournamentTest.opensManualFightCreatorForCustomizedTournament` | Yes | Creates a customized tournament and verifies access to its manual fight creator, because this format does not generate fights automatically. |
| `SenbatsuTournamentTest.opensSenbatsuChallengeCreator` | Yes | Creates a three-team Senbatsu tournament and verifies access to the challenge-constrained fight creator. |
| `TenantIsolationTest.tenantAdministratorsOnlySeeTheirOwnDataAndTournaments` | Yes, tenancy enabled | The super administrator creates two tenants and their administrators. Each tenant administrator creates a user, club, two participants, and resolves a league tournament. Both sessions verify rankings/statistics for their own tournament and that the other tenant's clubs, participants, and tournaments are absent. |
| `TournamentGuestQrTest.guestQrLinkShowsLiveTournamentScoreUpdates` | Yes | Generates a tournament guest QR link, opens it in a separate guest browser session, updates a score as administrator, and verifies the guest view receives the updated score. |
| `WebSocketScoreUpdateTest.scoreUpdateIsDeliveredToSecondAuthenticatedSession` | Yes | Creates an editor user, opens the same tournament in two independently authenticated browser sessions, and verifies a score entered by the administrator is propagated to the editor through STOMP. |
| `LockedTournamentTest.lockedTournamentDoesNotAllowScoreEdition` | Yes | Creates a minimal league, locks it from the tournament list, and verifies that its fight view no longer exposes editable score controls. |
| `LockedTournamentGuestAccessTest.lockedTournamentRejectsGuestQrAccess` | Yes | Generates a guest QR link, locks the tournament, and verifies that a new guest browser session is redirected to login rather than accessing scores. |
| `TournamentCloneTest.clonedTournamentCanGenerateFreshFights` | Yes | Creates and starts a minimal league, clones it from the tournament list, then verifies that the copied teams can generate a fresh fight without copying the original fights. |
| `CsvImportTest.importsClubAndParticipantsAndRejectsInvalidParticipant` | Yes | Imports a club and participants through the CSV controls, then submits an invalid participant row and verifies it is not persisted. |
| `ParticipantStatisticsE2ETest.scoredMenAppearsInWinnerParticipantStatistics` | Yes | Resolves a minimal league after recording a MEN and verifies that the winning participant statistics dashboard reflects the recorded hit. |
| `ParticipantQrAccessTest.personalQrAuthenticatesParticipantAndShowsOwnPersistedStatistics` | Administrator creates data; participant enters independently | Generates the personal participant QR through the registry, opens its destination in a fresh browser, verifies participant authentication and the correct identity with two persisted MEN, then reloads to check continued access. |
| `TournamentScoreRulesTest.persistsEuropeanInternationalAndCustomScoreRules` | Yes | Creates leagues using European, International, and Custom scoring rules, verifies each persisted selector value, and opens the custom points editor. |

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

## Synchronization and diagnostics

### Requested feature coverage

- **Projector:** `ScoringPanelTest` opens a second editor session, checks read-only
  score projection, live updates, the finished-event empty state, and Escape exit.
- **Member reordering:** a three-team, two-member league finishes its first fight,
  swaps members repeatedly on both sides, reloads to verify persistence, and
  completes the league with distinct MEN/KOTE scores. Every participant's scored
  and received MEN/KOTE totals are checked, along with the unchanged finished fight.
- **Club ranking:** `GeneratedExportsTest` checks only club members appear, with
  the actual number of duels and hits after a resolved team league.
- **Exports:** browser actions generate role/team/fight lists, team/competitor
  rankings, tournament QR, diplomas, accreditations, championship group lists and
  visual bracket PDFs. Tests parse real PDF bytes/pages/text (or drawing content
  for the visual bracket). ZIP exports must include every expected report.
- **WordPress:** the locked tournament export must contain its title, participants,
  both teams and HTML tables. Download Blobs are captured in-browser without
  mocking HTTP responses or invoking generation APIs instead of UI controls.

- **Tenancy:** `TenantIsolationTest` creates two organizations and administrators,
  verifies isolated clubs/participants/tournaments, and checks their rankings and statistics.
- **Tournament formats:** all eight active `TournamentType` values have scenarios:
  Championship, League, Loop, King of the Mountain, Bubble Sort, Customized,
   Senbatsu, and Swiss. `ManualTournamentProgressionTest` completes Custom and
   Senbatsu; `SwissProgressionTest` completes three rounds with even and odd fields.
- **Personal participant QR:** `ParticipantQrAccessTest` follows the actual link
  encoded by the generated QR in an independent browser. This covers token-based
  entry, not physical camera scanning, and is distinct from tournament guest QR access.
- **Participant statistics:** `ParticipantStatisticsE2ETest` checks the administrator
  dashboard; `ParticipantQrAccessTest` checks the same persisted scores as the participant.

### Negative workflows

`NegativeWorkflowsTest` adds nine independent browser scenarios (each with a fresh
browser session):

- Wrong password: rejected login creates no JWT; a valid retry succeeds.
- Anonymous registry deep link: redirects to login without exposing the table.
- Missing participant surname: field validation, no persisted row after reload,
  and exactly one row after correcting and submitting valid data.
- Cancelled participant edit: a full reload retrieves the original persisted values.
- Cancelled participant deletion: the record remains present and editable after reload.
- Mismatched account passwords: no account persists; correction allows login.
- Viewer score mutation attempt: no score menu or finish action, and the existing
  score remains unchanged after reloading.
- Editor tenant-administration deep link: redirects to tournaments and does not
  render the super-administrator form.
- Logged-out session deep link: no JWT or protected registry; login restores access.

Run these cases alone with:

```bash
E2E_TESTS=NegativeWorkflowsTest bash ./backend/kendo-tournament-e2e/run-e2e.sh
```

Together with the existing invalid CSV and locked-tournament/guest scenarios,
these exercise rejection and recovery without changing application business logic.
Navigation to the same hash route can retain component state; persistence checks
explicitly reload the browser rather than relying on reopening the route.

Participant role cards can be clicked to assign an available competitor; drag and
drop remains available for choosing other roles. Assignments respect edit
permissions and locks, wait for persistence, and retain failed assignments for retry.
Individual automatic teams disambiguate repeated abbreviated names with numeric suffixes.

The Angular application explicitly enables Zone.js change detection because its
timer-driven UI requires it. Dropdown option arrays use immutable replacements so
pure conversion pipes refresh only when their inputs change. Fight helpers wait
for pending score saves and finished-duel state before advancing; transient
notifications are not a persistence signal because repeated messages are deduplicated.

For backend diagnostics while preserving an environment:

```bash
docker compose --project-name <printed-e2e-project> \
  --file backend/kendo-tournament-e2e/docker-compose.e2e.yml logs backend
```

The invalid CSV and locked guest scenarios intentionally produce rejection logs.
Unexpected JWT, authorization, database, or frontend errors still require investigation.
Start a fresh runner environment for the full suite: the CSV fixtures contain fixed
identifiers and are not repeatable against a previously populated database.

## Advanced progression and recovery

The suite contains **52 browser scenarios**. The advanced scenarios use real UI
actions plus authenticated browser HTTP requests for precise persisted-state
assertions. `AdvancedBrowserTest` provides isolated browser setup and those reads;
it does not mock backend responses.

| Class | Coverage |
| --- | --- |
| `SwissProgressionTest` | Three rounds with eight teams; independent match-point and Buchholz calculations; no repeated opponents; configured round limit. Five-team variant verifies distinct byes and no fabricated MEN statistics. Scoring is explicitly configured to 3/1. |
| `ManualTournamentProgressionTest` | Manual Custom fight and ranking; rejected out-of-range Senbatsu drag, four completed challenges, and one surviving team. |
| `ResultCorrectionTest` | Reopen a completed duel, remove MEN and replace with opponent KOTE; verify ranking, both participants' statistics, and a second session. League tie: verify shared position and draw counters, select representatives, resolve daihyōsen, and check the winner and deciding hit after reload. Championship tie: verify automatic daihyōsen, blocked final while pending, correct winner progression, and final classification without duplicate fights. |
| `TenantDirectAccessTest` | Known foreign participant/club/tournament IDs: read, update, delete, statistics and export rejection; original data preserved; logout and login into another organization; token-renewal request. |
| `WebSocketReconnectTest` | Close actual browser WebSockets, observe a new open connection, and verify subsequent live score delivery without reloading the page. |
| `SubstitutionTest` | Swap a reserve into a future fight, reload the persisted order, preserve the completed fight, and attribute later KOTE to the replacement. |
| `TimerAndShiaijoTest` | Start/pause/resume/finish with real elapsed time, persisted duration and participant totals; two independent sessions on courts A/B, score isolation and court-specific projection. Court fixtures are allocated through authenticated fight/group endpoints. |

These tests exposed and now guard fixes for transient Swiss fight identities being
saved twice, unlinked manual-fight drafts, lost tenant context during tournament
and duel conversion, and unchecked cross-tenant update/delete IDs. Expected
negative-test logs include foreign-resource 404s, invalid credentials, invalid CSV
and locked guest access.

Latest clean validation: **50 E2E passed, 0 failures/errors/skips**;
**866 frontend unit tests passed**; **59 focused backend regressions passed**.
The subsequent full backend run through Core also passed: **1,356 Core**, **421
Persistence**, and **44 Logger** tests, with no failures or skips.
