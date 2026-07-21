@echo off
chcp 65001 >nul
title OA Platform - Restart
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0oa.ps1" restart
echo.
pause
