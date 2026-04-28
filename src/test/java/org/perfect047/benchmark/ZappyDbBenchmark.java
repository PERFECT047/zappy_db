package org.perfect047.benchmark;

import org.perfect047.command.CommandFactory;
import org.perfect047.command.ICommand;
import org.perfect047.storage.keyvalue.KeyValueKeyValueStore;
import org.perfect047.storage.listvalue.ListValueStore;
import org.perfect047.util.EnvLoader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;

public final class ZappyDbBenchmark {

    private ZappyDbBenchmark() {
    }

    public static void main(String[] args) throws Exception {
        EnvLoader.load();
        BenchmarkConfig config = BenchmarkConfig.fromEnvAndArgs(args);

        printConfig(config);

        try (BenchmarkEnvironment environment = createEnvironment(config)) {
            environment.seed(config);
            runMixedWorkloadBenchmark(config, environment);
            runBlockingListBenchmark(config, environment);
        }
    }

    private static BenchmarkEnvironment createEnvironment(BenchmarkConfig config) throws IOException {
        return switch (config.target) {
            case LOCAL -> new LocalBenchmarkEnvironment();
            case REDIS -> new RedisBenchmarkEnvironment(config);
        };
    }

    private static void runMixedWorkloadBenchmark(BenchmarkConfig config, BenchmarkEnvironment environment) throws Exception {
        System.out.println();
        System.out.println("=== Mixed Workload Benchmark ===");

        ExecutorService executor = Executors.newFixedThreadPool(config.threads);
        CountDownLatch ready = new CountDownLatch(config.threads);
        CountDownLatch start = new CountDownLatch(1);
        ConcurrentLinkedQueue<String> failures = new ConcurrentLinkedQueue<>();
        List<Future<WorkerResult>> futures = new ArrayList<>();

        try {
            for (int workerId = 0; workerId < config.threads; workerId++) {
                int id = workerId;
                futures.add(executor.submit(
                        () -> runMixedWorker(id, config, environment, ready, start, failures)
                ));
            }

            ready.await();
            long startedAt = System.nanoTime();
            start.countDown();

            List<WorkerResult> results = new ArrayList<>(config.threads);
            for (Future<WorkerResult> future : futures) {
                results.add(future.get());
            }

            long elapsedNanos = System.nanoTime() - startedAt;
            printMixedSummary(config, results, failures, elapsedNanos);
        } finally {
            executor.shutdownNow();
        }
    }

    private static WorkerResult runMixedWorker(
            int workerId,
            BenchmarkConfig config,
            BenchmarkEnvironment environment,
            CountDownLatch ready,
            CountDownLatch start,
            ConcurrentLinkedQueue<String> failures
    ) throws Exception {
        WorkerResult result = new WorkerResult();

        try (BenchmarkSession session = environment.openSession()) {
            for (int i = 0; i < config.warmupOpsPerThread; i++) {
                BenchmarkOperation operation = pickOperation(ThreadLocalRandom.current().nextInt(100));

                try {
                    executeMixedOperation(session, config, workerId, i, operation);
                } catch (Exception e) {
                    captureFailure(failures, operation.commandName + " warmup worker " + workerId + ": " + e.getMessage());
                }
            }

            ready.countDown();
            start.await();

            for (int i = 0; i < config.measuredOpsPerThread; i++) {
                long startedAt = System.nanoTime();
                BenchmarkOperation operation = pickOperation(ThreadLocalRandom.current().nextInt(100));

                try {
                    executeMixedOperation(session, config, workerId, i, operation);
                    result.recordSuccess(operation, System.nanoTime() - startedAt);
                } catch (Exception e) {
                    result.recordError(operation);
                    captureFailure(failures, operation.commandName + " worker " + workerId + ": " + e.getMessage());
                }
            }
        }

        return result;
    }

