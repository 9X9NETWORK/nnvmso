var awsS3Post;

function img_file_dialog_start() {
	$("#s1 .upload-holder .thumb").attr("src", "../images/thumb_noImage.jpg");
}

function img_fileDialogComplete(numFilesSelected, numFilesQueued) {
	try {
		if (numFilesQueued > 0) {
			this.startUpload();
		}
	} catch (ex) {
		this.debug(ex);
	}
}

function img_uploadStart(file) {
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

function img_fileQueued(file) {	
	$("#s1 .upload-holder .thumb").hide();
	$(".upload-holder").addClass("thumb-uploading").delay(2000);	
	
	uploadurl = "/show/thumbupload/" + $("#cid").val() + "/" + $("#pid").val();
	$.ajax({
		type:"GET",
		url:uploadurl,
		data:{filename : file.name},
		async:false,
		success: function(data) {
		   awsS3Post = data;
	    }
	});
}

function img_fileQueueError(file, errorCode, message) {
	alert("filequeueerror:" + message);
}

function img_uploadProgress(file, bytesLoaded) {
	
}

function img_uploadSuccess(file, serverData) {	
	$("#pid").val()
	image="https://s3.amazonaws.com/com-nnaws-showthumb/" + awsS3Post.key;
	$("#s1 .upload-holder .thumb").attr("src", image);	
	$("#s1 .upload-holder .thumb").delay(100).fadeIn();
	$("#imageUrl").val(image);
}

function img_uploadComplete(file) {
	//alert("upload complete");
	try {
		/*  I want the next upload to continue automatically so I'll call startUpload here */
		if (this.getStats().files_queued > 0) {
			this.startUpload();
		} else {
		}
	} catch (ex) {
		this.debug(ex);
	}
}

function img_uploadError(file, errorCode, message) {
	alert("upload error:" + errorCode + ";" + message);
	$("#s1 .result").empty();
	$("#s1 .result").html('<span class="exciting">Error:</span><br/>Upload error. Please try again later or contact customer support');
	$("#s1 .result").delay(1500).fadeIn();	

}
