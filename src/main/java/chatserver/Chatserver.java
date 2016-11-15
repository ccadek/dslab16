package chatserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;

import chatserver.executor.ParsingException;
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
	public static Shell shell;
	private static ExecutorService executorService;
	private static UserMap userMap;
	private BufferedReader in;
	private PrintWriter out;
	private boolean isRunning;
	private static List<Chatserver> instances = new ArrayList<>();

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
		} catch (IOException e){

		}
	}

	public void stop(){
		isRunning = false;
	}

	public void closeConnection(){
		try{
			if(in != null) {
				in.close();
			}
			if(out != null) {
				out.close();
			}
			if(socket != null) {
				socket.close();
			}
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
		synchronized (instances){
			instances.add(this);
		}
		try {
			while (isRunning) {
				if (socket != null) {
					request = in.readLine().trim();
				} else if (datagramPacket != null) {
					request = new String(datagramPacket.getData());
				}

				// delegate query to requestexecutor
				try {
					RequestParser parser = new RequestParser(request, this);
					parser.getRequestExecutor().execute(this);
				} catch (ParsingException e) {
					answer(e.getMessage());
				}
			}
		}
		catch (NullPointerException e){
			// when Client does not properly close the connection
			userMap.logoutUser((InetSocketAddress) socket.getRemoteSocketAddress());
			closeConnection();
		}
		catch (IOException e){

		}
		synchronized (instances){
			instances.remove(this);
		}

	}

	@Command
	@Override
	public String users() throws IOException {
		List<String> usernames = userMap.getAllLoggedInUsersnames();

		Config userConfig = new Config("user");
		Set<String> tmp = userConfig.listKeys();
		List<String> allusers = new ArrayList<>();
		for(String s : tmp){
			String t = s.substring(0,s.length()-9);
			allusers.add(t);
		}
		Collections.sort(allusers);

		if(usernames.size() == 0){
			return "No users logged in";
		}
		String rtn = "";
		int i = 1;
		for(String str : allusers){
			if(usernames.contains(str)) {
				rtn += i+". "+str + " online";
			} else {
				rtn += i+". "+str+" offline";
			}
			i++;
			rtn += "\n";
		}
		return rtn;
	}

	@Command
	@Override
	public String exit() throws IOException {
		// Simply close the connection for each server instance. They will remove themselves from the Instance-list
		for(Chatserver c : instances){
			c.stop();
			c.closeConnection();
		}
		executorService.shutdown();
		if(serverSocket != null){
			serverSocket.close();
		}
		if(datagramSocket != null){
			datagramSocket.close();
		}
		closeConnection();
		userMap.clear();
		shell.close();
		return "Server closed.";
	}

	/*
		Getter and Setter for testing purposes, but also utilised for cleaner Constructors and parameter lists
	 */
	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public Socket getSocket(){
		return socket;
	}

	public void setServerSocket(ServerSocket s){
		serverSocket = s;
	}

	public void setDatagramSocket(DatagramSocket d){
		datagramSocket = d;
	}

	public DatagramPacket getDatagramPacket(){
		return datagramPacket;
	}

	public List<Chatserver> getInstances(){
		return instances;
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

		Chatserver.shell = new Shell(chatserver.componentName,chatserver.userRequestStream,chatserver.userResponseStream);
		executorService = ServerFactory.getExecutorService();
		TCPListener tcpListener = new TCPListener(chatserver.componentName, new Config("user"),
				serverSocket, executorService);
		UDPListener udpListener = new UDPListener(chatserver.componentName,chatserver.config,
				datagramSocket,executorService);
		Chatserver.shell.register(chatserver);

		executorService.execute(Chatserver.shell);
		executorService.execute(tcpListener);
		executorService.execute(udpListener);
	}

}
