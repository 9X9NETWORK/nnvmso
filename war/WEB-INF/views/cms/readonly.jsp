<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="global.jsp" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title><spring:message code="cms.readonly.title"/></title>
<link href="${root}/stylesheets/cms.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<div class="header"></div>
<div class="content loginContent">
  <div class="loginPanel">
    <img alt="" src="${msoLogo}" /><br/><br/>
    <div style="text-align:center">
      <div style="margin-left:150px;width:300px">
        <br/>
        <span style="color:dimgrey;font-size:large"><spring:message code="cms.readonly.msg.readonly_message"/></span>
        <br/><br/><br/>
        <a href="/9x9" class="btnCreate" style="width:140px;margin-left:50px"><span><spring:message code="cms.readonly.btn.back_to_channel_store"/></span></a>
      </div>
    </div>
  </div>
</div>
</body>
</html>
