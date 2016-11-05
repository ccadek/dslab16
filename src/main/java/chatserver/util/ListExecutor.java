package chatserver.util;

import chatserver.Chatserver;
import chatserver.ServerFactory;
import chatserver.UDPSender;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ListExecutor implements IRequestExecutor {

	private DatagramPacket packet;


	public ListExecutor(DatagramPacket packet) {
		this.packet = packet;
	}

	@Override
	public void execute(Chatserver chatserver) {
		byte[] buffer = new byte[1024];
		String request = new String(packet.getData());

		String[] reqArgs = request.split("\\s");
		List<String> userList = chatserver.getAllLoggedInUserNames();

		for(String name : userList) {

			InetAddress address = packet.getAddress();
			int port = packet.getPort();
			//TODO what if the response is too big?
			ExecutorService executorService = ServerFactory.getExecutorService();
			buffer = name.getBytes();
			packet = new DatagramPacket(buffer, buffer.length, address, port);
			UDPSender sender = new UDPSender(packet);
			executorService.execute(sender);
		}
	}
}
