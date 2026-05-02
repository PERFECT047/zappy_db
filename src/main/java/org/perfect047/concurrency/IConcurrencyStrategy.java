package org.perfect047.concurrency;

import java.io.IOException;
import java.net.Socket;

public interface IConcurrencyStrategy {
    void start() throws Exception;
    void shutdown() throws Exception;
}
