package com.nhnacademy.tictactoe;

import java.io.IOException;
import java.net.ServerSocket;

public class Server implements Runnable {
    int port;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server Created");

            while (!serverSocket.isClosed()) {
                GameService gameService = new GameService(serverSocket.accept());
                gameService.start();

            }


        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Server(12345));

        thread.start();
        thread.join();

    }
}
