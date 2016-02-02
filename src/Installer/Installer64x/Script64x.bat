@echo off

rem delete old output and project
@RD /S /Q "%~dp0..\Installer64x\Output"
@RD /S /Q "%~dp0..\Installer86x\Output"
pause

rem copy project to desktop
robocopy "%~dp0..\..\..\..\EastWeb.V2" "%temp%\EastWeb.V2" /s /mt[:18] /xf *.iss *exe *bat
pause

rem copy project from desktop to installer folder (this prevents infinite loop)
rem then remove project form desktop
robocopy "%temp%\EastWeb.V2" "%~dp0..\EastWeb.V2" /mir /mt[:18]
@RD /S /Q "%temp%\EastWeb.V2"
pause

rem compile installer 
"%~dp0..\InstallerCompiler\IScc.exe" %~dp0EastWeb64x.iss
@RD /S /Q "%~dp0..\EastWeb.V2"
pause