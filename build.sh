#!/bin/bash

###############################################################################
# Build and Test Script for Copilot Workshop
# Usage: ./build.sh [options]
# Options:
#   --skip-tests      Skip running tests
#   --quality         Run quality checks (CheckStyle, PMD, SpotBugs)
#   --coverage        Generate code coverage reports
#   --mutation        Run mutation testing (slow)
#   --profile=<name>  Use specific Maven profile (dev, test, prod)
#   --help            Show this help message
###############################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
SKIP_TESTS=false
RUN_QUALITY=false
RUN_COVERAGE=false
RUN_MUTATION=false
MAVEN_PROFILE="dev"

# Parse command line arguments
for arg in "$@"; do
    case $arg in
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --quality)
            RUN_QUALITY=true
            shift
            ;;
        --coverage)
            RUN_COVERAGE=true
            shift
            ;;
        --mutation)
            RUN_MUTATION=true
            shift
            ;;
        --profile=*)
            MAVEN_PROFILE="${arg#*=}"
            shift
            ;;
        --help)
            echo "Build and Test Script for Copilot Workshop"
            echo "Usage: ./build.sh [options]"
            echo ""
            echo "Options:"
            echo "  --skip-tests      Skip running tests"
            echo "  --quality         Run quality checks (CheckStyle, PMD, SpotBugs)"
            echo "  --coverage        Generate code coverage reports"
            echo "  --mutation        Run mutation testing (slow)"
            echo "  --profile=<name>  Use specific Maven profile (dev, test, prod)"
            echo "  --help            Show this help message"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $arg${NC}"
            exit 1
            ;;
    esac
done

echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}  Copilot Workshop Build Script${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""

# Print build configuration
echo -e "${YELLOW}Build Configuration:${NC}"
echo "  Maven Profile: $MAVEN_PROFILE"
echo "  Skip Tests: $SKIP_TESTS"
echo "  Run Quality Checks: $RUN_QUALITY"
echo "  Run Coverage: $RUN_COVERAGE"
echo "  Run Mutation Testing: $RUN_MUTATION"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Maven is not installed. Please install Maven first.${NC}"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ -z "$JAVA_VERSION" ]; then
    # Fallback for different version format
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk '{print $3}' | tr -d '"' | cut -d'.' -f1)
fi

if [ -z "$JAVA_VERSION" ] || [ "$JAVA_VERSION" -lt 21 ]; then
    echo -e "${RED}Java 21 or higher is required. Current version: ${JAVA_VERSION:-unknown}${NC}"
    exit 1
fi

echo -e "${GREEN}Java version: $JAVA_VERSION${NC}"
echo ""

# Clean previous builds
echo -e "${YELLOW}Cleaning previous builds...${NC}"
mvn clean -P$MAVEN_PROFILE

# Compile the project
echo -e "${YELLOW}Compiling project...${NC}"
mvn compile -P$MAVEN_PROFILE

# Run tests if not skipped
if [ "$SKIP_TESTS" = false ]; then
    echo -e "${YELLOW}Running tests...${NC}"
    mvn test -P$MAVEN_PROFILE
else
    echo -e "${YELLOW}Skipping tests...${NC}"
fi

# Package the application
echo -e "${YELLOW}Packaging application...${NC}"
if [ "$SKIP_TESTS" = true ]; then
    mvn package -DskipTests -P$MAVEN_PROFILE
else
    mvn package -P$MAVEN_PROFILE
fi

# Run quality checks if requested
if [ "$RUN_QUALITY" = true ]; then
    echo -e "${YELLOW}Running quality checks...${NC}"
    mvn checkstyle:check pmd:check spotbugs:check -P$MAVEN_PROFILE
fi

# Generate coverage reports if requested
if [ "$RUN_COVERAGE" = true ]; then
    echo -e "${YELLOW}Generating coverage reports...${NC}"
    mvn jacoco:report -Pcoverage
    echo -e "${GREEN}Coverage report generated at: target/site/jacoco/index.html${NC}"
fi

# Run mutation testing if requested
if [ "$RUN_MUTATION" = true ]; then
    echo -e "${YELLOW}Running mutation testing (this may take a while)...${NC}"
    mvn org.pitest:pitest-maven:mutationCoverage -Pmutation
    echo -e "${GREEN}Mutation report generated at: target/pit-reports/index.html${NC}"
fi

echo ""
echo -e "${GREEN}=====================================${NC}"
echo -e "${GREEN}  Build completed successfully!${NC}"
echo -e "${GREEN}=====================================${NC}"
echo ""
echo -e "${BLUE}Artifacts:${NC}"
echo "  JAR: target/copilot-workshop-1.0.0.jar"
if [ "$RUN_COVERAGE" = true ]; then
    echo "  Coverage Report: target/site/jacoco/index.html"
fi
if [ "$RUN_MUTATION" = true ]; then
    echo "  Mutation Report: target/pit-reports/index.html"
fi
echo ""
