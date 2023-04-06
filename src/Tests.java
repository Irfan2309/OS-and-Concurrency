
// You should develop your tests here to assure yourself that your DrillLoginManage class 
// meets the UR published in the coursework specification.

//Note that you may use *any* classes in this Tests class that are available in Java 12 or SE 1.8. This includes these versions thread safe classes


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Tests {
	int  t1 = 50; //Sleep timeout 
	
	
	

	public void exampleTest_A(){
		/*
		 *  UR1 Example
		 *  
		 * 	First:
		 *      drillLoginManager.smallTeamRequest(team) is called with team = {Roustabout=2} 
		 * 	Then:
		 *      5 worker threads call workerLogin("Roustabout")
		 * 
		 *  The result should be: 
		 *     2 Roustabout worker threads proceed (i.e., return from workerLogin),
		 *     3 Roustabout workers should be blocked from proceeding.
		 * 
		 *  Note that when this test is run an overall Timeout (t2 = 2 x t1 + 100ms) will be imposed.
		 *  At t2 the process running this test will be terminated.
		 *  
		 */
		
		System.out.println("\nExampleTest_A");
		AtomicInteger roustaboutReleases = new AtomicInteger(0); //Declare thread-safe counters to accumulate the number of worker threads that return
		DrillLoginManager drillLoginManager =  new DrillLoginManager();	
		
		//Define Roustabout worker threads:
		class ExampleTestWorkerThread extends Thread {
			public void run(){
				drillLoginManager.workerLogin("Roustabout"); 
				roustaboutReleases.incrementAndGet();
			};	
		};			
		
		//Instantiate and start 5 Roustabouts:
		int nRoustabouts = 5;
		for (int i=0; i < nRoustabouts; i++) (new ExampleTestWorkerThread()).start();
		System.out.println(nRoustabouts + " Roustabout threads started");
		
		//Give time for worker threads to execute and call workerLogin()
		try {Thread.sleep(t1);} catch (InterruptedException e) { e.printStackTrace();} 

		//Define team request:
		Map<String, Integer> teamRequest = new HashMap <String, Integer>();
		teamRequest.put("Roustabout", 2);
		System.out.println("teamRequest = " + teamRequest.toString());	
		
		//Make team request to the manager:
		drillLoginManager.smallTeamRequest(teamRequest); //Note 'anonymous' Request (no teamName) and no Driller required
		//Now give time for 2 worker threads to wake up:
		try {Thread.sleep(t1);} catch (InterruptedException e) { e.printStackTrace();}  

		//Check results:
		System.out.println("Number of Roustabouts released by manager = " + roustaboutReleases.get());
		if (roustaboutReleases.get() == 2) System.out.println("Hence: SUCCESS");
		else System.out.println("Hence: FAIL");
	}
	
	public void exampleTest_B(){
		/*
		 * This example is relevant to UR6 as it tests the 'teamReturned' string returned
		 * by your .workerLogin() method. 
		 * 
		 * It uses the .drillerRequest() method, so 1 driller is required in each team. 
		 *    3 Roustabout threads are started that each call .workerLogin("Roustabout")
		 *    1 driller thread is started that calls .drillerRequest("TeamX", team = {Driller=1, Roustabout=2})
		 *    
		 *    The result should be that:
		 *       The driller thread is allowed to proceed
		 *       2 Roustabouts are allowed to proceed (and their team names are checked = "TeamX")
		 *       1 Roustabout remains blocked.
		 *       
		 *  Note that when this test is run an overall Timeout (t2 = 2 x t1 + 100ms) will be imposed.
		 *  At t2 the process running this test will be terminated.
		 *  		 
		 */
		
		System.out.println("\nExampleTest_B");
		DrillLoginManager drillLoginManager =  new DrillLoginManager();
		
		//Declare thread-safe counters to accumulate the number of worker 
		// and Driller threads that are allowed to proceed:
		AtomicInteger drillerReleases = new AtomicInteger(0);
		AtomicInteger roustaboutReleases = new AtomicInteger(0);	
		
		//Define TeamX = request for 1 Driller & 2 Roustabouts 
		String teamXname = "TeamX";		
		Map<String, Integer> teamXrequest = new HashMap <String, Integer>();
		teamXrequest.put("Driller", 1);
		teamXrequest.put("Roustabout", 2);	
		
		//Define driller thread:
		class ExampleDrillerThread extends Thread {
			Map<String, Integer> teamRequest;
			String teamName;
			//Constructor to set teamName and TeamRequest of thread
			ExampleDrillerThread(String teamName, Map<String, Integer> teamRequest) {
				this.teamName = teamName;
				this.teamRequest = new HashMap<String, Integer> (teamRequest); //Make shallow copy for safety
			}
			//Execution Block:
		    public void run(){
		        drillLoginManager.drillerRequest(this.teamName, this.teamRequest);
		        drillerReleases.incrementAndGet();
		    };	
		};	
		
		//Define worker thread
		class ExampleTestWorkerThread extends Thread {
			public void run(){
				String threadName = Thread.currentThread().getName();
				String teamNameReturned = drillLoginManager.workerLogin("Roustabout"); 
				if (teamNameReturned == teamXname) {
					System.out.println("teamName returned by worker thread " + threadName + "(Roustabout) = " + teamNameReturned + ", is correct.");
					roustaboutReleases.incrementAndGet();
				}
				else {
					System.out.println("Error: teamName returned by worker thread " + threadName + "(Roustabout) = " + teamNameReturned + ", is incorrect (it should be " + teamXname + "). Terminating test.");
					System.out.println("Hence: FAIL");
					System.exit(1);
				};	
			}	
		};		
		
		//Start 3 roustabouts:
		for (int i=0; i < 3; i++) (new ExampleTestWorkerThread()).start();
		System.out.println("3 Roustabout threads started");
		//Wait for them to run and block:
		try {Thread.sleep(10); } catch (InterruptedException e) {e.printStackTrace();}
		
		//Start teamX Driller:
		ExampleDrillerThread exampleDrillerThread = new ExampleDrillerThread(teamXname, teamXrequest);
		exampleDrillerThread.start();
		System.out.println("1 Driller thread started by calling: 'drillLoginManager.drillerRequest', teamRequest = " + teamXrequest + ", teamName = " + teamXname);
		//Wait for Driller to run and release Roustabouts:		
		try {Thread.sleep(10); } catch (InterruptedException e) {e.printStackTrace();}	
		
		//Check results:
		System.out.println("Number of Roustabouts released to run by drillLoginManager for team " + teamXname + " = " + roustaboutReleases.get());
		System.out.println("Number of Driller threads released to run by drillLoginManager = " + drillerReleases.get());
		if (drillerReleases.get() == 1 && roustaboutReleases.get() == 2) System.out.println("Hence: SUCCESS");
		else System.out.println("Hence: FAIL");
	}

	public void test_UR2() {
		System.out.println("\nTest_UR2");
		AtomicInteger roustaboutReleases = new AtomicInteger(0);
		AtomicInteger floorhandReleases = new AtomicInteger(0); 
		DrillLoginManager drillLoginManager =  new DrillLoginManager();	
		
		class ExampleTestWorkerThread extends Thread {
			public void run(){
				drillLoginManager.workerLogin("Roustabout"); 
				roustaboutReleases.incrementAndGet();
			};	
		};	
		class ExampleTestWorkerThread1 extends Thread {
			public void run(){
				drillLoginManager.workerLogin("Floorhand"); 
				floorhandReleases.incrementAndGet();
			};	
		};
		
		Map<String, Integer> team1 = new HashMap <String, Integer>();
		team1.put("Roustabout", 3);
		System.out.println("team1 = " + team1.toString());	
		
		Map<String, Integer> team2 = new HashMap <String, Integer>();
		team2.put("Roustabout", 2);
		team2.put("Floorhand", 3);
		System.out.println("team2 = " + team2.toString());
		
		drillLoginManager.smallTeamRequest(team1);
		drillLoginManager.smallTeamRequest(team2);
		
		try {Thread.sleep(t1);} catch (InterruptedException e) { e.printStackTrace();} 

		
		int nRoustabouts = 5;
		int nFloorhands = 4;
		for (int i=0; i < nRoustabouts; i++) (new ExampleTestWorkerThread()).start();
		System.out.println(nRoustabouts + " Roustabout threads started");
		
		for (int i=0; i < nFloorhands; i++) (new ExampleTestWorkerThread1()).start();
		System.out.println(nFloorhands + " Floorhand threads started");
		
		try {Thread.sleep(t1);} catch (InterruptedException e) { e.printStackTrace();} 
		
		System.out.println("Number of Roustabouts released by manager = " + roustaboutReleases.get());
		System.out.println("Number of Floorhands released by manager = " + floorhandReleases.get());
		
		if (roustaboutReleases.get() == 5 && floorhandReleases.get() == 3) {
			System.out.println("Hence: SUCCESS");
		}	
		else {
			System.out.println("Hence: FAIL");
		}
	}
	
	public void test_UR3(){
		System.out.println("\nTest_UR3");
		AtomicInteger roustaboutReleases = new AtomicInteger(0);
		AtomicInteger floorhandReleases = new AtomicInteger(0); 
		DrillLoginManager drillLoginManager =  new DrillLoginManager();	
		
		class ExampleTestWorkerThread extends Thread {
			public void run(){
				drillLoginManager.workerLogin("Roustabout"); 
				roustaboutReleases.incrementAndGet();
			};	
		};	
		
		Map<String, Integer> team1 = new HashMap <String, Integer>();
		team1.put("Roustabout", 3);
		System.out.println("team1 = " + team1.toString());	
		
		Map<String, Integer> team2 = new HashMap <String, Integer>();
		team2.put("Roustabout", 2);
		team2.put("Floorhand", 3);
		System.out.println("team2 = " + team2.toString());
		
		drillLoginManager.smallTeamRequest(team1);
		drillLoginManager.smallTeamRequest(team2);
		
		try {Thread.sleep(t1);} catch (InterruptedException e) { e.printStackTrace();} 
		
		class ExampleTestWorkerThread1 extends Thread {
			public void run(){
				drillLoginManager.workerLogin("Floorhand"); 
				floorhandReleases.incrementAndGet();
			};	
		};
		
		int nRoustabouts = 5;
		int nFloorhands = 4;
		
		for (int i=0; i < nFloorhands; i++) (new ExampleTestWorkerThread1()).start();
		System.out.println(nFloorhands + " Floorhand threads started");
		
		for (int i=0; i < nRoustabouts; i++) (new ExampleTestWorkerThread()).start();
		System.out.println(nRoustabouts + " Roustabout threads started");
		
		try {Thread.sleep(t1);} catch (InterruptedException e) { e.printStackTrace();} 
		
		
		
		
		System.out.println("Number of Roustabouts released by manager = " + roustaboutReleases.get());
		System.out.println("Number of Floorhands released by manager = " + floorhandReleases.get());
		
		if (roustaboutReleases.get() == 5 && floorhandReleases.get() == 3) {
			System.out.println("Hence: SUCCESS");
		}	
		else {
			System.out.println("Hence: FAIL");
		}
		
	}
	
	public void test_UR4() {
		System.out.println("\nTest_UR4");
		AtomicInteger roustaboutReleases = new AtomicInteger(0);
		AtomicInteger floorhandReleases = new AtomicInteger(0); 
		DrillLoginManager drillLoginManager =  new DrillLoginManager();	
		
		class ExampleTestWorkerThread extends Thread {
			public void run(){
				drillLoginManager.workerLogin("Roustabout"); 
				roustaboutReleases.incrementAndGet();
			};	
		};	
		class ExampleTestWorkerThread1 extends Thread {
			public void run(){
				drillLoginManager.workerLogin("Floorhand"); 
				floorhandReleases.incrementAndGet();
			};	
		};
		
		int nRoustabouts = 5;
		int nFloorhands = 4;
		
		Map<String, Integer> team1 = new HashMap <String, Integer>();
		team1.put("Roustabout", 3);
		System.out.println("team1 = " + team1.toString());	
		
		drillLoginManager.smallTeamRequest(team1);
		
		try {Thread.sleep(t1);} catch (InterruptedException e) { e.printStackTrace();} 
		
		Map<String, Integer> team2 = new HashMap <String, Integer>();
		team2.put("Roustabout", 2);
		team2.put("Floorhand", 3);
		System.out.println("team2 = " + team2.toString());
		

		for (int i=0; i < nRoustabouts; i++) (new ExampleTestWorkerThread()).start();
		System.out.println(nRoustabouts + " Roustabout threads started");
				
		for (int i=0; i < nFloorhands; i++) (new ExampleTestWorkerThread1()).start();
		System.out.println(nFloorhands + " Floorhand threads started");
		
		try {Thread.sleep(t1);} catch (InterruptedException e) { e.printStackTrace();} 
		
		drillLoginManager.smallTeamRequest(team2);
		
		try {Thread.sleep(t1);} catch (InterruptedException e) { e.printStackTrace();} 
		
		System.out.println("Number of Roustabouts released by manager = " + roustaboutReleases.get());
		System.out.println("Number of Floorhands released by manager = " + floorhandReleases.get());
		
		if (roustaboutReleases.get() == 5 && floorhandReleases.get() == 3) {
			System.out.println("Hence: SUCCESS");
		}	
		else {
			System.out.println("Hence: FAIL");
		}
	}
	
	public void test_UR5() {
	    System.out.println("\nTest_UR5");
	    DrillLoginManager drillLoginManager = new DrillLoginManager();
	    AtomicInteger drillerReleases = new AtomicInteger(0);
	    AtomicInteger floorhandReleases = new AtomicInteger(0);

	    String teamXname = "TeamX";	
	    Map<String, Integer> teamX = new HashMap<String, Integer>();
	    teamX.put("Driller", 1);
	    teamX.put("Floorhand", 4);
	    
	    String teamYname = "TeamY";	
	    Map<String, Integer> teamY = new HashMap<String, Integer>();
	    teamY.put("Driller", 1);
	    teamY.put("Roustabout", 6);
	    teamY.put("Floorhand", 3);
	    
	  	class ExampleDrillerThread extends Thread {
	  		Map<String, Integer> teamRequest;
	  		String teamName;
	  		ExampleDrillerThread(String teamName, Map<String, Integer> teamRequest) {
	  			this.teamName = teamName;
	  			this.teamRequest = teamRequest; 
	  		}
	  		public void run(){
	  		    drillLoginManager.drillerRequest(this.teamName, this.teamRequest);
	  		    drillerReleases.incrementAndGet();
	  		};	
	  };	
	  			
		ExampleDrillerThread driller1 = new ExampleDrillerThread("TeamX",teamX);
		ExampleDrillerThread driller2 = new ExampleDrillerThread("TeamY",teamY);
	    
	    driller1.start();
	    System.out.println("Driller thread started , teamRequest = " + teamX + ", teamName = " + teamXname);

	    driller2.start();
	    System.out.println("Driller thread started , teamRequest = " + teamY + ", teamName = " + teamYname);
	    
	    try { Thread.sleep(t1); } catch (InterruptedException e) {e.printStackTrace();}
		
		class ExampleTestWorkerThread1 extends Thread {
			public void run(){
				drillLoginManager.workerLogin("Floorhand"); 
				floorhandReleases.incrementAndGet();
			};	
		};
		
		int nFloorhands = 5;
		for (int i=0; i < nFloorhands; i++) (new ExampleTestWorkerThread1()).start();
		System.out.println(nFloorhands + " Floorhand threads started");
		
		try {Thread.sleep(t1);} catch (InterruptedException e) { e.printStackTrace();} 
	

	    System.out.println("Number of drillers released by manager = " + drillerReleases.get());
		System.out.println("Number of Floorhands released by manager = " + floorhandReleases.get());

	    if (floorhandReleases.get() == 4 && drillerReleases.get() == 1) {
	        System.out.println("Hence: SUCCESS");
	    } else {
	        System.out.println("Hence: FAIL");
	    }
	}
	
	public void testUR6() {
		System.out.println("\nTest_UR6");
		DrillLoginManager drillLoginManager = new DrillLoginManager();
	    AtomicInteger drillerReleases = new AtomicInteger(0);
	    AtomicInteger floorhandReleases = new AtomicInteger(0);
	    
	    String teamXname = "TeamX";	
	    Map<String, Integer> teamX = new HashMap<String, Integer>();
		teamX.put("Driller", 1);
		teamX.put("Floorhand", 4);
	
		
		String teamYname = "TeamY";	
	    Map<String, Integer> teamY = new HashMap<String, Integer>();
	    teamY.put("Driller", 1);
	    teamY.put("Roustabout", 6);
	    teamY.put("Floorhand", 3);

	    class ExampleDrillerThread extends Thread {
	  		Map<String, Integer> teamRequest;
	  		String teamName;
	  		ExampleDrillerThread(String teamName, Map<String, Integer> teamRequest) {
	  			this.teamName = teamName;
	  			this.teamRequest = teamRequest; 
	  		}
	  		public void run(){
	  		    drillLoginManager.drillerRequest(this.teamName, this.teamRequest);
	  		    drillerReleases.incrementAndGet();
	  		};	
	    };
	    
	    ExampleDrillerThread driller1 = new ExampleDrillerThread(teamXname, teamX);
		ExampleDrillerThread driller2 = new ExampleDrillerThread(teamYname, teamY);
		
			
		driller1.start();
		System.out.println("Driller thread started , teamRequest = " + teamX + ", teamName = " + teamXname);
		
		driller2.start();
		System.out.println("Driller thread started , teamRequest = " + teamY + ", teamName = " + teamYname);
		
		try { Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
	  
		class ExampleTestWorkerThread1 extends Thread {
			public void run(){
				drillLoginManager.workerLogin("Floorhand"); 
				floorhandReleases.incrementAndGet();
			};	
		}
			
		int nFloorhands = 5;
		for (int i=0; i < nFloorhands; i++) (new ExampleTestWorkerThread1()).start();
		System.out.println(nFloorhands + " Floorhand threads started");
			
		try {Thread.sleep(t1);} catch (InterruptedException e) { e.printStackTrace();} 
		
		
		System.out.println("Number of drillers released by manager = " + drillerReleases.get());
		System.out.println("Number of Floorhands released by manager = " + floorhandReleases.get());

		if (drillerReleases.get() == 1 && floorhandReleases.get() == 4) {
			System.out.println("Hence: SUCCESS");
		} else {
			System.out.println("Hence: FAIL");
		}
	}
	
}


