$(document).ready(function () {
	console.log("doc ready")
	//checkGoogleDriveLinkStatus();

	$("#linkGoogleButton").click(function () {
		window.location.href = "/googledrivelink";

	});
	$("#linkDropBoxButton").click(function () {
		window.location.href = "/dropbox/auth";

	});
	$("#uploadFileButton").click(function () {
		var fileInfo = $('#fileUploadInput')[0].files[0];
		uploadFile(fileInfo);

	});
	$("#loadTestBut").click(function () {
		loadTesting();

	});

	$("#DownloadTestBut").click(function () {
		downloadTesting();

	});
	document.getElementById('bodycontainer').style.display = 'block';
	populateUserInfo("");

});

function loadTesting(){
	var files = $('#loadTestInp')[0].files;
	console.log(files);
	for (var i = 0; i < files.length; i++) {
		var finfo  = files[i];
		uploadFile(finfo);
		
	}
}

function downloadTesting(){
	$.ajax({
		
		url: "/storage/getListOfExistingFilesForUser?userName=dummy",
		
		success: function (result) {
			
			var fileList = result;
			console.log(fileList);
			
			for (var i = 0; i < fileList.length; i++) {
				
				var row = fileList[i];
				console.log(row);
				
				var filename = row["fileName"];
				//console.log($(fileList));
				console.log("-------------");
				console.log(filename);
				
				var lin = "/storage/filedownload?userName="+"dummy"+"&fileName="+filename;
				
				window.open(lin);
				
				}
			}


	});

}

function uploadFile(fileInfo){
	var fd = new FormData();
	fd.append('files', fileInfo);
	fd.append('userName',"dummy");
	$("#fileUploadInfo").html(" Uploading file, pls wait ...");
	$.ajax({
		url: '/storage/fileupload',
		type: 'post',
		data: fd,
		contentType: false,
		processData: false,
		success: function(response){
			console.log(response);
			if(response != 0){
				console.log('file uploaded');
				$("#fileUploadInfo").html("File uploaded status: "+response);
				populateUserInfo("");
			}
			else{
				$("#fileUploadInfo").html("SOME ERROR WHILE UPLOADING");
			}
		},
	});
}

// function downloadFile(fileInfo){
// 	var fd = new FormData();
// 	fd.append('userName',"dummy");
// 	fd.append('fileName', fileInfo);
//
// 	$("#fileUploadInfo").html(" Uploading file, pls wait ...");
// 	$.ajax({
// 		url: '/storage/filedownload',
// 		method: 'GET',
// 		data: fd,
// 		contentType: false,
// 		processData: false,
// 		success: function(response){
// 			console.log(response);
// 			if(response != 0){
// 				console.log('file uploaded');
// 				$("#fileUploadInfo").html("File uploaded status: "+response);
// 				populateUserInfo("");
// 			}
// 			else{
// 				$("#fileUploadInfo").html("SOME ERROR WHILE DOWNLOADING");
//
// 			}
// 		},
// 	});
// }


//function downloadFile(userName,fileName){
//
//	$.ajax({
//		url: "/storage/filedownload?userName="+userName+"&fileName="+fileName,
//		success: function (result) {
//			console.log(result);
//			$("#fileUploadInfo").html(" Uploading file, pls wait ...");
//
//		},
//		error: function (jqXHR, textStatus, errorThrown) {
//			console.log(textStatus, errorThrown);
//		}
//	});
//}

function checkGoogleDriveLinkStatus(){
	$.ajax({
		url: "/checkDriveAccount",
		success: function (result) {
			console.log(result);
			if(result == "true"){
				document.getElementById('googleLinkDiv').style.display = 'none';
				document.getElementById('bodycontainer').style.display = 'block';
				populateUserInfo(userName);
			} else {
				$("#logInInfo").html(" drive not connected !!");
			}

		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log(textStatus, errorThrown);
		}
	});
}

function eventFire(el, etype){
	if (el.fireEvent) {
		el.fireEvent('on' + etype);
	} else {
		var evObj = document.createEvent('Events');
		evObj.initEvent(etype, true, false);
		el.dispatchEvent(evObj);
	}
}
function populateUserInfo(userName){
	$.ajax({
		url: "/storage/getListOfExistingFilesForUser?userName=dummy",
		success: function (result) {
			console.log(result);
			$("#logInInfo").html(" User Name: "+userName);
			var fileList = result;
			console.log(fileList);
			$("#existingFileTable tbody tr").remove();
			$("#existingFileTable tbody").append("<tr></tr>");
			for (var i = 0; i < fileList.length; i++) {
				var row = fileList[i];
				console.log(row);
				var filename = row["fileName"];
				var rowStr = "<tr>";
				rowStr = rowStr + "<td>"+filename+"</td>" 
				//rowStr = rowStr + "<td><button filename='" + filename + "' class='downFileBut' >download</button></td>"
				console.log(filename);
				var lin = "/storage/filedownload?userName="+"dummy"+"&fileName="+filename;
				var fname = filename;
				fname = fname.replace(" ", "");
				rowStr = rowStr + "<td><a id="+fname+" href="+lin+" >download</a></td>"
				rowStr = rowStr + "</tr>";
				$('#existingFileTable tr:last').after(rowStr);
			}

//			$('.downFileBut').click(function () {
//			var fileName = $(this).attr('filename');
//			console.log("table number clicked: ", fileName);
//			downloadFile(userName,fileName)
//			});

		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log(textStatus, errorThrown);
		}
	});

}