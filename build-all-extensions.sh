#!/bin/bash

# Build script for all AIX AppDynamics Extensions
set -e

echo "Building AppDynamics AIX Extensions..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven 3.6+ to build the extensions."
    exit 1
fi

# Check Java version
if ! command -v java &> /dev/null; then
    print_error "Java is not installed. Please install Java 8+ to build the extensions."
    exit 1
fi

# Create artifacts directory
mkdir -p deployment/artifacts

# Extensions to build
extensions=(
    "process-monitor-extension"
    "nfs-monitor-extension" 
    "service-monitor-extension"
    "file-change-monitor-extension"
)

# Build each extension
for extension in "${extensions[@]}"; do
    print_status "Building $extension..."
    
    if [ -d "$extension" ]; then
        cd "$extension"
        
        # Clean and compile
        mvn clean compile -q
        if [ $? -ne 0 ]; then
            print_error "Failed to compile $extension"
            exit 1
        fi
        
        # Package
        mvn package -q
        if [ $? -ne 0 ]; then
            print_error "Failed to package $extension"
            exit 1
        fi
        
        # Copy artifact to deployment directory
        if [ -f "target/*.zip" ]; then
            cp target/*-dist.zip "../deployment/artifacts/"
            print_status "✓ Built $extension successfully"
        else
            print_warning "No zip artifact found for $extension"
        fi
        
        cd ..
    else
        print_error "Extension directory $extension not found"
        exit 1
    fi
done

print_status "All extensions built successfully!"
print_status "Artifacts are available in: deployment/artifacts/"

# List built artifacts
echo ""
echo "Built artifacts:"
ls -la deployment/artifacts/

echo ""
print_status "Ready for deployment with Ansible!"
print_status "Run: ansible-playbook deployment/ansible-playbooks/aix-extensions-deploy.yml"