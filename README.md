# 🚀 ZappyDB - In-Memory Database (In-Progress)

[![Java Version](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.java.net/projects/jdk/25/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A high-performance, from-scratch implementation of an in-memory database in Java, inspired by Redis.

## ✨ Features

### Implemented
- ✅ TCP Server with non-blocking I/O
- ✅ RESP (Redis Serialization Protocol) parsing
- ✅ Multi-threaded client handling with thread pools
- ✅ Key-value store (GET, SET)
- ✅ List operations (LPUSH, RPUSH, LPOP, LLEN, LRANGE)
- ✅ Blocking list operations (BLPOP)
- ✅ Key expiration with TTL support
- ✅ Lock management for thread safety
- ✅ Environment-based configuration

### Planned Features
- 🔄 Data persistence with RDB snapshots
- 🔄 Replication and clustering support
- 🔄 Pub/Sub messaging
- 🔄 Lua scripting engine
- 🔄 Additional data types (Sets, Sorted Sets, Hashes)
- 🔄 Memory eviction policies

## 🚀 Quick Start

### Prerequisites
- Java 25+ (with preview features enabled)
- Maven 3.6+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/PERFECT047/zappy_db.git
   cd zappy_db
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run locally**
   ```bash
   ./zappy_run.sh
   ```

The server will start on port 6379 (default Redis port).

### Testing

```bash
./run_tests.sh
# Or with Maven
mvn test
```

## 📊 Performance Benchmarks

### Benchmark Scripts

```bash
# Run ZappyDB benchmark locally
./run_benchmarks.sh

# Run benchmark in container (ZappyDB)
./run_benchmarks_container.sh

# Run benchmark in container (Redis for comparison)
./run_redis_vs_zappy_benchmarks_container.sh
```

## 📊 Performance Benchmarks

### Benchmark Methodology

Benchmarks are executed using a **single unified benchmark client** (`ZappyDbBenchmark`) against both ZappyDB and Redis to ensure:

- Identical workload distribution
- Same concurrency model
- Same network conditions (Docker bridge)
- Same measurement logic (latency + throughput)
- No tool bias (`redis-benchmark` is NOT used)

This ensures a **fair, apples-to-apples comparison**.

---

### Benchmark Configuration

- **Java**: 25.0.2
- **Processors**: 4
- **Max Memory**: 494 MiB
- **Threads**: 4
- **Warmup Ops/Thread**: 5,000
- **Measured Ops/Thread**: 20,000
- **Key Space**: 1,024
- **List Key Space**: 256
- **Value Size**: 32 bytes
- **Blocking Pairs**: 2
- **Blocking Ops/Pair**: 2,000

---

## 🔹 Mixed Workload Benchmark

### ZappyDB

| Command | Throughput (ops/s) | Avg (µs) | P50 (µs) | P95 (µs) | P99 (µs) | Max (µs) |
|--------|-------------------|----------|----------|----------|----------|----------|
| PING   | 24,897 | 12.76 | 3.28 | 6.87 | 11.46 | 68,519 |
| ECHO   | 25,309 | 4.52  | 3.58 | 7.75 | 12.03 | 238 |
| SET    | 37,690 | 15.78 | 3.73 | 7.43 | 12.01 | 69,373 |
| GET    | 38,089 | 10.22 | 3.83 | 7.88 | 11.91 | 67,674 |
| LPUSH  | 37,857 | 22.66 | 4.55 | 8.60 | 15.70 | 70,602 |
| RPUSH  | 37,759 | 10.67 | 3.92 | 7.94 | 13.22 | 70,921 |
| LPOP   | 25,416 | 13.81 | 4.77 | 9.28 | 17.26 | 65,787 |
| LLEN   | 12,352 | 3.91  | 2.85 | 6.39 | 15.00 | 332 |
| LRANGE | 12,274 | 64.56 | 9.03 | 18.13 | 83.78 | 70,854 |
| **OVERALL** | **251,646** | **15.37** | **4.10** | **9.20** | **17.88** | **70,921** |

---

### Redis (same benchmark client)

| Command | Throughput (ops/s) | Avg (µs) | P50 (µs) | P95 (µs) | P99 (µs) | Max (µs) |
|--------|-------------------|----------|----------|----------|----------|----------|
| PING   | 20,459 | 12.80 | 3.99 | 7.88 | 12.06 | 67,312 |
| ECHO   | 19,712 | 13.34 | 4.47 | 8.75 | 15.14 | 63,571 |
| SET    | 30,375 | 10.76 | 4.31 | 8.49 | 14.45 | 68,270 |
| GET    | 30,127 | 16.07 | 4.52 | 8.77 | 13.54 | 66,553 |
| LPUSH  | 30,643 | 23.08 | 5.23 | 9.87 | 23.04 | 70,385 |
| RPUSH  | 30,357 | 10.96 | 4.63 | 8.95 | 16.03 | 66,647 |
| LPOP   | 20,087 | 23.63 | 5.44 | 10.36 | 23.69 | 69,930 |
| LLEN   | 10,366 | 20.84 | 3.57 | 7.03 | 11.62 | 68,516 |
| LRANGE | 10,237 | 63.25 | 10.89 | 20.14 | 72.29 | 71,432 |
| **OVERALL** | **202,365** | **18.35** | **4.76** | **10.62** | **21.37** | **71,432** |

---

## 🔹 Blocking List Benchmark

| Metric | ZappyDB | Redis |
|--------|--------|-------|
| Throughput | **114,211 ops/s** | **48,889 ops/s** |
| Avg Latency | 15.63 µs | 38.96 µs |
| P50 | 5.76 µs | 7.02 µs |
| P99 | 380.95 µs | 348.32 µs |

---

## 📈 Performance Summary

| Metric | ZappyDB | Redis | Improvement |
|--------|--------|-------|-------------|
| Total Throughput | 251k ops/s | 202k ops/s | **~1.24× faster** |
| P50 Latency | 4.10 µs | 4.76 µs | **lower** |
| P99 Latency | 17.88 µs | 21.37 µs | **lower** |
| Blocking Throughput | 114k | 48k | **~2.3× faster** |

---

## ⚠️ Notes on Benchmarking

- Both systems are tested using the **same client and workload**
- No Redis-specific optimizations (like pipelining) are used
- Results reflect **balanced, realistic workloads**, not synthetic extremes
- Tail latency spikes (~70ms) are observed due to JVM/OS scheduling

---

## 🧠 Key Takeaways

- ZappyDB demonstrates **consistent latency improvements**
- Shows **higher throughput under mixed workloads**
- Significantly better performance in **blocking operations**
- Overall performance is **competitive and stable**, not artificially inflated

---

## 🛠️ Architecture

### Project Structure
```
ZappyDB/
├── src/main/java/org/perfect047/
│   ├── Main.java                    # Application entry point
│   ├── Server.java                  # Server lifecycle & configuration
│   ├── handler/
│   │   └── ClientHandler.java       # Client connection processing
│   ├── command/                     # Command implementations
│   │   ├── BaseCommand.java
│   │   ├── CommandFactory.java
│   │   ├── EchoCommand.java
│   │   ├── GetCommand.java
│   │   ├── SetCommand.java
│   │   ├── PingCommand.java
│   │   ├── LPushCommand.java
│   │   ├── RPushCommand.java
│   │   ├── LPopCommand.java
│   │   ├── LLenCommand.java
│   │   ├── LRangeCommand.java
│   │   ├── BLPopCommand.java
│   │   └── ListValueCommand.java
│   ├── concurrency/                  # Concurrency handling
│   │   ├── ThreadPool.java
│   │   ├── IConcurrencyStrategy.java
│   │   └── ConcurrencyFactory.java
│   ├── storage/                      # Storage implementations
│   │   ├── StoreFactory.java
│   │   ├── keyvalue/
│   │   │   ├── IKeyValueStore.java
│   │   │   ├── KeyValueKeyValueStore.java
│   │   │   ├── LockManager.java
│   │   │   └── ExpiryManager.java
│   │   └── listvalue/
│   │       ├── IListValueStore.java
│   │       ├── IListReader.java
│   │       ├── IListWriter.java
│   │       ├── IBlockingListReader.java
│   │       └── ListValueStore.java
│   ├── util/                         # Utilities
│   │   ├── RespString.java
│   │   ├── SafeEnvParse.java
│   │   └── EnvLoader.java
│   └── Enum/
│       └── ConcurrencyStrategy.java
├── src/test/java/org/perfect047/
│   └── benchmark/
│       └── ZappyDbBenchmark.java      # Benchmark suite
├── pom.xml
├── zappy_run.sh
├── run_benchmarks.sh
├── run_benchmarks_container.sh
├── run_redis_benchmarks_container.sh
└── README.md
```

### Components

**Server Layer**
- `Main.java`: Entry point, initiates server startup
- `Server.java`: Manages server lifecycle, accepts connections
- `ClientHandler.java`: Handles individual client connections

**Command Layer**
- `CommandFactory.java`: Creates command instances
- `*Command.java`: Individual command implementations

**Storage Layer**
- `KeyValueKeyValueStore.java`: Key-value storage with TTL support
- `ListValueStore.java`: List data structure storage
- `ExpiryManager.java`: Manages key expiration
- `LockManager.java`: Thread-safe locking

**Concurrency Layer**
- `ThreadPool.java`: Thread pool management
- `ConcurrencyStrategy.java`: Concurrency strategy enum

## 📋 Supported Commands

| Command | Description |
|---------|-------------|
| PING | Ping the server |
| ECHO | Echo back message |
| GET | Get value by key |
| SET | Set key-value pair |
| LPUSH | Push to list (left) |
| RPUSH | Push to list (right) |
| LPOP | Pop from list (left) |
| LLEN | List length |
| LRANGE | Get list range |
| BLPOP | Blocking list pop |

## 🔧 Configuration

```bash
# Custom port
PORT=6380 ./zappy_run.sh

# Debug mode
DEBUG=true ./zappy_run.sh
```

Environment variables can also be set in `.env` file.

## 📚 Learning Resources

- [Redis Documentation](https://redis.io/documentation)
- [RESP Protocol Specification](https://redis.io/topics/protocol)
- [Java Networking Guide](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/net/package-summary.html)

---

## 🤝 Contributing

We welcome contributions to **ZappyDB**! To maintain stability and code quality, please follow these guidelines.

---

### 🔁 Branching Strategy

- `main` → Production-ready, stable code
- `dev-test` → Integration branch for all contributions
- Feature branches → Created from `dev-test`

---

### 🚨 Pull Request Rules

- All PRs **must target `dev-test`**
- PRs directly to `main` will be **rejected**
- `main` can only be updated via:

---

## 📄 License

MIT License - see the [LICENSE](LICENSE) file for details.

---

⭐ Star this repo if you find it helpful!