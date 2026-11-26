# Create the frontend file.

```
ng build --configuration docker --output-hashing=all
```

And in folder `frontend/dist` zip the content of the folder `frontend` as `kendo-tournament-frontend-docker.zip`.

Or for a standard server

```
ng build --configuration production --output-hashing=all
```

And in folder `frontend/dist` zip the content of the folder `frontend` as `kendo-tournament-frontend.zip`.

# Create the backend file.

Access the backend folder and compile the backend project with the command:

```
mvn clean install
```

User the file generated on `kendo-tournament-rest/target/kendo-tournament-backend.jar`

# Update docker configuration

Update the `.env` file with the new version and release names.

# Deploy library to GitHub and Maven Repository

## Automatic Maven Central Deployment

**New Workflow (Recommended)**

As of version 2.17.8+, deployments to Maven Central are now **automatic** via GitHub Actions:

1. **Create a release** (via GitHub UI or git tag)
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
   Or use GitHub UI: **Releases → Draft a new release → Publish**

2. **The workflow triggers automatically**:
   - The `maven-central-publish.yml` workflow detects the release
   - Builds the backend with Maven
   - Signs artifacts with GPG
   - Deploys to Maven Central

3. **Verify deployment** in workflow logs: **Actions → Publish to Maven Central**

See [`MAVEN_CENTRAL_SETUP.md`](MAVEN_CENTRAL_SETUP.md) for configuration details.

## Manual Deployment (Legacy)

For uploading a SNAPSHOT version on GitHub

```
 mvn deploy -DskipTests -DdeploySnapshot=true
```

For uploading a stable version to Maven Central

```
 mvn deploy -DskipTests -DdeployCentral=true
 mvn deploy -DskipTests -DdeployCentral=true
```
# Automation with GitHub Actions

Beginning with version `2.17.8`, the process of generating new releases has been automated.
Whenever a new pull request (PR) is created, a draft release is either generated or an existing draft is updated to include the information from the new PR,
as configured in `release-drafter.yml`.
It is essential that each PR includes appropriate labels to ensure accurate documentation generation.

To finalize a draft and create an official release, a new git tag corresponding to the new version must be created.
Subsequently, the system will package all relevant code and binaries directly into the release.