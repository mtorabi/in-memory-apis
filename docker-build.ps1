# PowerShell script to build and run the Docker container

Write-Host "Building Docker image..." -ForegroundColor Yellow
docker build -t reckless-bank/in-memory-apis:latest .

if ($LASTEXITCODE -eq 0) {
    Write-Host "Docker image built successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Starting the application..." -ForegroundColor Yellow
    
    # Stop and remove existing container if it exists
    docker stop reckless-bank-api 2>$null
    docker rm reckless-bank-api 2>$null
    
    docker run -d `
        --name reckless-bank-api `
        -p 8080:8080 `
        --restart unless-stopped `
        reckless-bank/in-memory-apis:latest
    
    Write-Host ""
    Write-Host "Container Status:" -ForegroundColor Cyan
    docker ps | Select-String "reckless-bank-api"
    
    Write-Host ""
    Write-Host "Application will be available at:" -ForegroundColor Green
    Write-Host "   Main API: http://localhost:8080" -ForegroundColor White
    Write-Host "   Health Check: http://localhost:8080/actuator/health" -ForegroundColor White
    Write-Host "   Actuator: http://localhost:8080/actuator" -ForegroundColor White
    
    Write-Host ""
    Write-Host "Useful commands:" -ForegroundColor Cyan
    Write-Host "   View logs: docker logs -f reckless-bank-api" -ForegroundColor White
    Write-Host "   Stop container: docker stop reckless-bank-api" -ForegroundColor White
    Write-Host "   Remove container: docker rm reckless-bank-api" -ForegroundColor White
} else {
    Write-Host "Docker build failed!" -ForegroundColor Red
    exit 1
}