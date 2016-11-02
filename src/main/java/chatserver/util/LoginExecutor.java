package chatserver.util;

import chatserver.Chatserver;
import util.Config;

import java.util.List;
import java.util.Set;

public class LoginExecutor implements IRequestExecutor {

	private List<String> arguments;

	public LoginExecutor(List<String> arguments) {
		this.arguments = arguments;
	}

	@Override
	public void execute(Chatserver chatserver) {
		if(arguments.size() != 2){
			chatserver.answer("Not a valid request.");
		}
		Config config = new Config("user");
		Set<String> users = config.listKeys();
		String username = arguments.get(0);
		String password = arguments.get(1);
		if(users.contains(username) && config.getString(username).equals(password)){
			chatserver.login(username);
			chatserver.answer("Successfully logged in.");
		} else {
			chatserver.answer("Not a valid login");
		}
	}
}
