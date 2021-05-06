package com.clocomp.project.appengine.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Repository;

import com.clocomp.project.appengine.pojo.FileInfoPojo;
import com.clocomp.project.appengine.pojo.UserInfoPojo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Repository
public class OurDatabase {
	Logger logger = Logger.getLogger(this.getClass().getName());

	Storage storage;
	ObjectMapper mapper = new ObjectMapper();
	String userBucketName = "cc-proj-user-data-bukt";
	String filesBucketName = "cc-proj-file-data-bukt";

	public static void main(String[] args) {

		try {
			OurDatabase thiss = new OurDatabase();
			//thiss.putFileInfoPojo(thiss.dummyFileInfoPojo());
			thiss.getListOfFilesOfUser("defaultUserName");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public OurDatabase() {
		this.storage = StorageOptions.getDefaultInstance().getService();

	}

	public List<FileInfoPojo> getListOfFilesOfUser(String userName) throws IOException {
		List<FileInfoPojo> list = new ArrayList<>();
		Page<Blob> blobs = storage.list(filesBucketName, Storage.BlobListOption.prefix(userName));
		for (Blob blob : blobs.iterateAll()) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			blob.downloadTo(bos);
			FileInfoPojo pojo = mapper.readValue(bos.toByteArray(), FileInfoPojo.class);
			list.add(pojo);
			System.out.println(blob.getBlobId()+" "+pojo.getFileName());
		}
		
		return list;
	}

	private UserInfoPojo getUserInfoPojo(String userName) throws IOException {
		BlobId blobId = BlobId.of(userBucketName, userName);
		byte[] blob = storage.readAllBytes(blobId);
		UserInfoPojo pojo = mapper.readValue(blob, UserInfoPojo.class);
		logger.info("Read from DB username: " + pojo.getUserName());
		return pojo;
	}

	private BlobId putUserInfoPojo(UserInfoPojo userInfoPojo) throws IOException {
		String userName = userInfoPojo.getUserName();
		BlobId blobId = BlobId.of(userBucketName, userName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
		InputStream is = new ByteArrayInputStream(mapper.writeValueAsBytes(userInfoPojo));
		Blob blob = storage.createFrom(blobInfo, is);
		is.close();
		logger.info("Inserted into DB " + userName);
		return blobId;
	}

	public FileInfoPojo getFileInfoPojo(String key) throws IOException {
		BlobId blobId = BlobId.of(filesBucketName, key);
		byte[] blob = storage.readAllBytes(blobId);
		FileInfoPojo pojo = mapper.readValue(blob, FileInfoPojo.class);
		logger.info("getFileInfoPojo from DB username: " + pojo.getUserName() + ",filename:" + pojo.getFileName());
		return pojo;
	}

	public BlobId putFileInfoPojo(FileInfoPojo bla) throws IOException {
		String blobName = "" + bla.getUserName() + "-" + bla.getFileName();

		BlobId blobId = BlobId.of(filesBucketName, blobName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
		InputStream is = new ByteArrayInputStream(mapper.writeValueAsBytes(bla));
		Blob blob = storage.createFrom(blobInfo, is);
		is.close();
		logger.info("Inserted into DB putFileInfoPojo " + blobName);
		return blobId;

	}

	private UserInfoPojo dummyUserInfoPojo() throws JsonProcessingException {
		UserInfoPojo pojo = new UserInfoPojo();
		pojo.setUserName("defaultUserName");
		return pojo;
	}

	private FileInfoPojo dummyFileInfoPojo() throws JsonProcessingException {
		FileInfoPojo a = new FileInfoPojo();
		a.setUserName("defaultUserName");
		a.setFileName("FileA");
		a.setShardLocations(Arrays.asList("locaA", "locB"));
		return a;

	}

}
