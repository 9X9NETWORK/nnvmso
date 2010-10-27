<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<script src="/WEB-INF/../javascripts/jquery-ui-1.8.5.custom.min.js"></script>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<script type="text/javascript">
$(document).ready(function() {
	url = "/show/create/" + $("#channel-selector").val();
	$("#btn-newshow").attr("href", url);
	$("#sortable").sortable();
})

</script>

<form id="shows-form">
  <div class="container">
    <div id="shows-content" class="content">
      <ul class="titlebarL">
        <li>Manage shows</li>
        <li class="right"><a href="javascript:;" id="btn-newshow"></a></li>
        <li class="select">
          <label for="select channel">Select your channel to manage your shows:</label>
          <select id="channel-selector" class="selector">
            <c:forEach items="${channels}" var="c">
            <option value="<c:out value="${c.id}"/>" selected><c:out value="${c.name}"></c:out></option>
            </c:forEach>
          </select>
        </li>
      </ul>
      <div id="clips"><%@ include file="clips.jsp" %></div>
    </div>
  </div>
</form>      
<div id="mask"></div>

<div class="popup">
  <p class="popup-header">
    <span class="popup-title">History of the Internet</span>

    <a href="javascript:;" id="popup-close"></a>  </p>
  <p class="video">
    <object width="765" height="455"><param name="movie" value="http://www.youtube.com/v/9hIQjrMHTv4?fs=1&amp;hl=en_US"></param><param name="allowFullScreen" value="true"></param><param name="allowscriptaccess" value="always"></param><embed src="http://www.youtube.com/v/9hIQjrMHTv4?fs=1&amp;hl=en_US" type="application/x-shockwave-flash" allowscriptaccess="always" allowfullscreen="true" width="765" height="455"></embed></object>
  </p>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
