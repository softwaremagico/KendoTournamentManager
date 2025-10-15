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

# Deploy library to GitHub and Mvn Repository

For uploading a SNAPSHOT version on GitHub

```
 mvn deploy -DskipTests -DdeploySnapshot=true
```

For uploading a stable version on Mvn Repository

```
 mvn release:prepare release:perform
```

# Automation with GitHub Actions

Beginning with version `2.17.8`, the process of generating new releases has been simplified.
To create an official release, simply execute the `Create Release` GitHub action. 