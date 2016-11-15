package client;

import cli.Shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class PrivateMessageListener implements Runnable{

	private ServerSocket serverSocket;
	private Shell shell;
	private boolean isRunning;

	public PrivateMessageListener(ServerSocket serverSocket, Shell shell) {
		this.serverSocket = serverSocket;
		this.shell = shell;
		isRunning = true;
	}

	private String getPrivateMessage(String msg){
		String[] parts = msg.trim().split(" ");

		String message = parts[1]+": ";
		for(int i = 2; i < parts.length; i++){
			message += parts[i]+" ";
			if(i < (parts.length-1)){
				message += " ";
			}
		}
		return message;
	}

	@Override
	public void run() {
		Socket socket = null;
		BufferedReader in = null;
		PrintWriter out = null;
		try{
			while(isRunning){
				socket = serverSocket.accept();
				in = ClientFactory.createBufferedReader(socket);
				out = ClientFactory.createPrintWriter(socket);
				String message = in.readLine();
				message = getPrivateMessage(message);
				out.println("!ack");
				shell.writeLine(message);
				out.close();
				in.close();
				socket.close();
			}
		} catch (SocketException e){

		}
		catch (IOException e){

		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
				if (socket != null) {
					socket.close();
				}
				if (serverSocket != null) {
					serverSocket.close();
				}
			} catch (IOException e){

			}
		}
	}
}
