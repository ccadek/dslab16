package chatserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chatserver.util.IRequestExecutor;
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
	private Shell shell;
	private static ExecutorService executorService;
	private static volatile UserMap users;
	private BufferedReader in;
	private PrintWriter out;

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
		this.in = null;
		this.out = null;
	}

	private boolean checkUser(String username, String password){
		Set<String> users = config.listKeys();
		if(users.contains(username) && config.getString(username).equals(password)){
			return true;
		}
		return false;
	}


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

	public void answer(String message){
		out.println(message);
	}

	public void answer(DatagramPacket packet) throws IOException{
		//TODO implement
	}

	@Override
	public void run() {
		String request = "";
		if(socket != null){
			in = new BufferedReader(new InputStreamReader(userRequestStream));
			out = new PrintWriter(userResponseStream,true);
			request = "";
			try {
				request = in.readLine().trim();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if(datagramPacket != null){
			byte[] buffer = new byte[1024];
			request = new String(datagramPacket.getData());
		}

		// delegate query to requestexecutor
		RequestParser parser = new RequestParser(request,socket,datagramPacket);
		parser.getRequestExecutor().execute(this);

		try {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		} catch (SocketException e){

		}
		catch (IOException e){

		}

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

	public void setSocket(Socket socket) {
		this.socket = socket;
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
		executorService = Executors.newCachedThreadPool();
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
