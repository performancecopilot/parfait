#!/bin/sh
set -e

IMAGE_NAME="parfait-test"

echo "Building test container..."
podman build -t "$IMAGE_NAME" .

if [ $# -eq 0 ]; then
    echo "Running: mvn -B clean verify"
    podman run --rm -v "$(pwd):/parfait:Z" "$IMAGE_NAME"
else
    echo "Running: $*"
    podman run --rm -v "$(pwd):/parfait:Z" "$IMAGE_NAME" "$@"
fi
