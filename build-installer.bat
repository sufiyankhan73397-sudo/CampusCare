@echo off
REM Builds dist\CampusCare-1.0.0.exe  (Windows setup wizard with Start-menu + desktop shortcut)
REM Needs: JDK 21 + Maven + WiX Toolset 3.x (https://github.com/wixtoolset/wix3/releases) on PATH.
setlocal
call mvn -q clean package
if errorlevel 1 ( echo Maven build failed. & exit /b 1 )

if exist target\jpackage-input rmdir /s /q target\jpackage-input
mkdir target\jpackage-input
copy /y target\campuscare.jar target\jpackage-input\ >nul

if exist dist rmdir /s /q dist
jpackage --type exe ^
  --name CampusCare ^
  --app-version 1.0.0 ^
  --vendor "CampusCare Team" ^
  --input target\jpackage-input ^
  --main-jar campuscare.jar ^
  --main-class com.campuscare.Launcher ^
  --dest dist ^
  --win-menu --win-shortcut --win-dir-chooser ^
  --java-options "-Dfile.encoding=UTF-8"
if errorlevel 1 ( echo jpackage failed - is WiX 3 installed? & exit /b 1 )

echo.
echo DONE. Installer: dist\CampusCare-1.0.0.exe
endlocal
