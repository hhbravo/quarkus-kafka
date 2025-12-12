@echo off
echo Starting orders-service...
echo Using Maven Wrapper...
echo Note: Using Byte Buddy experimental mode for Java 25 compatibility
call .\mvnw.cmd quarkus:dev -pl orders-service -Dnet.bytebuddy.experimental=true
pause

