; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define MyAppName "EastWeb"
#define MyAppVersion "2.1.0.0"
#define MyAppPublisher "South Dakota State University "
#define MyAppURL "https://epidemia.sdstate.edu/eastweb/download.php"
#define MyAppExeName "EastWeb.jar"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{52DB3703-0D8B-4F76-BFDE-A9E9F1C77B7C}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={pf}\{#MyAppName}
DefaultGroupName={#MyAppName}
OutputBaseFilename=EastWeb Installer
SetupIconFile= sdsu_logo_trans2.ico
Compression=lzma
SolidCompression=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "..\EastWeb.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\EastWeb.V2\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "postgresql-9.5.0-1-windows-x64.exe"; DestDir: "{tmp}"
Source: "jre-8u66-windows-x64.exe"; DestDir: "{tmp}"

; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\{#MyAppName}"; IconFilename: sdsu_logo_trans2.ico; Filename: "{app}\{#MyAppExeName}"
Name: "{commondesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: sdsu_logo_trans2.ico; Tasks: desktopicon

[Run]
Filename: "{tmp}\postgresql-9.5.0-1-windows-x64.exe"; StatusMsg: Install PostgreSQL; 
Filename: "{tmp}\jre-8u66-windows-x64.exe"; StatusMsg: Install JRE; 
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: shellexec postinstall skipifsilent