<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>登入</title>
<link href="/stylesheets/cms.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.0/jquery.min.js"></script>
<script type="text/javascript">
// $(function()
//   {
//     $('#submit_button').click(function()
//       {
//         $('#login_form').submit();
//       });
//   });
</script>
</head>
<body>
  <div class="header"></div>
  <div class="content loginContent">
    <div class="loginPanel">
      <img alt="" src="${msoLogo}" /><br/><br/>
      <form id="login_form" method="POST">
        <fieldset class="setAlbum">
          <label class="floatL">9x9帳號 (Email) : </label>
          <input type="text" name="email" size="30" value="${email}"/><br/><br/>
          <label class="floatL">密碼 : </label>
          <input type="password" name="password" size="30" value="${password}"/><br/><br/>
          <label class="floatL">&nbsp;</label>
          <input type="checkbox" name="rememberMe" checked="checked"/><label>&nbsp;&nbsp;記住我的帳號</label><br/><br/>
          <label class="floatL">&nbsp;</label>
          <a href="javascript:$('#login_form').submit();" class="btnLogin" id="submit_button"></a>
          <br/><br/>
          <label class="floatL">&nbsp;</label>
          <span class="error"><c:out value="${error}"/></span>
        </fieldset>
      </form>
    </div>
</div>
</body>
</html>
