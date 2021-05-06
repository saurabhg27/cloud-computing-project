package com.clocomp.project.appengine.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.clocomp.project.appengine.controller.OurDatabase;
import com.clocomp.project.appengine.pojo.FileInfoPojo;
import com.google.appengine.api.users.UserServiceFactory;

@RestController
public class StorageController {

	Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	OurDatabase db;

	@Autowired
	FileStoreService fileStoreService;

	@GetMapping("/storage/getListOfExistingFilesForUser")
	public List<FileInfoPojo> getExistingFiles(@RequestParam String userName) throws IOException {
		
		if(userName.compareToIgnoreCase("dummy") == 0)//this is a request from front-end
			userName = UserServiceFactory.getUserService().getCurrentUser().getEmail();
		
		return db.getListOfFilesOfUser(userName);

	}

	@PostMapping("/storage/fileupload")
	public String handleFileUpload(@RequestParam("files") MultipartFile file,@RequestParam String userName) {
		String fileName = file.getOriginalFilename();
		
		if(userName.compareToIgnoreCase("dummy") == 0)//this is a request from front-end
			userName = UserServiceFactory.getUserService().getCurrentUser().getEmail();
		
		logger.info("File Uplod request for user: "+userName+ " file: "+fileName);
		String str =  "file: "+fileName + ",size:" + file.getSize()+" ";

		try {

			fileStoreService.shardAndUploadFile(fileName,file,userName);
			str+= "uploaded successfully !!";
		} catch (Exception e) {
			str += e.getMessage();
			logger.severe("Excep: " + e.getMessage());
		}

		return str;

	}
	@GetMapping("/storage/filedownload")
	public ResponseEntity downloadFile(@RequestParam String userName,@RequestParam String fileName) {
		logger.info("user: "+userName+" file: " +fileName);
		try {
			if(userName.compareToIgnoreCase("dummy") == 0)//this is a request from front-end
				userName = UserServiceFactory.getUserService().getCurrentUser().getEmail();

			InputStreamResource resource = new InputStreamResource(fileStoreService.downloadFile(fileName, userName));
			logger.info("Getting the inputstream ress");
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName);
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
			return ResponseEntity.ok()
		            .headers(headers)
		            .contentType(MediaType.APPLICATION_OCTET_STREAM)
		            .body(resource);
		    
		} catch (Exception e) {
			logger.severe("Excep: " + e.toString());
			logger.severe("Excep: " + e.getStackTrace());
			return ResponseEntity.status(500).body(e.getMessage());
		}
		
	}


}
