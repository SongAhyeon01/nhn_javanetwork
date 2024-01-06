package com.nhnacademy.tictactoe;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GameService implements Runnable {
    static List<GameService> serviceList = new LinkedList<>();
    static String[][] gameBoard = new String[3][3];
    static int playTime;

    String userId = "";
    Thread thread;
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;

    public GameService(Socket socket) throws IOException {
        thread = new Thread(this);
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        serviceList.add(this);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public synchronized void start() {
        thread.start();
    }

    public void write(String message) {
        writer.println(message);
        writer.flush();
    }

    public synchronized void broadcast (String message) {
        for (GameService service : serviceList) {
            service.write(message);
        }
    }

    public synchronized void directMessage (String message) {
        for (GameService service : serviceList) {
            if (service.getUserId().equals(this.getUserId())) {
                service.write(message);
            }
        }
    }

    public void resetGameBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                gameBoard[i][j] = ".";
            }
        }
    }

    public synchronized void countPlayTime() {
        playTime += 1;
    }

    // *한 줄씩 가게 됨. 제대로 끝까지 가는지 확인*
    public synchronized void showGameBoard() {
        broadcast("showGameBoard : 게임판 시작");
        for (String[] marks : gameBoard) {
            broadcast(Arrays.toString(marks));
        }
        broadcast("showGameBoard : 게임판 끝");
    }

    public synchronized void setGameBoard(String mark, int x, int y) {
        gameBoard[x-1][y-1] = mark;
    }

    public int hasWinner() {
        for (int i=0; i<gameBoard.length; i++) {
            // 어떠한 가로줄이 모두 O
            if (gameBoard[i][0].equals("O") && gameBoard[i][1].equals("O") && gameBoard[i][2].equals("O")) {
                return 1;
            }
            // 어떠한 세로줄이 모두 O
            if (gameBoard[0][i].equals("O") && gameBoard[1][i].equals("O") && gameBoard[2][i].equals("O")) {
                return 1;
            }
            // 어떠한 가로줄이 모두 X
            if (gameBoard[i][0].equals("X") && gameBoard[i][1].equals("X") && gameBoard[i][2].equals("X")) {
                return 2;
            }
            // 어떠한 세로줄이 모두 X
            if (gameBoard[0][i].equals("X") && gameBoard[1][i].equals("X") && gameBoard[2][i].equals("X")) {
                return 2;
            }
        }
        // 승자가 없음
        return 0;
    }

    public int isGameEnd() {
        // O를 마킹한 유저가 승리하여 종료
        if (hasWinner() == 1) {
            return 1;
        }
        // X를 마킹한 유저가 승리하여 종료
        if (hasWinner() == 2) {
            return 2;
        }
        // 게임이 9번 진행되어 종료
        if (playTime > 10) {
            return 3;
        }
        // 게임이 종료되지 않음
        return 0;
    }

    public boolean isRightPlace(int x, int y) {
        if (gameBoard[x-1][y-1].equals("O") || gameBoard[x-1][y-1].equals("X")) {
            return false;
        }
        return true;
    }

    public synchronized void gameStop() {
        if (socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        try {
            resetGameBoard();
            playTime = 0;
            while (socket.isConnected()) {
                // 유저에게 좌표를 받는다
                String message = reader.readLine();

                if (message.matches("#[a-zA-Z]\\w*")) {
                    setUserId(message.substring(1));
                    directMessage(getUserId() + "님 안녕하세요");
                } else if (message.matches("\\w\\s\\d\\s\\d")) {
                    String[] fields = message.split("\\s", 3);
                    String mark = fields[0];
                    int x = Integer.parseInt(fields[1]);
                    int y = Integer.parseInt(fields[2]);

                    // 입력된 좌표를 게임판에 표시한다
                    if (isRightPlace(x, y)) {
                        setGameBoard(mark, x, y);
                        countPlayTime();
                    } else {
                        directMessage("해당 좌표는 이미 표시가 되어있습니다");
                    }

                    // 게임판을 클라이언트에게 출력한다
                    showGameBoard();

                    // 만약 게임 종료 조건이 성립될 경우 메세지를 출력하고 연결을 끊는다
                    int gameResult = isGameEnd();
                    if (gameResult == 1) {
                        broadcast("O 를 표시한 유저가 승리했습니다");
                        gameStop();
                    } else if (gameResult == 2) {
                        broadcast("X 를 표시한 유저가 승리했습니다");
                        gameStop();
                    } else if (gameResult == 3) {
                        broadcast("이번 판은 9번 진행되어 종료되었습니다");
                        gameStop();
                    } else {
                        broadcast("다음에 표시할 좌표를 알려주세요");
                    }
                }

            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                reader.close();
                writer.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

    }

}
