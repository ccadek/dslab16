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
		System.out.println(arguments.get(0));
		InetSocketAddress registeredUserAddress = chatserver.getUserMap().getRegisteredUser(arguments.get(0));
		System.out.println(registeredUserAddress.toString());
		String response = Answers.USER_NOT_REGISTERED;

		if(registeredUserAddress != null){
			response = registeredUserAddress.toString();
		}
		System.out.println(registeredUserAddress);
		chatserver.answer(response);
	}
}