    private static void executeMixedOperation(
            BenchmarkSession session,
            BenchmarkConfig config,
            int workerId,
            int iteration,
            BenchmarkOperation operation
    ) throws Exception {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String key = config.namespace + ":key:" + random.nextInt(config.keySpace);
        String listKey = config.namespace + ":list:" + random.nextInt(config.listKeySpace);
        String value = buildValue(workerId, iteration, config.valueSize);

        List<String> args = switch (operation) {
            case PING -> List.of("PING");
            case ECHO -> List.of("ECHO", value);
            case SET -> List.of("SET", key, value);
            case GET -> List.of("GET", key);
            case LPUSH -> List.of("LPUSH", listKey, value);
            case RPUSH -> List.of("RPUSH", listKey, value);
            case LPOP -> List.of("LPOP", listKey);
            case LLEN -> List.of("LLEN", listKey);
            case LRANGE -> List.of("LRANGE", listKey, "0", "4");
        };

        BenchmarkResponse response = session.execute(args);

        if (response.isEmpty()) {
            throw new IllegalStateException("Command produced no response: " + operation.commandName);
        }
    }

    private static void runBlockingListBenchmark(BenchmarkConfig config, BenchmarkEnvironment environment) throws Exception {
        System.out.println();
        System.out.println("=== Blocking List Robustness Benchmark ===");

        ExecutorService executor = Executors.newFixedThreadPool(config.blockingPairs * 2);
        CountDownLatch consumersReady = new CountDownLatch(config.blockingPairs);
        CountDownLatch start = new CountDownLatch(1);
        ConcurrentLinkedQueue<String> failures = new ConcurrentLinkedQueue<>();
        List<Future<BlockingConsumerResult>> consumerFutures = new ArrayList<>();
        List<Future<Long>> producerFutures = new ArrayList<>();

        try {
            for (int pair = 0; pair < config.blockingPairs; pair++) {
                String listName = config.namespace + ":blocking:list:" + pair;
                int pairId = pair;

                consumerFutures.add(executor.submit(() -> runBlockingConsumer(
                        pairId,
                        listName,
                        config,
                        environment,
                        consumersReady,
                        start,
                        failures
                )));

                producerFutures.add(executor.submit(() -> runBlockingProducer(
                        pairId,
                        listName,
                        config,
                        environment,
                        start,
                        failures
                )));
            }

            consumersReady.await();
            long startedAt = System.nanoTime();
            start.countDown();

            BlockingSummary summary = new BlockingSummary();

            for (Future<BlockingConsumerResult> future : consumerFutures) {
                summary.merge(future.get());
            }

            for (Future<Long> future : producerFutures) {
                summary.producedItems += future.get();
            }

            long elapsedNanos = System.nanoTime() - startedAt;
            printBlockingSummary(config, summary, failures, elapsedNanos);
        } finally {
            executor.shutdownNow();
        }
    }

    private static BlockingConsumerResult runBlockingConsumer(
            int pairId,
            String listName,
            BenchmarkConfig config,
            BenchmarkEnvironment environment,
            CountDownLatch consumersReady,
            CountDownLatch start,
            ConcurrentLinkedQueue<String> failures
    ) throws Exception {
        BlockingConsumerResult result = new BlockingConsumerResult();

        try (BenchmarkSession session = environment.openSession()) {
            consumersReady.countDown();
            start.await();

            for (int i = 0; i < config.blockingOpsPerPair; i++) {
                long startedAt = System.nanoTime();

                try {
                    BenchmarkResponse response = session.execute(List.of("BLPOP", listName, "1"));
                    long elapsedNanos = System.nanoTime() - startedAt;

                    if (response.isNull()) {
                        result.timeouts++;
                    } else if (response.isArray()) {
                        result.latencies.add(elapsedNanos);
                        result.successes++;
                    } else {
                        result.protocolErrors++;
                        captureFailure(failures, "BLPOP pair " + pairId + " unexpected response: " + response.raw);
                    }
                } catch (Exception e) {
                    result.protocolErrors++;
                    captureFailure(failures, "BLPOP pair " + pairId + ": " + e.getMessage());
                }
            }
        }

        return result;
    }

    private static long runBlockingProducer(
            int pairId,
            String listName,
            BenchmarkConfig config,
            BenchmarkEnvironment environment,
            CountDownLatch start,
            ConcurrentLinkedQueue<String> failures
    ) throws Exception {
        try (BenchmarkSession session = environment.openSession()) {
            start.await();

            for (int i = 0; i < config.blockingOpsPerPair; i++) {
                try {
                    session.execute(List.of("LPUSH", listName, buildValue(pairId, i, config.valueSize)));

                    if ((i & 63) == 0) {
                        LockSupport.parkNanos(100_000L);
                    }
                } catch (Exception e) {
                    captureFailure(failures, "LPUSH producer pair " + pairId + ": " + e.getMessage());
                }
            }
        }

        return config.blockingOpsPerPair;
    }

