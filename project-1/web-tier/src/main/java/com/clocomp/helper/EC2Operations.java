package com.clocomp.helper;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.ShutdownBehavior;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagSpecification;

/*
 * Helper class that contains EC2 functions
 * */

public class EC2Operations {
	private static final Logger logger = LoggerFactory.getLogger(EC2Operations.class);
	
	private AmazonEC2 mEc2Client;
		
	public EC2Operations() {
		BasicAWSCredentials mCredentials = Settings.getAWSCREDENTIALS();
		mEc2Client = AmazonEC2ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(mCredentials))
				.withRegion( Regions.US_EAST_1).build();
	
		logger.info("Created EC2 client {} ",mEc2Client);
	}
	
	
	public String createInstance(String amiId, String keyName, String instanceName) {
		
		logger.info("Received create instance request. Using AMI: " + amiId + ", key:" + keyName);
		
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest().withImageId(amiId)
                .withInstanceType("t2.micro")
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(keyName)
                .withTagSpecifications(new TagSpecification().withTags(new Tag().withKey("Name")
                		.withValue(instanceName)).withResourceType("instance"))
                .withInstanceInitiatedShutdownBehavior(ShutdownBehavior.Terminate);
		
		try {
			
			RunInstancesResult runInstancesResult = mEc2Client.runInstances(runInstancesRequest);
			Instance instance = runInstancesResult.getReservation().getInstances().get(0);
			return instance.getInstanceId();
			
		}catch(Exception e) {
			logger.error("Exception while creating instance - " ,e);
			return "";
		}
	}
	
	/*
	 * Get the total count of instances that are currently running or being created (pending)
	 * */
	public int getNumberOfRunningOrPendingInstances() {
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
		
		Filter runningInstancesFilter = new Filter();
		runningInstancesFilter.setName("instance-state-name");
		runningInstancesFilter.setValues(Arrays.asList(new String[] {"running", "pending"}));
		
		describeInstancesRequest.setFilters( Arrays.asList(new Filter[] {runningInstancesFilter}));
		
		describeInstancesRequest.setMaxResults(1000);
		
		DescribeInstancesResult result = mEc2Client.describeInstances(describeInstancesRequest);
		
		int count = 0;
		for(Reservation r : result.getReservations()) {
			count += r.getInstances().size();
		}
		
		return count;
	}
}
