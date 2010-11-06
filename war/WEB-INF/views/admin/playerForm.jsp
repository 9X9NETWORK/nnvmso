<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %> 
 <form:form method="post" modelAttribute="form">
    <div>mso email: <form:input path="mso.email"/></div>
    <div>logoUrl: <form:input path="player.logoUrl"/></div>    
    <div>code: </div>	
    <div><form:textarea path="player.code" rows="40" cols="100"/>
    </div>        
    </p>
    <div><input type="submit" value="save" /></div>
 </form:form>
  <!-- <textarea rows="40" cols="100"/><c:out value="${code.value}"/></textarea> -->
  