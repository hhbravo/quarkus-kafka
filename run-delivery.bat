@echo off
echo Starting stock-service...
echo Using Maven Wrapper...
echo Note: Using Byte Buddy experimental mode for Java 25 compatibility
call .\mvnw.cmd quarkus:dev -pl stock-service -Dquarkus.http.port=8081 -Dnet.bytebuddy.experimental=true
pause

