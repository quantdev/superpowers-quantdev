#!/usr/bin/env bash
# Bisection script to find which test creates unwanted files/state
# Usage: ./find-polluter.sh <file_or_dir_to_check> [maven_module]
# Example: ./find-polluter.sh '.git' core
#          ./find-polluter.sh 'target/junk.db'      # single-module project

set -e

if [ $# -lt 1 ] || [ $# -gt 2 ]; then
  echo "Usage: $0 <file_to_check> [maven_module]"
  echo "Example: $0 '.git' core"
  exit 1
fi

POLLUTION_CHECK="$1"
MODULE="${2:-}"

if [ -n "$MODULE" ]; then
  TEST_ROOT="$MODULE/src/test/java"
  MVN_ARGS="-pl $MODULE"
else
  TEST_ROOT="src/test/java"
  MVN_ARGS=""
fi

echo "🔍 Searching for test that creates: $POLLUTION_CHECK"
echo "Test root: $TEST_ROOT"
echo ""

# Get list of test classes (surefire convention: *Test.java)
TEST_FILES=$(find "$TEST_ROOT" -name '*Test.java' | sort)
TOTAL=$(echo "$TEST_FILES" | wc -l | tr -d ' ')

echo "Found $TOTAL test classes"
echo ""

COUNT=0
for TEST_FILE in $TEST_FILES; do
  COUNT=$((COUNT + 1))
  CLASS=$(basename "$TEST_FILE" .java)

  # Skip if pollution already exists
  if [ -e "$POLLUTION_CHECK" ]; then
    echo "⚠️  Pollution already exists before test $COUNT/$TOTAL"
    echo "   Skipping: $CLASS"
    continue
  fi

  echo "[$COUNT/$TOTAL] Testing: $CLASS"

  # Run just this test class
  mvn $MVN_ARGS test -Dtest="$CLASS" -DfailIfNoTests=false -q > /dev/null 2>&1 || true

  # Check if pollution appeared
  if [ -e "$POLLUTION_CHECK" ]; then
    echo ""
    echo "🎯 FOUND POLLUTER!"
    echo "   Test: $TEST_FILE"
    echo "   Created: $POLLUTION_CHECK"
    echo ""
    echo "Pollution details:"
    ls -la "$POLLUTION_CHECK"
    echo ""
    echo "To investigate:"
    echo "  mvn $MVN_ARGS test -Dtest=$CLASS    # Run just this test class"
    echo "  cat $TEST_FILE                       # Review test code"
    exit 1
  fi
done

echo ""
echo "✅ No polluter found - all tests clean!"
exit 0
