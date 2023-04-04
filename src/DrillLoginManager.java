
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
import java.util.Iterator;
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
	private final LinkedHashMap<String, Map<String, Integer>> requestsMade = new LinkedHashMap<>();
	
	/* storing all the workers that log-in in a map. 
	 * storing the role along with the number of workers that are logged in and ready to work*/
	private final HashMap<String, Integer> availableWorkers = new HashMap<>();
	
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
			
			/* check 2 possible scenarios: 
			 * if the request contains the needed role 
			 * if the request has already fulfilled the role requirement
			 * 
			 * if scenarios are positive, then add the worker to the team and remove them from availableWorkers	
			 * else use await. this will block the worker thread until a signal is called on it (use c.v. workerCondition)
			 * */
			while (true) {
				
				String teamName = null;
				Map<String, Integer> team = null;
				
				//use a for loop to iterate through the requests
				for (Map.Entry<String, Map<String, Integer>> iteration : requestsMade.entrySet()) {
					//store the current iterations team
					Map<String, Integer> currTeam = iteration.getValue();
					
					//store the request information if the worker can be used for this team
					if(currTeam.containsKey(role) && currTeam.get(role) > 0) {
						teamName = iteration.getKey();
						team = currTeam;
						break;
					}
				}
				/* Add the worker to the team
				 * decrement the number of workers needed for that role in the request
				 * also remove the worker from available workers (similar decrement from role) */
				team.put(role, team.get(role) - 1);
				availableWorkers.put(role, availableWorkers.get(role) -1);
			}	
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
