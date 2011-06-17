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
<script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#pubid=ra-4dcccc98718a5dbe"></script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.0/jquery.min.js"></script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.12/jquery-ui.min.js"></script>
<script type="text/javascript" src="/javascripts/jquery.bubble.js"></script>
<script type="text/javascript" src="/javascripts/jquery.textTruncate.js"></script>
<script type="text/javascript" src="/javascripts/jquery.jstree.js"></script>
<script type="text/javascript" src="http://www.netgrow.com.au/assets/files/jquery_plugins/jquery.dump.js"></script>
<script type="text/javascript" src="/javascripts/cms-common.js"></script>
<script type="text/javascript" src="/javascripts/directoryManagement.js"></script>
<title>目錄管理</title>
</head>
<body>
<div class="header">
  <input type="hidden" id="msoId" value="${msoId}"/>
  <div class="floatL"><img alt="" src="${msoLogo}"/></div>
  <div class="floatR">
    <p><spring:message code="cms.header.welcome"/> <span><c:out value="${mso.contactEmail}"/></span></p>
    <a href="#" class="setup"></a>
    <a href="${logoutUrl}" class="logout"></a>
  </div>
</div>
<div class="content">
  <div class="left_content floatL">
    <ul class="menu">
      <li><a href="channelManagement" class="menuA"></a></li>
      <li><a href="channelSetManagement" class="menuB"></a></li>
      <li><a href="javascript:" class="menuC_active"></a></li>
      <li><a href="#" class="menuD"></a></li>
      <li><a href="#" class="menuE"></a></li>
    </ul>
    <div class="clear"></div>
    <div class="left_body">
      <p class="ch_sub_title"><span>9x9提供您一個可以自訂的品牌目錄，讓您能夠將所擁有的頻道與頻道網收錄在您自有的目錄中，並被觀眾在9x9.tv瀏覽目錄時看到</span></p>
      <p>
        <button id="btn_delete_directory">刪除目錄</button>
        <button id="btn_create_directory">創建目錄</button>
        <button id="btn_rename_directory">重新命名</button>
      </p>
      <div class="directoryList" id="treeview"><!--
        <ul>
          <li id="root" rel="folder">
            <a href="javascript:">大愛電視目錄</a>
            <ul>
              <li id="ch1" rel="file">
                <a href="javascript:">大愛醫生館</a>
              </li>
              <li id="ch2">
                <a href="javascript:">大愛劇場</a>
                <ul>
                  <li rel="set">
                    <a href="javascript:">芳草碧連天</a>
                  </li>
                  <li>
                    <a href="javascript:">有情飲水飽</a>
                  </li>
                </ul>
              </li>
            </ul>
          </li>
        </ul>-->
      </div>
    </div>
    <div class="left_footer"></div>
  </div>
  <div class="right_content floatL">
    <div class="right_body">
      <div class="right_title">頻道網與頻道庫</div>
      <div class="ch_pool">
        <p class="ch_sub_title"><span>系統會自動將您所建立的頻道與頻道套餐收錄至您的根目錄中，您可以在左側新增子目錄後，自行拖曳頻道或頻道套餐，重新進行編排</span></p>
        <div class="directoryChList jstree-drop">
          <ul class="directory_ch" id="directory_list_ul">
            <!--
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <li class="ch_normal">
              <img alt="" src="/images/cms/ch_img.jpg"/>
              <p class="ch_name">頻道名稱名...</p>
            </li>
            <div style="clear:both"/>
            -->
          </ul>
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
</body>
</html>
