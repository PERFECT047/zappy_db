#!/bin/bash

set -euo pipefail

IMAGE_NAME="${IMAGE_NAME:-zappydb-benchmark}"
CPU_LIMIT="${CPU_LIMIT:-4}"
MEMORY_LIMIT="${MEMORY_LIMIT:-1g}"
ENV_FILE="${ENV_FILE:-benchmark.env}"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker CLI is not installed."
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "Docker daemon is not running. Start Docker Desktop or your Docker service and retry."
  exit 1
fi

if [ ! -f "${ENV_FILE}" ]; then
  echo "Benchmark env file not found: ${ENV_FILE}"
  exit 1
fi

echo "Building benchmark image: ${IMAGE_NAME}"
docker build -f Dockerfile.benchmark -t "${IMAGE_NAME}" .

echo "Running benchmark in container..."
docker run --rm \
  --cpus="${CPU_LIMIT}" \
  --memory="${MEMORY_LIMIT}" \
  --env-file "${ENV_FILE}" \
  "${IMAGE_NAME}"
