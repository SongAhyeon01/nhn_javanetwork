package com.nhnacademy.tictactoe;

import java.io.*;
import java.net.Socket;

public class Client1 implements Runnable {
    String userId;
    String host;
    int port;

    public Client1(String userId, String host, int port) {
        this.userId = userId;
        this.host = host;
        this.port = port;
    }

    // 입력값이 1~3 인지 확인
    public boolean isRightXY (String message) {
        int x;
        int y;

        String[] fields = message.split(" ", 2);
        x = Integer.parseInt(fields[0]);
        y = Integer.parseInt(fields[1]);

        if ((x > 0 && x < 4) && (y > 0 && y < 4)) {
            return true;
        }
        return false;
    }


    @Override
    public void run() {
        try (Socket socket = new Socket(host, port);
             BufferedReader terminalInput = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter terminalOutput = new PrintWriter(System.out);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream())) {

            // 서버에서 보내는 메세지를 받는다
            // 게임판 출력 or 게임 승리 or 게임 종료
            Thread receiver = new Thread(() -> {
                String message;
                try {
                    while ((message = input.readLine()) != null) {
                        terminalOutput.println(message);
                        terminalOutput.flush();
                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            });
            receiver.start();

            // [로그인] userId 을 보낸다 & 게임을 할 준비를 한다
            output.println("#" + userId);
            output.flush();

            // [게임 진행] 게임을 시작하여 좌표를 입력한다
            // 내 턴인지 확인하는 것 필요
            while (socket.isConnected()) {
                terminalOutput.println("좌표를 입력하세요. ex)x y");
                String message = terminalInput.readLine();
                if (isRightXY(message)) {
                    output.println("O " + message);
                    output.flush();
                } else {
                    terminalOutput.println("입력 가능한 좌표는 1~3 범위의 숫자입니다");
                }
            }


        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Client1("C1", "localhost", 12345));

        thread.start();
        thread.join();
    }
}
