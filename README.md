# 🚀 ZappyDB - In-Memory Database

[![Java Version](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.java.net/projects/jdk/25/)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A high-performance, from-scratch implementation of an In-Memory Database in Java, inspired by Redis.

## ✨ Features

### Current Implementation
- ✅ TCP Server with basic client handling
- ✅ RESP (Redis Serialization Protocol) parsing foundation
- ✅ PING/PONG command support
- ✅ Multi-threaded client handling with thread pools
- ✅ Graceful connection management
- ✅ Basic key-value store operations (GET, SET)
- ✅ List operations

### Planned Features
- 🔄 Full Redis command set (DEL, EXPIRE, etc.)
- 🔄 Data persistence with RDB snapshots
- 🔄 Replication and clustering support
- 🔄 Pub/Sub messaging
- 🔄 Lua scripting engine
- 🔄 Advanced data types (Lists, Sets, Sorted Sets, Hashes)
- 🔄 Memory management and eviction policies

### Core Components

- **Main.java**: Entry point and server socket management
- **Server.java**: Server lifecycle and configuration
- **ClientHandler.java**: Individual client connection processing
- **Command System**: Modular command implementation (Ping, Echo, Get, Set)
- **Storage Engines**: Key-value and list value stores
- **Concurrency**: Thread pool-based client handling
- **RESP Protocol**: Redis wire protocol implementation

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

Run the comprehensive test suite:

```bash
# Run all tests
./run_tests.sh

# Or run tests with Maven
mvn test

# Run specific test class
mvn test -Dtest=PingCommandTest
```

The test suite covers all implemented commands with unit tests ensuring correct RESP protocol responses and data store operations.

## 🛠️ Development

### Project Structure
```
ZappyDB/
├── src/main/java/org/perfect047/
│   ├── Main.java                    # Application entry point
│   ├── Server.java                  # Server implementation
│   ├── command/                     # Command implementations
│   │   ├── BaseCommand.java
│   │   ├── CommandFactory.java
│   │   ├── EchoCommand.java
│   │   ├── GetCommand.java
│   │   ├── ICommand.java
│   │   ├── PingCommand.java
│   │   └── SetCommand.java
│   ├── concurrency/                 # Concurrency handling
│   │   ├── ConcurrencyFactory.java
│   │   ├── IConcurrencyStrategy.java
│   │   └── ThreadPool.java
│   ├── Enum/                        # Enums
│   │   └── ConcurrencyStrategy.java
│   ├── handler/                     # Client handlers
│   │   └── ClientHandler.java
│   ├── storage/                     # Storage implementations
│   │   ├── StoreFactory.java
│   │   ├── keyvalue/
│   │   │   ├── IKeyValueStore.java
│   │   │   └── KeyValueStore.java
│   │   └── listvalue/
│   │       ├── IListValueStore.java
│   │       └── ListValueStore.java
│   └── util/                        # Utilities
│       ├── RespString.java
│       └── SafeEnvParse.java
├── pom.xml                          # Maven configuration
├── zappy_run.sh                     # Local run script
└── README.md
```

### Building and Running

```bash
# Compile
mvn compile

# Run directly
mvn exec:java -Dexec.mainClass="org.perfect047.Main"

# Or run the script
./zappy_run.sh
```

### Environment Variables
- `PORT`: Server port (default: 6379)

## 📋 Redis Protocol (RESP)

This implementation uses the Redis Serialization Protocol for client-server communication:

- **Simple Strings**: `+OK\r\n`
- **Errors**: `-Error message\r\n`
- **Integers**: `:1000\r\n`
- **Bulk Strings**: `$6\r\nfoobar\r\n`
- **Arrays**: `*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n`

## 🔧 Configuration

The server can be configured through environment variables and configuration files:

```bash
# Custom port
PORT=6380 ./zappy_run.sh

# Debug mode
DEBUG=true ./zappy_run.sh
```

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Integration testing with redis-cli
echo "PING" | nc localhost 6379
```

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Java naming conventions
- Add unit tests for new features
- Update documentation
- Ensure code compiles with `--enable-preview` flag

## 📚 Learning Resources

- [Redis Documentation](https://redis.io/documentation)
- [RESP Protocol Specification](https://redis.io/topics/protocol)
- [Java Networking Guide](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/net/package-summary.html)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Note**: This is a work-in-progress implementation. Many Redis features are not yet implemented.

⭐ Star this repo if you find it helpful!