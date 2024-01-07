package com.nhnacademy.simplenc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    private String hostName;
    private int port;

    public Client(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    @Override
    public void run() {
        try (Socket clientSocket = new Socket(this.hostName, this.port);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            clientSocket.setTcpNoDelay(true); // TCP_NODELAY 옵션 설정

            Thread receiveThread = new Thread(() -> {
                String inputLine;
                try {
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("Received from server: " + inputLine+"\n");
                    }
                } catch (IOException e) {
                    System.err.println("Client Error: " + e.getMessage());
                }
            });

            Thread sendThread = new Thread(() -> {
                String userInput;
                try {
                    while ((userInput = stdIn.readLine()) != null) {
                        out.println(userInput);
                    }
                } catch (IOException e) {
                    System.err.println("Client Error: " + e.getMessage());
                }
            });

            receiveThread.start();
            sendThread.start();

            receiveThread.join();
            sendThread.join();

        } catch (IOException e) {
            System.err.println("Client Error: " + e.getMessage());
        } catch (InterruptedException e){
            System.err.println("Client Err :" + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        String hostName = args[0];
        int port = Integer.parseInt(args[1]);
        Thread thread = new Thread(new Client(hostName, port));

        thread.start();
    }
}
