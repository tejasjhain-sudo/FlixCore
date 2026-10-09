#!/bin/bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PARENT_DIR="$(dirname "$DIR")"

# Locate Java 21
if [ -z "$JAVA_HOME" ]; then
    if [ -d "/Users/tejas/Library/Application Support/PrismLauncher/java/java-runtime-delta" ]; then
        export JAVA_HOME="/Users/tejas/Library/Application Support/PrismLauncher/java/java-runtime-delta"
    fi
fi

JAVAC="$JAVA_HOME/bin/javac"
JAVA="$JAVA_HOME/bin/java"

if [ ! -f "$JAVAC" ]; then
    JAVAC="javac"
    JAVA="java"
fi

echo "==========================================="
echo "   FlixCore Build Tool"
echo "==========================================="
echo "Using Java: $("$JAVA" -version 2>&1 | head -n 1)"

M2_REPO="$HOME/.m2/repository"
ASM_JAR="$M2_REPO/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar"
ASM_TREE_JAR="$M2_REPO/org/ow2/asm/asm-tree/9.7.1/asm-tree-9.7.1.jar"
PAPER_JAR="$M2_REPO/io/papermc/paper/paper-api/1.21.1-R0.1-SNAPSHOT/paper-api-1.21.1-R0.1-SNAPSHOT.jar"
PAPI_JAR="$M2_REPO/me/clip/placeholderapi/2.11.7/placeholderapi-2.11.7.jar"
ADVENTURE_JAR="$M2_REPO/net/kyori/adventure-api/4.17.0/adventure-api-4.17.0.jar"
BUNGEE_CHAT_JAR="$M2_REPO/net/md-5/bungeecord-chat/1.21-R0.2-deprecated+build.21/bungeecord-chat-1.21-R0.2-deprecated+build.21.jar"

SOURCE_JAR=""
if [ -f "$PARENT_DIR/SwiftCore-beta-4.7.0-obf.jar" ]; then
    SOURCE_JAR="$PARENT_DIR/SwiftCore-beta-4.7.0-obf.jar"
elif [ -f "$DIR/SwiftCore-beta-4.7.0-obf.jar" ]; then
    SOURCE_JAR="$DIR/SwiftCore-beta-4.7.0-obf.jar"
fi

if [ -z "$SOURCE_JAR" ]; then
    echo "ERROR: Could not find base jar SwiftCore-beta-4.7.0-obf.jar"
    exit 1
fi

BUILD_DIR="$DIR/target"
mkdir -p "$BUILD_DIR/classes"

echo "[1/3] Compiling helper classes (dual placeholder expansion, updater & transformer)..."
"$JAVAC" -cp "$SOURCE_JAR:$PAPI_JAR:$PAPER_JAR" -d "$BUILD_DIR/classes" "$DIR/decompiled_src/org/lime/swiftCore/o/d.java"
"$JAVAC" -cp "$SOURCE_JAR:$PAPER_JAR:$ADVENTURE_JAR:$BUNGEE_CHAT_JAR" -d "$BUILD_DIR/classes" "$DIR/decompiled_src/org/lime/swiftCore/updater/FlixCoreUpdater.java"
"$JAVAC" -cp "$ASM_JAR:$ASM_TREE_JAR" -d "$BUILD_DIR/classes" "$DIR/decompiled_src/src/main/java/FlixCoreTransformer.java"

# Generate git-version.properties
GIT_COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "initial")
GIT_BRANCH=$(git branch --show-current 2>/dev/null || echo "main")
[ -z "$GIT_BRANCH" ] && GIT_BRANCH="main"
BUILD_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

cat << EOF > "$DIR/decompiled_src/git-version.properties"
git.commit=$GIT_COMMIT
git.branch=$GIT_BRANCH
git.build_time=$BUILD_TIME
git.version=4.7.0
git.repo=https://github.com/tejasjhain-sudo/FlixCore
EOF

echo "[2/3] Transforming bytecode and assembling jar..."

"$JAVA" -cp "$BUILD_DIR/classes:$ASM_JAR:$ASM_TREE_JAR" FlixCoreTransformer "$SOURCE_JAR" "$DIR/decompiled_src" "$BUILD_DIR/FlixCore-4.7.0.jar"

echo "[3/3] Finalizing output..."
cp "$BUILD_DIR/FlixCore-4.7.0.jar" "$DIR/FlixCore-4.7.0.jar"
cp "$BUILD_DIR/FlixCore-4.7.0.jar" "$PARENT_DIR/FlixCore-4.7.0.jar"

echo "==========================================="
echo "   BUILD SUCCESSFUL!"
echo "   Output: $DIR/FlixCore-4.7.0.jar"
echo "           $PARENT_DIR/FlixCore-4.7.0.jar"
echo "==========================================="
