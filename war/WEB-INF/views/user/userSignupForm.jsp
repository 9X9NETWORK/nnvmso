 <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
 <form:form method="post" modelAttribute="form">
 	<div>mso: <form:input path="mso.email"/></div>
 	<div><span class="error"><form:errors path="mso.email"/></span></div>
    <div>email: <form:input path="user.email"/></div>
    <div><span class="error"><form:errors path="user.email"/></span></div>	
    <div>name: <form:input path="user.name"/></div>
    <div><span class="error"><form:errors path="user.name"/></span></div>    
    <div>password: <form:input path="user.password"/></div>
    <div><span class="error"><form:errors path="user.password"/></span></div>
    <div>description: <form:input path="user.intro"/></div>	
    <div>thumbnailUrl: <form:input path="user.imageUrl"/></div>        
    </p>    
    <div><input type="submit" value="createUser" /></div>
 </form:form>
