package chatserver.util;

import chatserver.Chatserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

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
		String response = Answers.INVALID_REQUEST;
		if(reqArgs.length == 1){
			if(request.equals("!list")){
				//TODO implements real answer
				response = "something something";
			}
		}
		InetAddress address = datagramPacket.getAddress();
		int port = datagramPacket.getPort();
		//TODO what if the response is too big?
		buffer = response.getBytes();
		datagramPacket = new DatagramPacket(buffer,buffer.length,address,port);
		try {
			//TODO isn't the datagramPacket blocked by the Listener?
			//TODO delegate to chatserver
			chatserver.answer(datagramPacket);
		} catch (SocketException e){
			//thrown if executorservice shuts down
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
