#!/bin/bash

# ZappyDB Test Runner Script
# This script runs comprehensive tests for all commands

echo "Running ZappyDB Command Tests..."

# Run unit tests
echo "Running unit tests..."
mvn test

# Check if tests passed
if [ $? -eq 0 ]; then
    echo "All unit tests passed successfully!"
    echo "Test results:"
    find target/surefire-reports -name "*.txt" -exec grep -H "Tests run:" {} \;
else
    echo "Some unit tests failed!"
    echo "Check target/surefire-reports for details."
    exit 1
fi

echo "Test execution completed."