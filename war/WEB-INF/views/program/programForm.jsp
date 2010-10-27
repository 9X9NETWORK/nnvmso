<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<script src="/WEB-INF/../swfupload/swfupload.js"></script>
<script src="/WEB-INF/../swfupload/file_handlers.js"></script>
<script src="/WEB-INF/../swfupload/img_handlers.js"></script>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
  
<script type="text/javascript">
var swfu; 
var settings;

$(document).ready(function() {
 settings = {
   // Flash Settings   
   flash_url : "/WEB-INF/../swfupload/swfupload.swf", 
   upload_url: "<c:out value="${file_upload_url}"/>",
   //http_success : [ 200, 201, 204, 303 ], 
   http_success : [ 303 ],
   //file
   file_size_limit : "2 GB",
   file_types : "*.*",
   file_types_description : "All Files",
   file_upload_limit : 100,
   file_post_name: 'file',    
   
   //debug
   debug: false,
   
   //button
   button_image_url: "/WEB-INF/../images/upload.png",
   button_width: "104",
   button_height: "35",
   button_placeholder_id: "spanButtonPlaceHolder",
   button_text: '<span class="theFont">Upload</span>',
   button_text_style: ".theFont{ color: #ffffff; font-size:12px; font-family:verdana, 新細明體; display:block;}",
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
   upload_complete_handler : uploadComplete
 };    
 swfu = new SWFUpload(settings); 
}) 
 
</script>
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
   upload_start_handler : img_uploadStart,
   file_dialog_start_handler : img_file_dialog_start, 
   file_queued_handler : img_fileQueued,
   file_queue_error_handler : img_fileQueueError,
   file_dialog_complete_handler : img_fileDialogComplete,
   upload_progress_handler : img_uploadProgress,
   upload_error_handler : img_uploadError,
   upload_success_handler : img_uploadSuccess,
   upload_complete_handler : img_uploadComplete
   
   //post params   
 };    
 img_swfu = new SWFUpload(img_settings); 
}) 
 
</script>
<div id="action" style="display:none"><c:if test="${action == 'create'}">create</c:if></div>
<div class="container">
<div id="newshow-content" class="content">
  <ul class="titlebarL">
    <li><c:out value="${channel.name}"/> - Create new show</li>
    <li><span class="title-note"><span class="star">*</span> Fields are mandatory.</span></li>    
    <li class="right"><a href="/show/create/<c:out value='${program.channelId}'/>" id="btn-moreshow"></a></li>
  </ul>
  <div class="showbox">
    <div class="show" id="s1">
      <form id="showupload-form">
        <div class="showupload">
          <p class="show-title"><span class="tabNum">1</span>Upload show</p>
          <ul class="form">
            <li>
              <label for="show url">Show URL:</label>
              <span class="textfieldbox">
              <c:if test="${action == 'create'}">
              <input name="s1-url" id="s1-url" type="text" class="textfield">
              </c:if>
              <c:if test="${action != 'create'}">
              <input name="s1-url" id="s1-url" type="text" class="textfield" value="<c:out value="${program.webMFileUrl}"/>" />
              </c:if>
              </span> </li>
            <li class="btn-line">
               <div class="btn-uploadShow"><div id="spanButtonPlaceHolder" ></div></div>
               <a href="javascript:;" class="btn-cancelShow">Cancel</a>
               <a href="javascript:;" id="btn-show-url-save" class="btn-cancelShow">Save Url</a>
            </li>
          </ul>
          <p class="uploading"></p>
          <p class="result"><span class="exciting">Complete!</span><br>
            The new show is in the Off-Air Show area now.</p>
        </div>
      <form:form id="showinfo-form" method="post" modelAttribute="program">
        <input type="hidden" id="cid" value="<c:out value="${channel.id}"/>" />
        <c:if test="${action == 'create'}">
        <input type="hidden" id="pid" value="" />
        </c:if>              
        <c:if test="${action == 'edit'}">
        <input type="hidden" id="pid" value="<c:out value="${program.id}"/>" />
        </c:if>              
        <div class="showinfo">
          <p class="show-title"><span class="tabNum">2</span>Edit show information</p>
          <ul class="form">
            <li>
              <label for="show title">Show title:<span class="star">*</span></label>
              <span class="textfieldbox">
              <form:input path="name" class="textfield"/>
              <form:hidden path="channelId" />
              </span>
            </li>
            <li>
              <label for="show description">Show description:</label>
              <!-- <span class="textareabox"> -->
              <!-- <textarea name="s1-description" id="s1-description" cols="10" rows="6" class="textarea"></textarea>  -->
              <!-- <form:textarea path="intro" cols="10" rows="6" class="textarea" />  -->
              <span class="textfieldbox">  
              <form:input path="intro" class="textfield"/>
              </span>
            </li>
            <li>
              <label for="show title">Thumbnail url: [for existing url]</label>
              <span class="textfieldbox">
              <form:input path="imageUrl" class="textfield"/>
              </span>
            </li>
            <li><input id="program-save" disabled="disabled" type="button" value="" class="btn btn-saveInfo disable"/></li>
          </ul>
        </div>
      </form:form>
      <form id="showthumb-form">
        <div class="showthumb">
          <p class="show-title"><span class="tabNum">3</span>Upload show icon</p>
          <ul class="thumbnail">
            <li><label for="show-icon">Show icon:</label></li>
            <li><p class="upload-holder">
                  <c:if test="${action == 'create'}">
                    <img src="<c:out value="/WEB-INF/../images/thumb_noImage.jpg"/>" class="thumb">
                  </c:if>
                  <c:if test="${action != 'create'}">
                     <img src="<c:out value="${program.imageUrl}"/>" class="thumb">
                  </c:if>  
                </p>
                <span class="guide">Recommended upload size under 2MB</span></li>            
            <li><div class="btn-uploadThumb"><div id="img_spanButtonPlaceHolder" class="btn-uploadThumb"></div></div>
                <a href="javascript:;" class="btn-cancelThumb">Cancel</a>                                              
            </li>
          </ul>
        </div>
      </form>
    </div>
  </div>
</div>
</div>
<%@ include file="/WEB-INF/views/layout/footer.jsp" %>