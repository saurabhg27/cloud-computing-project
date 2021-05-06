package com.clocomp.webtier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.clocomp.helper.Settings;

/*
 * Helper class for SQS-related functions
 * */

@Repository
public class SQSUtility {

	private static final String requestQueueUrl = Settings.SQS_REQUEST_URL;
	private static final String responseQueue = Settings.SQS_RESPONSE_URL;
	
	private static final Logger logger = LoggerFactory.getLogger(SQSUtility.class);

	private AmazonSQS sqs;
	
	public SQSUtility() {

		BasicAWSCredentials AWS_CREDENTIALS = Settings.getAWSCREDENTIALS();

		sqs = AmazonSQSClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(AWS_CREDENTIALS)).withRegion(Regions.US_EAST_1)
				.build();
		
		
		logger.info("SQSUtility created");
		
	}
	
	public void sendMsgToRequestQueue(String message){
		
		sqs.sendMessage(requestQueueUrl, message);
		logger.info("message {} sent ",message);
	}
	
	/*
	 * Read up to 10 messages from the response queue
	 * */	
	public List<Message> readMessages() {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
				.withQueueUrl(responseQueue)
				.withMaxNumberOfMessages(10)
				.withWaitTimeSeconds(15);	//use long polling


		ReceiveMessageResult result = sqs.receiveMessage(receiveMessageRequest);
		List<Message> msgList=result.getMessages();
		
		logger.info("Messages Read from queue {} ",msgList.size());
		return msgList;

	}
	
	/*
	 * Keeps reading from the response queue for results until the results for all the
	 * input images are obtained.
	 * */
	public Map<String,String> receiveImageProcessingResults(List<String> fileNameList) {
		
		Map<String,String> results = new TreeMap<>();
		int numOfFiles = fileNameList.size();
		
		while(!results.keySet().containsAll(fileNameList)) {
			List<Message> messages = readMessages();
			for(Message m : messages) {
				String body = m.getBody();
				//responses are of the form "image name,result"
				String[] values = body.split(",");
				
				//first part is the name of image, second part is the result from the 
				//Neural network
				results.put(values[0], values[1]);
				
				logger.debug("Received " + values[0]);
				--numOfFiles;
				
				
				try{
					//delete the message
					sqs.deleteMessage(responseQueue, m.getReceiptHandle());
				}catch (Exception e) {
					logger.info("exception while deleting message, maybe deleted already");
				}
			}
			logger.info("Response map size {} ",results.size());
		}
		logger.debug("Received all " + String.valueOf(numOfFiles) + " results...");
		return results;
		
	}
	
	/*
	 * Calculate approximate length of request queue
	 * */
	public int getNumberOfPendingMessages() {
		GetQueueAttributesRequest queueAttributesRequest = new GetQueueAttributesRequest();
		queueAttributesRequest.setAttributeNames(Arrays.asList(new String[] {"ApproximateNumberOfMessages"}));
		queueAttributesRequest.setQueueUrl(requestQueueUrl);
		try {
			GetQueueAttributesResult attributesResult = sqs.getQueueAttributes(queueAttributesRequest);
			 Map<String, String> attributes = attributesResult.getAttributes();
			if(attributes == null) {
				logger.error("Failed to get queue length.");
				return -1;
			}
			else {
				String value = attributes.get("ApproximateNumberOfMessages");
				if(value == null) {
					logger.error("Got null from queue attribute response");
					return -1;
				}
				
				int numberOfPendingMessages = Integer.parseInt(value);
				return numberOfPendingMessages;
			}
		}catch(Exception e) {
			logger.error("Exception while getting queue attributes - " , e);
			return -1;
		}
	}

}

