package org.perfect047;

import org.perfect047.concurrency.ConcurrencyFactory;
import org.perfect047.concurrency.IConcurrencyStrategy;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private IConcurrencyStrategy concurrencyStrategy;

    public Server() {
        this.concurrencyStrategy = new ConcurrencyFactory().getConcurrencyStrategy();
    }

    public void startServer(int port) {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);

            while (true) {
                clientSocket = serverSocket.accept();
                concurrencyStrategy.handleConnection(clientSocket);
            }

        } catch (Exception e) {
            System.out.println("Server Exception: " + e.getMessage());
        }
        finally {
            try {
                concurrencyStrategy.shutdown();
            } catch (Exception e) {
                System.out.println("Server PostProcess Exception: " + e.getMessage());
            }
        }
    }

}
