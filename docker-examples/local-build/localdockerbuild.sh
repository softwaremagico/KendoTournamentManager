#!/bin/bash

cd ../../frontend
rm -rf ./dist
npm install
ng build --configuration=docker
cd ./dist
zip -r frontend.zip ./frontend
mv ./frontend.zip ../../docker-examples/local-build/frontend/
cd ../../backend
mvn clean install -DdeploySnapshot=true
mv ./kendo-tournament-rest/target/kendo-tournament-backend.jar ../docker-examples/local-build/backend/
cp -R ../docker/backend/libraries ../docker-examples/local-build/backend
cd ../docker-examples/local-build/
/usr/libexec/docker/cli-plugins/docker-compose build
rm -rf ./backend/libraries
#/usr/libexec/docker/cli-plugins/docker-compose up -d
