package chatserver.util;

import chatserver.Chatserver;

import java.util.List;

public class RegisterExecutor implements IRequestExecutor {

	private List<String> arguments;

	public RegisterExecutor(List<String> arguments) {
		this.arguments = arguments;
	}

	@Override
	public void execute(Chatserver chatserver) {

	}
}
