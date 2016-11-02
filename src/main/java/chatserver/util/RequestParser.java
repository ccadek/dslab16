package chatserver.util;

import java.util.ArrayList;
import java.util.List;

public class RequestParser{

	String command;
	List<String> arguments;

	public RequestParser(String request){
		String[] parts = request.trim().split("\\s");
		command = parts[0];
		this.arguments = new ArrayList<>():
		for(int i = 1; i < parts.length; i++){
			arguments.add(parts[i]);
		}
	}

	public String getCommand(){
		return command;
	}

	public IRequestExecutor getRequestExecutor() {
		if(command.equals("!login")){
			return new LoginExecutor(arguments);
		}
		else if(command.equals("!logout")){
			return new LogoutExecutor();
		}
		else if(command.equals("!send")){
			return new SendExecutor(arguments);
		}
		else if(command.equals("!msg")){
			return new MsgExecutor();
		}
		else if(command.equals("!list")){
			return new ListExecutor();
		}
		else if (command.equals("!register")){
			return new RegisterExecutor(arguments);
		}
		else {
			throw new IllegalArgumentException("Not a valid request.");
		}
	}
}
