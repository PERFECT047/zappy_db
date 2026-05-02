package org.perfect047.concurrency;

public interface IConcurrencyStrategy {
    void start() throws Exception;

    void shutdown() throws Exception;
}