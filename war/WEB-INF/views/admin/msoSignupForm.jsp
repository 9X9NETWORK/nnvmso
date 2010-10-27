 <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
 <form:form method="post" modelAttribute="mso">
    <div>email: <form:input path="email"/></div>	
    <div>name: <form:input path="name"/></div>    
    <div>password: <form:input path="password"/></div>
    <div>description: <form:input path="intro"/></div>	
    <div>thumbnailUrl: <form:input path="imageUrl"/></div>        
    </p>
    <div><input type="submit" value="createMso" /></div>
 </form:form>
