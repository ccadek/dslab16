package test;

import chatserver.*;
import cli.Shell;
import nameserver.INameserverCli;
import nameserver.Nameserver;
import util.Config;
import util.TestInputStream;
import util.TestOutputStream;
import client.Client;
import client.IClientCli;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Provides methods for starting an arbitrary amount of various components.
 */
public class ComponentFactory {
	/**
	 * Creates and starts a new client instance using the provided
	 * {@link Config} and I/O streams.
	 *
	 * @param componentName
	 *            the name of the component to create
	 * @return the created component after starting it successfully
	 * @throws Exception
	 *             if an exception occurs
	 */
	public IClientCli createClient(String componentName, TestInputStream in,
			TestOutputStream out) throws Exception {
		/*
		 * TODO: Here you can do anything in order to construct a client
		 * instance. Depending on your code you might want to modify the
		 * following lines but you do not have to.
		 */
		Config config = new Config("client");
		Client client = new Client(componentName, config, in, out);
		ExecutorService executorService = Executors.newCachedThreadPool();
		executorService.execute(client.getShell());
		executorService.execute(client);
		return client;
	}

	/**
	 * Creates and starts a new chatserver instance using the provided
	 * {@link Config} and I/O streams.
	 *
	 * @param componentName
	 *            the name of the component to create
	 * @return the created component after starting it successfully
	 * @throws Exception
	 *             if an exception occurs
	 */
	public IChatserverCli createChatserver(String componentName,
			TestInputStream in, TestOutputStream out) throws Exception {
		/*
		 * TODO: Here you can do anything in order to construct a chatserver
		 * instance. Depending on your code you might want to modify the
		 * following lines but you do not have to.
		 */
		Config config = new Config(componentName);
		Chatserver chatserver = new Chatserver(componentName,
				config, in, out);
		ServerSocket serverSocket = null;
		DatagramSocket datagramSocket = null;
		try {
			serverSocket = new ServerSocket(config.getInt("tcp.port"));
			datagramSocket = new DatagramSocket(config.getInt("udp.port"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		chatserver.setServerSocket(serverSocket);
		chatserver.setDatagramSocket(datagramSocket);

		Chatserver.shell = new Shell(componentName,System.in,System.out);
		ExecutorService executorService = Executors.newCachedThreadPool();
		chatserver.setExecutorService(executorService);
		TCPListener tcpListener = new TCPListener(componentName, new Config("user"),
				serverSocket, executorService);
		UDPListener udpListener = new UDPListener(componentName,config,
				datagramSocket,executorService);
		Chatserver.shell.register(chatserver);

		executorService.execute(Chatserver.shell);
		executorService.execute(tcpListener);
		executorService.execute(udpListener);
		return chatserver;
	}

	// --- Methods needed for Lab 2. Please note that you do not have to
	// use them for the first submission. ---

	/**
	 * Creates and starts a new nameserver instance using the provided
	 * {@link Config} and I/O streams.
	 *
	 * @param componentName
	 *            the name of the component to create
	 * @return the created component after starting it successfully
	 * @throws Exception
	 *             if an exception occurs
	 */
	public INameserverCli createNameserver(String componentName, TestInputStream in,
			TestOutputStream out) throws Exception {
		/*
		 * TODO: Here you can do anything in order to construct a nameserver
		 * instance. Depending on your code you might want to modify the
		 * following lines but you do not have to.
		 */
		Config config = new Config(componentName);
		return new Nameserver(componentName, config, in, out);
	}
}
