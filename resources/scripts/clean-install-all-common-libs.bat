@echo off

cd ../../src_common

for /d %%i in (*) do (call mvn clean install -f %%i/pom.xml)

cmd /k
