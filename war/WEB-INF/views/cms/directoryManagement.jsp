<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="/stylesheets/cms.css" />
<link rel="stylesheet" type="text/css" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.12/themes/start/jquery-ui.css" />
<link rel="stylesheet" type="text/css" href="/stylesheets/jquery.bubble.css" />
<link rel="stylesheet" href="/stylesheets/jquery.jqModal.css" type="text/css"/>
<script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#pubid=ra-4dcccc98718a5dbe"></script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.js"></script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.12/jquery-ui.min.js"></script>
<script type="text/javascript" src="/javascripts/jquery.bubble.js"></script>
<script type="text/javascript" src="/javascripts/jquery.textTruncate.js"></script>
<script type="text/javascript" src="/javascripts/jquery.jqModal.js"></script>
<script type="text/javascript" src="/javascripts/jquery.jstree.js"></script>
<script type="text/javascript" src="http://www.netgrow.com.au/assets/files/jquery_plugins/jquery.dump.js"></script>
<script type="text/javascript" src="/javascripts/cms/common.js"></script>
<script type="text/javascript" src="/javascripts/cms/directoryManagement.js"></script>
<title><spring:message code="cms.directory_management.title"/></title>
</head>
<body>
<div class="header">
  <input type="hidden" id="msoId" value="${msoId}"/>
  <div class="floatL"><img alt="" src="${msoLogo}"/></div>
  <div id="setup_page" class="jqmWindow"></div>
  <div class="floatR">
    <p><spring:message code="cms.header.welcome"/> <span><c:out value="${mso.contactEmail}"/></span></p>
    <a href="javascript:" class="setup" id="setup"></a>
    <a href="${logoutUrl}" class="logout"></a>
  </div>
</div>
<div class="content">
  <div class="left_content floatL">
    <ul class="menu">
      <li><a href="channelManagement" class="menuA"></a></li>
      <li><a href="channelSetManagement" class="menuB"></a></li>
      <li><a href="javascript:" class="menuC_active"></a></li>
      <li><a href="promotionTools" class="menuD"></a></li>
      <li><a href="statistics" class="menuE"></a></li>
    </ul>
    <div class="clear"></div>
    <div class="left_body">
      <p class="ch_sub_title"><span><spring:message code="cms.directory_management.msg.directory_explanation"/></span></p>
      <p>
        <button id="btn_delete_directory"><spring:message code="cms.directory_management.button.delete_directory"/></button>
        <button id="btn_create_directory"><spring:message code="cms.directory_management.button.create_directory"/></button>
        <button id="btn_rename_directory"><spring:message code="cms.directory_management.button.rename_directory"/></button>
      </p>
      <div class="directoryList" id="treeview"></div>
    </div>
    <div class="left_footer"></div>
  </div>
  <div class="right_content floatL">
    <div class="right_body">
      <div class="right_title"><spring:message code="cms.directory_management.title.channel_and_set_pool"/></div>
      <div class="ch_pool">
        <p class="ch_sub_title"><span><spring:message code="cms.directory_management.channel_and_set_ecplanation"/></span></p>
        <div class="directoryChList jstree-drop">
          <ul class="directory_ch" id="directory_list_ul"></ul>
        </div>
      </div>
    </div>
    <div class="right_footer"></div>
    <div class="clear"></div>
  </div>
</div>
<div style="clear:both"/>
<!-- language tags for javascript -->
<label class="lang" id="lang_label_program_count"><spring:message code="cms.directory_management.label.program_count"/></label>
<label class="lang" id="lang_label_update_time"><spring:message code="cms.directory_management.label.update_time"/></label>
<label class="lang" id="lang_label_channel_set"><spring:message code="cms.directory_management.label.channel_set"/></label>
<label class="lang" id="lang_warning_error_occurs"><spring:message code="cms.warning.error_occurs"/></label>
<label class="lang" id="lang_warning_you_must_select_directory"><spring:message code="cms.warning.you_must_select_directory"/></label>
<label class="lang" id="lang_warning_cannot_drag_directory"><spring:message code="cms.warning.cannot_drag_root_directory"/></label>
<label class="lang" id="lang_warning_cannot_remove_root"><spring:message code="cms.warning.cannot_remove_root_directory"/></label>
<label class="lang" id="lang_confirm_remove_directory"><spring:message code="cms.warning.are_you_sure_to_remove_directory"/></label>
</body>
</html>
