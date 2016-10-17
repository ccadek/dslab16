package client;

import cli.Command;
import cli.Shell;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ClientCli implements IClientCli, Runnable {

    private Config config;
    private Shell shell;
    private String loggedInUser;

    public ClientCli(Config config, String componentName, InputStream inputStream, OutputStream outputStream) {
        this.config = config;
        shell = new Shell(componentName,inputStream,outputStream);
        shell.register(this);
        loggedInUser = null;
    }

    @Command
    @Override
    public String login(String username, String password) throws IOException {
        if(loggedInUser != null){
            return "Already logged in.";
        }
        if(checkUser(username,password)){
            this.loggedInUser = username;
            return "Successfully logged in.";
        }
        return "Wrong username or password.";
    }

    private boolean checkUser(String username, String pw){
        String password = config.getString(username);
        if (password == null) {
            return false;
        }

        if (password.equals(pw)) {
            return true;
        }

        return false;
    }

    @Command
    @Override
    public String logout() throws IOException {
        if(loggedInUser == null){
            return "Not logged in.";
        }
        loggedInUser = null;
        return "Successfully logged out.";
    }

    @Command
    @Override
    public String send(String message) throws IOException {
        return message;
    }

    @Command
    @Override
    public String list() throws IOException {
        return null;
    }

    @Command
    @Override
    public String msg(String username, String message) throws IOException {
        return username+" "+message;
    }

    @Command
    @Override
    public String lookup(String username) throws IOException {
        if(username != null){
            return username;
        }
        return "Wrong username or user not registered .";
    }

    @Command
    @Override
    public String register(String privateAddress) throws IOException {
        if(privateAddress != null) {
            return privateAddress;
        } else {
            return "Not a valid private adress.";
        }
    }

    @Command
    @Override
    public String lastMsg() throws IOException {
        return null;
    }

    @Command
    @Override
    public String exit() throws IOException {
        logout();
        shell.close();
        return "Client successfully closed";
    }

    @Override
    public void run() {
        new Thread(shell).start();
    }

    // --- Commands needed for Lab 2. Please note that you do not have to
    // implement them for the first submission. ---
    
    @Command
    @Override
    public String authenticate(String username) throws IOException {
        return null;
    }
}
