<#
.SYNOPSIS
  协同办公平台 一键启停脚本 (PowerShell)
.DESCRIPTION
  后端 10001 / PC 端 10002 / 移动端 10003
.EXAMPLE
  .\oa.ps1 start      # 启动全部
  .\oa.ps1 stop       # 停止全部
  .\oa.ps1 restart    # 重启
  .\oa.ps1 status     # 查看运行状态
#>
param(
  [Parameter(Position = 0)]
  [ValidateSet('start', 'stop', 'restart', 'status', '', 'help')]
  [string]$Action = ''
)

$ErrorActionPreference = 'SilentlyContinue'
# 统一 UTF-8 输出，避免中文乱码（配合 .bat 中的 chcp 65001）
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
$OutputEncoding = [System.Text.UTF8Encoding]::new()
$Root   = Split-Path -Parent $MyInvocation.MyCommand.Path

# 服务清单：名称 => 工作目录 / 启动命令 / 端口
$Services = [ordered]@{
  '后端'   = @{ Dir = Join-Path $Root 'oa-backend';  Cmd = 'mvn spring-boot:run'; Port = 10001 }
  'PC端'   = @{ Dir = Join-Path $Root 'oa-frontend'; Cmd = 'npm run dev';         Port = 10002 }
  '移动端' = @{ Dir = Join-Path $Root 'oa-mobile';   Cmd = 'npm run dev';         Port = 10003 }
}

function Test-OaPort($port) {
  return [bool](Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)
}

function Stop-OaPort($port) {
  $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
  if (-not $conns) { return $false }
  foreach ($c in $conns) {
    Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue
    # 连带终止子进程树（mvn -> java 等）
    try { taskkill /F /T /PID $c.OwningProcess | Out-Null } catch {}
  }
  return $true
}

function Start-Oa {
  foreach ($name in $Services.Keys) {
    $svc = $Services[$name]
    if (Test-OaPort $svc.Port) {
      Write-Host "  [$name] 端口 $($svc.Port) 已在运行，跳过" -ForegroundColor Yellow
      continue
    }
    # 在独立控制台窗口启动，窗口标题 OA-<名称>
    $argLine = "/k title OA-$name && cd /d `"$($svc.Dir)`" && $($svc.Cmd)"
    Start-Process -FilePath 'cmd.exe' -ArgumentList $argLine | Out-Null
    Write-Host "  [$name] 已启动 -> http://localhost:$($svc.Port)" -ForegroundColor Green
  }
  Write-Host ''
  Write-Host '  后端 API 文档: http://localhost:10001/doc.html'
  Write-Host '  默认账号: admin / <REDACTED_DEFAULT_PASSWORD>' -ForegroundColor Cyan
  Write-Host '  后端首次启动约需 20-40 秒。'
}

function Stop-Oa {
  foreach ($name in $Services.Keys) {
    $port = $Services[$name].Port
    if (Stop-OaPort $port) {
      Write-Host "  [$name] 端口 $port 已停止" -ForegroundColor Green
    } else {
      Write-Host "  [$name] 端口 $port 未在运行" -ForegroundColor Yellow
    }
  }
}

function Show-OaStatus {
  foreach ($name in $Services.Keys) {
    $port = $Services[$name].Port
    if (Test-OaPort $port) {
      Write-Host "  [运行中] $name :$port" -ForegroundColor Green
    } else {
      Write-Host "  [已停止] $name :$port" -ForegroundColor Red
    }
  }
}

function Show-OaUsage {
  Write-Host '协同办公平台 一键启停 (PowerShell)' -ForegroundColor Cyan
  Write-Host '用法: .\oa.ps1 { start | stop | restart | status }'
  Write-Host '  start   启动 后端(10001) + PC(10002) + 移动(10003)'
  Write-Host '  stop    停止全部'
  Write-Host '  restart 重启全部'
  Write-Host '  status  查看端口监听状态'
}

switch ($Action) {
  'start'   { Write-Host '启动协同办公平台 ...'; Start-Oa }
  'stop'    { Write-Host '停止协同办公平台 ...'; Stop-Oa }
  'restart' { Write-Host '重启协同办公平台 ...'; Stop-Oa; Start-Sleep -Seconds 3; Start-Oa }
  'status'  { Show-OaStatus }
  default   { Show-OaUsage }
}

