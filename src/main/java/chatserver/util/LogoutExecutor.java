package chatserver.util;


import chatserver.Chatserver;

import java.net.InetSocketAddress;
import java.net.Socket;

public class LogoutExecutor implements IRequestExecutor {

	Socket socket;

	public LogoutExecutor(Socket socket){
		this.socket = socket;
	}

	@Override
	public void execute(Chatserver chatserver) {
		boolean logout = chatserver.logoutUser((InetSocketAddress) socket.getRemoteSocketAddress());
		if(logout){
			chatserver.answer(Answers.SUCCESS_LOGOUT);
		} else{
			chatserver.answer(Answers.NOT_LOGGED_IN);
		}
	}
}
