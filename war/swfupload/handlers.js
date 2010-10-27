var awsS3Post;
var cancel = false;

function swfuploadLoaded() {
	/* flash check
	$("#s1 .result").empty();
	$("#s1 .result").html('<span class="exciting">Error:</span><br/>You need Flash Player 9 or above');
	$("#s1 .result").delay(1500).fadeIn();
	return false;
	*/	
}

function fileDialogComplete(numFilesSelected, numFilesQueued) {			
	try {
		if (numFilesQueued > 0) {
			this.startUpload();
		}
	} catch (ex) {
		this.debug(ex);
	}
}


function uploadStart(file) {
	if (awsS3Post == null) {
		alert("enter timeout");
		//timer does not work
	}
	
	this.addPostParam("AWSAccessKeyId", awsS3Post.awsaccessKeyId);
	this.addPostParam("key", awsS3Post.key);
	this.addPostParam("acl", awsS3Post.acl);
	this.addPostParam("success_action_redirect", awsS3Post.success_action_redirect);
	this.addPostParam("policy", awsS3Post.policy);
	this.addPostParam("signature", awsS3Post.signature);
	this.addPostParam("content-type", awsS3Post.content_type);
	this.addPostParam("x-amz-meta-filename", awsS3Post.x_amz_meta_filename);		
	this.addPostParam("x-amz-meta-token", awsS3Post.x_amz_meta_token);
	this.addPostParam("x-amz-meta-creatDate", awsS3Post.x_amz_meta_creatDate);		
}

function fileQueued(file) {
	url = "/show/upload/" + $("#cid").val();
	$.getJSON(url, {filename : file.name}, function(data) {
		uploadStart = true;
		awsS3Post = data;
		$("#name").val(data.filename);
		$("#btn-show-url-save").removeAttr("href");
		alert("pid=" + data.pid);
		$("#pid").val(data.pid);				
	});		
}

function fileQueueError(file, errorCode, message) {
	alert("filequeueerror:" + message);
}

function uploadProgress(file, bytesLoaded) {
	$("#s1 .result").empty();
	$("#s1 .uploading").show().delay(1000).fadeOut();
	$("#s1 .result").delay(1500).fadeIn();
}

function uploadSuccess(file, serverData) {
	alert("upload success, serverData = " + serverData);
	$("#s1 .result").empty();
	$("#s1 .result").html('<span class="exciting">Complete:</span><br/>The new show is in the Off-Air Show area now.');
	$("#s1 .result").delay(1500).fadeIn();	
}

function uploadComplete(file) {
	alert("upload complete");
	try {
		/*  I want the next upload to continue automatically so I'll call startUpload here */
		if (this.getStats().files_queued > 0) {
			this.startUpload();
		} else {
			//var progress = new FileProgress(file,  this.customSettings.upload_target);
			//progress.setComplete();
			//progress.setStatus("All images received.");
			//progress.toggleCancel(false);
		}
	} catch (ex) {
		this.debug(ex);
	}
}

function uploadError(file, errorCode, message) {
	alert("upload error:" + errorCode + ";" + message);
	$("#s1 .result").empty();
	$("#s1 .result").html('<span class="exciting">Error:</span><br/>Upload error. Please try again later or contact customer support');
	$("#s1 .result").delay(1500).fadeIn();	

}
