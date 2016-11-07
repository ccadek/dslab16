package chatserver.executor;


import chatserver.Chatserver;

import java.net.InetSocketAddress;
import java.util.List;

public class LookupExecutor implements IRequestExecutor{

	private List<String> arguments;

	public LookupExecutor(List<String> arguments) {
		this.arguments = arguments;
	}

	@Override
	public void execute(Chatserver chatserver) {
		InetSocketAddress userAddress = chatserver.getRegisteredUserAddress(arguments.get(0));
		String response = "Wrong username or user not registered.";
		if(userAddress != null){
			response = userAddress.toString();
		}
		chatserver.answer(response);
	}
}