    private static void printConfig(BenchmarkConfig config) {
        System.out.println("=== ZappyDB Benchmark Configuration ===");
        System.out.printf(
                Locale.US,
                "target=%s java=%s processors=%d maxMemoryMiB=%d%n",
                config.target.name().toLowerCase(Locale.ROOT),
                System.getProperty("java.version"),
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().maxMemory() / (1024 * 1024)
        );

        if (config.target == BenchmarkTargetType.REDIS) {
            System.out.printf(
                    Locale.US,
                    "redisHost=%s redisPort=%d redisFlush=%s%n",
                    config.redisHost,
                    config.redisPort,
                    config.redisFlush
            );
        }

        System.out.printf(
                Locale.US,
                "threads=%d warmupOps/thread=%d measuredOps/thread=%d keySpace=%d listKeySpace=%d valueSize=%d blockingPairs=%d blockingOps/pair=%d namespace=%s%n",
                config.threads,
                config.warmupOpsPerThread,
                config.measuredOpsPerThread,
                config.keySpace,
                config.listKeySpace,
                config.valueSize,
                config.blockingPairs,
                config.blockingOpsPerPair,
                config.namespace
        );
    }

    private static void printMixedSummary(
            BenchmarkConfig config,
            List<WorkerResult> results,
            ConcurrentLinkedQueue<String> failures,
            long elapsedNanos
    ) {
        EnumMap<BenchmarkOperation, AggregateStats> aggregated = new EnumMap<>(BenchmarkOperation.class);
        LongSamples overallLatencies = new LongSamples();
        long totalErrors = 0L;

        for (BenchmarkOperation operation : BenchmarkOperation.values()) {
            aggregated.put(operation, new AggregateStats());
        }

        for (WorkerResult result : results) {
            for (BenchmarkOperation operation : BenchmarkOperation.values()) {
                AggregateStats aggregateStats = aggregated.get(operation);
                aggregateStats.latencies.addAll(result.latencies.get(operation));
                aggregateStats.errors += result.errors.get(operation);
                overallLatencies.addAll(result.latencies.get(operation));
                totalErrors += result.errors.get(operation);
            }
        }

        long totalOps = (long) config.threads * config.measuredOpsPerThread;
        double throughput = opsPerSecond(totalOps, elapsedNanos);

        System.out.printf(
                Locale.US,
                "totalOps=%d errors=%d throughput=%.2f ops/s elapsed=%.2f ms%n",
                totalOps,
                totalErrors,
                throughput,
                elapsedNanos / 1_000_000.0
        );
        printLatencyHeader();

        for (BenchmarkOperation operation : BenchmarkOperation.values()) {
            AggregateStats stats = aggregated.get(operation);
            printLatencyRow(operation.commandName, stats.latencies, stats.errors, elapsedNanos);
        }

        System.out.println();
        printLatencyRow("OVERALL", overallLatencies, totalErrors, elapsedNanos);
        printFailures(failures);
    }

    private static void printBlockingSummary(
            BenchmarkConfig config,
            BlockingSummary summary,
            ConcurrentLinkedQueue<String> failures,
            long elapsedNanos
    ) {
        System.out.printf(
                Locale.US,
                "pairs=%d produced=%d consumed=%d timeouts=%d errors=%d throughput=%.2f ops/s elapsed=%.2f ms%n",
                config.blockingPairs,
                summary.producedItems,
                summary.successes,
                summary.timeouts,
                summary.protocolErrors,
                opsPerSecond(summary.successes + summary.producedItems, elapsedNanos),
                elapsedNanos / 1_000_000.0
        );
        printLatencyHeader();
        printLatencyRow("BLPOP", summary.latencies, summary.timeouts + summary.protocolErrors, elapsedNanos);
        printFailures(failures);
    }

    private static void printLatencyHeader() {
        System.out.printf(
                Locale.US,
                "%-10s %10s %10s %15s %12s %12s %12s %12s %15s%n",
                "COMMAND",
                "COUNT",
                "ERRORS",
                "THROUGHPUT",
                "AVG us",
                "P50 us",
                "P95 us",
                "P99 us",
                "MAX us"
        );
    }

