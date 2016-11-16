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
	private PrintWriter out;
	private ServerSocket privateMsgServerSocket;
	private boolean isRunning;
	private String lastMessage;
	private String privateAddressOfUser;
	//needed for private Messages
	private String ownUsername;
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
		this.shell.register(this);
		try {
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
		isRunning = true;
		lastMessage = "No public message has been received yet.";
		privateAddressOfUser = "";
	}

	@Override
	public void run() {
		try(BufferedReader in = ClientFactory.createBufferedReader(socket)){
			while (isRunning){
				String response = in.readLine().trim();
				if(response.startsWith("!pub")){
					lastMessage = response.substring(5,response.length());
					shell.writeLine(lastMessage);
				}
				else if(response.startsWith("!msg")){
					synchronized (privateAddressOfUser) {
						privateAddressOfUser = response.substring(4, response.length());
					}
				}
				else {
					shell.writeLine(response);
				}
			}
		}catch(SocketException e){

		}catch (IOException e) {
		}
	}

	@Command
	@Override
	public String login(String username, String password) throws IOException {
		out.println("!login "+username+" "+password);
		// it doesn't matter that we saved a wrong username if login fails
		// the username is only used for privateMessages, nothing else
		this.ownUsername = username;
		return null;
	}

	@Command
	@Override
	public String logout() throws IOException {
		out.println("!logout");
		return null;
	}

	@Command
	@Override
	public String send(String message) throws IOException {
		out.println("!send " + message);
		lastMessage = ownUsername+": "+message;
		return null;
	}

	@Command
	@Override
	public String list() throws IOException {
		byte[] buffer = "!list".getBytes();
		InetAddress address = InetAddress.getByName(config.getString("chatserver.host"));
		int port = config.getInt("chatserver.udp.port");
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length, address, port);
		DatagramSocket datagramSocket = ClientFactory.createDatagramSocket();
		datagramSocket.send(packet);

		buffer = new byte[1024];
		packet = new DatagramPacket(buffer,buffer.length);
		datagramSocket.receive(packet);
		String response = new String(packet.getData());
		String[] parts = response.trim().split(",");
		StringBuilder rst = new StringBuilder("Online Users:\n");
		for(int i = 0; i < parts.length; i++){
			rst.append("* "+parts[i]+"\n");
		}
		datagramSocket.close();
		return rst.toString();
	}

	@Command
	@Override
	public String msg(String username, String message) throws IOException {
		privateAddressOfUser = "";
		out.println("!msg "+username);
		while(privateAddressOfUser.isEmpty()){
			// wait until private address of 'username' is returned.
		}
		String[] parts = privateAddressOfUser.split(":");
		InetAddress address;
		try {
			address = InetAddress.getByName(parts[0]);
		} catch (UnknownHostException e){
			// Incase the other user is on localhost
			address = InetAddress.getByName(null);
		}

		int port = Integer.parseInt(parts[1]);
		Socket s = new Socket(address, port);
		PrintWriter o = ClientFactory.createPrintWriter(s);
		BufferedReader i = ClientFactory.createBufferedReader(s);

		o.println("!msg "+ownUsername+" "+message);
		String privateResponse = i.readLine();

		o.close();
		s.close();
		return username+" replied with "+privateResponse;

	}

	@Command
	@Override
	public String lookup(String username) throws IOException {
		out.println("!lookup "+username);
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
		try {
			privateMsgServerSocket = new ServerSocket(port);
			out.println("!register "+privateAddress);
			PrivateMessageListener privateMessageListener = new PrivateMessageListener(privateMsgServerSocket, shell);
			executorService.execute(privateMessageListener);
			return null;
		} catch (BindException e){
			return "Port already used, choose another one";
		}
	}

	@Command
	@Override
	public String lastMsg() throws IOException {
		return lastMessage;
	}

	@Command
	@Override
	public String exit() throws IOException {
		logout();
		out.println("!exit");
		stop();
		if(privateMsgServerSocket != null) {
			privateMsgServerSocket.close();
		}
		closeConnection();
		shell.close();
		executorService.shutdown();
		return null;
	}

	private void stop(){
		isRunning = false;
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
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String authenticate(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
