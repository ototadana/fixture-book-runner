@echo off

java -cp .\conf;.\lib\*;%FIXTURE_BOOK_DIR%\lib\* com.xpfriend.fixture.runner.Setup .\tests\102-ExampleJobTest.xlsx main 指定されたIDのNAME項目の値を更新することができる %*

..\app\ExampleJob 1 xxx

java -cp .\conf;.\lib\*;%FIXTURE_BOOK_DIR%\lib\* com.xpfriend.fixture.runner.ValidateStorage .\tests\102-ExampleJobTest.xlsx main 指定されたIDのNAME項目の値を更新することができる %*
