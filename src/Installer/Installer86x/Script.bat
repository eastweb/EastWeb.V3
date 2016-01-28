@echo off

rem delete old output and project
@RD /S /Q "%~dp0..\Installer86x\EastWeb.V2"
@RD /S /Q "%~dp0..\Installer64x\EastWeb.V2"
@RD /S /Q "%~dp0..\Installer64x\Output"
@RD /S /Q "%~dp0..\Installer86x\Output"

rem copy project to desktop
robocopy "%~dp0..\..\..\..\EastWeb.V2" "%systemdrive%\Documents and Settings\All Users\Desktop\EastWeb.V2" /mir
pause

rem copy project from desktop to installer folder (this prevents infinite loop)
rem then remove project form desktop
robocopy "%systemdrive%\Documents and Settings\All Users\Desktop\EastWeb.V2" "%~dp0EastWeb.V2" /mir
@RD /S /Q "%systemdrive%\Documents and Settings\All Users\Desktop\EastWeb.V2"
pause

rem compile installer 
"%~dp0..\InstallerCompiler\IScc.exe" %~dp0EastWeb86x.iss
pause