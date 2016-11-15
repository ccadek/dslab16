package chatserver.executor;

import chatserver.Chatserver;

import java.net.InetSocketAddress;
import java.util.List;

public class MsgExecutor implements IRequestExecutor {

	private List<String> arguments;

	public MsgExecutor(List<String> arguments) {
		this.arguments = arguments;
	}

	@Override
	public void execute(Chatserver chatserver) {
		InetSocketAddress registeredUserAddress = chatserver.getUserMap().getRegisteredUser(arguments.get(0));
		String response = Answers.USER_NOT_REGISTERED;

		if(registeredUserAddress != null){
			response = registeredUserAddress.toString();
		}
		System.out.println(registeredUserAddress);
		chatserver.answer(response);
	}
}
