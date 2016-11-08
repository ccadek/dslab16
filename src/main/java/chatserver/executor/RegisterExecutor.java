package chatserver.executor;

import chatserver.Chatserver;
import chatserver.UserMap;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class RegisterExecutor implements IRequestExecutor {

	private List<String> arguments;
	private Socket socket;

	public RegisterExecutor(List<String> arguments, Socket socket) {
		this.arguments = arguments;
		this.socket = socket;
	}

	@Override
	public void execute(Chatserver chatserver) {
		UserMap users = chatserver.getUserMap();
		String username = users.getLoggedInUsername((InetSocketAddress) socket.getRemoteSocketAddress());
		String[] adressparts = arguments.get(0).split(":");
		int port = Integer.parseInt(adressparts[1]);
		InetSocketAddress address = InetSocketAddress.createUnresolved(adressparts[0],port);
		if(users.registerUser(username, address)){
			chatserver.answer(Answers.SUCCESS_REGISTER+" "+username+".");
		} else {
			chatserver.answer(Answers.INVALID_REQUEST);
		}
	}
}
