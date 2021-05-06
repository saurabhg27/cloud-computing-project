package com.clocomp.webtier.temp;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.ShutdownBehavior;
import com.clocomp.helper.Settings;

public class TestEC2 {

	/**
	 * 
	 * This is a temporary utility class to test EC2 functions
	 */
	public static void main(String[] args) {
		System.out.println("Start");

		
		AmazonEC2 ec2Client = AmazonEC2ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(Settings.getAWSCREDENTIALS()))
				.withRegion(Regions.US_EAST_1)
				.build();
		System.out.println("ec2Client --> "+ec2Client);
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest().withImageId(Settings.APP_TIER_AMI_ID)
				.withInstanceType("t2.micro")
				.withMinCount(1)
				.withMaxCount(1)
				.withKeyName("")
				.withSecurityGroupIds("")
				.withInstanceInitiatedShutdownBehavior(ShutdownBehavior.Terminate);

		RunInstancesResult runInstancesResult = ec2Client.runInstances(runInstancesRequest);
		Instance instance = runInstancesResult.getReservation().getInstances().get(0);
		String instanceId = instance.getInstanceId();

		System.out.println(instanceId);

		System.out.println("Over");
	}

}
