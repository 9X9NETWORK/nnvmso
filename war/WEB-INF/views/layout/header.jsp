<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="false"%>
<%@ page isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" href="/WEB-INF/../stylesheets/main.css" />
<script src="/WEB-INF/../javascripts/jquery-1.4.1.min.js"></script>
<script src="/WEB-INF/../javascripts/basic.js"></script>
<script src="/WEB-INF/../javascripts/jquery.cookie.js"></script>
<script src="/WEB-INF/../javascripts/setcookie.js"></script>
<title>9x9 vMSO</title>
</head>
<body>
<div id="header">
  <a href="/mso/login"><img src="/WEB-INF/../images/logo_9x9.png" id="logo"></a>
  <ul id="tabs">
    <li><a href="/channel/list" id="ipg">Channel IPG</a></li>
    <li><a href="/show/list/*" id="shows">Shows</a></li>
    <li><a href="/statistics/index" id="statistics">Statistics</a></li>
    <li><a href="/player/embed" id="embed">Embed</a></li>
  </ul>
  <a href="javascript:;" id="btn-signout">Sign Out</a>
  <a href="javascript:;" id="btn-account">Account</a>
</div>
