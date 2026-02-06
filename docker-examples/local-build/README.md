This folder contains a script (localdockerbuild.sh) that will build locally the current code and docker images.
You can customise the `.env` file
You need nodejs 20 installed, angular 15 & maven in order to build locally frontend & backend.
This is a totally unsafe way to run the tournament, should be used only for testing purposes.

After it, execute:

```
docker-compose up -d
```

For accessing to the tool, please open a browser and access to:

```
http://localhost:4200
```
