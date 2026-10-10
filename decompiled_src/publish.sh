#!/bin/bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MSG="${1:-Update FlixCore}"

echo "==========================================="
echo "   FlixCore Git & Release Publisher"
echo "==========================================="
echo "Commit message: $MSG"

cd "$DIR"

# Stage changes (excluding ignored files)
git add -A

# Check if there are changes to commit
if git diff-index --quiet HEAD --; then
    echo "No uncommitted changes in git. Using latest commit."
else
    git commit -m "$MSG"
    echo "Changes committed."
fi

# Push to GitHub
echo "Pushing to GitHub origin/main..."
git push origin main

# Build and release
echo "Building FlixCore and uploading release asset..."
if [ -d "$DIR/decompiled_src" ]; then
    bash "$DIR/decompiled_src/build.sh" --release
elif [ -f "$DIR/build.sh" ]; then
    bash "$DIR/build.sh" --release
fi

echo "==========================================="
echo "   PUBLISHED SUCCESSFULLY!"
echo "   Latest Release: https://github.com/tejasjhain-sudo/FlixCore/releases/latest"
echo "   Download URL:   https://github.com/tejasjhain-sudo/FlixCore/releases/latest/download/FlixCore.jar"
echo "==========================================="
