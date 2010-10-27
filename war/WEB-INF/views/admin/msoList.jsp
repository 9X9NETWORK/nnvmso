<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head></head>

<body>
   
  <c:forEach items="${msos}" var="m" > 
	<p>
	<div>key: ${m.keyStr} <br/></div>
	<div>email: ${m.email}</div>
	<div>name: ${m.name}</div>
	</p>		  
  </c:forEach>   
</body>
</html>