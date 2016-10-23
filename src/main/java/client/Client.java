package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cli.Command;
import cli.Shell;
import util.Config;

public class Client implements IClientCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private String loggedInUser;
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
	public Client(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;
		this.shell = new Shell(componentName,userRequestStream,userResponseStream);
		// TODO
	}

	private boolean checkUser(String username, String pw){
		String password = ResourceBundle.getBundle("user").getString(username);
		if (password == null) {
			return false;
		}

		if (password.equals(pw)) {
			return true;
		}

		return false;
	}

	@Override
	public void run() {
		// TODO
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
		return "!send "+message;
	}

	@Command
	@Override
	public String list() throws IOException {
		return "!list";
	}

	@Command
	@Override
	public String msg(String username, String message) throws IOException {
		return "!msg "+username+" "+message;
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
		return "!lastMsg";
	}

	@Command
	@Override
	public String exit() throws IOException {
		logout();
		shell.close();
		executorService.shutdown();
		return "!exit";
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Client} component
	 */
	public static void main(String[] args) {
		Client client = new Client(args[0], new Config("client"), System.in,
				System.out);
		client.shell.register(client);
		executorService = Executors.newCachedThreadPool();
		executorService.execute(client.shell);
		executorService.execute(client);
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String authenticate(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
