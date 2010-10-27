var awsS3Post;
var cancel = false;

function swfuploadLoaded() {	
	if ($("#action").html() != "create") {
		swfu.setButtonDisabled(true);         
        $("#program-save").removeClass("disable");
	    $("#program-save").removeAttr('disabled');	    	   
    } else {
    	img_swfu.setButtonDisabled(true);
    }
	
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
	uploadurl = "/show/fileupload/" + $("#cid").val();	
	$.ajax({
		type:"GET",
		url:uploadurl,
		data:{filename : file.name},
		async:false,
		success: function(data) {
		   img_swfu.setButtonDisabled(false);
		   awsS3Post = data;
		   $("#name").val(data.filename);		   
		   $("#btn-show-url-save").removeAttr("href");
	       $("#program-save").removeClass("disable");
		   $("#program-save").removeAttr('disabled');	    	   
		   $("#pid").val(data.pid);
		}		   
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
	$("#s1 .result").empty();
	$("#s1 .result").html('<span class="exciting">Complete:</span><br/>The new show is in the Off-Air Show area now.');
	$("#s1 .result").delay(1500).fadeIn();	
	url = "/aws/contentDrop?bucket=com-nnaws&id=" + $("#pid").val();
	$.get(url);				
	
}

function uploadComplete(file) {
	try {
		/*  I want the next upload to continue automatically so I'll call startUpload here */
		if (this.getStats().files_queued > 0) {
			this.startUpload();
		}
	} catch (ex) {
		this.debug(ex);
	}
}

function uploadError(file, errorCode, message) {
	$("#s1 .result").empty();
	$("#s1 .result").html('<span class="exciting">Error:</span><br/>Upload error. Please try again later or contact customer support');
	$("#s1 .result").delay(1500).fadeIn();	

}
