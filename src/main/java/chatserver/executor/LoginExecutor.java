package chatserver.executor;

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
			chatserver.answer(Answers.INVALID_REQUEST);
		}
		Config config = new Config("user");
		Set<String> users = config.listKeys();
		String username = arguments.get(0);
		String usernameFile = arguments.get(0)+".password";
		String password = arguments.get(1);
		if(users.contains(usernameFile) && config.getString(usernameFile).equals(password)){
			chatserver.loginUser(username);
			chatserver.answer(Answers.SUCCESS_LOGIN);
		} else {
			chatserver.answer(Answers.INVALID_LOGIN);
		}
	}
}