    private static void printLatencyRow(String label, LongSamples samples, long errors, long elapsedNanos) {
        long count = samples.size();
        long[] sorted = samples.sortedCopy();

        System.out.printf(
                Locale.US,
                "%-10s %10d %10d %15.2f %12.2f %12.2f %12.2f %12.2f %15.2f%n",
                label,
                count,
                errors,
                opsPerSecond(count, elapsedNanos),
                micros(samples.averageNanos()),
                micros(percentile(sorted, 0.50)),
                micros(percentile(sorted, 0.95)),
                micros(percentile(sorted, 0.99)),
                micros(samples.max())
        );
    }

    private static void printFailures(ConcurrentLinkedQueue<String> failures) {
        if (failures.isEmpty()) {
            return;
        }

        System.out.println();
        System.out.println("sample failures:");
        int shown = 0;

        for (String failure : failures) {
            System.out.println(" - " + failure);
            shown++;

            if (shown >= 10) {
                break;
            }
        }
    }

    private static void captureFailure(ConcurrentLinkedQueue<String> failures, String failure) {
        if (failures.size() < 10) {
            failures.add(failure);
        }
    }

    private static double opsPerSecond(long count, long elapsedNanos) {
        if (elapsedNanos == 0L) {
            return 0.0;
        }

        return (count * 1_000_000_000.0) / elapsedNanos;
    }

    private static double micros(long nanos) {
        return nanos / 1_000.0;
    }

    private static long percentile(long[] sorted, double percentile) {
        if (sorted.length == 0) {
            return 0L;
        }

        int index = (int) Math.ceil(percentile * sorted.length) - 1;
        index = Math.max(0, Math.min(index, sorted.length - 1));
        return sorted[index];
    }

    private static String buildValue(int workerId, int iteration, int valueSize) {
        String base = "value-" + workerId + "-" + iteration;
        if (base.length() >= valueSize) {
            return base.substring(0, valueSize);
        }

        return base + "x".repeat(valueSize - base.length());
    }

    private static BenchmarkOperation pickOperation(int pick) {
        if (pick < 10) return BenchmarkOperation.PING;
        if (pick < 20) return BenchmarkOperation.ECHO;
        if (pick < 35) return BenchmarkOperation.SET;
        if (pick < 50) return BenchmarkOperation.GET;
        if (pick < 65) return BenchmarkOperation.LPUSH;
        if (pick < 80) return BenchmarkOperation.RPUSH;
        if (pick < 90) return BenchmarkOperation.LPOP;
        if (pick < 95) return BenchmarkOperation.LLEN;
        return BenchmarkOperation.LRANGE;
    }

    private static String argValue(Map<String, String> args, String key, String envKey, String defaultValue) {
        String cliValue = args.get(key);
        if (cliValue != null) {
            return cliValue;
        }

        String envValue = EnvLoader.get(envKey);
        return envValue == null || envValue.isBlank() ? defaultValue : envValue;
    }

