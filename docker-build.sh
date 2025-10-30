#!/bin/bash

# Build and run the Docker container for the in-memory-apis project

echo "🏗️  Building Docker image..."
docker build -t reckless-bank/in-memory-apis:latest .

if [ $? -eq 0 ]; then
    echo "✅ Docker image built successfully!"
    echo ""
    echo "🚀 Starting the application..."
    docker run -d \
        --name reckless-bank-api \
        -p 8080:8080 \
        --restart unless-stopped \
        reckless-bank/in-memory-apis:latest
    
    echo ""
    echo "📋 Container Status:"
    docker ps | grep reckless-bank-api
    
    echo ""
    echo "🔗 Application will be available at:"
    echo "   Main API: http://localhost:8080"
    echo "   Health Check: http://localhost:8080/actuator/health"
    echo "   Actuator: http://localhost:8080/actuator"
    
    echo ""
    echo "📝 Useful commands:"
    echo "   View logs: docker logs -f reckless-bank-api"
    echo "   Stop container: docker stop reckless-bank-api"
    echo "   Remove container: docker rm reckless-bank-api"
else
    echo "❌ Docker build failed!"
    exit 1
fi