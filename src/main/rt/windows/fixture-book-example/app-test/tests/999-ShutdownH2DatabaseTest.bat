@echo off

java -cp .\lib\* org.h2.tools.Server -tcpShutdown tcp://localhost
