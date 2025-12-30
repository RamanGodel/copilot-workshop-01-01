#!/bin/bash

###############################################################################
# Test Execution Script for Copilot Workshop
# Usage: ./test.sh [options]
# Options:
#   --unit            Run unit tests only
#   --integration     Run integration tests only
#   --all             Run all tests (default)
#   --coverage        Generate coverage report
#   --mutation        Run mutation testing
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
TEST_TYPE="all"
RUN_COVERAGE=false
RUN_MUTATION=false
MAVEN_PROFILE="test"

# Parse command line arguments
for arg in "$@"; do
    case $arg in
        --unit)
            TEST_TYPE="unit"
            shift
            ;;
        --integration)
            TEST_TYPE="integration"
            shift
            ;;
        --all)
            TEST_TYPE="all"
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
            echo "Test Execution Script for Copilot Workshop"
            echo "Usage: ./test.sh [options]"
            echo ""
            echo "Options:"
            echo "  --unit            Run unit tests only"
            echo "  --integration     Run integration tests only"
            echo "  --all             Run all tests (default)"
            echo "  --coverage        Generate coverage report"
            echo "  --mutation        Run mutation testing"
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
echo -e "${BLUE}  Copilot Workshop Test Script${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""

# Print test configuration
echo -e "${YELLOW}Test Configuration:${NC}"
echo "  Test Type: $TEST_TYPE"
echo "  Maven Profile: $MAVEN_PROFILE"
echo "  Generate Coverage: $RUN_COVERAGE"
echo "  Run Mutation Testing: $RUN_MUTATION"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Maven is not installed. Please install Maven first.${NC}"
    exit 1
fi

# Run tests based on type
case $TEST_TYPE in
    unit)
        echo -e "${YELLOW}Running unit tests...${NC}"
        if [ "$RUN_COVERAGE" = true ]; then
            mvn test -P$MAVEN_PROFILE,coverage
        else
            mvn test -P$MAVEN_PROFILE
        fi
        ;;
    integration)
        echo -e "${YELLOW}Running integration tests...${NC}"
        if [ "$RUN_COVERAGE" = true ]; then
            mvn verify -P$MAVEN_PROFILE,coverage
        else
            mvn verify -P$MAVEN_PROFILE
        fi
        ;;
    all)
        echo -e "${YELLOW}Running all tests...${NC}"
        if [ "$RUN_COVERAGE" = true ]; then
            mvn verify -P$MAVEN_PROFILE,coverage
        else
            mvn verify -P$MAVEN_PROFILE
        fi
        ;;
esac

# Generate coverage report if requested
if [ "$RUN_COVERAGE" = true ]; then
    echo -e "${YELLOW}Generating coverage report...${NC}"
    mvn jacoco:report -Pcoverage
    echo ""
    echo -e "${GREEN}Coverage report generated at: target/site/jacoco/index.html${NC}"
    
    # Display coverage summary
    if [ -f "target/site/jacoco/index.html" ]; then
        echo -e "${BLUE}Opening coverage report...${NC}"
        echo "To view the report, open: file://$(pwd)/target/site/jacoco/index.html"
    fi
fi

# Run mutation testing if requested
if [ "$RUN_MUTATION" = true ]; then
    echo -e "${YELLOW}Running mutation testing (this may take a while)...${NC}"
    mvn org.pitest:pitest-maven:mutationCoverage -Pmutation
    echo ""
    echo -e "${GREEN}Mutation report generated at: target/pit-reports/index.html${NC}"
    echo "To view the report, open: file://$(pwd)/target/pit-reports/index.html"
fi

echo ""
echo -e "${GREEN}=====================================${NC}"
echo -e "${GREEN}  Tests completed successfully!${NC}"
echo -e "${GREEN}=====================================${NC}"
echo ""
