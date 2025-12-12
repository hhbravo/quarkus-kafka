#!/bin/bash
echo "Starting stock-service..."
echo "Note: Using Byte Buddy experimental mode for Java 25 compatibility"
./mvnw quarkus:dev -pl stock-service -Dquarkus.http.port=8081 -Dnet.bytebuddy.experimental=true

