@echo off

java -cp .\conf;.\lib\*;%FIXTURE_BOOK_DIR%\lib\* com.xpfriend.fixture.runner.Setup .\tests\102-ExampleJobTest.xlsx main �w�肳�ꂽID��NAME���ڂ̒l���X�V���邱�Ƃ��ł��� %*

..\app\ExampleJob 1 xxx

java -cp .\conf;.\lib\*;%FIXTURE_BOOK_DIR%\lib\* com.xpfriend.fixture.runner.ValidateStorage .\tests\102-ExampleJobTest.xlsx main �w�肳�ꂽID��NAME���ڂ̒l���X�V���邱�Ƃ��ł��� %*
