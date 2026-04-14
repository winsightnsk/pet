#!/bin/bash

cd webapp/pet && ./gradlew clean build && cd ../..

docker compose down
docker compose up -d --build