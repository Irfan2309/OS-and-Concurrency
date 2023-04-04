
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
import java.util.LinkedHashMap;


//IMPORTANT: DrillLoginManager must handle exceptions locally i.e. it must not  explicitly 'throw' exceptions 
//otherwise the compilation with the Test classes will fail
public class DrillLoginManager implements Manager {

	//adding a Reentrant lock
	private final ReentrantLock lock = new ReentrantLock();
	
	//adding a condition variable to block workers until a team request needs the worker
	private final Condition workerCondition = lock.newCondition();
	
	/* making a map that will store all the team that makes the request and their worker requirements 
	 * the map stores the team name as the key and the value is another map that stores the worker role and the number of workers 
	 * Using LinkedHashMap to have an order in which the requests enter the map. (so we can use FIFO) */
	private final Map<String, Map<String, Integer>> requestsMade = new LinkedHashMap<>();
	
	/* storing all the workers that log-in in a map. 
	 * storing the role along with the number of workers that are logged in and ready to work*/
	private final Map<String, Integer> availableWorkers = new HashMap<>();
	
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
			/* adding worker to the map
			 * if the role is already in the map, update the count of workers for that role
			 * else, create a key-value for that role and set count to 1 */
			if (availableWorkers.containsKey(role)) {
				
				int workerCount = availableWorkers.get(role);
				availableWorkers.put(role, workerCount + 1);
			}
			else {
				availableWorkers.put(role, 1);
			}
			
			/* check if any requests are available (check requestsMade) 
			 * check if worker is needed in any requests (check in availableWorkers)
			 * if yes, then add them to the team and remove worker from availableWorkers	
			 * if no, block using await (will be woken up by the signals in the request methods)
			 * */
			return null;
		}
		finally {
			lock.unlock();
		}	
	}
	
	private class proceedRequest {
		/*once the role requirement is fulfilled, the thread should proceed 
		 * i.e. the request should no longer be available (remove from map requestsMade) */
	}	
}
