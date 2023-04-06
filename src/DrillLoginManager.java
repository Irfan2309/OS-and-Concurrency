
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition; //Note that the 'notifyAll' method or similar polling mechanism MUST not be used

// IMPORTANT:
//Thread safe classes other than those above (e.g. java.util.concurrent) MUST not be used.
//You MUST not use the keyword 'synchronized', or any other `thread safe` classes or mechanisms  
//or any delays or 'busy waiting' (spin lock) methods.
//However, you may import non-tread safe classes e.g.:
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;


//IMPORTANT: DrillLoginManager must handle exceptions locally i.e. it must not  explicitly 'throw' exceptions 
//otherwise the compilation with the Test classes will fail
public class DrillLoginManager implements Manager {

	//adding a Reentrant lock
	private final ReentrantLock lock = new ReentrantLock();
	
	//adding a condition variable to block workers until a team request needs the worker
	private final Condition workerCondition = lock.newCondition();
	
	//adding a condition variable to block driller
	private final Condition drillerConditon = lock.newCondition();
	
	//adding a condition variable to check if the request is fulfilled or not
	private final Condition isFulfilled = lock.newCondition();
	
	/* making a map that will store all the team that makes the request and their worker requirements 
	 * the map stores the team name as the key and the value is another map that stores the worker role and the number of workers 
	 * Using LinkedHashMap to have an order in which the requests enter the map. (so we can use FIFO) */
	private final LinkedHashMap<String, Map<String, Integer>> requestsMade = new LinkedHashMap<>();
	
	/* storing all the workers that log-in in a map. 
	 * storing the role along with the number of workers that are logged in and ready to work*/
	private final HashMap<String, Integer> availableWorkers = new HashMap<>();
	
	//checking if the request put forth is by a drillerTeam or not 
	private boolean byDriller = false;
	
	//keeps a track of all workers that log-in
	private Integer workerCount = 0;
		
	@Override
	public void smallTeamRequest(Map<String, Integer> team) {
		
		//lock protection
		lock.lock();
		try {
			//add the request to the map
			String teamName = "team" + (requestsMade.size() +1);
			requestsMade.put(teamName, team);
			
			//unblock worker
			callSignal(team);
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
			
			//setting the variable true because request is by driller team
			byDriller = true;
			
			//add the request to the map
			requestsMade.put(teamName, team);
			
			//remove the driller from the request and process the rest as normal
			for (Map.Entry<String, Map<String, Integer>> requests : requestsMade.entrySet()) {
				requests.getValue().remove("Driller");
			}			
			//unblock worker
			callSignal(team);
			
			//await the driller
			drillerConditon.awaitUninterruptibly();
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
			/* adding worker to the map*/
			loginWorker(role);
			
			/* check possible scenarios: 
			 * if the team in the request is null
			 * if the request contains the needed role 
			 * if the request for the role requirement has already fulfilled
			 * if scenarios are positive, then add the worker to the team
			 * else use await. this will block the worker thread until a signal is called on it (use c.v. workerCondition)
			 * */
			while (true) {
				
				//checking if the linkedHashMap is empty before getting its head value
				if(requestsMade.isEmpty()) {
					workerCondition.awaitUninterruptibly();
				}
				else {
				 Map.Entry<String, Map<String, Integer>> head = requestsMade.entrySet().iterator().next();	
				 Map<String, Integer> team = head.getValue();
				 String teamName = head.getKey();
							
					if((team != null) && team.containsKey(role) &&  team.get(role) > 0) {
						
				
							/* Add the worker to the team */
							addWorker(team, role);
							
							//if the team has no worker requirement left, remove the team request
							if (canProceed(team)) {
								
								//if the request was by a drillerTeam, unblock the driller
								if(byDriller) {
									drillerConditon.signal();
								}
								
								requestsMade.remove(teamName);
								return(teamName);
							}
							
							//unblock a waiting worker
							workerCondition.signal();
							
							
							//block until the request can be processed
							isFulfilled.awaitUninterruptibly();
							//return the team name as per requirements
							return teamName;
						}
						else {
							workerCondition.awaitUninterruptibly();	
						}
					}
				}
			}
		
		finally {
			lock.unlock();
		}	
	}
	
	//using helper functions to simplify workerLogin
	
	/* if the role is already in the map, update the count of workers for that role
	 * else, create a key-value for that role and set count to 1 */
	private void loginWorker(String role) {
		if (availableWorkers.containsKey(role)) {
			int workerCount = availableWorkers.get(role);
			availableWorkers.put(role, workerCount + 1);
		}
		else {
			availableWorkers.put(role, 1);
		}
		workerCount ++;
	}
	
	/* decrement the number of workers needed for that role in the request
	 * also remove the worker from available workers (similar decrement from role) */
	private void addWorker(Map<String, Integer> team, String role) {
		team.put(role, team.get(role) - 1);
		availableWorkers.put(role, availableWorkers.get(role) -1);
	}
	
	private void callSignal(Map<String, Integer> request) {
		int requestCount = 0;
		
		 for (Map.Entry<String, Integer> roles : request.entrySet()) {
			 int currCount = roles.getValue();
			 requestCount += currCount;
		 }

		for (int i = 0; i < requestCount; i++)
		{
			workerCondition.signal();
		}
	}
	
	//checking if the team has any worker requirements left
	private boolean canProceed(Map<String, Integer> team) {
		for (int count : team.values()) {
			if (count != 0) {
				return false;
			}
		}
		for(int i = 0; i < workerCount; i++){
			
		//signal that the request can be processed
		isFulfilled.signal();
		}
		return true;
	}
}
