# Docker Management Script for Currency Exchange Service
# Usage: .\docker-manager.ps1 [command]

param(
    [Parameter(Position=0)]
    [ValidateSet('build', 'up', 'down', 'restart', 'logs', 'status', 'clean', 'test', 'help')]
    [string]$Command = 'help'
)

$ErrorActionPreference = "Stop"

function Show-Help {
    Write-Host "`nCurrency Exchange Service - Docker Manager" -ForegroundColor Cyan
    Write-Host "=========================================`n" -ForegroundColor Cyan
    Write-Host "Usage: .\docker-manager.ps1 [command]`n"
    Write-Host "Commands:" -ForegroundColor Yellow
    Write-Host "  build     - Build all Docker images"
    Write-Host "  up        - Start all services (detached mode)"
    Write-Host "  down      - Stop all services"
    Write-Host "  restart   - Restart all services"
    Write-Host "  logs      - Show logs from all services"
    Write-Host "  status    - Show status of all services"
    Write-Host "  clean     - Stop services and remove volumes"
    Write-Host "  test      - Test all service endpoints"
    Write-Host "  help      - Show this help message`n"
}

function Build-Services {
    Write-Host "`n==> Building Docker images..." -ForegroundColor Green
    docker-compose build
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✓ Build completed successfully!" -ForegroundColor Green
    } else {
        Write-Host "`n✗ Build failed!" -ForegroundColor Red
        exit 1
    }
}

function Start-Services {
    Write-Host "`n==> Starting all services..." -ForegroundColor Green
    docker-compose up -d
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✓ Services started successfully!" -ForegroundColor Green
        Write-Host "`nWaiting for services to be healthy..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
        Show-Status
        Write-Host "`nService URLs:" -ForegroundColor Cyan
        Write-Host "  Main App:      http://localhost:8080" -ForegroundColor White
        Write-Host "  Swagger UI:    http://localhost:8080/swagger-ui.html" -ForegroundColor White
        Write-Host "  Health Check:  http://localhost:8080/actuator/health" -ForegroundColor White
        Write-Host "  Mock Service 1: http://localhost:8081/rates?base=USD" -ForegroundColor White
        Write-Host "  Mock Service 2: http://localhost:8082/api/rates?from=USD" -ForegroundColor White
        Write-Host "  PostgreSQL:    localhost:5432`n" -ForegroundColor White
    } else {
        Write-Host "`n✗ Failed to start services!" -ForegroundColor Red
        exit 1
    }
}

function Stop-Services {
    Write-Host "`n==> Stopping all services..." -ForegroundColor Green
    docker-compose down
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✓ Services stopped successfully!" -ForegroundColor Green
    } else {
        Write-Host "`n✗ Failed to stop services!" -ForegroundColor Red
        exit 1
    }
}

function Restart-Services {
    Write-Host "`n==> Restarting all services..." -ForegroundColor Green
    Stop-Services
    Start-Services
}

function Show-Logs {
    Write-Host "`n==> Showing service logs (Ctrl+C to exit)..." -ForegroundColor Green
    docker-compose logs -f
}

function Show-Status {
    Write-Host "`n==> Service Status:" -ForegroundColor Green
    docker-compose ps
}

function Clean-Services {
    Write-Host "`n==> Cleaning up (stopping services and removing volumes)..." -ForegroundColor Yellow
    Write-Host "WARNING: This will delete all data in the database!" -ForegroundColor Red
    $confirmation = Read-Host "Are you sure? (yes/no)"
    if ($confirmation -eq 'yes') {
        docker-compose down -v
        Write-Host "`n✓ Cleanup completed!" -ForegroundColor Green
    } else {
        Write-Host "`nCleanup cancelled." -ForegroundColor Yellow
    }
}

function Test-Services {
    Write-Host "`n==> Testing service endpoints..." -ForegroundColor Green

    Write-Host "`nTesting PostgreSQL..." -ForegroundColor Cyan
    docker exec currency-exchange-db pg_isready -U admin -d currency_exchange
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ PostgreSQL is ready" -ForegroundColor Green
    } else {
        Write-Host "✗ PostgreSQL is not ready" -ForegroundColor Red
    }

    Write-Host "`nTesting Mock Service 1..." -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8081/rates?base=USD" -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ Mock Service 1 is responding" -ForegroundColor Green
            Write-Host "  Response preview: $($response.Content.Substring(0, [Math]::Min(100, $response.Content.Length)))..." -ForegroundColor Gray
        }
    } catch {
        Write-Host "✗ Mock Service 1 is not responding" -ForegroundColor Red
    }

    Write-Host "`nTesting Mock Service 2..." -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8082/api/rates?from=USD" -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ Mock Service 2 is responding" -ForegroundColor Green
            Write-Host "  Response preview: $($response.Content.Substring(0, [Math]::Min(100, $response.Content.Length)))..." -ForegroundColor Gray
        }
    } catch {
        Write-Host "✗ Mock Service 2 is not responding" -ForegroundColor Red
    }

    Write-Host "`nTesting Main Application..." -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ Main Application is responding" -ForegroundColor Green
            Write-Host "  Health status: $($response.Content)" -ForegroundColor Gray
        }
    } catch {
        Write-Host "✗ Main Application is not responding" -ForegroundColor Red
        Write-Host "  This is normal if the app is still starting up. Wait 30-60 seconds." -ForegroundColor Yellow
    }

    Write-Host ""
}

# Main script execution
switch ($Command) {
    'build' { Build-Services }
    'up' { Start-Services }
    'down' { Stop-Services }
    'restart' { Restart-Services }
    'logs' { Show-Logs }
    'status' { Show-Status }
    'clean' { Clean-Services }
    'test' { Test-Services }
    'help' { Show-Help }
    default { Show-Help }
}

