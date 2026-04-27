package org.perfect047.handler;

import org.perfect047.command.CommandFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientHandler implements Runnable{

    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {

        try {
            int bytesRead;
            byte[] buffer = new byte[1024];
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();

            while((bytesRead = inputStream.read(buffer)) != -1 ){
                List<String> cmd = Arrays.stream(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8).split("\\r\\n"))
                        .filter(s -> !s.isEmpty() && !s.startsWith("*") && !s.startsWith("$"))
                        .toList();

                for(String cmdStr : cmd) System.out.println(cmdStr);

                CommandFactory.getCommand(cmd.getFirst().toUpperCase(), outputStream).execute(cmd);
            }
        }
        catch (Exception e){
            System.out.println("Client Handler Exception: " + e.getMessage());
        }
        finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                System.out.println("Client Socket close Exception: " + e.getMessage());
            }
        }
    }

}
