package client;


import util.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

/*
    Simple class for creating instances of commonly used classes
 */
public class ClientFactory {

    private static Config config = new Config("client");

    public static Socket createSocket() throws IOException{
        return new Socket(config.getString("chatserver.host"),config.getInt("chatserver.tcp.port"));
    }

    public static BufferedReader createBufferedReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public static PrintWriter createPrintWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(),true);
    }

    public static DatagramSocket createDatagramSocket() throws SocketException {
        return new DatagramSocket();
    }
}
