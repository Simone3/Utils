@echo off

powershell -executionpolicy remotesigned -File ../../scripts/clone-npm-project.ps1 -targetDirectory "../../../src"

SET /P NEXT=DONE. Press Enter to exit...
