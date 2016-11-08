package chatserver;

import util.Config;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class TCPListener implements Runnable{
    private String componentName;
    private Config config;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private List<Chatserver> serverList;

    public TCPListener(String componentName, Config config, ServerSocket serverSocket,
                       ExecutorService executorService){
        this.componentName = componentName;
        this.config = config;
        this.serverSocket = serverSocket;
        this.executorService = executorService;
        this.serverList = new ArrayList<>();
    }

    @Override
    public void run(){
        Socket socket = null;
        try {
            while(!executorService.isShutdown()){
                socket = serverSocket.accept();
                Chatserver chatserver = new Chatserver(componentName,config,
                        socket.getInputStream(),new PrintStream(socket.getOutputStream()));
                serverList.add(chatserver);
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
            Iterator listIterator = serverList.iterator();
            while(listIterator.hasNext()){
                Chatserver elem = (Chatserver) listIterator.next();
                elem.stop();
                listIterator.remove();
            }
            if(serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    // very unlikely
                }
            }
        }
    }
}
