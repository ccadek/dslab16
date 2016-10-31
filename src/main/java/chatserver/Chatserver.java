package chatserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
	private static ServerSocket serverSocket;
	private static DatagramSocket datagramSocket;
	private Socket socket;
	private DatagramPacket datagramPacket;
	private Shell shell;
	private static ExecutorService executorService;
	private static ConcurrentHashMap<String,InetSocketAddress> loggedInUsers;
	private static ConcurrentHashMap<String, InetSocketAddress> registeredUsers;

	private static final String INVALID_REQUEST = "This is not a valid request.";
	private static final String NOT_LOGGED_IN = "Not logged in.";

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
	}

	private boolean checkUser(String username, String password){
		Set<String> users = config.listKeys();
		if(users.contains(username) && config.getString(username).equals(password)){
			return true;
		}
		return false;
	}

	private boolean isUserLoggedIn(String username){
		return loggedInUsers.containsKey(username);
	}

	private boolean isUserLoggedIn(SocketAddress user){
		return loggedInUsers.values().contains(user);
	}

	@Override
	public void run() {
		if(socket != null){
			BufferedReader in = new BufferedReader(new InputStreamReader(userRequestStream));
			PrintWriter out = new PrintWriter(userResponseStream,true);
			String request = "";
			try {
				request = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String[] requestParts = request.split("\\s");

			// !login
			if(request.trim().startsWith("!login") && requestParts.length == 3){
				if(checkUser(requestParts[1],requestParts[2])){
					loggedInUsers.put(requestParts[1], (InetSocketAddress) socket.getRemoteSocketAddress());
					out.println("Successful login.");
				} else {
					out.println("Wrong username or password.");
				}
			}

			if(!isUserLoggedIn(socket.getRemoteSocketAddress())){
				out.println("Not logged in.");
			}

			// !logout
			else if(request.trim().startsWith("!logout") && requestParts.length == 1){
				SocketAddress user = socket.getRemoteSocketAddress();
				if(loggedInUsers.values().contains(user)) {
					loggedInUsers.values().remove(user);
					registeredUsers.values().remove(user);
					out.println("Successfully logged out.");
				} else {
					out.println("Not logged in.");
				}
			}

			// !send msg
			else if(request.trim().startsWith("!send") && requestParts.length == 2){
				String message = "";
				String username = "";
				//TODO really done?
				for(Map.Entry entry: loggedInUsers.entrySet()){
					if(socket.getRemoteSocketAddress().equals(entry.getValue())){
						username = (String) entry.getKey();
						break; //breaking because its one to one map
					}
				}
				for(int i = 1; i < requestParts.length; i++){
					message += requestParts[i];
				}
				for(InetSocketAddress adress : loggedInUsers.values()){
					if(adress == socket.getRemoteSocketAddress()){
						continue;
					}
					try {
						Socket s = ServerFactory.createSocket(adress.getAddress(),adress.getPort());
						PrintWriter messageOut = ServerFactory.createPrintWriter(socket);
						messageOut.println(username+": "+message);
						messageOut.close();
						s.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				in.close();
				out.close();
				socket.close();
				in.close();
				out.close();
			}
			catch(SocketException e){
				// In case executorservice shuts down the socket from another thread
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			out.println(INVALID_REQUEST);


		} else if(datagramPacket != null){
			byte[] buffer = new byte[1024];
			String request = new String(datagramPacket.getData());

			String[] reqArgs = request.split("\\s");
			String response = INVALID_REQUEST;
			if(reqArgs.length == 1){
				if(request.trim().equals("!list")){
					//TODO implements real answer
					response = "something something";
				}
			}
			InetAddress address = datagramPacket.getAddress();
			int port = datagramPacket.getPort();
			//TODO what if the response is too big?
			buffer = response.getBytes();
			datagramPacket = new DatagramPacket(buffer,buffer.length,address,port);
			try {
				//TODO isn't the datagramSocket blocked by the Listener?
				datagramSocket.send(datagramPacket);
			} catch (SocketException e){
				//thrown if executorservice shuts down
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	@Override
	public String users() throws IOException {
		if(loggedInUsers.size() == 0){
			return "No users logged in";
		}
		String rtn = "";
		for(String str : loggedInUsers.keySet()){
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
		loggedInUsers.clear();
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
		loggedInUsers = new ConcurrentHashMap<>();
		registeredUsers = new ConcurrentHashMap<>();

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
