<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<script type="text/javascript">
$(document).ready(function() {
	$("#ipg").addClass("on");
})
</script>
<div class="container">
  <div id="ipg-content" class="content">
    <ul class="titlebarL">
      <li>IPG of SanLi TV Station<span class="title-note">Channels as of September 28, 2010</span></li>
      <li class="right"><a href="/channel/create/" id="btn-createChannel"></a></li>
      <li class="right"><a href="/channel/podcast" id="btn-createChannel"></a></li>
      <li class="right"><a href="javascript:;" id="btn-toggle">Hide Off-Air Channels</a></li>
    </ul>
    
    <ul class="clip-list" id="on-list">
      <c:forEach items="${onair}" var="c">
      <li id="<c:out value="${c.id}"/>"><img src="<c:out value="${c.imageUrl}"/>"></li>		
	  </c:forEach>
	  <c:forEach begin="1" end="${others}">
      <li></li>
      </c:forEach>  
	  <c:forEach begin="1" end="${system}">
      <li class="system"><img src="/WEB-INF/../thumb/9x9default.jpg"></li>
      </c:forEach>  
    </ul>
    
    <div id="off-list">
    <p class="list-title">Off-Air Channels</p>
    <ul class="clip-list">
      <c:forEach items="${offair}" var="c">    
      <li id="<c:out value="${c.id}"/>"><img src="<c:out value="${c.imageUrl}"/>"></li>
      </c:forEach>
    </ul>
  </div>    
  </div>
 </div>
<div class="info-bubble">
</div>
  
<%@ include file="/WEB-INF/views/layout/footer.jsp" %>