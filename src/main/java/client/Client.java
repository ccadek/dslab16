package client;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
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
	private Shell shell;
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
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
		try {
			this.socket = ClientFactory.createSocket();
			this.out = ClientFactory.createPrintWriter(socket);
			this.in = ClientFactory.createBufferedReader(socket);

		} catch(IOException e){

		}
		// TODO
	}

	@Override
	public void run() {
		// TODO
	}

	@Command
	@Override
	public String login(String username, String password) throws IOException {
		out.println("!login "+username+" "+password);
		String response = in.readLine();

		return response;
	}

	@Command
	@Override
	public String logout() throws IOException {
		out.println("!logout");
		String response = in.readLine();

		return response;
	}

	@Command
	@Override
	public String send(String message) throws IOException {
		out.println("!send " + message);
		String response = in.readLine();

		return response;
	}

	@Command
	@Override
	public String list() throws IOException {
		//TODO implement it
		DatagramSocket datagramSocket = ClientFactory.createDatagramSocket();
		byte[] buffer = new byte[1024];
		buffer = "!list".getBytes();
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length,
				InetAddress.getByName(config.getString("chatserver.host")),config.getInt("chatserver.udp.port"));
		datagramSocket.send(packet);

		buffer = new byte[1024];
		packet = new DatagramPacket(buffer,buffer.length);
		datagramSocket.receive(packet);
		String response = "Request was not handled by server";
		response = new String(packet.getData());
		datagramSocket.close();
		return response;
	}

	@Command
	@Override
	public String msg(String username, String message) throws IOException {
		out.println("!msg "+username+" "+message);
		String response = in.readLine();

		return response;

	}

	@Command
	@Override
	public String lookup(String username) throws IOException {
		out.println("!lookup "+username);
		String response = in.readLine();

		return response;

	}

	@Command
	@Override
	public String register(String privateAddress) throws IOException {
		//TODO create Serversocket, run thread...

		return null;
	}

	@Command
	@Override
	public String lastMsg() throws IOException {
		//TODO implement it
		return "!lastMsg";
	}

	@Command
	@Override
	public String exit() throws IOException {
		//logout();
		closeConnection(socket,in,out);
		shell.close();
		executorService.shutdown();
		return null;
	}

	private void closeConnection(Socket s, BufferedReader b, PrintWriter p) throws IOException {
		if(s != null){
			s.close();
		}
		if(b != null){
			b.close();
		}
		if(p != null){
			p.close();
		}
	}

	public Shell getShell(){
		return shell;
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
