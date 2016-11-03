package chatserver.util;

import chatserver.Chatserver;
import chatserver.ServerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class SendExecutor implements IRequestExecutor {

	private List<String> arguments;
	private Socket socket;

	public SendExecutor(List<String> arguments, Socket socket) {
		this.arguments = arguments;
		this.socket = socket;
	}

	@Override
	public void execute(Chatserver chatserver) {
		String username = chatserver.getUsername((InetSocketAddress) socket.getRemoteSocketAddress());
		String message = "";
		for(String part : arguments){
			message += part;
		}
		for(InetSocketAddress adress : chatserver.getAllLoggedInUsersAddresses()){
			if(adress == socket.getRemoteSocketAddress()){
				continue;
			}
			try {
				Socket s = ServerFactory.createSocket(adress.getAddress(),adress.getPort());
				PrintWriter messageOut = ServerFactory.createPrintWriter(socket);
				messageOut.println(username+": "+message);
				messageOut.close();
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
