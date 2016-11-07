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
	private DatagramSocket datagramSocket;
	private PrintWriter out;
	private static ExecutorService executorService;
	private static ResponseListener responseListener;

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
		logout();
		out.println("!exit");
		responseListener.stop();
		closeConnection(socket,out);
		shell.close();
		executorService.shutdown();
		return null;
	}

	private void closeConnection(Socket s, PrintWriter p) throws IOException {
		if(s != null){
			s.close();
		}
		if(p != null){
			p.close();
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
		client.shell.register(client);
		executorService = Executors.newCachedThreadPool();
		executorService.execute(client.shell);
		executorService.execute(client);
		executorService.execute(responseListener);
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String authenticate(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
