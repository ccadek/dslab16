package chatserver.executor;

import chatserver.Chatserver;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RequestParser{

	private String command;
	private List<String> arguments;
	private Socket socket;
	private DatagramPacket datagramPacket;
	private Chatserver chatserver;

	public RequestParser(String request, Chatserver chatserver){
		this.chatserver = chatserver;
		String[] parts = request.trim().split("\\s");
		command = parts[0];
		this.arguments = new ArrayList<>();
		for(int i = 1; i < parts.length; i++){
			arguments.add(parts[i]);
		}
		this.socket = chatserver.getSocket();
		this.datagramPacket = chatserver.getDatagramPacket();
	}

	public String getCommand(){
		return command;
	}

	public IRequestExecutor getRequestExecutor() {
		if(socket != null) {
			if (chatserver.getUserMap().isUserLoggedIn((InetSocketAddress) socket.getRemoteSocketAddress())) {
				if (command.equals("!logout")) {
					return new LogoutExecutor(socket);
				} else if (command.equals("!send")) {
					return new SendExecutor(arguments, socket);
				} else if (command.equals("!lookup")) {
					return new LookupExecutor(arguments);
				} else if (command.equals("!msg")) {
					return new MsgExecutor();
				} else if (command.equals("!exit")) {
					return new ExitExecutor();
				} else if (command.equals("!register")) {
					return new RegisterExecutor(arguments, socket);
				} else {
					throw new IllegalArgumentException(Answers.INVALID_REQUEST);
				}
			} else if (command.equals("!login")) {
				return new LoginExecutor(arguments, socket);
			} else {
				throw new IllegalArgumentException(Answers.NOT_LOGGED_IN);
			}
		}
		else if(command.equals("!list")){
			return new ListExecutor(datagramPacket);
		}
		else {
			throw new IllegalArgumentException(Answers.INVALID_REQUEST);
		}
	}
}
