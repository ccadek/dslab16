package chatserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.*;
import java.util.*;
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
	private static HashMap<String,InetAddress> loggedInUsers;
	private static HashMap<String, InetAddress> registeredUsers;

	private static final String INVALID_REQUEST = "This is not a valid request!";

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

	@Override
	public void run() {
		if(socket != null){

		} else if(datagramPacket != null){
			byte[] buffer = new byte[1024];
			String request = new String(datagramPacket.getData());
			System.out.println("Request from client: "+request);

			String[] reqArgs = request.split("\\s");
			String response = INVALID_REQUEST;
			if(reqArgs.length == 1){
				if("!list".equals(reqArgs[0])){
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
		loggedInUsers = new HashMap<>();
		registeredUsers = new HashMap<>();

		chatserver.shell = new Shell(chatserver.componentName,chatserver.userRequestStream,chatserver.userResponseStream);
		executorService = Executors.newCachedThreadPool();
		TCPListener tcpListener = new TCPListener(chatserver.componentName, chatserver.config,
				serverSocket, executorService);
		UDPListener udpListener = new UDPListener(chatserver.componentName,chatserver.config,
				datagramSocket,executorService);
		chatserver.shell.register(chatserver);

		executorService.execute(chatserver.shell);
		executorService.execute(tcpListener);
		executorService.execute(udpListener);
	}

}
