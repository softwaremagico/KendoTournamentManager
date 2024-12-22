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