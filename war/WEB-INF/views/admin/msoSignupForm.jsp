 <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
 <form:form method="post" modelAttribute="mso">
    <div>email: <form:input path="email"/></div>	
    <div><form:errors path="email"/></div>
    <div>name: <form:input path="name"/></div>    
    <div><span class="error"><form:errors path="name"/></span></div>
    <div>password: <form:input path="password"/></div>
    <div><form:errors path="password"/></div>
    <div>description: <form:input path="intro"/></div>	
    <div>thumbnailUrl: <form:input path="imageUrl"/></div>        
    </p>
    <div><input type="submit" value="createMso" /></div>
 </form:form>
