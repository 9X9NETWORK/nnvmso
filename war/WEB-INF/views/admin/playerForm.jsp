 <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
 <form:form method="post" modelAttribute="player">
    <div>mso key: <form:input path="mso.keyStr"/></div>
    <div>logoUrl: <form:input path="logoUrl"/></div>    
    <div>code: </div>
    <div><form:textarea path="code" rows="40" cols="50"/>
    </div>        
    </p>
    <div><input type="submit" value="save" /></div>
 </form:form>