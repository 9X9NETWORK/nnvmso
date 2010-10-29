<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<script src="/WEB-INF/../swfupload/swfupload.js"></script>
<script src="/WEB-INF/../swfupload/channelImgHandler.js"></script>

<script type="text/javascript">
var img_swfu; 
var img_settings;

$(document).ready(function() {
   img_settings = {
   // Flash Settings   
   flash_url : "/WEB-INF/../swfupload/swfupload.swf", 
   upload_url: "<c:out value="${thumb_upload_url}"/>",
   http_success : [ 200, 201, 204, 303 ], 
   
   //file
   file_size_limit : "1 MB",
   file_types : "*.jpg;*.jpeg;*.png;*.gif;*.JPG;*.JPEG;*.PNG;*.GIF",
   file_types_description : "Image Files",
   file_upload_limit : 100,
   file_post_name: 'file', 
   
   //debug
   debug: false,
   
   //button
   button_image_url: "/WEB-INF/../images/upload.png",
   button_width: "104",
   button_height: "35",
   button_placeholder_id: "img_spanButtonPlaceHolder",
   button_text: '<span class="theFont">Upload</span>',
   button_text_style: ".theFont{ color: #ffffff; font-size:12px; font-family:verdana, 新細明體; display:block; }",
   button_text_top_padding: 8,
   button_text_left_padding: 40,
   button_window_mode : SWFUpload.WINDOW_MODE.TRANSPARENT,   
 
   //The event handler functions are defined in handlers.js
   swfupload_loaded_handler: swfuploadLoaded,   
   upload_start_handler : img_uploadStart,
   file_dialog_start_handler : img_file_dialog_start, 
   file_queued_handler : img_fileQueued,
   file_queue_error_handler : img_fileQueueError,
   file_dialog_complete_handler : img_fileDialogComplete,
   upload_progress_handler : img_uploadProgress,
   upload_error_handler : img_uploadError,
   upload_success_handler : img_uploadSuccess,
   upload_complete_handler : img_uploadComplete
 };    
 img_swfu = new SWFUpload(img_settings); 
}) 
 
</script>

<div id="action" style="display:none"><c:if test="${action == 'create'}">create</c:if></div>
<div id="cid" style="display:none"><c:out value="${channel.id}"/></div>
<form:form id="newchannel-form" method="post" modelAttribute="channel">
  <div class="container">
    <div id="newchannel-content" class="content">
      <ul class="titlebarL">
        <li class="on"><span class="tabNum">1</span><span class="tabName">Create new channel</span></li>
        <li><span class="tabNum">2</span><span class="tabName">Create new show</span></li>
        <li class="title-note"><span class="star">*</span> Fields are mandatory.</li>
      </ul>

      <div class="formholder" >
        <ul class="thumbnail">
          <li>Channel thumbnail:</li>
          <c:if test="${channel.imageUrl == null}">          
          <li><img id="thumb_upload" src="/WEB-INF/../images/thumb_noImage.jpg" class="thumb"></li>
          </c:if>
          <c:if test="${channel.imageUrl != null}">
          <li><img id="thumb_upload" src="<c:out value="${channel.imageUrl}"/>" class="thumb"></li>
          </c:if>
          <li><span class="guide">Recommended upload size under 1MB</span></li>
          <li>
            <!-- img src="/WEB-INF/../images/btn_upload.png" id="btn-upload"> -->
            <div id="img_spanButtonPlaceHolder" class="btn-uploadThumb"></div>
          </li>
        </ul>
        <ul class="form">
          <li>
            <label for="channel name">Channel name:<span class="star">*</span></label>
            <span class="textfieldbox">
            <form:input path="name" class="textfield"/>
            </span> <span class="guide">30 English or 15 Chinese characters only.</span> </li>
          <li>
            <label for="channel thumbnail">Channel thumbnail url:<span class="star">*</span></label>
            <span class="textfieldbox">
            <form:input path="imageUrl" class="textfield"/>
            </span> <span class="guide">30 English or 15 Chinese characters only.</span> </li>
          <li class="select">
            <label for="channel language">Language:</label>             
            <form:select path="langCode" class="selector">
              <form:option value="zh-tw" label="中文(繁體)"/>              
              <form:option selected="en" value="en" label="English"/>
            </form:select>
          </li>
          <li>
            <label for="channel tag">Tag:</label>
            <span class="textfieldbox">
            <form:input path="tag" class="textfield"/>
            </span> <span class="guide">100 English or 50 Chinese characters only.</span> </li>
          <li>
            <label for="channel introduction">Introduction:<span class="star">*</span></label>
            <span class="textareabox">
            <form:textarea path="intro" class="textarea" cols="10" rows="6" />
            </span> <span class="guide">80 English or 40 Chinese characters only.</span> </li>
        </ul>
      </div>
      <c:if test="${action == 'create'}">
      <p class="control">
      </c:if>
      <c:if test="${action == 'edit'}">
      <p id="<c:out value="${channel.id}"/>" class="control">
      </c:if>
          <a href="javascript:;" class="btn" id="btn-cancel">Cancel</a>
          <input type="submit" class="btn" id="btn-saveChannel" value="">
          <c:if test="${action == 'create'}">
          <input type="submit" id="btn-createShow" value="">
          </c:if>
    	  <c:if test="${action == 'edit'}">           
          <a href="/show/list/<c:out value="${channel.id}"/>" class="btnblue" id="btn-mgnShows">Manage Shows</a>            
          <a href="javascript:;" class="btnblue" id="btn-takeOffAir">
            <c:if test="${channel.public == true}">Take Off-Air</c:if>
            <c:if test="${channel.public == false}">Take On-Air</c:if>              
          </a>            
          <a href="javascript:;" class="btnblue" id="btn-deleteChannel">Delete Channel</a>         
          </c:if>
        </p>
    </div>
  </div>
</form:form>
<div id="mask"></div>

<div class="confirm-box">
  <a href="javascript:;" id="btn-boxclose"></a>
  <p id="message">If you delete the channel, all associated shows and subscribers will be removed from your vMSO line up.<span class="linebreak">Continue?</span></p>
  <ul id="control">
    <li><a href="javascript:;" class="btn" id="btn-yes">Yes</a></li>
    <li><a href="javascript:;" class="btn" id="btn-no">No</a></li>
    <li><a href="javascript:;" class="btn" id="btn-ok">OK</a></li>
  </ul>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>