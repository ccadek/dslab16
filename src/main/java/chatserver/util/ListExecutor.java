package chatserver.util;

import chatserver.Chatserver;

import java.net.DatagramSocket;

public class ListExecutor implements IRequestExecutor {

	private DatagramSocket datagramSocket;

	public ListExecutor(DatagramSocket datagramSocket) {
		this.datagramSocket = datagramSocket;
	}

	@Override
	public void execute(Chatserver chatserver) {

	}
}
