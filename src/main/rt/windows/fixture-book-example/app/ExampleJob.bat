@echo off

cd /d %~sdp0

java -cp .\classes;.\lib\* com.xpfriend.fixture.runner.example.ExampleJob %*
