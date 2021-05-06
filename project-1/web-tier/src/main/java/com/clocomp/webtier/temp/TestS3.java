package com.clocomp.webtier.temp;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.clocomp.helper.Settings;

public class TestS3 {

	static String[] keyNames = new String[] { "test_0.JPEG", "test_1.JPEG", "test_2.JPEG", "test_3.JPEG", "test_4.JPEG",
			"test_5.JPEG", "test_6.JPEG", "test_7.JPEG", "test_8.JPEG", "test_9.JPEG", "test_10.JPEG", "test_11.JPEG",
			"test_12.JPEG", "test_13.JPEG", "test_14.JPEG", "test_15.JPEG", "test_16.JPEG", "test_17.JPEG",
			"test_18.JPEG", "test_19.JPEG", "test_20.JPEG", "test_21.JPEG", "test_22.JPEG", "test_23.JPEG",
			"test_24.JPEG", "test_25.JPEG", "test_26.JPEG", "test_27.JPEG", "test_28.JPEG", "test_29.JPEG",
			"test_30.JPEG", "test_31.JPEG", "test_32.JPEG", "test_33.JPEG", "test_34.JPEG", "test_35.JPEG",
			"test_36.JPEG", "test_37.JPEG", "test_38.JPEG", "test_39.JPEG", "test_40.JPEG", "test_41.JPEG",
			"test_42.JPEG", "test_43.JPEG", "test_44.JPEG", "test_45.JPEG", "test_46.JPEG", "test_47.JPEG",
			"test_48.JPEG", "test_49.JPEG", "test_50.JPEG", "test_51.JPEG", "test_52.JPEG", "test_53.JPEG",
			"test_54.JPEG", "test_55.JPEG", "test_56.JPEG", "test_57.JPEG", "test_58.JPEG", "test_59.JPEG",
			"test_60.JPEG", "test_61.JPEG", "test_62.JPEG", "test_63.JPEG", "test_64.JPEG", "test_65.JPEG",
			"test_66.JPEG", "test_67.JPEG", "test_68.JPEG", "test_69.JPEG", "test_70.JPEG", "test_71.JPEG",
			"test_72.JPEG", "test_73.JPEG", "test_74.JPEG", "test_75.JPEG", "test_76.JPEG", "test_77.JPEG",
			"test_78.JPEG", "test_79.JPEG", "test_80.JPEG", "test_81.JPEG", "test_82.JPEG", "test_83.JPEG",
			"test_84.JPEG", "test_85.JPEG", "test_86.JPEG", "test_87.JPEG", "test_88.JPEG", "test_89.JPEG",
			"test_90.JPEG", "test_91.JPEG", "test_92.JPEG", "test_93.JPEG", "test_94.JPEG", "test_95.JPEG",
			"test_96.JPEG", "test_97.JPEG", "test_98.JPEG", "test_99.JPEG" };

	public static void main(String[] args) throws IOException {
		Map<String,String> results = new TreeMap<>();
		System.out.println("size "+results.size());
		results.put("nla", "alsk");
		System.out.println("size "+results.size());
		
		System.exit(0);
		AmazonS3 s3 = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(Settings.getAWSCREDENTIALS()))
				.withRegion(Regions.US_EAST_1).build();

		for (String key : keyNames) {
			S3Object val = s3.getObject(Settings.S3_ResultsBucket, key);
			System.out.print(""+key+" ");
			val.getObjectContent().transferTo(System.out);
			System.out.println("");
			

		}

	}

}
