#!/bin/bash

# RoadVault Build Script
# This script helps build the application in different modes

set -e

echo "🚗 RoadVault Build Script"
echo "========================"

# Function to display help
show_help() {
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "  dev       - Build for development (skip frontend build)"
    echo "  prod      - Build for production (include frontend build)"
    echo "  frontend  - Build only the frontend"
    echo "  clean     - Clean all build artifacts"
    echo "  help      - Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 dev      # Quick build for development"
    echo "  $0 prod     # Full production build"
    echo "  $0 frontend # Build only the Vue.js frontend"
    echo ""
    echo "Prerequisites:"
    echo "  - Node.js and npm must be installed"
    echo "  - Run 'npm ci' in src/frontend/ if node_modules missing"
}

# Function to check prerequisites
check_prerequisites() {
    if ! command -v node &> /dev/null; then
        echo "❌ Node.js is not installed. Please install Node.js first."
        exit 1
    fi
    
    if ! command -v npm &> /dev/null; then
        echo "❌ npm is not installed. Please install npm first."
        exit 1
    fi
    
    echo "✅ Prerequisites check passed"
    echo "   Node.js: $(node --version)"
    echo "   npm: $(npm --version)"
}

# Function to build frontend only
build_frontend() {
    echo "🔨 Building Vue.js frontend..."
    cd src/frontend
    
    if [ ! -d "node_modules" ]; then
        echo "📦 Installing npm dependencies..."
        npm ci
    fi
    
    npm run build
    cd ../..
    echo "✅ Frontend build completed!"
}

# Function to clean build artifacts
clean_build() {
    echo "🧹 Cleaning build artifacts..."
    mvn clean
    rm -rf src/frontend/dist
    rm -rf src/frontend/node_modules/.cache
    echo "✅ Clean completed!"
}

# Main script logic
case "${1:-prod}" in
    "dev")
        check_prerequisites
        echo "🔨 Building for development (skipping frontend)..."
        mvn clean package -Pdev -DskipTests
        echo "✅ Development build completed!"
        echo "💡 Run 'cd src/frontend && npm run dev' in a separate terminal for frontend development"
        ;;
    "prod")
        check_prerequisites
        echo "🔨 Building for production (including frontend)..."
        mvn clean package -Pprod -DskipTests
        echo "✅ Production build completed!"
        echo "🚀 Your application JAR is ready in target/ directory"
        ;;
    "frontend")
        check_prerequisites
        build_frontend
        ;;
    "clean")
        clean_build
        ;;
    "help"|"-h"|"--help")
        show_help
        ;;
    *)
        echo "❌ Unknown option: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
