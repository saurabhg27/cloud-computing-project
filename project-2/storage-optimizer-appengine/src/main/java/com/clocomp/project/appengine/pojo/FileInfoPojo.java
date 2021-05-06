package com.clocomp.project.appengine.pojo;

import java.util.List;

public class FileInfoPojo {

	String userName;
	String fileName;
	List<String> shardLocations;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<String> getShardLocations() {
		return shardLocations;
	}

	public void setShardLocations(List<String> shardLocations) {
		this.shardLocations = shardLocations;
	}
}
