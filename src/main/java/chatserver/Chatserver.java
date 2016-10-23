package chatserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cli.Command;
import cli.Shell;
import util.Config;

public class Chatserver implements IChatserverCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private ServerSocket serverSocket;
	private List<String> loggedInUsers;
	private Shell shell;
	private static ExecutorService executorService;

	/**
	 * @param componentName
	 *            the name of the component - represented in the prompt
	 * @param config
	 *            the configuration to use
	 * @param userRequestStream
	 *            the input stream to read user input from
	 * @param userResponseStream
	 *            the output stream to write the console output to
	 */
	public Chatserver(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;
		this.loggedInUsers = new ArrayList<>();
		this.shell = new Shell(componentName,userRequestStream,userResponseStream);
	}

	@Override
	public void run() {
		// TODO
	}

	@Command
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

	@Command
	@Override
	public String exit() throws IOException {
		if(serverSocket != null){
			serverSocket.close();
		}
		loggedInUsers.clear();
		shell.close();
		executorService.shutdown();
		return "Server closed.";
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Chatserver}
	 *            component
	 */
	public static void main(String[] args) {
		Chatserver chatserver = new Chatserver(args[0],
				new Config("chatserver"), System.in, System.out);
		//TODO change Sys.in and Sys.out
		chatserver.shell.register(chatserver);
		executorService = Executors.newCachedThreadPool();
		executorService.execute(chatserver.shell);
		executorService.execute(chatserver);
	}

}
