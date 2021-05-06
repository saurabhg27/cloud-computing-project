package com.clocomp.webtier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import j2html.TagCreator;
import j2html.tags.ContainerTag;

/*
 * This class implements the Spring application
 * */

@RestController
@RequestMapping("/")
public class WebTierRESTController {

	private static final Logger logger = LoggerFactory.getLogger(WebTierRESTController.class);

	@Autowired
	S3Utility s3Utility;
	
	@Autowired
	SQSUtility sqsUtility;

	@GetMapping("/test")
	public String defaultStatus() {
		return "OK";
	}

	
	/*
	 * URL end point handler for file upload
	 * */
	@PostMapping("/fileupload")
	public String handleFileUpload(@RequestParam("files") MultipartFile[] files) {

		//processes the input and waits and fetches all the responses
		Map<String,String> results=processUploadedFiles(files);
	
		//format results into HTML table
		ContainerTag table = createHTMLTable(results);
		return TagCreator.html(
				TagCreator.body(
							table
						)
				).render();		
		
	}

	public static ContainerTag createHTMLTable(Map<String,String> m) {
		
		ArrayList<ContainerTag> rows = new ArrayList();
		for(String k : m.keySet()) {
			List<ContainerTag> cells = new ArrayList();
			cells.add(TagCreator.td(k));
			cells.add(TagCreator.td(m.get(k)));
			rows.add(TagCreator.tr().with(cells));
		}
		return TagCreator.table().with(rows);
	}
	
	/*
	 * This function handles the input received from the client and gets the
	 * responses from the response queue.
	 * */
	private Map<String,String> processUploadedFiles(MultipartFile[] files){
		
		Map<String,String> imgProcResults = null;
		
		try {

			//save input images to S3 bucket
			List<String> s3KeyNamesList= s3Utility.saveMultipleImagesToS3(files);
					
			//after s3 upload complete, push into request SQS here
			for (String imageName : s3KeyNamesList) {
				sqsUtility.sendMsgToRequestQueue(imageName);
			}
			logger.info("messages pushed to sqs");
			
			//wait for and read all the responses from the app tier
			return sqsUtility.receiveImageProcessingResults(s3KeyNamesList);
			
			
		} catch (Exception e) {
			logger.error("Exception in rest controller",e);
			imgProcResults = new HashMap<>();
			imgProcResults.put("ERROR", e.getMessage());
		}

		return imgProcResults;
		
	}

}
