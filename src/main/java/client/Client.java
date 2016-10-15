package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import util.Config;

public class Client implements IClientCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;

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

		// TODO
	}

	@Override
	public void run() {
		// TODO
	}

	@Override
	public String login(String username, String password) throws IOException {
		return "!login "+username+" "+password;
	}

	@Override
	public String logout() throws IOException {
		return "!logout";
	}

	@Override
	public String send(String message) throws IOException {
		return "!send "+message;
	}

	@Override
	public String list() throws IOException {
		return "!list";
	}

	@Override
	public String msg(String username, String message) throws IOException {
		return "!msg "+username+" "+message;
	}

	@Override
	public String lookup(String username) throws IOException {
		return "!lookup "+username;
	}

	@Override
	public String register(String privateAddress) throws IOException {
		return "!register "+privateAddress;
	}
	
	@Override
	public String lastMsg() throws IOException {
		return "!lastMsg";
	}

	@Override
	public String exit() throws IOException {
		return "!exit";
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Client} component
	 */
	public static void main(String[] args) {
		Client client = new Client(args[0], new Config("client"), System.in,
				System.out);
		// TODO: start the client
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String authenticate(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
