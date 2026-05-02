#!/usr/bin/env bash
set -euo pipefail

# -----------------------------
# Config
# -----------------------------
NETWORK_NAME="${NETWORK_NAME:-bench-net}"

ZAPPY_IMAGE="${ZAPPY_IMAGE:-zappydb-server}"
REDIS_IMAGE="${REDIS_IMAGE:-redis:7-alpine}"
BENCH_IMAGE="${BENCH_IMAGE:-zappydb-benchmark}"

ZAPPY_CONTAINER="${ZAPPY_CONTAINER:-zappydb-server}"
REDIS_CONTAINER="${REDIS_CONTAINER:-redis-server}"

CPU_LIMIT="${CPU_LIMIT:-2}"
MEMORY_LIMIT="${MEMORY_LIMIT:-2g}"
CPUSET="${CPUSET:-0-1}"

ENV_FILE="${ENV_FILE:-benchmark.env}"

# -----------------------------
# Helpers
# -----------------------------
require() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1"
    exit 1
  }
}

docker_run_common() {
  local name="$1"; shift
  local image="$1"; shift

  docker run -d \
    --name "$name" \
    --network "$NETWORK_NAME" \
    --cpus="$CPU_LIMIT" \
    --memory="$MEMORY_LIMIT" \
    ${CPUSET:+--cpuset-cpus="$CPUSET"} \
    "$image" "$@"
}

wait_for_port() {
  local host="$1"
  local port="$2"

  echo "Waiting for $host:$port ..."
  docker run --rm --network "$NETWORK_NAME" alpine:3.18 sh -c "
    apk add --no-cache netcat-openbsd >/dev/null 2>&1;
    for i in \$(seq 1 60); do
      nc -z $host $port && exit 0
      sleep 1
    done
    exit 1
  "
}

cleanup() {
  docker rm -f "$ZAPPY_CONTAINER" >/dev/null 2>&1 || true
  docker rm -f "$REDIS_CONTAINER" >/dev/null 2>&1 || true
  docker network rm "$NETWORK_NAME" >/dev/null 2>&1 || true
}
trap cleanup EXIT

# -----------------------------
# Preconditions
# -----------------------------
require docker
docker info >/dev/null 2>&1 || {
  echo "Docker daemon not running"
  exit 1
}

[ -f "$ENV_FILE" ] || {
  echo "Missing env file: $ENV_FILE"
  exit 1
}

# -----------------------------
# Build images
# -----------------------------
echo "Building images..."
docker build -f Dockerfile.server -t "$ZAPPY_IMAGE" . >/dev/null
docker build -f Dockerfile.benchmark -t "$BENCH_IMAGE" . >/dev/null

# -----------------------------
# Network
# -----------------------------
docker network create "$NETWORK_NAME" >/dev/null 2>&1 || true

# -----------------------------
# Start ZappyDB
# -----------------------------
echo "Starting ZappyDB..."
docker rm -f "$ZAPPY_CONTAINER" >/dev/null 2>&1 || true
docker run -d \
  --name "$ZAPPY_CONTAINER" \
  --network "$NETWORK_NAME" \
  --cpus="$CPU_LIMIT" \
  --memory="$MEMORY_LIMIT" \
  ${CPUSET:+--cpuset-cpus="$CPUSET"} \
  -e BENCH_MODE=true \
  "$ZAPPY_IMAGE" \
  java -cp target/classes org.perfect047.Main

# -----------------------------
# Start Redis
# -----------------------------
echo "Starting Redis..."
docker rm -f "$REDIS_CONTAINER" >/dev/null 2>&1 || true
docker_run_common "$REDIS_CONTAINER" "$REDIS_IMAGE"

# -----------------------------
# Wait for readiness
# -----------------------------
wait_for_port "$ZAPPY_CONTAINER" 6379
wait_for_port "$REDIS_CONTAINER" 6379

# -----------------------------
# Warmup (important for fairness)
# -----------------------------
echo "Warming up..."

docker run --rm \
  --network "$NETWORK_NAME" \
  --cpus="$CPU_LIMIT" \
  --memory="$MEMORY_LIMIT" \
  ${CPUSET:+--cpuset-cpus="$CPUSET"} \
  --env-file "$ENV_FILE" \
  -e BENCH_HOST="$ZAPPY_CONTAINER" \
  -e BENCH_PORT=6379 \
  -e BENCH_REQUESTS=100000 \
  -e BENCH_CONCURRENCY=20 \
  -e BENCH_PIPELINE=64 \
  "$BENCH_IMAGE" >/dev/null 2>&1

docker run --rm \
  --network "$NETWORK_NAME" \
  --cpus="$CPU_LIMIT" \
  --memory="$MEMORY_LIMIT" \
  ${CPUSET:+--cpuset-cpus="$CPUSET"} \
  --env-file "$ENV_FILE" \
  -e BENCH_HOST="$REDIS_CONTAINER" \
  -e BENCH_PORT=6379 \
  -e BENCH_REQUESTS=100000 \
  -e BENCH_CONCURRENCY=20 \
  -e BENCH_PIPELINE=64 \
  "$BENCH_IMAGE" >/dev/null 2>&1

# -----------------------------
# Actual Benchmarks
# -----------------------------
echo ""
echo "======================================"
echo "=== ZappyDB Benchmark (Unified) ======"
echo "======================================"

docker run --rm \
  --network "$NETWORK_NAME" \
  --cpus="$CPU_LIMIT" \
  --memory="$MEMORY_LIMIT" \
  ${CPUSET:+--cpuset-cpus="$CPUSET"} \
  --env-file "$ENV_FILE" \
  -e BENCH_HOST="$ZAPPY_CONTAINER" \
  -e BENCH_PORT=6379 \
  -e BENCH_PIPELINE=64 \
  "$BENCH_IMAGE"

echo ""
echo "======================================"
echo "=== Redis Benchmark (Unified) ========"
echo "======================================"

docker rm -f "$ZAPPY_CONTAINER" >/dev/null 2>&1 || true
sleep 2

docker run --rm \
  --network "$NETWORK_NAME" \
  --cpus="$CPU_LIMIT" \
  --memory="$MEMORY_LIMIT" \
  ${CPUSET:+--cpuset-cpus="$CPUSET"} \
  --env-file "$ENV_FILE" \
  -e BENCH_HOST="$REDIS_CONTAINER" \
  -e BENCH_PORT=6379 \
  -e BENCH_PIPELINE=64 \
  "$BENCH_IMAGE"