#!/bin/bash
echo "Starting orders-service..."
echo "Note: Using Byte Buddy experimental mode for Java 25 compatibility"
./mvnw quarkus:dev -pl orders-service -Dnet.bytebuddy.experimental=true

