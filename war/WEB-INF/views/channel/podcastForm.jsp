<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<script type="text/javascript"> 
</script>

<div id="action" style="display:none"><c:if test="${action == 'create'}">create</c:if></div>
<div id="cid" style="display:none"><c:out value="${channel.id}"/></div>
<form:form id="newchannel-form" method="post" modelAttribute="channel">
  <div class="container">
    <div id="newchannel-content" class="content">
      <ul class="titlebarL">
        <li class="on"><span class="tabNum">1</span><span class="tabName">Create new channel</span></li>
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
            <label for="channel name">Podcast RSS Link:<span class="star">*</span></label>
            <span class="textfieldbox">
            <form:input path="podcast" class="textfield"/>
            </span> </li>
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