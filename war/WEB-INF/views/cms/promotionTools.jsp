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
<script type="text/javascript" src="/javascripts/cms-common.js"></script>
<script type="text/javascript" src="/javascripts/promotionTools.js"></script>
<script type=text/javascript>
	function setTab ( i )
	{
		selectTab(i);
	}
	
	function selectTab ( i )
	{
		if( i == 1){
			//case 1:
			get("pro_hint").style.display = "block";
			
			//break;
		} else if ( i == 2) {
			//case 2:
			get("pro_hint").style.display="none";
			//break;
			
		}
	}
	function get(id) {
		return document.getElementById(id);
	}

</script>
<title>推廣工具</title>
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
      <li><a href="#" class="menuE"></a></li>
    </ul>
    <div class="clear"></div>
    <div class="left_body">
      <div class="createChList2">
        <div class="left_title">頻道網清單</div>
        <ul class="chList">
          <li>
            <div class="chFocus">
              <div class="chFocusTitle">小灰熊的大愛劇場 <a href="#" class="btnDel"></a></div>
              <div class="chFocusImg"></div>
              <div class="floatL chInfo">
                <a href="#" class="floatL">http://www.9x9.tv/channel/3958</a>
                <div class="addthis_toolbox addthis_default_style floatL">
                  <a class="addthis_button_compact"></a>
                </div>
                <a href="#" class="iconStatistics" title="觀看數據"></a>
                <div class="clear"></div>
                <div class="floatL">
                  <p>頻道類型 : <span>9x9</span></p>
                  <p>節目數量 : <span>0</span></p>
                  <p>訂閱人數 : <span>0</span></p>
                  <p>更新時間 : <span>2011/04/15 14:45</span></p>
                </div>
                <ul class="floatL">
                  <li><a href="#" class="chUnPublic"></a></li>
                  <li><a href="#" class="btnGray"><span>頻道資訊</span></a></li>
                </ul>
                <div class="clear"></div>
              </div>
            </div>
          </li>
          <div class="clear"/>
        </ul>
        <div class="left_title">頻道清單</div>
        <ul class="chList">
          <li>
            <div class="chUnFocus">
              <div class="chUnFocusTitle">小灰熊的大愛劇場 <a href="#" class="btnDel"></a></div>
              <div class="chUnFocusImg"></div>
              <div class="floatL chInfo">
                <a href="#" class="floatL">http://www.9x9.tv/channel/3958</a>
                <div class="addthis_toolbox addthis_default_style floatL">
                  <a class="addthis_button_compact"></a>
                </div>
                <a href="#" class="iconStatistics" title="觀看數據"></a>
                <div class="clear"></div>
                <div class="floatL">
                  <p>頻道類型 : <span>9x9</span></p>
                  <p>節目數量 : <span>0</span></p>
                  <p>訂閱人數 : <span>0</span></p>
                  <p>更新時間 : <span>2011/04/15 14:45</span></p>
                </div>
                <ul class="floatL">
                  <li><a href="#" class="chUnPublic"></a></li>
                  <li><a href="#" class="btnGray"><span>頻道資訊</span></a></li>
                </ul>
                <div class="clear"></div>
              </div>
            </div>
          </li>
          <li>
            <div class="chUnFocus">
              <div class="chUnFocusTitle">小灰熊的大愛劇場 <a href="#" class="btnDel"></a></div>
              <div class="chUnFocusImg"></div>
              <div class="floatL chInfo" >
                <a href="#" class="floatL">http://www.9x9.tv/channel/3958</a>
                <div class="addthis_toolbox addthis_default_style floatL">
                  <a class="addthis_button_compact"></a>
                </div>
                <a href="#" class="iconStatistics" title="觀看數據"></a>
                <div class="clear"></div>
                <div class="floatL">
                  <p>頻道類型 : <span>9x9</span></p>
                  <p>節目數量 : <span>0</span></p>
                  <p>訂閱人數 : <span>0</span></p>
                  <p>更新時間 : <span>2011/04/15 14:45</span></p>
                </div>
                <ul class="floatL">
                  <li><a href="#" class="chUnPublic"></a></li>
                  <li><a href="#" class="btnGray"><span>頻道資訊</span></a></li>
                </ul>
                <div class="clear"></div>
              </div>
            </div>
          </li>
          <li>
            <div class="chUnFocus">
              <div class="chUnFocusTitle">小灰熊的大愛劇場 <a href="#" class="btnDel"></a></div>
              <div class="chUnFocusImg"></div>
              <div class="floatL chInfo">
                <a href="#" class="floatL">http://www.9x9.tv/channel/3958</a>
                <div class="addthis_toolbox addthis_default_style floatL">
                  <a class="addthis_button_compact"></a>
                </div>
                <a href="#" class="iconStatistics" title="觀看數據"></a>
                <div class="clear"></div>
                <div class="floatL">
                  <p>頻道類型 : <span>9x9</span></p>
                  <p>節目數量 : <span>0</span></p>
                  <p>訂閱人數 : <span>0</span></p>
                  <p>更新時間 : <span>2011/04/15 14:45</span></p>
                </div>
                <ul class="floatL">
                  <li><a href="#" class="chUnPublic"></a></li>
                  <li><a href="#" class="btnGray"><span>頻道資訊</span></a></li>
                </ul>
                <div class="clear"></div>
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
      <div class="right_title"><span>大愛電視頻道網</span></div>
      <div class="promote_title">
        <div class="floatL">自動分享&nbsp;&nbsp;</div>
        <a href="#" class="promoteInfo" onmouseover="setTab(1);" onmouseout="setTab(2);"></a>
        <div class="clear"></div>
        <div class="promote_hint" id="pro_hint">
          <ul>
            <li>若內容更新非常頻繁，使用同步發佈功能有可能造成您社群粉絲接收資訊上的困擾，請審慎設定。</li>
            <li>若頻道屬於頻道網，同時開啓頻道網同步與頻道同步，將造成單一頻道重複宣傳，請審慎設定。</li>
          </ul>
        </div>
      </div>
      <br/>
      <label class="pro_check"><input type="checkbox" name="CheckboxGroup1" value="checkbox" id="CheckboxGroup1_0" />&nbsp; facebook</label>
      <label class="pro_check"><input type="checkbox" name="CheckboxGroup1_" value="checkbox" id="CheckboxGroup1_1" />&nbsp; plurk</label>
      <label class="pro_check"><input type="checkbox" name="CheckboxGroup1_" value="checkbox" id="CheckboxGroup1_2" />&nbsp; twitter</label>
      <label class="pro_check"><input type="checkbox" name="CheckboxGroup1_" value="checkbox" id="CheckboxGroup1_3" />&nbsp; sina</label>
      <br/><br/><br/>
      <div class="promote_title">9x9 Video RSS&nbsp;&nbsp; <a href="#">使用教學</a></div>
      <br/><br/>
      <input type="text" size="40" />&nbsp;&nbsp;<a href="#">點擊複製</a>
    </div>
    <div class="right_footer"></div>
  </div>
  <div class="clear"></div>
</div>
</body>
</html>
