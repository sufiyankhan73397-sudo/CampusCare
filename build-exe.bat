@echo off
REM Builds dist\CampusCare\CampusCare.exe  (portable app with bundled Java runtime)
REM Needs: JDK 21 (jpackage) + Maven. No extra tools required.
setlocal
call mvn -q clean package
if errorlevel 1 ( echo Maven build failed. & exit /b 1 )

if exist target\jpackage-input rmdir /s /q target\jpackage-input
mkdir target\jpackage-input
copy /y target\campuscare.jar target\jpackage-input\ >nul

if exist dist rmdir /s /q dist
jpackage --type app-image ^
  --name CampusCare ^
  --app-version 1.0.0 ^
  --vendor "CampusCare Team" ^
  --input target\jpackage-input ^
  --main-jar campuscare.jar ^
  --main-class com.campuscare.Launcher ^
  --dest dist ^
  --java-options "-Dfile.encoding=UTF-8"
if errorlevel 1 ( echo jpackage failed. & exit /b 1 )

echo.
echo DONE. Run: dist\CampusCare\CampusCare.exe
echo Zip the whole dist\CampusCare folder to give it to the evaluator.
endlocal
