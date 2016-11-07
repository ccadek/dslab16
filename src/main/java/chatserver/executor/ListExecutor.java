package chatserver.executor;

import chatserver.Chatserver;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class ListExecutor implements IRequestExecutor {

	private DatagramPacket packet;


	public ListExecutor(DatagramPacket packet) {
		this.packet = packet;
	}

	@Override
	public void execute(Chatserver chatserver) {

		List<String> userList = chatserver.getAllLoggedInUserNames();
		String response = "";

		for(int i = 0; i < userList.size(); i++){
			response += userList.get(i);
			if(i < (userList.size()-1)){
				response += ",";
			}
		}
		InetAddress address = packet.getAddress();
		int port = packet.getPort();
		byte[] answer = response.getBytes();
		packet = new DatagramPacket(answer, answer.length, address, port);
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			socket.send(packet);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(socket != null) {
				socket.close();
			}
		}

	}
}
