package chatserver.executor;

import java.net.DatagramPacket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RequestParser{

	private String command;
	private List<String> arguments;
	private Socket socket;
	private DatagramPacket datagramPacket;

	public RequestParser(String request, Socket socket, DatagramPacket datagramPacket){
		String[] parts = request.trim().split("\\s");
		command = parts[0];
		this.arguments = new ArrayList<>();
		for(int i = 1; i < parts.length; i++){
			arguments.add(parts[i]);
		}
		this.socket = socket;
		this.datagramPacket = datagramPacket;
	}

	public String getCommand(){
		return command;
	}

	public IRequestExecutor getRequestExecutor() {
		if(command.equals("!login")){
			return new LoginExecutor(arguments);
		}
		else if(command.equals("!logout")){
			return new LogoutExecutor(socket);
		}
		else if(command.equals("!send")){
			return new SendExecutor(arguments, socket);
		}
		else if(command.equals("!lookup")){
			return new LookupExecutor(arguments);
		}
		else if(command.equals("!msg")){
			return new MsgExecutor();
		}
		else if(command.equals("!list")){
			return new ListExecutor(datagramPacket);
		}
		else if (command.equals("!register")){
			return new RegisterExecutor(arguments);
		}
		else {
			throw new IllegalArgumentException();
		}
	}
}
