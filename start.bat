@echo off
cd /d "%~dp0"
echo.
echo ==================================================
echo   KAMMO Mobile - Scan QR with Expo Go
echo ==================================================
echo   1. Install "Expo Go" on your phone
echo   2. Connect phone and PC to same Wi-Fi
echo   3. Scan the QR code shown below
echo      (Android: Expo Go app - Scan QR)
echo      (iPhone: Camera app - tap the link)
echo ==================================================
echo.
call npx.cmd expo start --lan --port 8082
