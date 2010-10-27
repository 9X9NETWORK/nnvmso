<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<script type="text/javascript">
  //alert("<c:out value="${uploadUrl}"/>");
</script>

 <form action="/content/c3" method="post">
	<p>
    <div>content name: <input type="text" name="name"/></div>
    <div>content name: <input type="text" name="type"/>slideshow/video</div>    
    </p>
    <div><input type="submit" value="createContent" /></div>
</form>
