Please ensure that you update the domain, email, and passwords in the `.env` file.
Additionally, set the same passwords in the `application.properties` file and update the machine domain accordingly.
The machine domain must also be configured in the `config.js` file.

Subsequently, execute the following command:

```
docker-compose build && docker-compose up -d
```

To access the tool, open a web browser and navigate to:

```
http://localhost:4200
```

An example demonstrating how to configure these files can be
found  [here](https://github.com/softwaremagico/KendoTournamentManager/wiki/Installation-using-docker-hub)
