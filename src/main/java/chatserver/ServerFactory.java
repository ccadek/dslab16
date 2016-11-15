package chatserver;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerFactory {

	private static ExecutorService executorService = null;

	public static ExecutorService getExecutorService(){
		if(executorService == null){
			executorService = Executors.newCachedThreadPool();
		}
		return executorService;
	}
}
