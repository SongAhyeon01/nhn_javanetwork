package com.nhnacademy.simplenc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private int port;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("server created!");
            while (true) { // 서버를 계속 실행 상태로 유지합니다.
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

                    clientSocket.setTcpNoDelay(true); // TCP_NODELAY 옵션 설정
                    System.out.println("client connected");

                    Thread receiveThread = new Thread(() -> {
                        String inputLine;
                        try {
                            while ((inputLine = in.readLine()) != null) {
                                System.out.println("Received from client: " + inputLine+"\n");
                            }
                        } catch (IOException e) {
                            System.err.println("Server Error: " + e.getMessage());
                        }
                    });

                    Thread sendThread = new Thread(() -> {
                        String userInput;
                        try {
                            while ((userInput = stdIn.readLine()) != null) {
                                out.println(userInput);
                            }
                        } catch (IOException e) {
                            System.err.println("Server Error: " + e.getMessage());
                        }
                    });

                    receiveThread.start();
                    sendThread.start();

                    receiveThread.join();
                    sendThread.join();

                } catch (IOException e) {
                    System.err.println("Server Error: " + e.getMessage());
                } catch (InterruptedException e){
                    System.err.println("Server Err :" + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        Thread thread = new Thread(new Server(port));

        thread.start();

    }
}
