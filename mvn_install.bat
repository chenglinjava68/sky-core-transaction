cd /d %~dp0
call mvn clean install eclipse:eclipse -DdownloadSources
pause