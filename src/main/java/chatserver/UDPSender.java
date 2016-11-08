package chatserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPSender implements Runnable{

	private DatagramPacket packet;

	public UDPSender(DatagramPacket packet){
		this.packet = packet;
	}

	@Override
	public void run() {
		try {
			DatagramSocket s = new DatagramSocket();
			s.send(packet);
			s.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
