package com.clocomp.webtier;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.clocomp.helper.Settings;

/**
 * This class used to do S3 related things
 */
@Repository
public class S3Utility {

	private static final Logger logger= LoggerFactory.getLogger(S3Utility.class);
	
	//names of the S3 buckets
	private String s3ImagesBucket;
	private String s3ResultsBucket;

	private AmazonS3 s3;


	public S3Utility() {
		//create s3 connection
		this.s3ImagesBucket = Settings.S3_ImagesBucket;
		this.s3ResultsBucket = Settings.S3_ResultsBucket;
		s3 = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(Settings.getAWSCREDENTIALS()))
				.withRegion(Regions.US_EAST_1)
				.build();

		

	}
	
	/*
	 * Save the input images in S3.
	 * */
	public List<String> saveMultipleImagesToS3(MultipartFile[] files) throws IOException  {
		
		List<String> imageKeyList = new ArrayList<String>();
		
		for (MultipartFile uploadedFile : files) {
			String fileName = uploadedFile.getOriginalFilename();
			saveimagetoS3(fileName, uploadedFile);
			imageKeyList.add(fileName);
		}
		logger.info("Saved {} images into S3",files.length);
		return imageKeyList;
	}

	//to upload single image
	public void saveimagetoS3(String fileName,MultipartFile uploadedImageFile) throws IOException {
		String key_name = fileName;
		InputStream is = uploadedImageFile.getInputStream();
		ObjectMetadata metadata = null;
		logger.info("Trying to put into s3 bucket: {} keyname: {} metadata: {}",s3ImagesBucket,key_name,metadata);
		s3.putObject(s3ImagesBucket, key_name, is, metadata);
		//logger.info("Put image into s3 {} ",key_name);
	}

//	//get status form S3 bucket
//	public Map<String,String> getImageProcessingStatusFromS3(List<String> s3KeysList) {
//		return null;
//		
//	}
	
	//just to test if api works, can be removed later
	public static void main(String[] args) {
		S3Utility bla = new S3Utility();
		List<Bucket> buckets = bla.s3.listBuckets();
		for (Bucket bucket : buckets) {
			System.out.println("bucket--> "+bucket);
		}
		System.out.println("done");
	}
}
