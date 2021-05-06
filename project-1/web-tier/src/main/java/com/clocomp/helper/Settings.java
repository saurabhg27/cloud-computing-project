package com.clocomp.helper;

import com.amazonaws.auth.BasicAWSCredentials;

public class Settings {
	public static final String SQS_REQUEST_URL = "https://sqs.us-east-1.amazonaws.com/33640/cse546-project1-request-queue";
	public static final String SQS_RESPONSE_URL = "https://sqs.us-east-1.amazonaws.com/33640/cse546-project1-response-queue";
	
	public static final  String S3_ImagesBucket="cse546-project1-image-bucket";
	public static final  String S3_ResultsBucket="cse546-project1-results-bucket";

	
	
	public static final String APP_TIER_AMI_ID = "ami-053f91be";
	public static final String APP_TIER_KEY_PAIR_NAME = "";
	
	
	private static final String ACCESS_KEY = "";
	private static final String SECRET_KEY = "";
	public static BasicAWSCredentials getAWSCREDENTIALS() {
		BasicAWSCredentials AWS_CREDENTIALS = new BasicAWSCredentials(
				ACCESS_KEY,                
				SECRET_KEY
				);
		return AWS_CREDENTIALS;
		
	}
}
