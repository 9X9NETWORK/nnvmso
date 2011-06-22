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
<script type="text/javascript" src="/javascripts/jquery.jqModal.js"></script>
<script type="text/javascript" src="http://www.netgrow.com.au/assets/files/jquery_plugins/jquery.dump.js"></script>
<script type="text/javascript" src="/javascripts/cms/common.js"></script>
<script type="text/javascript" src="/javascripts/cms/statistics.js"></script>
<title>統計數據</title>
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
      <li><a href="javascript:" class="menuC"></a></li>
      <li><a href="promotionTools" class="menuD"></a></li>
      <li><a href="statistics" class="menuE_active"></a></li>
    </ul>
    <div class="clear"></div>
    <div class="left_body">
      <div class="createChList2">
        <div class="left_title">頻道網清單</div>
        <ul class="chList" id="channel_set_list_ul">
        </ul>
        <div class="left_title">頻道清單</div>
        <ul class="chList" id="channel_list_ul">
          <li style="display:none">
            <div class="chUnFocus channel_info_block" id="channel_info_block">
              <div class="chUnFocusTitle channel_info_title"><div>小灰熊的大愛劇場</div></div>
              <div class="chUnFocusImg channel_info_image"></div>
              <div class="floatL chInfo">
                <a href="#" target="_player" class="floatL channel_info_promoteurl">http://www.9x9.tv/channel/3958</a>
                <a class="floatL channel_info_addthis"><img src="http://cache.addthiscdn.com/icons/v1/thumbs/addthis.gif"/></a>
                <a href="#" class="iconStatistics" title="觀看數據"></a>
                <div class="clear"></div>
                <div class="floatL">
                  <p class="channel_info_contenttype"><spring:message code="cms.channel_management.label.channel_type"/> : <span>9x9</span></p>
                  <p class="channel_info_programcount"><spring:message code="cms.channel_management.label.program_count"/> : <span>0</span></p>
                  <p class="channel_info_subscribers"><spring:message code="cms.channel_management.label.subscribers"/> : <span>0</span></p>
                  <p class="channel_info_updatedate"><spring:message code="cms.channel_management.label.update_time"/> : <span>2011/04/15 14:45</span></p>
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
      <div class="set_stastics" style="display:none" id="channel_set_statistics"><!--set stastics-->
        <div class="right_title"><div>大愛電視頻道網 - 套餐數據</div></div>
        <div class="stastics_title">
          <div>連續觀看15秒即算一次有效收看次數</div>
          <div class="datePick">請選擇時間區間&nbsp;<input id="set_pickdate" type="text"/><!-- <a href="#"><img alt="" src="/images/cms/icon_calendar.png"/></a> --></div>
          <div class="clear"></div>
        </div>
        <div class="stastics_chart">
          <select name="">
            <option>累計收看次數</option>
            <option>每次平均觀看次數</option>
            <option>訂閱戶數</option>
            <option>每次平均停留時間</option>
            <option>回訪率</option>
          </select>
          <img alt="數據圖表" src="/images/cms/img_stastics.png"/>
        </div>
        <table border="0" cellpadding="0" cellspacing="0" class="stastics_list">
          <tr>
            <td>1,057,201</td>
            <td><a href="javascript:">累計收看次數</a></td>
            <td>00:03:21</td>
            <td><a href="javascript:">每次平均停留時間</a></td>
          </tr>
          <tr>
            <td>3</td>
            <td><a href="javascript:">每次平均觀看次數</a></td>
            <td>20%</td>
            <td><a href="javascript:">回訪率</a></td>
          </tr>
          <tr>
            <td id="set_subscription_count">13,596</td>
            <td><a href="javascript:">訂閱戶數</a></td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
          </tr>
        </table>
      </div>
      <div class="ch_stastics" id="channel_statistics" style="display:none"><!--channel stastics-->
        <div class="right_title"><div>小灰熊的大愛劇場</div></div>
        <div class="stasticTab">
          <ul>
            <li id="stasticTabA" class="tab_focus"><a href="javascript:"">頻道數據</a></li>
            <li id="stasticTabB" class="tab_unfocus"><a href="javascript:">節目數據</a></li>
            <div class="clear"></div>
          </ul>
        </div>
        <div class="ch_stastics" id="ch_stastics">
          <div class="stastics_title">
            <div>連續觀看15秒即算一次有效收看次數</div>
            <div class="datePick">請選擇時間區間&nbsp;<input type="text"/><!-- <a href="#"><img alt="" src="/images/cms/icon_calendar.png"/></a> --></div>
            <div class="clear"></div>
          </div>
          <div class="stastics_chart">
            <select name="">
              <option>累計收看次數</option>
              <option>每次平均觀看次數</option>
              <option>訂閱戶數</option>
              <option>每次平均停留時間</option>
              <option>回訪率</option>
            </select>
            <img alt="數據圖表" src="/images/cms/img_stastics.png"/>
          </div>
          <table border="0" cellpadding="0" cellspacing="0" class="stastics_list">
            <tr>
              <td>1,057,201</td>
              <td><a href="javascript:">累計收看次數</a></td>
              <td>00:03:21</td>
              <td><a href="javascript:">每次平均停留時間</a></td>
            </tr>
            <tr>
              <td>3</td>
              <td><a href="javascript:">每次平均觀看次數</a></td>
              <td>20%</td>
              <td><a href="javascript:">回訪率</a></td>
            </tr>
            <tr>
              <td id="ch_subscription_count">13,596</td>
              <td><a href="javascript:">訂閱戶數</a></td>
              <td>&nbsp;</td>
              <td>&nbsp;</td>
            </tr>
          </table>
        </div>
        <div class="ep_stastics" id="ep_stastics" style="display:none">
          <div class="stastics_title">
            <div>連續觀看15秒即算一次有效收看次數</div>
            <div class="datePick">請選擇時間區間&nbsp;<input type="text"/><!-- <a href="#"><img alt="" src="/images/cms/icon_calendar.png"/></a> --></div>
            <div class="clear"></div>
          </div>
          <div class="stastics_chart">
            <select id="ep_selector">
              <option>請選擇節目</option>
              <option>小灰熊的大愛劇場1</option>
              <option>小灰熊的大愛劇場2</option>
            </select>&nbsp;&nbsp;&nbsp;
            <select name="">
              <option>累計收看次數</option>
              <option>分享次數</option>
              <option>每次平均收看時間</option>
            </select>
            <img alt="數據圖表" src="/images/cms/img_stastics.png"/>
          </div>
          <table border="0" cellpadding="0" cellspacing="0" class="stastics_list">
            <tr>
              <td>1,057,201</td>
              <td><a href="javascript:">累計收看次數</a></td>
              <td id="ep_share_count">3</td>
              <td><a href="javascript:">分享次數</a></td>
            </tr>
            <tr>
              <td>00:03:21</td>
              <td><a href="javascript:">每次平均收看時間</a></td>
              <td></td>
              <td></td>
            </tr>
          </table>
        </div>
      </div>
    </div>
    <div class="right_footer"></div>
  </div>
  <div class="clear"></div>
</div>
<label class="lang" id="lang_view_statistics"><spring:message code="cms.channel_management.msg.view_statistics"/></label>
<label class="lang" id="lang_label_channel_set">頻道網</label>
<label class="lang" id="lang_title_set_statistics">套餐數據</label>
<label class="lang" id="lang_label_please_select_program">請選擇節目</label>
</body>
</html>
