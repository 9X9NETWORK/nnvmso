<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<form:form id="signin-form" method="post" modelAttribute="mso">
<div class="container">
<div id="signin-content" class="content">
  <p class="titlebar">Welcome to the vMSO Management Portal</p>
  <div class="formholder">
    <ul class="form">
      <li class="error"><form:errors path="*" /></li>          
      <li>
        <label for="user account">Account:</label>
        <span class="textfieldbox"><form:input path="email" class="textfield"/></span>
        <span class="error"><form:errors path="email" /></span>          
      </li>
      <li>
        <label for="password">Password:</label>
        <span class="textfieldbox"><form:input path="password" type="password" class="textfield" /></span>
        <span class="error"><form:errors path="password" /></span>
        <a href="javascript:;" id="btn-forgotpw">Forgot password?</a>
      </li>
      <li>
        <input type="submit" id="btn-signin" value="">
      </li>
    </ul>
  </div>
</div>
</div>
</form:form>
<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
