package chatserver;


import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserMap {

	private static volatile Map<String, InetSocketAddress> loggedInUsers = new HashMap<>();
	private static volatile Map<String, InetSocketAddress> registeredUsers = new HashMap<>();

	public synchronized boolean loginUser(String name, InetSocketAddress adress){
		if(!loggedInUsers.containsKey(name)) {
			loggedInUsers.put(name, adress);
			return true;
		}
		return false;
	}

	public synchronized boolean registerUser(String name, InetSocketAddress address){
		if(!registeredUsers.containsKey(name)){
			registeredUsers.put(name,address);
			return true;
		}
		return false;
	}

	public synchronized boolean logoutUser(InetSocketAddress address){
		String username = "";
		for(String name : loggedInUsers.keySet()){
			if(loggedInUsers.get(name).equals(address)){
				username = name;
			}
		}
		if(username.isEmpty()){
			return false;
		}

		loggedInUsers.remove(username);
		registeredUsers.remove(username);

		return true;
	}

	public synchronized List<InetSocketAddress> getAllLoggedInUsersAddresses(){
		List<InetSocketAddress> list = new ArrayList<>();
		for(InetSocketAddress address : loggedInUsers.values()){
			list.add(address);
		}
		return list;
	}

	public synchronized List<String> getAllLoggedInUsersnames(){
		List<String> list = new ArrayList<>();
		for(String name : loggedInUsers.keySet()){
			list.add(name);
		}
		return list;
	}

	public synchronized String getLoggedInUsername(InetSocketAddress address){
		String name = "";
		for(String user : loggedInUsers.keySet()){
			if(loggedInUsers.get(user).equals(address)){
				name = user;
				break;
			}
		}
		return name;
	}

	public synchronized boolean isUserLoggedIn(String username){
		return loggedInUsers.containsKey(username);
	}

	public synchronized boolean isUserLoggedIn(InetSocketAddress address){
		return loggedInUsers.values().contains(address);
	}

	public synchronized void clear(){
		loggedInUsers.clear();
		registeredUsers.clear();
	}

	public InetSocketAddress getRegisteredUser(String username) {
		return registeredUsers.get(username);
	}
}
