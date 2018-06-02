@echo off

powershell -executionpolicy remotesigned -File ../../scripts/clone-html-file.ps1 -targetDirectory "../../../utils"

SET /P NEXT=DONE. Press Enter to exit...
