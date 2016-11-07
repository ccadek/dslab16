package chatserver.executor;


import chatserver.Chatserver;

public class ExitExecutor implements IRequestExecutor{

	@Override
	public void execute(Chatserver chatserver) {
		chatserver.stop();
	}
}
