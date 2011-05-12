<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link href="/stylesheets/jquery.bubble.css" rel="stylesheet" type="text/css"/>
<link href="/stylesheets/cms-doc.css" rel="stylesheet" type="text/css"/>
<link href="/stylesheets/cms.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.0/jquery.min.js"></script>
<script type="text/javascript" src="/javascripts/jquery.bubble.js"></script>
<script type="text/javascript" src="/javascripts/cms.js"></script>
<title>Insert title here</title>
</head>
<body>
<div class="header">
  <div class="floatL"><img alt="" src="${mso.logoUrl}"/></div>
  <div class="floatR">
    <p><spring:message code="cms.header.welcome"/> <span><c:out value="${mso.contactEmail}"/></span></p>
    <a href="#" class="setup"></a>
    <a href="#" class="logout"></a>
  </div>
</div>
<div class="content">
  <div class="left_content floatL">
    <ul>
      <li><a href="#" class="menuA"></a></li>
      <li><a href="#" class="menuB_active"></a></li>
      <li><a href="#" class="menuC"></a></li>
      <li><a href="#" class="menuD"></a></li>
      <li><a href="#" class="menuE"></a></li>
    </ul>
    <div class="clear"></div>
    <div class="left_body">
      <div class="left_title">頻道網資訊</div>
      <p class="hint_title"><span class="red">＊</span>為必填資訊</p>
      <form>
        <fieldset class="setAlbum">
          <label class="floatL"><span class="red">＊</span>圖示</label>
          <div class="uploadImg">
            <img alt="" src="/images/cms/upload_img.jpg" class="floatL"/>
            <div class="floatL imgBtn">
              <p class="gray">多螢幕最佳顯示品質建議<br/>解析度至少為720x480</p>
              <a href="#" class="uploadBtn"></a>
            </div>
            <div class="clear"></div>
          </div>
          <label class="floatL">網址</label>
          <a href="#" class="floatL">http://www.9x9.tv/daai</a>
          <!-- AddThis Button BEGIN -->
          <div class="addthis_toolbox addthis_default_style floatL">
            <a class="addthis_button_compact"></a>
          </div>
          <!-- AddThis Button END -->
          <div class="clear"></div><br/>
          <label class="floatL"><span class="red">＊</span>名稱</label>
          <div class="bg_input floatL"><input type="text" size="50" maxlength="40"/></div>
          <div class="clear"></div>
          <p class="hint">限40字元</p>
          <br/>
          <label class="floatL">介紹</label>
          <div class="bg_textarea floatL"><textarea name="" cols="37" rows="4"></textarea></div>
          <div class="clear"></div>
          <p class="hint">限200字元</p>
          <br/>
          <label class="floatL">標籤</label>
          <div class="bg_input floatL"><input type="text" size="50" maxlength="40"/></div>
          <div class="clear"></div>
          <p class="hint">請用“ , ”分開</p>
          <br/>
          <label class="floatL"><span class="red">＊</span>系統分類</label>
          <div class="floatL">
            <select name="" class="sys_directory">
              <option selected="selected">請選擇分類</option>
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
            </select>
          </div>
          <div class="clear"></div>
          <p class="hint">選擇分類後，頻道網將被收錄至9x9.tv的系統目錄中，供觀眾瀏覽，您也可以至「目錄管理」編輯您自訂的目錄，匯錄您的所有內容。</p>
        </fieldset>
      </form>
    </div>
    <div class="left_footer"></div>
  </div>
  <div class="right_content floatL">
    <div class="right_body">
      <div class="right_title">編排頻道網</div>
        <div class="ch_arrange">
          <div class="ch_bg">
            <ul>
              <li class="ch_none ch_exsist"></li>
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
          <p class="ch_sub_title">請選擇頻道拖曳至頻道網<span>將頻道拖曳回來即可從頻道網中移除</span></p>
          <div id="slideshow">
            <div id="slidesContainer">
              <div class="slide">
                <ul>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <div style="clear:both"/>
                </ul>
              </div>
              <div class="slide">
                <ul>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <div style="clear:both"/>
                </ul>
              </div>
              <div class="slide">
                <ul>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <li class="ch_normal">
                    <img alt="" src="/images/cms/ch_img.jpg"/>
                    <p class="ch_name">頻道名稱名...</p><!--10 letters , if  channel name is more than 10 letters, please use "..." -->
                  </li>
                  <div style="clear:both"/>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="right_footer"></div>
    </div>
  <div class="clear"></div>
</div>
</body>
</html>
