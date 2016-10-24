package chatserver;

import util.Config;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class TCPListener implements Runnable{
    private String componentName;
    private Config config;
    private ServerSocket serverSocket;
    private List<String> loggedInUsers;
    private ExecutorService executorService;

    public TCPListener(String componentName, Config config, ServerSocket serverSocket,
                       ExecutorService executorService){
        this.componentName = componentName;
        this.config = config;
        this.serverSocket = serverSocket;
        this.executorService = executorService;
    }

    @Override
    public void run(){
        Socket socket = null;
        try {
            while(!executorService.isShutdown()){

                socket = serverSocket.accept();
                Chatserver chatserver = new Chatserver(componentName,config,
                        socket.getInputStream(),new PrintStream(socket.getOutputStream()));
                chatserver.setSocket(socket);
                executorService.execute(chatserver);

            }
        } catch (SocketException e){
            // thrown if executorService closes thread
        }
        catch (IOException e) {
            e.printStackTrace();
            // will be thrown, don't worry
        } finally {
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    // very unlikely
                }
            }
        }
    }
}
