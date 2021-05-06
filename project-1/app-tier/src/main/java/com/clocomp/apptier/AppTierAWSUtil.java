package com.clocomp.apptier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

/*
 * Class that provides helper funtions for S3 and SQS-related tasks
 * 
 * */

public class AppTierAWSUtil {

	private static final Logger logger = LogManager.getLogger();

	private AWSCredentials AWS_CREDENTIALS;

	private AmazonSQS sqs;
	private String requestQueueUrl;
	private String responseQueueUrl;

	private AmazonS3 s3;
	private String s3ImagesBucket;
	private String s3ResultsBucket;

	/*
	 * Initialize credentials and details of the queues and S3 buckets
	 * */
	public AppTierAWSUtil() {
		AWS_CREDENTIALS = GeneralUtil.getAWSCREDENTIALS();

		requestQueueUrl = GeneralUtil.SQS_REQUEST_URL;
		responseQueueUrl = GeneralUtil.SQS_RESPONSE_URL;
		sqs = AmazonSQSClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(AWS_CREDENTIALS))
				.withRegion(Regions.US_EAST_1)
				.build();

		s3ImagesBucket=GeneralUtil.S3_ImagesBucket;
		s3ResultsBucket=GeneralUtil.S3_ResultsBucket;
		s3 = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(AWS_CREDENTIALS))
				.withRegion(Regions.US_EAST_1)
				.build();
		logger.info("Constructed AppTierSQSUtil");
	}

	
	/*
	 * Download an image with name 'imageName' from S3. The image is saved locally
	 * in a file of the same name. 
	 * */
	public Path downloadImageFromS3(String imageName) throws IOException {
		S3Object s3Obj = s3.getObject(s3ImagesBucket, imageName);
		S3ObjectInputStream s3InputStream = s3Obj.getObjectContent();
		Path outputPath = Paths.get(imageName);
		long imageSize = Files.copy(s3InputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
		logger.info("downloaded image from s3 {} with size {}",imageName,imageSize);
		return outputPath;
	}

	public void sendMessage(String s) {
		sqs.sendMessage(responseQueueUrl, s);
		logger.info("message {} sent ", s);
	}
	
	/*
	 * Reads a message from the request queue using long poll. If the queue is empty
	 * proceeds to terminate in order to scale down.
	 * */
	public Message readMessage() {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
				.withQueueUrl(requestQueueUrl)
				.withMaxNumberOfMessages(1)
				.withWaitTimeSeconds(15);	//use long poll while receiving messages to 
											//prevent empty responses from SQS.


		ReceiveMessageResult result = sqs.receiveMessage(receiveMessageRequest);
		List<Message> msgList=result.getMessages();
		if(msgList == null || msgList.isEmpty()) {
			logger.info("No messages in SQS, terminating this app-tier instance");
			sqs.shutdown();
			s3.shutdown();
			terminateThisEC2instance();
			return null;
		}

		logger.info("Messages Read from queue {} ",msgList.size());
		Message message = msgList.get(0);
		logger.info("readMessage {} ",message.getBody());
		return message;

	}

	public void deleteMessage(Message msg) {
		String receiptHandle = msg.getReceiptHandle();
		String msgBody = msg.getBody();
		DeleteMessageRequest req = new DeleteMessageRequest().withQueueUrl(requestQueueUrl)
				.withReceiptHandle(receiptHandle);
		DeleteMessageResult res = sqs.deleteMessage(req);
		logger.info("deleted message: {}",msgBody);
	}
	

	public void saveMessageResultinS3(String imageName, String result) {
		s3.putObject(s3ResultsBucket, imageName, result);
		logger.info("Saved result in s3 image: {} result: {}",imageName,result);
	}
	
	public void terminateThisEC2instance() {
		String cmd="sudo shutdown -h now";

		Runtime run = Runtime.getRuntime();
		try {
			Process pr = run.exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
