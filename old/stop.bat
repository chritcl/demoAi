@echo off
chcp 65001 >nul
title OA Platform - Stop
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0oa.ps1" stop
echo.
pause
