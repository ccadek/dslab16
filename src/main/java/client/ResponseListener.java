package client;


import cli.Shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ResponseListener implements Runnable{

	private boolean isRunning;
	private Socket socket;
	private Shell shell;

	public ResponseListener(Socket socket, Shell shell) {
		this.isRunning = true;
		this.socket = socket;
		this.shell = shell;
	}

	@Override
	public void run() {
		try(BufferedReader in = ClientFactory.createBufferedReader(socket)){
			while (isRunning){
				String response = in.readLine();
				shell.writeLine(response);
			}
		}catch(SocketException e){

		}catch (IOException e) {
		}
	}

	public void stop(){
		this.isRunning = false;
	}
}
