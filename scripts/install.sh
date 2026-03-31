#!/usr/bin/env bash

set -e

REPO="bhumitbedse/NeoHub"
BINARY_NAME="neostore"
VERSION="latest"

echo "🚀 Installing NeoHub CLI..."

# Detect OS
OS=$(uname | tr '[:upper:]' '[:lower:]')

# Detect ARCH
ARCH=$(uname -m)
case $ARCH in
    x86_64) ARCH="amd64" ;;
    arm64 | aarch64) ARCH="arm64" ;;
    *) echo "❌ Unsupported architecture: $ARCH"; exit 1 ;;
esac

# Map OS names
case $OS in
    linux) PLATFORM="linux" ;;
    darwin) PLATFORM="darwin" ;;
    *) echo "❌ Unsupported OS: $OS"; exit 1 ;;
esac

FILE_NAME="${BINARY_NAME}-${PLATFORM}-${ARCH}"

# Windows not supported via script
if [[ "$PLATFORM" == "windows" ]]; then
    echo "❌ Please download manually for Windows"
    exit 1
fi

# Download URL
URL="https://github.com/${REPO}/releases/latest/download/${FILE_NAME}"

echo "⬇️ Downloading $FILE_NAME..."

curl -L "$URL" -o "$BINARY_NAME"

chmod +x "$BINARY_NAME"

echo "📦 Installing to /usr/local/bin (may require sudo)..."

if mv "$BINARY_NAME" /usr/local/bin/ 2>/dev/null; then
    echo "✅ Installed successfully!"
else
    sudo mv "$BINARY_NAME" /usr/local/bin/
    echo "✅ Installed successfully (with sudo)!"
fi

echo ""
echo "🎉 NeoHub CLI installed!"
echo "👉 Run: neostore install telescope"