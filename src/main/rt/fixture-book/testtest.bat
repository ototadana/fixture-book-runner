@echo off

set FIXTURE_BOOK_DIR=%~sdp0

java -cp .\conf;.\lib\*;%FIXTURE_BOOK_DIR%\lib\* com.xpfriend.fixture.runner.Exec %*
