package com.clocomp.apptier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.sqs.model.Message;

/*
 * Class that contains the main driver code for the App-tier. This does
 * tasks like reading a message from the request queue, saving the results
 * in S3 and returning the result via the response queue. 
 * */

public class AppTierMainThread {

	private static final Logger logger = LogManager.getLogger();

	private boolean stopThread = false;

	private AppTierAWSUtil appTierAWSUtil;

	public AppTierMainThread() {
		this.appTierAWSUtil = new AppTierAWSUtil();
	}
	
	/*
	 * main driver function for app-tier.
	 * */
	public void run() {
		while (stopThread != true) {
			try {
				//read a message from request SQS queue
				Message message = appTierAWSUtil.readMessage();
				String imageName = message.getBody();
				
				//Download the image specified in the request from S3 
				Path pathToImage = appTierAWSUtil.downloadImageFromS3(message.getBody());
				
				//Run image recognition
				String result=runImageRecognitionAndGetResult(pathToImage.toAbsolutePath().toString());
				
				//save recognition result to output S3 bucket
				appTierAWSUtil.saveMessageResultinS3(imageName,result);
				
				//send result on response queue
				appTierAWSUtil.sendMessage(imageName + "," + result);
				
				//delete processed request from request SQS queue.
				appTierAWSUtil.deleteMessage(message);
			} catch (Exception e) {
				//all exception caught here, if any exception terminate this EC2 instance
				logger.error("Exception : ", e);
				stopThread();
				appTierAWSUtil.terminateThisEC2instance();
			}
		}
	}

	public void stopThread() {
		this.stopThread = true;
	}

	/*
	 * Runs the Py script for image recognition and gets the result.
	 * */
	private String runImageRecognitionAndGetResult(String pathToImage) {
		String result = "";
		
		//python3 /home/ubuntu/classifier/image_classification.py path_to_the_image
		String cmd="python3 image_classification.py "+pathToImage;
		logger.info("command is : {}",cmd);
		try {
			Runtime run = Runtime.getRuntime();
			Process pr = run.exec(cmd);
			boolean status = pr.waitFor(90, TimeUnit.SECONDS);
			if (status == true) {
				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				result = buf.readLine();
				buf.close();

			} else {
				result = "Timeout for image recognition passed no result";
			}
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
		logger.info("image recognition result for image: {} is: {}",pathToImage,result);
		return result;
	}
	
}
