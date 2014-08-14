@echo off

java -cp .\lib\* org.h2.tools.RunScript -url "jdbc:h2:tcp://localhost/mem:db/db1;DB_CLOSE_DELAY=-1" -user sa -script .\tests\002-CreateTable.sql
