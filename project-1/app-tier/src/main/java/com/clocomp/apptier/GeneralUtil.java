package com.clocomp.apptier;

import com.amazonaws.auth.BasicAWSCredentials;

public class GeneralUtil {
	public static BasicAWSCredentials getAWSCREDENTIALS() {
		BasicAWSCredentials AWS_CREDENTIALS = new BasicAWSCredentials(
				"",                
				""
				);
		return AWS_CREDENTIALS;
		
	}
	
	public static final String SQS_REQUEST_URL = "https://sqs.us-east-1.amazonaws.com/33640/cse546-project1-request-queue";
	public static final String SQS_RESPONSE_URL = "https://sqs.us-east-1.amazonaws.com/336403/cse546-project1-response-queue";
	
	public static final  String S3_ImagesBucket="cse546-project1-image-bucket";
	public static final  String S3_ResultsBucket="cse546-project1-results-bucket";

}
