#!/bin/bash
echo "Starting delivery-service..."
echo "Note: Using Byte Buddy experimental mode for Java 25 compatibility"
./mvnw quarkus:dev -pl delivery-service -Dquarkus.http.port=8081 -Dnet.bytebuddy.experimental=true

