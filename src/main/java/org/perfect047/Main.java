package org.perfect047;

import org.perfect047.util.SafeEnvParse;

public class Main {
    public static void main(String[] args) {

        System.out.println("Logs from your program will appear here!");

        int port = SafeEnvParse.getSafeEnvParse(System.getenv("PORT"), 6379, Integer::parseInt);

        new org.perfect047.Server().startServer(port);
    }
}

