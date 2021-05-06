package com.clocomp.project.appengine.file;


import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;


@Service
public class FileShardService {
	Logger logger = Logger.getLogger(this.getClass().getName());
	Storage storage;
	String bucketName = "stately-pulsar-308005.appspot.com"; // Change this to something unique

	public FileShardService() {
		logger.info("Constructed FileUploadController !!");
		this.storage = StorageOptions.getDefaultInstance().getService();
	}

	public String handleFileUpload(@RequestParam("files") MultipartFile[] files) {
		String str = "";
		for (MultipartFile f : files) {
			str=str+f.getOriginalFilename()+","+f.getSize()+";";
			logger.info("filename: "+f.getOriginalFilename());


			try {

				BlobId blobId = uploadFileToGCS(f);
				logger.info("will try to split now");
				
//				List<Blob> list=shardBlob(blobId,3);
				
//				BlobId id = reconstructShards(list);
				
			} catch (IOException e) {
				str+=e.getMessage();
				logger.severe("Excep: "+e.getMessage());
			}
			
		}
		return str;

	}

	public BlobId reconstructShards(List<BlobId> shardBlobIdList,String newName) throws IOException {
		BlobId reconsBlobID = BlobId.of(bucketName, newName);
		Blob file = storage.create(BlobInfo.newBuilder(reconsBlobID).build());
		try(WriteChannel writer = file.writer()){
			for (BlobId blobId : shardBlobIdList) {
				Blob shard = storage.get(blobId);
				try(ReadChannel reader = shard.reader()){
					ByteBuffer bytes = ByteBuffer.allocate(1024);
					while(reader.read(bytes)>0) {
						bytes.flip();

						writer.write(bytes);

						bytes.clear();
						
					}
				}
			}
		}
		
		logger.info("reconstructShards completed : "+file.getName());
		
		return reconsBlobID;
	}

	public List<BlobId> shardFile(MultipartFile file,int shardsCount, String suffix) throws IOException{
		BlobId blobId = uploadFileToGCS(file);
		
		List<BlobId> list=shardBlob(blobId,shardsCount,suffix);
		
		//delete original file
		storage.delete(blobId);
		return list;
	}
	
	private List<BlobId> shardBlob(BlobId blobId,int shardNumbers,String suffix) throws IOException {
		Blob file = storage.get(blobId);
		String shardName = blobId.getName() + suffix;
		long fileSize = file.getSize();//453013

		int chunkNumbers = shardNumbers; // number of shards to create
		int BUF_SIZE = 1024;
		long[] sizeList = new long[chunkNumbers];
		long sum  =0;
		for(int i =0;i<chunkNumbers-1;i++) {
			long shSize = fileSize/chunkNumbers;
			long extra = shSize%BUF_SIZE;
			long actualSize = shSize - extra;
			sizeList[i] = actualSize;
			sum +=sizeList[i];
		}

		sizeList[sizeList.length-1] = fileSize-sum;
		logger.info("sizeList: "+Arrays.toString(sizeList));
		List<BlobId> shardList = new ArrayList<>();

		try (ReadChannel reader = file.reader()) {
			for(int i =0;i<chunkNumbers;i++) {
				BlobId shardId = BlobId.of(bucketName, "shard" + String.valueOf(i) + shardName);
				Blob shard = storage.create(BlobInfo.newBuilder(shardId ).build());
				shardList.add(shardId);
				logger.info("ShardName: "+shard.getName());
				long shardSize = sizeList[i];
				long bytesRead =0;
				long bytesWritten = 0;
				
				try(WriteChannel writer = shard.writer()){
					ByteBuffer bytes = ByteBuffer.allocate(BUF_SIZE);
					
					while (bytesWritten<shardSize) {
						int curBytesRead = reader.read(bytes);
						
						bytesRead+=curBytesRead;
						bytes.flip();

						bytesWritten = bytesWritten + writer.write(bytes);
						//logger.info(" curBytesRead"+curBytesRead+",bytesWritten:"+bytesWritten);
						bytes.clear();
						
						if(curBytesRead<=0) {
							logger.info("file done bytesWritten"+bytesWritten);
							break;
						}
					}
				}
				logger.info("ShardName: "+shard.getName()+"bytesRead: "+bytesRead+" bytesWritten: "+bytesWritten);
			}


		}
		return shardList;

	}


	public BlobId uploadFileToGCS(MultipartFile file) throws IOException {
		
		String filename = file.getOriginalFilename();

		BlobId blobId = BlobId.of(bucketName, filename );
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
		Blob blob = storage.createFrom(blobInfo, file.getInputStream());

		logger.info("Written File size: "+blob.getSize());
		return blobId;

	}

	public void cleanUpShards(List<BlobId> list) {
		storage.delete(list);
	}

}