package chatserver;

import util.Config;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

public class UDPListener implements Runnable{

    private final String componentName;
    private Config config;
    private DatagramSocket datagramSocket;
    private ExecutorService executorService;

    public UDPListener(String componentName, Config config, DatagramSocket datagramSocket,
                       ExecutorService executorService){
        this.componentName = componentName;
        this.config = config;
        this.datagramSocket = datagramSocket;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        DatagramPacket packet = null;
        byte[] buffer;
        try{
            while(!executorService.isShutdown()){
                buffer = new byte[10];
                packet = new DatagramPacket(buffer,buffer.length);
                datagramSocket.receive(packet);

                Chatserver chatserver = new Chatserver(componentName,config,null,null);
                chatserver.setDatagramPacket(packet);

                executorService.execute(chatserver);
            }
        } catch (SocketException e){
            // thrown if socket is closed in another thread
        }
        catch (IOException e){
            e.printStackTrace();
            // dont't worry
        }

    }
}
