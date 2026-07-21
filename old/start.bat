@echo off
chcp 65001 >nul
title OA Platform - Start
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0oa.ps1" start
echo.
pause
