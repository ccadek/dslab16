package chatserver;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ServerFactory {

	public static Socket createSocket(InetAddress adress, int port) throws IOException{
		return new Socket(adress,port);
	}

	public static PrintWriter createPrintWriter(Socket socket) throws IOException {
		return new PrintWriter(socket.getOutputStream(),true);
	}
}
