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
<link rel="stylesheet" href="/stylesheets/jquery.jqModal.css" type="text/css"/>
<script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#pubid=ra-4dcccc98718a5dbe"></script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.js"></script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.12/jquery-ui.min.js"></script>
<script type="text/javascript" src="http://www.netgrow.com.au/assets/files/jquery_plugins/jquery.dump.js"></script>
<script type="text/javascript" src="/javascripts/jquery.jqModal.js"></script>
<script type="text/javascript" src="/javascripts/cms/common.js"></script>
<script type="text/javascript" src="/javascripts/cms/promotionTools.js"></script>
<title><spring:message code="cms.promotion_tools.title"/></title>
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
      <li><a href="directoryManagement" class="menuC"></a></li>
      <li><a href="javascript:" class="menuD_active"></a></li>
      <li><a href="statistics" class="menuE"></a></li>
    </ul>
    <div class="clear"></div>
    <div class="left_body">
      <div class="createChList2">
        <div class="left_title"><spring:message code="cms.promotion_tools.title.channel_set_list"/></div>
        <ul class="chList" id="channel_set_list_ul">
        </ul>
        <div class="left_title"><spring:message code="cms.promotion_tools.title.channel_list"/></div>
        <ul class="chList" id="channel_list_ul">
          <li style="display:none">
            <div class="chUnFocus channel_info_block" id="channel_info_block">
              <div class="chUnFocusTitle channel_info_title"><div>Tile Tile Tile</div></div>
              <div class="chUnFocusImg channel_info_image"></div>
              <div class="floatL chInfo">
                <a href="#" target="_player" class="floatL channel_info_promoteurl">http://www.9x9.tv/channel/3958</a>
                <a class="floatL channel_info_addthis"><img src="http://cache.addthiscdn.com/icons/v1/thumbs/addthis.gif"/></a>
                <a href="javascript:" class="iconStatistics" title=""></a>
                <div class="clear"></div>
                <div class="floatL">
                  <p class="channel_info_contenttype"><spring:message code="cms.promotion_tools.label.channel_type"/> : <span>9x9</span></p>
                  <p class="channel_info_programcount"><spring:message code="cms.promotion_tools.label.program_count"/> : <span>0</span></p>
                  <p class="channel_info_subscribers"><spring:message code="cms.promotion_tools.label.subscribers"/> : <span>0</span></p>
                  <p class="channel_info_updatedate"><spring:message code="cms.promotion_tools.label.update_time"/> : <span>2011/04/15 14:45</span></p>
                </div>
                <ul class="floatL">
                  <li><a class="chUnPublic channel_info_publish"></a></li>
                </ul>
              </div>
            </div>
          </li>
        </ul>
      </div>
    </div>
    <div class="left_footer"></div>
  </div>
  <div class="right_content floatL">
    <div class="right_body">
      <div id="promotion_content" style="display:none">
        <div class="right_title"><div>Title Title Title</div></div>
        <div class="promote_title">
          <div class="floatL"><spring:message code="cms.promotion_tools.title.autosharing"/>&nbsp;&nbsp;</div>
          <a href="javascript:" class="promoteInfo"></a>
          <div class="clear"></div>
          <div class="promote_hint" id="pro_hint">
            <ul>
              <li><spring:message code="cms.promotion_tools.msg.autosharing_hint_1"/></li>
              <li><spring:message code="cms.promotion_tools.msg.autosharing_hint_2"/></li>
            </ul>
          </div>
        </div>
        <br/>
        <label class="pro_check"><input type="checkbox" class="sns_checkbox" name="sns_facebook" disabled="disabled"/>&nbsp; <spring:message code="cms.promotion_tools.label.facebook"/></label>
        <label class="pro_check"><input type="checkbox" class="sns_checkbox" name="sns_twitter" disabled="disabled"/>&nbsp; <spring:message code="cms.promotion_tools.label.twitter"/></label>
        <label class="pro_check"><input type="checkbox" class="sns_checkbox" name="sns_plurk" disabled="disabled"/>&nbsp; <spring:message code="cms.promotion_tools.label.plurk"/></label>
        <label class="pro_check"><input type="checkbox" class="sns_checkbox" name="sns_sina" disabled="disabled"/>&nbsp; <spring:message code="cms.promotion_tools.label.sina"/></label>
        <br/><br/><br/>
        <div class="promote_title"><spring:message code="cms.promotion_tools.label.9x9_rss_feed"/>&nbsp;&nbsp; <a id="9x9_rss_tutorial" href="javascript:"><spring:message code="cms.promotion_tools.msg.tutorial"/></a></div>
        <br/><br/>
        <input type="text" size="40" disabled="disabled" value="9x9 RSS needs be provided"/>&nbsp;&nbsp;<a href="javascript:"><spring:message code="cms.promotion_tools.button.click_to_copy"/></a>
      </div>
    </div>
    <div class="right_footer"></div>
  </div>
  <div class="clear"></div>
</div>
<label class="lang" id="lang_view_statistics"><spring:message code="cms.promotion_tools.msg.view_statistics"/></label>
<label class="lang" id="lang_label_channel_set"><spring:message code="cms.promotion_tools.label.channel_set"/></label>
<label class="lang" id="lang_confirm_goto_setting_page"><spring:message code="cms.warning.goto_setting_page"/></label>
</body>
</html>
