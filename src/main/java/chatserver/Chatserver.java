package chatserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;

import chatserver.executor.Answers;
import chatserver.executor.RequestParser;
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
	private static UserMap userMap;
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
		userMap = new UserMap();
	}

	/*
		Methods for access to the Usermap
	 */
	public UserMap getUserMap(){
		return userMap;
	}

	// only called when socket != null
	public void answer(String message){
		out.println(message);
	}

	public void answer(DatagramPacket packet){
		try {
			DatagramSocket s = new DatagramSocket();
			s.send(packet);
			s.close();
		} catch (SocketException e){

		} catch (IOException e){

		}
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
		try {
			while (isRunning) {
				if (socket != null) {
					request = in.readLine().trim();
				} else if (datagramPacket != null) {
					request = new String(datagramPacket.getData());
				}

				// delegate query to requestexecutor
				try {
					RequestParser parser = new RequestParser(request, socket, datagramPacket);
					parser.getRequestExecutor().execute(this);
				} catch (IllegalArgumentException e) {
					answer(Answers.INVALID_REQUEST);
				}
			}
		}
		catch (NullPointerException e){
			// when Client does not properly close the connection
		}
		catch (IOException e){

		}
		finally {
			// close Datagram-/ServerSocket, Socket and its reader and printer
			closeConnection();
		}
	}

	@Command
	@Override
	public String users() throws IOException {
		List<String> usernames = userMap.getAllLoggedInUsersnames();
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
		executorService.shutdown();
		if(serverSocket != null){
			serverSocket.close();
		}
		if(datagramSocket != null){
			datagramSocket.close();
		}
		userMap.clear();
		shell.close();
		closeConnection();
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
