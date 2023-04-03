
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition; //Note that the 'notifyAll' method or similar polling mechanism MUST not be used

// IMPORTANT:
//Thread safe classes other than those above (e.g. java.util.concurrent) MUST not be used.
//You MUST not use the keyword 'synchronized', or any other `thread safe` classes or mechanisms  
//or any delays or 'busy waiting' (spin lock) methods.
//However, you may import non-tread safe classes e.g.:
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;


//IMPORTANT: DrillLoginManager must handle exceptions locally i.e. it must not  explicitly 'throw' exceptions 
//otherwise the compilation with the Test classes will fail
public class DrillLoginManager implements Manager {

	//adding a Reentrant lock
	private final ReentrantLock lock = new ReentrantLock();
	
	//adding a condition variable to block workers until a team request needs the worker
	private final Condition workerCondition = lock.newCondition();
	
	//making a map that will store all the team that makes the request and their worker requirements
	//the map stores the team name as the key and the value is another map that stores the worker role and the number of workers
	private final Map<String, Map<String, Integer>> requestsMade = new HashMap<>();
	
	@Override
	public void smallTeamRequest(Map<String, Integer> team) {
		
		//lock protection
		lock.lock();
		try {
			//add the request to the map
			requestsMade.put(null, team);
			//unblock a worker
			workerCondition.signal();
		}
		finally {
			lock.unlock();
		}	
	}

	@Override
	public void drillerRequest(String teamName, Map<String, Integer> team) {
		
		//lock protection
		lock.lock();
		try {
			//add the request to the map
			requestsMade.put(teamName, team);
			//unblock a worker
			workerCondition.signal();
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public String workerLogin(String role) {
		
		//lock protection
		lock.lock();
		try {
			// TODO Your code here	
			//e.g.
			myMethod();
			return null; //Note that this return string is ignored by all UR except UR6
		}
		finally {
			lock.unlock();
		}	
	}
	
	//Note that you may add inner classes and methods to this file as shown below:
	private class PriavateClass1 {
		void myMethod() {};
		// TODO Your code here	
	}
	
	void myMethod() {
		// TODO Your code here	
	}

}
