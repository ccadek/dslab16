package chatserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chatserver.util.Answers;
import chatserver.util.RequestParser;
import cli.Command;
import cli.Shell;
import util.Config;

public class Chatserver implements IChatserverCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private static ServerSocket serverSocket;
	private static DatagramSocket datagramSocket;
	private Socket socket;
	private DatagramPacket datagramPacket;
	public Shell shell;
	private static ExecutorService executorService;
	private static UserMap users;
	private BufferedReader in;
	private PrintWriter out;
	private boolean isRunning;

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
		this.socket = null;
		this.datagramPacket = null;
		if(userRequestStream != null) {
			this.in = new BufferedReader(new InputStreamReader(userRequestStream));
		}
		if(userResponseStream != null){
			this.out = new PrintWriter(userResponseStream, true);
		}
		isRunning = true;
	}

	/*
		Methods for access to the Usermap
	 */
	public String getUsername(InetSocketAddress remoteSocketAddress) {
		return users.getLoggedInUsername(remoteSocketAddress);
	}

	public void loginUser(String username){
		users.loginUser(username, (InetSocketAddress) socket.getRemoteSocketAddress());
	}

	public boolean logoutUser(InetSocketAddress address){
		return users.logoutUser(address);
	}

	public List<InetSocketAddress> getAllLoggedInUsersAddresses(){
		return users.getAllLoggedInUsersAddresses();
	}

	public List<String> getAllLoggedInUserNames(){
		return users.getAllLoggedInUsernames();
	}

	public InetSocketAddress getRegisteredUserAddress(String username){
		return users.getRegisteredUser(username);
	}

	// only called when socket != null
	public void answer(String message){
		out.println(message);
	}

	public void answer(DatagramPacket packet) throws IOException{
		datagramSocket.send(packet);
	}

	public void stop(){
		isRunning = false;
	}

	private void closeConnection(){
		try{
			in.close();
			out.close();
			socket.close();
		}
		catch (SocketException e){
			// just in case socket is closed before it is closed in here
		}
		catch (IOException e) {

		}
	}

	@Override
	public void run() {
		String request = "";
		while(!executorService.isShutdown()) {
			if (socket != null) {
				request = "";
				try {
					request = in.readLine().trim();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (datagramPacket != null) {
				request = new String(datagramPacket.getData());
			}

			// delegate query to requestexecutor
			try {
				RequestParser parser = new RequestParser(request, socket, datagramPacket);
				parser.getRequestExecutor().execute(this);
			} catch (IllegalArgumentException e){
				answer(Answers.INVALID_REQUEST);
			}
		}
		// close Datagram-/ServerSocket, Socket and its reader and printer
		closeConnection();
	}

	@Command
	@Override
	public String users() throws IOException {
		List<String> usernames = users.getAllLoggedInUsersnames();
		if(usernames.size() == 0){
			return "No users logged in";
		}
		String rtn = "";
		for(String str : usernames){
			rtn += str+", ";
		}
		return rtn;
	}

	@Command
	@Override
	public String exit() throws IOException {
		closeConnection();
		if(serverSocket != null){
			serverSocket.close();
		}
		if(datagramSocket != null){
			datagramSocket.close();
		}
		users.clear();
		shell.close();
		executorService.shutdown();
		return "Server closed.";
	}

	/*
		Methods mainly for testing purposes
	 */
	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public void setServerSocket(ServerSocket s){
		serverSocket = s;
	}

	public void setDatagramSocket(DatagramSocket d){
		datagramSocket = d;
	}

	public void setUserMap(UserMap u){
		users = u;
	}

	public void setExecutorService(ExecutorService service){
		executorService = service;
	}

	public void setDatagramPacket(DatagramPacket datagramPacket) {
		this.datagramPacket = datagramPacket;
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Chatserver}
	 *            component
	 */
	public static void main(String[] args) {
		Chatserver chatserver = new Chatserver(args[0],
				new Config("chatserver"), System.in, System.out);
		try {
			serverSocket = new ServerSocket(chatserver.config.getInt("tcp.port"));
			datagramSocket = new DatagramSocket(chatserver.config.getInt("udp.port"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		users = new UserMap();

		chatserver.shell = new Shell(chatserver.componentName,chatserver.userRequestStream,chatserver.userResponseStream);
		executorService = ServerFactory.getExecutorService();
		TCPListener tcpListener = new TCPListener(chatserver.componentName, new Config("user"),
				serverSocket, executorService);
		UDPListener udpListener = new UDPListener(chatserver.componentName,chatserver.config,
				datagramSocket,executorService);
		chatserver.shell.register(chatserver);

		executorService.execute(chatserver.shell);
		executorService.execute(tcpListener);
		executorService.execute(udpListener);
	}

}