    private static int argInt(Map<String, String> args, String key, String envKey, int defaultValue) {
        String value = argValue(args, key, envKey, String.valueOf(defaultValue));

        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static boolean argBoolean(Map<String, String> args, String key, String envKey, boolean defaultValue) {
        String value = argValue(args, key, envKey, String.valueOf(defaultValue));

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> parsed = new HashMap<>();

        for (String arg : args) {
            if (!arg.startsWith("--")) {
                continue;
            }

            int separator = arg.indexOf('=');
            if (separator < 0) {
                parsed.put(arg.substring(2), "true");
            } else {
                parsed.put(arg.substring(2, separator), arg.substring(separator + 1));
            }
        }

        return parsed;
    }

    private enum BenchmarkOperation {
        PING("PING"),
        ECHO("ECHO"),
        SET("SET"),
        GET("GET"),
        LPUSH("LPUSH"),
        RPUSH("RPUSH"),
        LPOP("LPOP"),
        LLEN("LLEN"),
        LRANGE("LRANGE");

        private final String commandName;

        BenchmarkOperation(String commandName) {
            this.commandName = commandName;
        }
    }

    private enum BenchmarkTargetType {
        LOCAL,
        REDIS
    }

    private static final class BenchmarkConfig {
        private final BenchmarkTargetType target;
        private final int threads;
        private final int warmupOpsPerThread;
        private final int measuredOpsPerThread;
        private final int keySpace;
        private final int listKeySpace;
        private final int valueSize;
        private final int blockingPairs;
        private final int blockingOpsPerPair;
        private final String namespace;
        private final String redisHost;
        private final int redisPort;
        private final boolean redisFlush;

        private BenchmarkConfig(
                BenchmarkTargetType target,
                int threads,
                int warmupOpsPerThread,
                int measuredOpsPerThread,
                int keySpace,
                int listKeySpace,
                int valueSize,
                int blockingPairs,
                int blockingOpsPerPair,
                String namespace,
                String redisHost,
                int redisPort,
                boolean redisFlush
        ) {
            this.target = target;
            this.threads = threads;
            this.warmupOpsPerThread = warmupOpsPerThread;
            this.measuredOpsPerThread = measuredOpsPerThread;
            this.keySpace = keySpace;
            this.listKeySpace = listKeySpace;
            this.valueSize = valueSize;
            this.blockingPairs = blockingPairs;
            this.blockingOpsPerPair = blockingOpsPerPair;
            this.namespace = namespace;
            this.redisHost = redisHost;
            this.redisPort = redisPort;
            this.redisFlush = redisFlush;
        }

        private static BenchmarkConfig fromEnvAndArgs(String[] args) {
            Map<String, String> cliArgs = parseArgs(args);
            String targetValue = argValue(cliArgs, "target", "BENCH_TARGET", "local");
            BenchmarkTargetType target = BenchmarkTargetType.valueOf(targetValue.toUpperCase(Locale.ROOT));

            int defaultThreads = Math.max(2, Runtime.getRuntime().availableProcessors());
            int threads = Math.max(1, argInt(cliArgs, "threads", "BENCH_THREADS", defaultThreads));
            int warmupOpsPerThread = Math.max(0, argInt(cliArgs, "warmup-ops", "BENCH_WARMUP_OPS", 5_000));
            int measuredOpsPerThread = Math.max(1, argInt(cliArgs, "measured-ops", "BENCH_MEASURED_OPS", 20_000));
            int keySpace = Math.max(16, argInt(cliArgs, "key-space", "BENCH_KEY_SPACE", 1_024));
            int listKeySpace = Math.max(8, argInt(cliArgs, "list-key-space", "BENCH_LIST_KEY_SPACE", Math.max(32, keySpace / 4)));
            int valueSize = Math.max(8, argInt(cliArgs, "value-size", "BENCH_VALUE_SIZE", 32));
            int blockingPairs = Math.max(1, argInt(cliArgs, "blocking-pairs", "BENCH_BLOCKING_PAIRS", Math.max(1, Math.min(4, threads / 2))));
            int blockingOpsPerPair = Math.max(1, argInt(cliArgs, "blocking-ops", "BENCH_BLOCKING_OPS", 2_000));
            String namespace = argValue(cliArgs, "namespace", "BENCH_NAMESPACE", "bench:" + System.currentTimeMillis());
            String redisHost = argValue(cliArgs, "redis-host", "BENCH_REDIS_HOST", "127.0.0.1");
            int redisPort = argInt(cliArgs, "redis-port", "BENCH_REDIS_PORT", 6379);
            boolean redisFlush = argBoolean(cliArgs, "redis-flush", "BENCH_REDIS_FLUSH", false);

            return new BenchmarkConfig(
                    target,
                    threads,
                    warmupOpsPerThread,
                    measuredOpsPerThread,
                    keySpace,
                    listKeySpace,
                    valueSize,
                    blockingPairs,
                    blockingOpsPerPair,
                    namespace,
                    redisHost,
                    redisPort,
                    redisFlush
            );
        }
    }

    private interface BenchmarkEnvironment extends AutoCloseable {
        void seed(BenchmarkConfig config) throws Exception;

        BenchmarkSession openSession() throws Exception;

        @Override
        default void close() throws Exception {
        }
    }

    private interface BenchmarkSession extends AutoCloseable {
        BenchmarkResponse execute(List<String> args) throws Exception;

        @Override
        void close() throws Exception;
    }

    private static final class LocalBenchmarkEnvironment implements BenchmarkEnvironment {
        private final KeyValueKeyValueStore keyValueStore = new KeyValueKeyValueStore();
        private final ListValueStore listValueStore = new ListValueStore();
        private final CommandFactory commandFactory = new CommandFactory(keyValueStore, listValueStore);

        @Override
        public void seed(BenchmarkConfig config) {
            for (int i = 0; i < config.keySpace; i++) {
                keyValueStore.set(config.namespace + ":key:" + i, buildValue(0, i, config.valueSize), null);
            }

            for (int i = 0; i < config.listKeySpace; i++) {
                listValueStore.rightAdd(config.namespace + ":list:" + i, List.of("seed-" + i, "seed-" + i + "-tail"));
            }
        }

        @Override
        public BenchmarkSession openSession() {
            return new LocalBenchmarkSession(commandFactory);
        }
    }

    private static final class LocalBenchmarkSession implements BenchmarkSession {
        private final CommandFactory commandFactory;
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256);

        private LocalBenchmarkSession(CommandFactory commandFactory) {
            this.commandFactory = commandFactory;
        }

        @Override
        public BenchmarkResponse execute(List<String> args) throws Exception {
            outputStream.reset();
            ICommand command = commandFactory.getCommand(args.getFirst(), outputStream);

            if (command == null) {
                throw new IllegalStateException("No command registered for " + args.getFirst());
            }

            command.execute(args);
            return BenchmarkResponse.fromRaw(outputStream.toString(StandardCharsets.UTF_8));
        }

        @Override
        public void close() {
        }
    }

