package chatserver;

import cli.Shell;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class ChatServerCli implements IChatserverCli,Runnable {

    private Config config;
    private Shell shell;
    private List<String> loggedInUsers;

    public ChatServerCli(Config config, String componentName, InputStream inputStream, OutputStream outputStream){
        this.config = config;
        Shell shell = new Shell(componentName,inputStream,outputStream);
        shell.register(this);
        loggedInUsers = new ArrayList<>();
    }

    @Override
    public String users() throws IOException {
        if(loggedInUsers.size() == 0){
            return "No users logged in";
        }
        String rtn = "";
        for(int i = 0; i < loggedInUsers.size(); i++){
            rtn += loggedInUsers.get(i)+",";
        }
        return rtn;
    }

    @Override
    public String exit() throws IOException {
        return "exit";
    }

    @Override
    public void run() {
        new Thread(shell).start();
    }
}
