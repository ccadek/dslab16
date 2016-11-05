package chatserver.util;

import chatserver.Chatserver;
import chatserver.ServerFactory;
import chatserver.UDPSender;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ListExecutor implements IRequestExecutor {

	private DatagramPacket datagramPacket;


	public ListExecutor(DatagramPacket datagramPacket) {
		this.datagramPacket = datagramPacket;
	}

	@Override
	public void execute(Chatserver chatserver) {
		byte[] buffer = new byte[1024];
		String request = new String(datagramPacket.getData());

		String[] reqArgs = request.split("\\s");
		List<String> userList = chatserver.();

		//TODO implements real answer
		String response = "something something";
		InetAddress address = datagramPacket.getAddress();
		int port = datagramPacket.getPort();
		//TODO what if the response is too big?
		ExecutorService executorService = ServerFactory.getExecutorService();
		buffer = response.getBytes();
		datagramPacket = new DatagramPacket(buffer,buffer.length,address,port);
		UDPSender sender = new UDPSender(datagramPacket);
		executorService.execute(sender);
	}
}
