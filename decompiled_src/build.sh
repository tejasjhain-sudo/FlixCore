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

COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "main")
BUILD_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
cat <<EOF > "$DIR/git-version.properties"
git.commit=$COMMIT
git.branch=$BRANCH
git.build_time=$BUILD_TIME
git.version=4.7.0
git.repo=https://github.com/tejasjhain-sudo/FlixCore
EOF

ADV_KEY="$M2_REPO/net/kyori/adventure-key/4.17.0/adventure-key-4.17.0.jar"
ADV_API="$M2_REPO/net/kyori/adventure-api/4.17.0/adventure-api-4.17.0.jar"
BUNGEE_JAR="$M2_REPO/net/md-5/bungeecord-chat/1.20-R0.2/bungeecord-chat-1.20-R0.2.jar"
GSON_JAR="$M2_REPO/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar"

echo "[1/3] Compiling helper classes (dual placeholder expansion, safety helper, updater, license client & transformer)..."
"$JAVAC" -cp "$SOURCE_JAR:$PAPI_JAR:$PAPER_JAR" -d "$BUILD_DIR/classes" "$DIR/org/lime/swiftCore/o/d.java"
"$JAVAC" -sourcepath "" -cp "$SOURCE_JAR:$PAPER_JAR:$ADV_KEY:$ADV_API" -d "$BUILD_DIR/classes" "$DIR/src/main/java/org/lime/swiftCore/arena/ArenaSafetyHelper.java"
"$JAVAC" -sourcepath "" -cp "$SOURCE_JAR:$PAPER_JAR:$ADV_KEY:$ADV_API:$BUNGEE_JAR" -d "$BUILD_DIR/classes" "$DIR/org/lime/swiftCore/updater/FlixCoreUpdater.java"
"$JAVAC" -cp "$SOURCE_JAR:$PAPER_JAR:$GSON_JAR" -d "$BUILD_DIR/classes" "$DIR/club/aspvp/license/LicenseClient.java"
"$JAVAC" -cp "$ASM_JAR:$ASM_TREE_JAR" -d "$BUILD_DIR/classes" "$DIR/src/main/java/FlixCoreTransformer.java"

echo "[2/3] Transforming bytecode and packaging FlixCore jar..."
"$JAVA" -cp "$BUILD_DIR/classes:$ASM_JAR:$ASM_TREE_JAR" FlixCoreTransformer "$SOURCE_JAR" "$DIR" "$BUILD_DIR/FlixCore-4.7.0.jar"

echo "[3/3] Finalizing output..."
cp "$BUILD_DIR/FlixCore-4.7.0.jar" "$DIR/FlixCore-4.7.0.jar"
cp "$BUILD_DIR/FlixCore-4.7.0.jar" "$PARENT_DIR/FlixCore-4.7.0.jar"
cp "$BUILD_DIR/FlixCore-4.7.0.jar" "/Users/tejas/Desktop/FlixCore.jar"
cp "$BUILD_DIR/FlixCore-4.7.0.jar" "/Users/tejas/Desktop/FlixCore-4.7.0.jar"
if [ -d "$PARENT_DIR/test_server/plugins" ]; then
    cp "$BUILD_DIR/FlixCore-4.7.0.jar" "$PARENT_DIR/test_server/plugins/FlixCore.jar"
fi

if [ "$1" == "--release" ] || [ "$RELEASE" == "1" ]; then
    echo "==========================================="
    echo "   Publishing to GitHub Releases..."
    echo "==========================================="
    GH_TOKEN="${GITHUB_TOKEN:-}"
    if [ -z "$GH_TOKEN" ] && [ -f "$PARENT_DIR/.gh_token" ]; then
        GH_TOKEN="$(tr -d '[:space:]' < "$PARENT_DIR/.gh_token")"
    fi
    if [ -z "$GH_TOKEN" ] && [ -f "$DIR/.gh_token" ]; then
        GH_TOKEN="$(tr -d '[:space:]' < "$DIR/.gh_token")"
    fi
    if [ -z "$GH_TOKEN" ]; then
        GH_TOKEN="$(git remote get-url origin 2>/dev/null | sed -n 's/.*:\(ghp_[^@]*\)@.*/\1/p')"
    fi
    REPO="tejasjhain-sudo/FlixCore"

    RELEASE_DATA=$(curl -s -H "Authorization: token $GH_TOKEN" "https://api.github.com/repos/$REPO/releases/tags/v4.7.0")
    RELEASE_ID=$(echo "$RELEASE_DATA" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null || true)

    if [ -z "$RELEASE_ID" ] || [ "$RELEASE_ID" == "None" ] || [ "$RELEASE_ID" == "null" ]; then
        echo "Creating release v4.7.0..."
        RELEASE_DATA=$(curl -s -X POST \
            -H "Authorization: token $GH_TOKEN" \
            -H "Accept: application/vnd.github+json" \
            "https://api.github.com/repos/$REPO/releases" \
            -d '{"tag_name":"v4.7.0","target_commitish":"main","name":"FlixCore v4.7.0","body":"Automated build release for FlixCore","draft":false,"prerelease":false}')
        RELEASE_ID=$(echo "$RELEASE_DATA" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null || true)
    fi

    echo "Release ID: $RELEASE_ID"

    # Find existing FlixCore.jar asset ID and delete it
    ASSETS_DATA=$(curl -s -H "Authorization: token $GH_TOKEN" "https://api.github.com/repos/$REPO/releases/$RELEASE_ID/assets")
    OLD_ASSET_ID=$(echo "$ASSETS_DATA" | python3 -c "
import sys, json
for a in json.load(sys.stdin):
    if a.get('name') == 'FlixCore.jar':
        print(a.get('id', ''))
        break
" 2>/dev/null || true)

    if [ -n "$OLD_ASSET_ID" ] && [ "$OLD_ASSET_ID" != "None" ]; then
        echo "Replacing existing release asset ID $OLD_ASSET_ID..."
        curl -s -X DELETE -H "Authorization: token $GH_TOKEN" "https://api.github.com/repos/$REPO/releases/assets/$OLD_ASSET_ID"
    fi

    echo "Uploading FlixCore.jar (Commit: $COMMIT)..."
    UPLOAD_RES=$(curl -s -X POST \
        -H "Authorization: token $GH_TOKEN" \
        -H "Content-Type: application/java-archive" \
        "https://uploads.github.com/repos/$REPO/releases/$RELEASE_ID/assets?name=FlixCore.jar" \
        --data-binary "@$BUILD_DIR/FlixCore-4.7.0.jar")

    DOWNLOAD_URL=$(echo "$UPLOAD_RES" | python3 -c "import sys, json; print(json.load(sys.stdin).get('browser_download_url', ''))" 2>/dev/null || true)
    echo "Release published successfully!"
    echo "Download URL: $DOWNLOAD_URL"
fi

echo "==========================================="
echo "   BUILD SUCCESSFUL!"
echo "   Commit: $COMMIT ($BRANCH)"
echo "   Output: $DIR/FlixCore-4.7.0.jar"
echo "           $PARENT_DIR/FlixCore-4.7.0.jar"
echo "           /Users/tejas/Desktop/FlixCore.jar"
echo "           /Users/tejas/Desktop/FlixCore-4.7.0.jar"
echo "==========================================="
