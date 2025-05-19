#!/bin/bash
cd /home/erib/project/roadvault
./mvnw clean package -DskipTests && docker compose up --build -d
