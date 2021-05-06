package com.clocomp.project.appengine.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.dropbox.core.DbxDownloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.clocomp.project.appengine.controller.OurDatabase;
import com.clocomp.project.appengine.dropbox.DropboxController;
import com.clocomp.project.appengine.googledrive.GoogleAuthService;
import com.clocomp.project.appengine.pojo.FileInfoPojo;
import com.dropbox.core.DbxException;
import com.dropbox.core.RateLimitException;
import com.dropbox.core.RetryException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;

@Service
public class FileStoreService {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	GoogleAuthService googleService;

	@Autowired
	FileShardService shardService;

	@Autowired
	OurDatabase db;

	
	public void shardAndUploadFile(String fileName, MultipartFile file, String userName) throws IOException {


		Drive d = googleService.getDriveForUserName(userName);
		DbxClientV2 dropBoxClient = DropboxController.getDropBoxClientForUserEmail(userName);

		//		BlobId id= shardService.uploadFileToGCS(file);
		//		InputStream is = Channels.newInputStream(shardService.storage.reader(id));
		//		uploadToDrive(d, "lkdajsldkajsl", is);
		List<BlobId> shards = shardService.shardFile(file,2,""+System.currentTimeMillis());
		FileInfoPojo fileShardingInfo = new FileInfoPojo();
		fileShardingInfo.setFileName(fileName);
		fileShardingInfo.setUserName(userName);
		List<String> shardLocations = new ArrayList<>();
		
		//Upload 1st shard to google drive, 2nd one to dropbox.
		String shardName = shards.get(0).getName();
		logger.info("Shard 0 name " + shards.get(0).getName());
		String id = uploadToDrive(d, shardName, Channels.newInputStream(shardService.storage.reader(shards.get(0))));	
		shardLocations.add(id);
		
		logger.info(shards.get(1).toString());
		shardName = shards.get(1).getName();
		while(true) {
			try {
				id = uploadToDropBox(dropBoxClient, shardName, Channels.newInputStream(shardService.storage.reader(shards.get(1))));
				break;
			}
			catch (RateLimitException e) {
				logger.severe("Got rate limited error. Waiting for " + (e.getBackoffMillis()) + 
						"milliseconds");
				try {
					Thread.sleep(e.getBackoffMillis());
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
			}
		}
		logger.info("uploaded to dropbox " + id);
		shardLocations.add(id);
		
		fileShardingInfo.setShardLocations(shardLocations);

		//save it to DB
		db.putFileInfoPojo(fileShardingInfo);

		//delete shards
		shardService.cleanUpShards(shards);

	}

	private String uploadToDropBox(DbxClientV2 dropBoxClient, String fileName, InputStream is) throws RateLimitException {
				
		while(true) {
			try {
				logger.info("dropbox upl " + fileName);
								
				FileMetadata metadata = dropBoxClient.files().uploadBuilder("/" + fileName)
				        .uploadAndFinish(is);
				return metadata.getId();
			}
			catch (RateLimitException e) {
				throw e;
			}
			catch (DbxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "";
	}

	private String uploadToDrive(Drive dri,String fileName,InputStream is) throws IOException {
		File fileInfo= new File();
		fileInfo.setName("shards/"+fileName);
		String type = null;

		InputStreamContent content = new InputStreamContent(type, is);

		File ret = dri.files().create(fileInfo,content).execute();

		logger.info("File uploaded: "+ret.getName());
		return ret.getId();
	}

	public InputStream downloadFile(String fileName, String userName) throws IOException, DbxException {
		String key = "" + userName + "-" + fileName;
		FileInfoPojo fileInfo = db.getFileInfoPojo(key);
		DbxClientV2 dropBoxClient = DropboxController.getDropBoxClientForUserEmail(userName);
		List<String> shardLocations = fileInfo.getShardLocations();

		Drive d = googleService.getDriveForUserName(userName); 
		List<BlobId> shards = new ArrayList<>();
//		for (String shLoc : shardLocations) {
//			InputStream is = d.files().get(shLoc).executeMediaAsInputStream();
//			BlobId shardId = BlobId.of(shardService.bucketName,shLoc );
//			shardService.storage.createFrom(BlobInfo.newBuilder(shardId).build(), is);
//
//			shards.add(shardId);
//		}
		InputStream is = d.files().get(shardLocations.get(0)).executeMediaAsInputStream();
		BlobId shardId = BlobId.of(shardService.bucketName,shardLocations.get(0) );
		shardService.storage.createFrom(BlobInfo.newBuilder(shardId).build(), is);
		shards.add(shardId);


		InputStream dboxIs =dropBoxClient.files().download(shardLocations.get(1)).getInputStream();
		BlobId dboxShardId = BlobId.of(shardService.bucketName,shardLocations.get(1) );
		shardService.storage.createFrom(BlobInfo.newBuilder(dboxShardId).build(), dboxIs);
		shards.add(dboxShardId);


		BlobId reconsBlobId = shardService.reconstructShards(shards,fileName+"-recons");
		logger.info("failing herer");
		shardService.cleanUpShards(shards);
		logger.info("recons blob id: "+reconsBlobId);
		return Channels.newInputStream(shardService.storage.reader(reconsBlobId));
		
	}

}
