package client;

import java.io.*;
import java.net.*;
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
	private DatagramSocket datagramSocket;
	private PrintWriter out;
	ServerSocket privateMsgServerSocket;
	private static ExecutorService executorService;
	private ResponseListener responseListener;

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
		this.shell.register(this);
		try {
			this.datagramSocket = ClientFactory.createDatagramSocket();
			this.socket = ClientFactory.getConfigSocket();
			this.out = ClientFactory.createPrintWriter(socket);

		} catch(IOException e){
			try {
				shell.writeLine("socket already used\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		responseListener = new ResponseListener(socket,shell);
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
		//String response = in.readLine();

		return null;
	}

	@Command
	@Override
	public String logout() throws IOException {
		out.println("!logout");
		//String response = in.readLine();

		return null;
	}

	@Command
	@Override
	public String send(String message) throws IOException {
		out.println("!send " + message);
		//String response = in.readLine();

		return null;
	}

	@Command
	@Override
	public String list() throws IOException {
		//TODO implement it
		byte[] buffer = new byte[1024];
		buffer = "!list".getBytes();
		InetAddress address = InetAddress.getByName(config.getString("chatserver.host"));
		int port = config.getInt("chatserver.udp.port");
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length, address, port);
		datagramSocket.send(packet);

		buffer = new byte[1024];
		packet = new DatagramPacket(buffer,buffer.length);
		datagramSocket.receive(packet);
		String response = new String(packet.getData());
		String[] parts = response.split(",");
		StringBuilder rst = new StringBuilder("Online Users:\n");
		for(int i = 0; i < parts.length; i++){
			rst.append("* "+parts[i]+"\n");
		}

		return rst.toString();
	}

	@Command
	@Override
	public String msg(String username, String message) throws IOException {
		out.println("!msg "+username+" "+message);
		//String response = in.readLine();

		return null;

	}

	@Command
	@Override
	public String lookup(String username) throws IOException {
		out.println("!lookup "+username);
		//String response = in.readLine();

		return null;

	}

	@Command
	@Override
	public String register(String privateAddress) throws IOException {
		String[] parts = privateAddress.split(":");
		String error = "Invalid address";
		if(parts.length != 2){
			return error;
		}
		int port = 0;
		try {
			InetAddress address = InetAddress.getByName(parts[0]);
			port = Integer.parseInt(parts[1]);
		} catch (UnknownHostException e){
			return error;
		} catch (NumberFormatException e){
			return error;
		}
		out.println("!register "+privateAddress);

		privateMsgServerSocket = new ServerSocket(port);
		PrivateMessageListener privateMessageListener = new PrivateMessageListener(privateMsgServerSocket,shell);
		executorService.execute(privateMessageListener);

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
		logout();
		out.println("!exit");
		responseListener.stop();
		if(privateMsgServerSocket != null) {
			privateMsgServerSocket.close();
		}
		closeConnection();
		shell.close();
		executorService.shutdown();
		return null;
	}

	private void closeConnection() throws IOException {
		if(socket != null){
			socket.close();
		}
		if(out != null){
			out.close();
		}
	}

	public Shell getShell(){
		return shell;
	}

	public Socket getSocket(){
		return socket;
	}

	public ResponseListener getResponseListener(){
		return responseListener;
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Client} component
	 */
	public static void main(String[] args) {
		Client client = new Client(args[0], new Config("client"), System.in,
				System.out);
		executorService = Executors.newCachedThreadPool();
		executorService.execute(client.getShell());
		executorService.execute(client);
		executorService.execute(client.getResponseListener());
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String authenticate(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
