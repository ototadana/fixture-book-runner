@echo off
echo %*
java  -Dcom.xpfriend.fixture.runner.report=target/reporttest -cp target\test-classes;target\classes;target\lib\* com.xpfriend.fixture.runner.Expect %~dp0\Test06Test.xlsx %*
