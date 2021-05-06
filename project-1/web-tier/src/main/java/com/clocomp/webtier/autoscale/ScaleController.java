package com.clocomp.webtier.autoscale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clocomp.helper.EC2Operations;
import com.clocomp.helper.Settings;
import com.clocomp.webtier.SQSUtility;

/*
 * Class that implements the scale out logic.
 * */

public class ScaleController {
	
	private EC2Operations mEC2Operations;
	private SQSUtility mSQSUtility;
	private int mInstanceId = 1;//to generate the id for the app-tier instance
	private Logger logger = LoggerFactory.getLogger(ScaleController.class);
	
	private void initialize() {
		
		 mSQSUtility = new SQSUtility();
        
        mEC2Operations = new EC2Operations();
	}
	
	/*
	 * Creates the specified number of EC2 instances
	 * */
	private void createInstances(int numOfInstances) {
		for(int i=0;i<numOfInstances;++i) {
			String instanceId = 
					mEC2Operations.createInstance(Settings.APP_TIER_AMI_ID, 
							Settings.APP_TIER_KEY_PAIR_NAME, "app-instance" + String.valueOf(mInstanceId));

			//generate id between 1 and 19
			mInstanceId = (mInstanceId+1)%19 + 1;
			
			if(instanceId == null)
				logger.error("Failed to create instance");
			else
				logger.info("Created instance " + instanceId);
		}
	}
	
	/*
	 * Main scale out logic
	 * */
	public void mainThread() {
		
		initialize();
		
		//keep track of previous queue size so as to calculate changes to queue size
		int previousSizeOfQueue = 0;
		while(true) {
			try {
				//takes into account instances already running and those being created
				int numOfRunningInstances = mEC2Operations.getNumberOfRunningOrPendingInstances();
				
				logger.info(String.valueOf(numOfRunningInstances) + " instances are running...");
				
				//get the approximate length of the request queue
				int numOfPendingMessages = mSQSUtility.getNumberOfPendingMessages();
				logger.info("Got " + String.valueOf(numOfPendingMessages) + " pending messages "
						+ "in the queue...");
				
				//calculate change in queue length compared to previous length
				int delta = numOfPendingMessages - previousSizeOfQueue;
				logger.info("Change from previous size = "+String.valueOf(delta));
				previousSizeOfQueue = numOfPendingMessages;
				
				if(delta > 0) {
					//this means that messages are arriving at a faster rate than they are being processed.
					
					int numOfInstancesToCreate = Math.min(delta, 20 - numOfRunningInstances);
					
					logger.info("Creating " + String.valueOf(numOfInstancesToCreate) + " instances...");
					
					createInstances(numOfInstancesToCreate);
				}
				
				logger.info("Sleeping...");
				
				Thread.sleep(2000);
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}
	}

}
