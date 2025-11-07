#!/bin/bash

cd ../frontend
rm -rf ./dist
npm install
ng build --configuration=docker
cd ./dist
zip -r frontend.zip ./frontend
mv ./frontend.zip ../../dockerlocalbuild/frontend/
cd ../../backend
mvn clean install -DdeploySnapshot=true
mv ./kendo-tournament-rest/target/kendo-tournament-backend.jar ../dockerlocalbuild/backend/
cd ../dockerlocalbuild/
/usr/libexec/docker/cli-plugins/docker-compose build
#/usr/libexec/docker/cli-plugins/docker-compose up -d
