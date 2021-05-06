package com.clocomp.apptier;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Start the app tier main thread
 * */

public class AppTierApplication {

	private static final Logger logger = LogManager.getLogger(AppTierApplication.class);
	
	public static void main(String[] args) {
		System.out.println("Starting AppTierApplication");
		logger.info("Starting AppTierApplication");
		
		AppTierMainThread mainLoop = new AppTierMainThread();
		mainLoop.run();
		
		logger.info("AppTierApplication finished");
		
	}

}
