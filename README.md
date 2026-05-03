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

## 📊 Performance Benchmarks (Updated – Pipelined & Batched)

### Benchmark Methodology

Benchmarks are executed using a **single unified benchmark client** (`ZappyDbBenchmark`) against both ZappyDB and Redis
to ensure:

- Identical workload distribution
- Same concurrency model (client-side)
- Same network conditions (Docker bridge)
- Same measurement logic (latency + throughput)
- No tool bias (`redis-benchmark` is NOT used)

### Critical Improvements (Recent)

- ✅ Command pipelining enabled (`pipelineSize=64`)
- ✅ Batched response flushing (reduces syscall overhead)
- ✅ Logging disabled in benchmark mode (`BENCH_MODE=true`)
- ✅ Warmup phase for JIT stabilization
- ✅ CPU pinning via Docker (`cpuset`)

---

## 🔹 Benchmark Configuration

- **Java**: 25.0.3
- **CPU Limit**: 2 cores
- **Memory Limit**: 1 GiB
- **GC**: ZGC
- **Threads**: 2
- **Warmup Ops/Thread**: 100,000
- **Measured Ops/Thread**: 500,000
- **Total Operations**: 1,000,000
- **Pipeline Size**: 64
- **Key Space**: 100,000
- **List Key Space**: 10,000
- **Value Size**: 32 bytes

---

## 🔹 Mixed Workload Benchmark (Latest)

### ZappyDB

| Metric      | Value            |
|-------------|------------------|
| Throughput  | **32,910 ops/s** |
| Avg Latency | 59.73 µs         |
| P50         | 48.73 µs         |
| P95         | 143.35 µs        |
| P99         | 208.51 µs        |
| Max         | 457.93 µs        |

---

### Redis

| Metric      | Value            |
|-------------|------------------|
| Throughput  | **30,469 ops/s** |
| Avg Latency | 63.42 µs         |
| P50         | 53.20 µs         |
| P95         | 150.37 µs        |
| P99         | 213.78 µs        |
| Max         | 635.97 µs        |

---

## 🔹 Blocking List Benchmark

| Metric      | ZappyDB        | Redis      |
|-------------|----------------|------------|
| Throughput  | **127k ops/s** | 120k ops/s |
| Avg Latency | 28.11 µs       | 29.68 µs   |
| P99         | 818 µs         | 952 µs     |

---

## 📈 Performance Summary

| Metric           | ZappyDB        | Redis           |
|------------------|----------------|-----------------|
| Total Throughput | ~32.9k ops/s   | ~30.5k ops/s    |
| P50 Latency      | ~48 µs         | ~53 µs          |
| P95 Latency      | Slightly lower | Slightly higher |
| Blocking Ops     | Faster         | Slightly slower |

---

## 🧠 Key Takeaways

- With proper pipelining and batching, ZappyDB achieves **comparable or slightly better throughput than Redis**
- Performance is now **CPU-bound rather than network-bound**
- ZappyDB shows **lower median and tail latency** in mixed workloads
- Batched flushing and reduced syscall overhead significantly improved throughput
- Results reflect a **fair, production-representative benchmark**

---

## ⚠️ Notes on Benchmarking

- Both systems are tested using the same workload and client
- No Redis-specific optimizations (like pipelining)
- Threads = CPU cores → no artificial scaling
- ZGC used for latency stability
- Results are CPU-bound and reproducible

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

| Command | Description                          |
|---------|--------------------------------------|
| PING    | Ping the server                      |
| ECHO    | Echo back message                    |
| GET     | Get value by key                     |
| SET     | Set key-value pair                   |
| LPUSH   | Push to list (left)                  |
| RPUSH   | Push to list (right)                 |
| LPOP    | Pop from list (left)                 |
| LLEN    | List length                          |
| LRANGE  | Get list range                       |
| BLPOP   | Blocking list pop                    |
| XREAD   | Blocking and non-blocking stream read|
| XADD    | Push to stream                       |

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
