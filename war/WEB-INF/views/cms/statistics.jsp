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
<script type="text/javascript" src="/javascripts/jquery.jstree.js"></script>
<script type="text/javascript" src="http://www.netgrow.com.au/assets/files/jquery_plugins/jquery.dump.js"></script>
<script type="text/javascript" src="/javascripts/cms-common.js"></script>
<script type="text/javascript" src="/javascripts/statistics.js"></script>
<title>統計數據</title>
<script type=text/javascript>
	function setTab ( i )
	{
		stasticTab(i);
	}
	
	function stasticTab ( i )
	{
		if( i == 1){
			//case 1:
			get("ch_stastics").style.display = "block";
			get("ep_stastics").style.display = "none";
			get("stasticTabA").className = "tab_focus";
			get("stasticTabB").className = "tab_unfocus";
			
			//break;
		} else if ( i == 2) {
			//case 2:
			get("ch_stastics").style.display="none";
			get("ep_stastics").style.display = "block";
			get("stasticTabA").className = "tab_unfocus";
			get("stasticTabB").className = "tab_focus";
			//break;
			
		}
	}
	function get(id) {
		return document.getElementById(id);
	}

</script>
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
      <div class="set_stastics" style="display:none"><!--set stastics-->
        <div class="right_title"><span>大愛電視頻道網 - 套餐數據</span></div>
        <div class="stastics_title">
          <div>連續觀看15秒即算一次有效收看次數</div>
          <div class="datePick">請選擇時間區間&nbsp;<input type="text"/><a href="#"><img alt="" src="/images/cms/icon_calendar.png"/></a></div>
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
            <td><a href="#">累計收看次數</a></td>
            <td>00:03:21</td>
            <td><a href="#">每次平均停留時間</a></td>
          </tr>
          <tr>
            <td>3</td>
            <td><a href="#">每次平均觀看次數</a></td>
            <td>20%</td>
            <td><a href="#">回訪率</a></td>
          </tr>
          <tr>
            <td>13,596</td>
            <td><a href="#">訂閱戶數</a></td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
          </tr>
        </table>
      </div>
      <div class="ch_stastics"><!--channel stastics-->
        <div class="right_title"><span>小灰熊的大愛劇場</span></div>
        <div class="stasticTab">
          <ul>
            <li id="stasticTabA" class="tab_focus"><a href="#"  onclick="stasticTab(1);">頻道數據</a></li>
            <li id="stasticTabB" class="tab_unfocus"><a href="#"  onclick="stasticTab(2);">節目數據</a></li>
            <div class="clear"></div>
          </ul>
        </div>
        <div class="ch_stastics" id="ch_stastics">
          <div class="stastics_title">
            <div>連續觀看15秒即算一次有效收看次數</div>
            <div class="datePick">請選擇時間區間&nbsp;<input type="text"/><a href="#"><img alt="" src="/images/cms/icon_calendar.png"/></a></div>
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
              <td><a href="#">累計收看次數</a></td>
              <td>00:03:21</td>
              <td><a href="#">每次平均停留時間</a></td>
            </tr>
            <tr>
              <td>3</td>
              <td><a href="#">每次平均觀看次數</a></td>
              <td>20%</td>
              <td><a href="#">回訪率</a></td>
            </tr>
            <tr>
              <td>13,596</td>
              <td><a href="#">訂閱戶數</a></td>
              <td>&nbsp;</td>
              <td>&nbsp;</td>
            </tr>
          </table>
        </div>
        <div class="ep_stastics" id="ep_stastics" style="display:none">
          <div class="stastics_title">
            <div>連續觀看15秒即算一次有效收看次數</div>
            <div class="datePick">請選擇時間區間&nbsp;<input type="text"/><a href="#"><img alt="" src="/images/cms/icon_calendar.png"/></a></div>
            <div class="clear"></div>
          </div>
          <div class="stastics_chart">
            <select name="">
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
              <td><a href="#">累計收看次數</a></td>
              <td>3</td>
              <td><a href="#">分享次數</a></td>
            </tr>
            <tr>
              <td>00:03:21</td>
              <td><a href="#">每次平均收看時間</a></td>
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
</body>
</html>