    private static final class RedisBenchmarkEnvironment implements BenchmarkEnvironment {
        private final BenchmarkConfig config;

        private RedisBenchmarkEnvironment(BenchmarkConfig config) {
            this.config = config;
        }

        @Override
        public void seed(BenchmarkConfig config) throws Exception {
            try (RedisBenchmarkSession session = new RedisBenchmarkSession(config.redisHost, config.redisPort)) {
                if (config.redisFlush) {
                    session.execute(List.of("FLUSHDB"));
                }

                for (int i = 0; i < config.keySpace; i++) {
                    session.execute(List.of("SET", config.namespace + ":key:" + i, buildValue(0, i, config.valueSize)));
                }

                for (int i = 0; i < config.listKeySpace; i++) {
                    session.execute(List.of("RPUSH", config.namespace + ":list:" + i, "seed-" + i, "seed-" + i + "-tail"));
                }
            }
        }

        @Override
        public BenchmarkSession openSession() throws Exception {
            return new RedisBenchmarkSession(config.redisHost, config.redisPort);
        }
    }

    private static final class RedisBenchmarkSession implements BenchmarkSession {
        private final Socket socket;
        private final BufferedInputStream inputStream;
        private final BufferedOutputStream outputStream;

        private RedisBenchmarkSession(String host, int port) throws IOException {
            this.socket = new Socket(host, port);
            this.inputStream = new BufferedInputStream(socket.getInputStream());
            this.outputStream = new BufferedOutputStream(socket.getOutputStream());
        }

        @Override
        public BenchmarkResponse execute(List<String> args) throws Exception {
            writeRespArray(args);
            outputStream.flush();

            ByteArrayOutputStream responseBytes = new ByteArrayOutputStream(128);
            readRespValue(responseBytes);
            return BenchmarkResponse.fromRaw(responseBytes.toString(StandardCharsets.UTF_8));
        }

        private void writeRespArray(List<String> args) throws IOException {
            writeAscii("*" + args.size() + "\r\n");

            for (String arg : args) {
                byte[] bytes = arg.getBytes(StandardCharsets.UTF_8);
                writeAscii("$" + bytes.length + "\r\n");
                outputStream.write(bytes);
                writeAscii("\r\n");
            }
        }

        private void readRespValue(ByteArrayOutputStream responseBytes) throws IOException {
            int prefix = inputStream.read();

            if (prefix == -1) {
                throw new EOFException("Redis closed the connection");
            }

            responseBytes.write(prefix);

            switch (prefix) {
                case '+', '-', ':' -> readLine(responseBytes);
                case '$' -> readBulkString(responseBytes);
                case '*' -> readArray(responseBytes);
                default -> throw new IOException("Unsupported RESP prefix: " + (char) prefix);
            }
        }

        private void readBulkString(ByteArrayOutputStream responseBytes) throws IOException {
            int length = Integer.parseInt(readLine(responseBytes));
            if (length < 0) {
                return;
            }

            readFixed(responseBytes, length + 2);
        }

        private void readArray(ByteArrayOutputStream responseBytes) throws IOException {
            int length = Integer.parseInt(readLine(responseBytes));
            if (length < 0) {
                return;
            }

            for (int i = 0; i < length; i++) {
                readRespValue(responseBytes);
            }
        }

