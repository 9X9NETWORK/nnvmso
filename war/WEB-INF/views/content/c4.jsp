<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<script src="/WEB-INF/../swfupload/swfupload.js"></script>
<script src="/WEB-INF/../swfupload/handlers.js"></script>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<script type="text/javascript">
var swfu; 
var settings;

$(document).ready(function() {
 settings = {
   // Flash Settings   
   flash_url : "/WEB-INF/../swfupload/swfupload.swf", 
   upload_url: "<c:out value="${upload_url}"/>",
   http_success : [ 200, 201, 204 ], 
   
   //file
   file_size_limit : "2 GB",
   file_types : "*.*",
   file_types_description : "All Files",
   file_upload_limit : 100,
   file_post_name: 'file', 
   
   debug: true,
   
   //button
   button_image_url: "/WEB-INF/../images/upload.png",
   button_width: "104",
   button_height: "35",
   button_placeholder_id: "spanButtonPlaceHolder",
   button_text: '<span class="theFont">Upload</span>',
   button_text_style: ".theFont{ color: #ffffff; font-size:12px; font-family:verdana, 新細明體; display:block; }",
   button_text_top_padding: 8,
   button_text_left_padding: 40,
   button_window_mode : SWFUpload.WINDOW_MODE.TRANSPARENT,   
 
   //The event handler functions are defined in handlers.js
   swfupload_loaded_handler: swfuploadLoaded,
   upload_start_handler : uploadStart,
   file_queued_handler : fileQueued,
   file_queue_error_handler : fileQueueError,
   file_dialog_complete_handler : fileDialogComplete,
   upload_progress_handler : uploadProgress,
   upload_error_handler : uploadError,
   upload_success_handler : uploadSuccess,
   upload_complete_handler : uploadComplete,
   
   //post params
   
 };  
  
 swfu = new SWFUpload(settings); 
 
}) 
 
</script>
<form action="c1" method="post" enctype="multipart/form-data">
 <div id="swfuploadBtn" style="margin: 0 auto; width: 90px;"><div id="spanButtonPlaceHolder"></div></div>
 
 <div id="thumbnails">hello</div> 
 <div id="divFileProgress"></div>

	</form>