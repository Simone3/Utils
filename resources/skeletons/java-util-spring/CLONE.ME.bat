@echo off

powershell -executionpolicy remotesigned -File ../../scripts/clone-maven-project.ps1 -targetDirectory "../../../src"

SET /P NEXT=DONE. Press Enter to exit...