        private String readLine(ByteArrayOutputStream responseBytes) throws IOException {
            ByteArrayOutputStream line = new ByteArrayOutputStream(32);

            while (true) {
                int b = inputStream.read();
                if (b == -1) {
                    throw new EOFException("Redis closed the connection");
                }

                responseBytes.write(b);

                if (b == '\r') {
                    int next = inputStream.read();
                    if (next == -1) {
                        throw new EOFException("Redis closed the connection");
                    }

                    responseBytes.write(next);
                    if (next != '\n') {
                        throw new IOException("Malformed RESP line ending");
                    }

                    break;
                }

                line.write(b);
            }

            return line.toString(StandardCharsets.UTF_8);
        }

        private void readFixed(ByteArrayOutputStream responseBytes, int bytesToRead) throws IOException {
            byte[] buffer = new byte[256];
            int remaining = bytesToRead;

            while (remaining > 0) {
                int read = inputStream.read(buffer, 0, Math.min(buffer.length, remaining));
                if (read == -1) {
                    throw new EOFException("Redis closed the connection");
                }

                responseBytes.write(buffer, 0, read);
                remaining -= read;
            }
        }

        private void writeAscii(String value) throws IOException {
            outputStream.write(value.getBytes(StandardCharsets.US_ASCII));
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }

    private static final class BenchmarkResponse {
        private final String raw;

        private BenchmarkResponse(String raw) {
            this.raw = raw;
        }

        private static BenchmarkResponse fromRaw(String raw) {
            return new BenchmarkResponse(raw == null ? "" : raw);
        }

        private boolean isEmpty() {
            return raw.isEmpty();
        }

        private boolean isNull() {
            return raw.startsWith("$-1\r\n") || raw.startsWith("*-1\r\n");
        }

        private boolean isArray() {
            return raw.startsWith("*") && !raw.startsWith("*-1\r\n");
        }
    }

    private static final class WorkerResult {
        private final EnumMap<BenchmarkOperation, LongSamples> latencies = new EnumMap<>(BenchmarkOperation.class);
        private final EnumMap<BenchmarkOperation, Long> errors = new EnumMap<>(BenchmarkOperation.class);

        private WorkerResult() {
            for (BenchmarkOperation operation : BenchmarkOperation.values()) {
                latencies.put(operation, new LongSamples());
                errors.put(operation, 0L);
            }
        }

        private void recordSuccess(BenchmarkOperation operation, long latencyNanos) {
            latencies.get(operation).add(latencyNanos);
        }

        private void recordError(BenchmarkOperation operation) {
            errors.put(operation, errors.get(operation) + 1L);
        }
    }

    private static final class BlockingSummary {
        private final LongSamples latencies = new LongSamples();
        private long successes;
        private long timeouts;
        private long protocolErrors;
        private long producedItems;

        private void merge(BlockingConsumerResult result) {
            latencies.addAll(result.latencies);
            successes += result.successes;
            timeouts += result.timeouts;
            protocolErrors += result.protocolErrors;
        }
    }

    private static final class BlockingConsumerResult {
        private final LongSamples latencies = new LongSamples();
        private long successes;
        private long timeouts;
        private long protocolErrors;
    }

    private static final class AggregateStats {
        private final LongSamples latencies = new LongSamples();
        private long errors;
    }

    private static final class LongSamples {
        private long[] values = new long[256];
        private int size;
        private long sum;
        private long max;

        private void add(long value) {
            ensureCapacity(size + 1);
            values[size++] = value;
            sum += value;
            max = Math.max(max, value);
        }

        private void addAll(LongSamples other) {
            ensureCapacity(size + other.size);
            System.arraycopy(other.values, 0, values, size, other.size);
            size += other.size;
            sum += other.sum;
            max = Math.max(max, other.max);
        }

        private int size() {
            return size;
        }

        private long averageNanos() {
            return size == 0 ? 0L : sum / size;
        }

        private long max() {
            return max;
        }

        private long[] sortedCopy() {
            long[] copy = Arrays.copyOf(values, size);
            Arrays.sort(copy);
            return copy;
        }

        private void ensureCapacity(int capacity) {
            if (capacity <= values.length) {
                return;
            }

            int nextSize = values.length;
            while (nextSize < capacity) {
                nextSize *= 2;
            }

            values = Arrays.copyOf(values, nextSize);
        }
    }
}
