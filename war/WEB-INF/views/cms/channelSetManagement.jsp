<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link href="/stylesheets/jquery.bubble.css" rel="stylesheet" type="text/css"/>
<link href="/stylesheets/cms.css" rel="stylesheet" type="text/css"/>
<link rel="stylesheet" href="/stylesheets/jquery.jqModal.css" type="text/css"/>
<script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#pubid=ra-4dcccc98718a5dbe"></script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.0/jquery.min.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.12/jquery-ui.min.js"></script>
<script type="text/javascript" src="/javascripts/jquery.bubble.js"></script>
<script type="text/javascript" src="/javascripts/jquery.textTruncate.js"></script>
<script type="text/javascript" src="/javascripts/jquery.jqModal.js"></script>
<script type="text/javascript" src="/javascripts/swfupload/swfupload.js"></script>
<script type="text/javascript" src="/javascripts/cms/common.js"></script>
<script type="text/javascript" src="/javascripts/cms/channelSetManagement.js"></script>
<title><spring:message code="cms.channel_set_management.title"/></title>
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
    <ul>
      <li><a href="channelManagement" class="menuA"></a></li>
      <li><a href="javascript:" class="menuB_active"></a></li>
      <li><a href="directoryManagement" class="menuC"></a></li>
      <li><a href="promotionTools" class="menuD"></a></li>
      <li><a href="statistics" class="menuE"></a></li>
    </ul>
    <div class="clear"></div>
    <div class="left_body">
      <div class="left_title"><spring:message code="cms.channel_set_management.title.channel_set_info"/></div>
      <p class="hint_title"><span class="red">＊</span><spring:message code="cms.channel_set_management.msg.necessary_info"/></p>
      <form>
        <fieldset class="setAlbum">
          <input type="hidden" id="cc_id" value="0"/>
          <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_set_management.label.logo"/></label>
          <div class="uploadImg">
            <img alt="" id="cc_image" src="/images/cms/upload_img.jpg" class="floatL"/><input type="hidden" id="cc_image_updated" value="false"/>
            <div class="floatL imgBtn">
              <p class="gray"><spring:message code="cms.channel_set_management.msg.best_resolution"/></p>
              <a href="javascript:" id="upload_image" class="uploadBtn"></a>
              <span id="uploading" style="display:none"><spring:message code="cms.channel_set_management.msg.uploading"/></span>
              <input type="hidden" id="s3_policy" value="${s3Policy}"/>
              <input type="hidden" id="s3_signature" value="${s3Signature}"/>
              <input type="hidden" id="s3_id" value="${s3Id}"/>
            </div>
            <div class="clear"></div>
          </div>
          <label class="floatL"><spring:message code="cms.channel_set_management.label.url"/></label>
          <div class="floatL">
            <a href="#" id="channel_set_promote_url" target="_player"></a>
            <a id="addthis_button"><img src="http://cache.addthiscdn.com/icons/v1/thumbs/addthis.gif"/></a>
          </div>
          <div class="clear"></div><br/>
          <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_set_management.label.name"/></label>
          <div class="bg_input floatL"><input type="text" id="cc_name" name="cc_name" size="25" maxlength="40"/></div>
          <div class="clear"></div>
          <p class="hint"><spring:message code="cms.channel_set_management.msg.fourty_characters"/></p>
          <br/>
          <label class="floatL"><spring:message code="cms.channel_set_management.label.introduction"/></label>
          <div class="bg_textarea floatL"><textarea name="cc_intro" id="cc_intro" cols="33" rows="5"></textarea></div>
          <div class="clear"></div>
          <p class="hint"><spring:message code="cms.channel_set_management.msg.two_hundred_characters"/></p>
          <br/>
          <label class="floatL"><spring:message code="cms.channel_set_management.label.tag"/></label>
          <div class="bg_input floatL"><input name="cc_tag" id="cc_tag" type="text" size="25" maxlength="40"/></div>
          <div class="clear"></div>
          <p class="hint"><spring:message code="cms.channel_set_management.msg.camma_seperated"/></p>
          <br/>
          <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_set_management.label.system_category"/></label>
          <div class="floatL">
            <select id="sys_directory" name="sys_directory" class="sys_directory">
              <option value="0" selected="selected"><spring:message code="cms.channel_set_management.msg.select_category"/></option>
              <!--
              <option>新聞 / 政治</option>
              <option>財經企管</option>
              <option>影視娛樂</option>
              <option>運動休閒</option>
              <option>科技 / 軟體應用</option>
              <option>電玩遊戲</option>
              <option>嗜好興趣</option>
              <option>旅遊生活</option>
              <option>藝術 / 文創</option>
              <option>非營利組織與社會行動</option>
              <option>教育 / 教學</option>
              <option>自然 / 動物</option>
              <option>個人 / 名人</option>
              <option>企業品牌 / 社團單位</option>
              <option>宗教 / 心靈</option>
              <option>其他</option>
              -->
            </select>
          </div>
          <div class="clear"></div>
          <p class="hint"><spring:message code="cms.channel_set_management.msg.system_category_explanation"/></p>
        </fieldset>
      </form>
    </div>
    <div class="left_footer"></div>
  </div>
  <div class="right_content floatL">
    <div class="right_body">
      <div class="right_title"><spring:message code="cms.channel_set_management.title.edit_channel_set"/></div>
      <div class="ch_arrange" id="channel_set_area">
        <div class="ch_bg">
          <ul>
            <!--             
            <li class="ch_none ch_exist"><img src="http://i2.ytimg.com/vi/qxwPtIOs9Q8/default.jpg" width="93" height="56" alt=""/></li>
            -->
            <li class="ch_none"></li>
            <li class="ch_none"></li>
            <li class="ch_none"></li>
            <li class="ch_none"></li>
            <li class="ch_none"></li>
            <li class="ch_none"></li>
            <li class="ch_none"></li>
            <li class="ch_none"></li>
            <li class="ch_none"></li>
          </ul>
          <div class="clear"></div>
        </div>
      </div>
      <div class="ch_pool">
        <p class="ch_sub_title"><spring:message code="cms.channel_set_management.msg.drag_drop_explanation"/></p>
        <div id="slideshow">
          <div id="slidesContainer">
          </div>
          <a href="javascript:" class="btn btnStep2 floatR" id="publish_channel_set"><span><spring:message code="cms.channel_set_management.btn.pubish"/></span></a>
        </div>
      </div>
    </div>
    <div class="right_footer"></div>
  </div>
  <div class="clear"></div>
</div>
<!-- language tags for javascript -->
<label class="lang" id="lang_label_program_count"><spring:message code="cms.channel_set_management.label.program_count"/></label>
<label class="lang" id="lang_label_update_time"><spring:message code="cms.channel_set_management.label.update_time"/></label>
<label class="lang" id="lang_warning_select_category"><spring:message code="cms.warning.select_system_category"/></label>
<label class="lang" id="lang_warning_empty_name"><spring:message code="cms.warning.empty_name"/></label>
<label class="lang" id="lang_warning_empty_logo"><spring:message code="cms.warning.empty_logo"/></label>
<label class="lang" id="lang_warning_tag_over_limitation"><spring:message code="cms.warning.tag_over_limitation"/></label>
<label class="lang" id="lang_warning_name_over_limitation"><spring:message code="cms.warning.name_over_limitation"/></label>
<label class="lang" id="lang_warning_intro_over_limitation"><spring:message code="cms.warning.intro_over_limitation"/></label>
<label class="lang" id="lang_warning_error_occurs"><spring:message code="cms.warning.error_occurs"/></label>
<label class="lang" id="lang_update_successfully"><spring:message code="cms.warning.update_successfully"/></label>
</body>
</html>
