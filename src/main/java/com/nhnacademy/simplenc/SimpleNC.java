package com.nhnacademy.simplenc;

public class SimpleNC {
    public static void main(String[] args) {
        if (args.length == 2 && args[0].equals("-l")) {
            Thread serverThread = new Thread(new Server(Integer.parseInt(args[1])));
            serverThread.start();
        } else if (args.length == 3) {
            Thread clientThread = new Thread(new Client(args[1], Integer.parseInt(args[2])));
            clientThread.start();
        } else {
            System.err.println("Usage: snc [option] [hostname] [port]");
            System.err.println("Options:");
            System.err.println("-l     <port>     Operates as a server and listens to the input port");
            System.exit(1);
        }
    }
}
