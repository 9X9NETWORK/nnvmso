<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="/stylesheets/cms.css" type="text/css"/>
<link rel="stylesheet" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.12/themes/start/jquery-ui.css" type="text/css" />
<link rel="stylesheet" href="/stylesheets/jquery.jqModal.css" type="text/css"/>
<script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#pubid=ra-4dcccc98718a5dbe"></script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.0/jquery.min.js"></script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.12/jquery-ui.min.js"></script>
<script type="text/javascript" src="/javascripts/jquery.textTruncate.js"></script>
<script type="text/javascript" src="/javascripts/jquery.jqModal.js"></script>
<script type="text/javascript" src="http://www.netgrow.com.au/assets/files/jquery_plugins/jquery.dump.js"></script>
<script type="text/javascript" src="/javascripts/swfupload/swfupload.js"></script>
<script type="text/javascript" src="/javascripts/cms-common.js"></script>
<script type="text/javascript" src="/javascripts/channelManagement.js"></script>
<title><spring:message code="cms.channel_management.title"/></title>
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
      <li><a href="javascript:" class="menuA_active"></a></li>
      <li><a href="channelSetManagement" class="menuB"></a></li>
      <li><a href="directoryManagement" class="menuC"></a></li>
      <li><a href="promotionTools" class="menuD"></a></li>
      <li><a href="#" class="menuE"></a></li>
    </ul>
    <div class="clear"></div>
    <div class="left_body">
      <!-- channel management - never create channel yet -->
      <div class="createCh" style="display:none" id="channel_list_empty">
        <div ><a href="javascript:" class="btnCreate create_channel_button"><spring:message code="cms.channel_management.btn.create_channel"/></a></div>
        <p><spring:message code="cms.channel_management.msg.empty_channel_list"/></p>
      </div>
      <!-- channel management - create ch step1 step2 -->
      <!--  if user create a channel, the "chShadow" will shows up at the place where the new channel placed during the create steps and disappear when the channel has been created-->
      <div class="createCh1" style="display:none">
        <div class="left_title">頻道清單</div>
        <div class="createChList">
          <div class="chShadow">
            <div class="chShadowTitle"></div>
            <div class="chImg"></div>
          </div>
        </div>
      </div>
      <!--more than one channel has been create -->
      <div class="createCh1" id="channel_list" style="display:none">
        <div class="left_title"><spring:message code="cms.channel_management.title.channel_list"/></div>
        <a href="javascript:" class="btnCreate create_channel_button"><spring:message code="cms.channel_management.btn.create_channel"/></a>
        <div class="createChList">
          <ul class="chList" id="channel_list_ul">
            <li style="display:none">
              <div class="chFocus">
                <div class="chFocusTitle">小灰熊的大愛劇場 <a href="javascript:" class="btnDel"></a></div>
                <div class="chFocusImg"></div>
                <div class="floatL chInfo">
                  <a href="#" class="floatL">http://www.9x9.tv/channel/3958</a>
                  <!-- AddThis Button BEGIN -->
                  <div class="addthis_toolbox addthis_default_style floatL">
                    <a class="addthis_button_compact"></a>
                  </div>
                  <!-- AddThis Button END -->
                  <a href="#" class="iconStatistics" title="觀看數據"></a>
                  <div class="clear"></div>
                  <div class="floatL">
                    <p>頻道類型 : <span>9x9</span></p>
                    <p>節目數量 : <span>0</span></p>
                    <p>訂閱人數 : <span>0</span></p>
                    <p>更新時間  : <span>2011/04/15 14:45</span></p>
                  </div>
                  <ul class="floatL">
                    <li><a href="#" class="chUnPublic"></a></li>
                    <li><a href="#" class="btnGray"><span>頻道資訊</span></a></li>
                  </ul>
                </div>
              </div>
            </li>
            <li style="display:none">
              <div class="chUnFocus channel_info_block" id="channel_info_block">
                <div class="chUnFocusTitle channel_info_title"><div>小灰熊的大愛劇場</div> <a href="javascript:" class="btnDel channel_info_removebutton"></a></div>
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
                    <li><a class="btnGray channel_info_detailbutton"><span><spring:message code="cms.channel_management.btn.channel_info"/></span></a></li>
                  </ul>
                </div>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
    <div class="left_footer"></div>
  </div>
  <div class="right_content floatL">
    <div class="right_body">
    <!-- channel management - create ch step1-->
    <div class="createChoose" style="display:none" id="choose_channel_type">
      <div class="right_title"><spring:message code="cms.channel_management.title.create_channel"/></div>
      <div class="createEpList">
        <div class="chStep1">
          <ul>
            <li class="createChItem">
              <div class="floatL createBtn"><a href="javascript:" id="create_9x9_channel_button" class="btn btnStep1"><span><spring:message code="cms.channel_management.btn.create_9x9_channel"/></span></a></div>
              <ul class="floatL createAbout">
                <spring:message code="cms.channel_management.msg.create_9x9_channel_benefit_list"/>
              </ul>
              <div class="clear"></div>
            </li>
            <li class="createChItem">
              <div class="floatL createBtn"><a href="javascript:" class="btn btnStep1 import_button"><span><spring:message code="cms.channel_management.btn.import_podcast_channel"/></span></a></div>
              <ul class="floatL createAbout">
                <spring:message code="cms.channel_management.msg.import_podcast_channel_benefit_list"/>
              </ul>
              <div class="clear"></div>
            </li>
            <li class="createChItem">
              <div class="floatL createBtn"><a href="javascript:" class="btn btnStep1 import_button"><span><spring:message code="cms.channel_management.btn.import_youtube_channel"/></span></a></div>
              <ul class="floatL createAbout">
                <spring:message code="cms.channel_management.msg.import_youtube_channel_benefit_list"/>
              </ul>
              <div class="clear"></div>
              <p class="hint"><spring:message code="cms.channel_management.msg.import_youtube_channel_hint"/></p>
            </li>
            <li class="createChItem">
              <div class="floatL createBtn"><a href="javascript:" class="btn btnStep1 import_button"><span><spring:message code="cms.channel_management.btn.import_youtube_playlist"/></span></a></div>
              <ul class="floatL createAbout">
                <spring:message code="cms.channel_management.msg.import_youtube_playlist_benefit_list"/>
              </ul>
              <div class="clear"></div>
              <p class="hint"><spring:message code="cms.channel_management.msg.import_youtube_playlist_hint"/></p>
            </li>
          </ul>
        </div>
      </div>
    </div>
    <!-- channel management - create ch step2 (create new channel)-->
    <div class="createChoose" style="display:none" id="channel_detail">
      <div class="right_title"><spring:message code="cms.channel_management.title.create_channel_info"/></div>
      <div class="createEpList">
        <div class="chStep2">
          <p class="hint_title"><span class="red">＊</span><spring:message code="cms.channel_management.msg.neccessary_info"/></p>
          <form>
            <fieldset class="setAlbum">
              <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_management.label.logo"/></label>
              <div class="uploadImg">
                <img id="ch_image" alt="" src="/images/cms/upload_img.jpg" class="floatL"/><input type="hidden" id="ch_image_updated" value="false"/>
                <div class="floatL imgBtn">
                  <p class="gray"><spring:message code="cms.channel_management.msg.best_resolution"/></p>
                  <span id="upload_button_place"><!--
                    <a href="#" id="ch_upload_image" class="uploadBtn"></a>
                    -->
                  </span>
                  <span id="ch_uploading" style="display:none"><spring:message code="cms.channel_management.msg.uploading"/></span>
                  <input type="hidden" id="s3_policy" value="${s3Policy}"/>
                  <input type="hidden" id="s3_signature" value="${s3Signature}"/>
                  <input type="hidden" id="s3_id" value="${s3Id}"/>
                </div>
                <div class="clear"></div>
              </div>
              <input type="hidden" id="ch_id" value="0"/>
              <div class="clear"></div><br/>
              <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_management.label.name"/></label>
              <div class="bg_input floatL"><input id="ch_name" type="text" size="25" maxlength="40"/></div>
              <div class="clear"></div>
              <p class="hint"><spring:message code="cms.channel_management.msg.fourty_characters"/></p>
              <br/>
              <label class="floatL"><spring:message code="cms.channel_management.label.introduction"/></label>
              <div class="bg_textarea floatL"><textarea id="ch_intro" name="" cols="33" rows="5"></textarea></div>
              <div class="clear"></div>
              <p class="hint"><spring:message code="cms.channel_management.msg.two_hundred_characters"/></p>
              <br/>
              <label class="floatL"><spring:message code="cms.channel_management.label.tag"/></label>
              <div class="bg_input floatL"><input id="ch_tag" type="text" size="25" maxlength="100" /></div>
              <div class="clear"></div>
              <p class="hint"><spring:message code="cms.channel_management.msg.camma_seperated"/></p>
              <br/>
              <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_management.label.system_category"/></label>
              <div class="floatL">
                <select name="" id="ch_category" class="sys_directory"></select>
              </div>
              <div class="clear"></div>
              <p class="hint"><spring:message code="cms.channel_management.msg.system_category_explanation"/></p>
              <div class="commitPlace">
                <a href="javascript:" class="btn btnStep2 floatL" id="channel_detail_savebutton"><span><spring:message code="cms.channel_management.btn.save"/></span></a><a href="javascript:" class="floatL btn_cancel" id="channel_detail_cancel"><span><spring:message code="cms.channel_management.btn.cancel"/></span></a>
              </div>
            </fieldset>
          </form>
        </div>
      </div>
    </div>
    <!-- channel management - create ch step3 end (created a new channel , choose to create episode)-->
    <div class="createEp" style="display:none" id="program_list_empty">
      <div class="right_title"><div class="floatL">小灰熊的大愛劇場</div> - <spring:message code="cms.channel_management.title.program_management"/></div>
      <div class="createEpList">
        <div class="createEpStep1">
          <div ><a href="javascript:" class="btnCreate create_program_button"><spring:message code="cms.channel_management.btn.create_program"/></a></div>
          <p><spring:message code="cms.channel_management.msg.empty_program_list"/></p>
        </div>
      </div>
    </div>
    <div class="createEp" style="display:none" id="program_list_empty_readonly">
      <div class="right_title"><div class="floatL">小灰熊的大愛劇場</div> - <spring:message code="cms.channel_management.title.program_management"/></div>
      <div class="createEpList">
        <div class="createEpStep1">
          <p><spring:message code="cms.channel_management.msg.empty_program_list_readonly"/></p>
        </div>
      </div>
    </div>
    <!-- channel management - create episode step1 (after press “”create channel button)
         1. class="addEpSection" please add vertical scrollbar on it, 
         2. class="addNew" this a tag will show up when the last uploadSection has already choosed a file to upload , and also fill up the episode title, if one of these did not satified  , the a tag wont show
         3. the button  "save"   will keep disable status (class="btnDisable" ), until the upload progress bar has been finished then switch to class="epSave"
         -->
    <div class="createEp" style="display:none" id="program_create_detail">
      <div class="right_title"><div class="floatL">小灰熊的大愛劇場</div> - <spring:message code="cms.channel_management.title.create_program"/> <a href="javascript:" class="floatR ep_return"><spring:message code="cms.channel_management.title.return_program_management"/>&nbsp;&gt;&gt;</a></div>
      <div class="createEpList">
        <p class="hint_title"><span class="red">＊</span><spring:message code="cms.channel_management.msg.neccessary_info"/></p>
        <ul class="addEpSection" id="program_create_ul">
          <li style="display:none">
            <div class="uploadSection program_create_detail_block" id="program_create_detail_block">
              <form>
                <fieldset class="setAlbum">
                  <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_management.label.upload_file"/></label>
                  <div class="floatL epImport ep_select_block">
                    <!-- 
                    <span class="btnCreate floatL ep_upload_video">從硬碟</span>
                    -->
                    <a href="javascript:" class="btnCreate floatL ep_urlbutton"><spring:message code="cms.channel_management.btn.from_url"/></a>
                    <span class="ep_upload_video">從硬碟</span>
                  </div>
                  <div style="width:100px;padding-left:5px" class="floatL ep_uploading_video" style="display:none">
                    <div></div>
                  </div>
                  <!--  upload by URL link -->
                  <div class="floatL uploadURL ep_url_block" style="display:none">
                    <div class="bg_input floatL"><input type="text" size="27" maxlength="100" class="ep_url_input"/></div>&nbsp;<a href="javascript:" class="ep_url_cancel"><spring:message code="cms.channel_management.btn.cancel"/></a>
                    <div class="clear"></div>
                    <p class="hint"><spring:message code="cms.channel_management.msg.video_url_hint"/></p><br/>
                  </div>
                  <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_management.label.program_logo"/></label>
                  <div class="uploadImg">
                    <img alt="" src="/images/cms/upload_img.jpg" class="floatL ep_image"/><input type="hidden" class="ep_image_updated" value="fase"/>
                    <div class="floatL imgBtn">
                      <p class="gray"><spring:message code="cms.channel_management.msg.best_resolution"/></p>
                      <a href="javascript:" class="uploadBtn ep_upload_image"></a>
                      <span class="ep_uploading_image" style="display:none"><spring:message code="cms.channel_management.msg.uploading"/></span>
                    </div>
                    <div class="clear"></div>
                  </div>
                  <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_management.label.program_name"/></label>
                  <div class="bg_input floatL"><input class="ep_name" type="text" size="25" maxlength="40"/></div>
                  <div class="clear"></div>
                  <p class="hint"><spring:message code="cms.channel_management.msg.fourty_characters"/></p>
                  <br/>
                  <label class="floatL"><spring:message code="cms.channel_management.label.program_introduction"/></label>
                  <div class="bg_textarea floatL"><textarea class="ep_intro" name="" cols="33" rows="5"></textarea></div>
                  <div class="clear"></div>
                  <p class="hint"><spring:message code="cms.channel_management.msg.two_hundred_characters"/></p>
                  <br/>
                  <!-- 
                  <label class="floatL"><spring:message code="cms.channel_management.label.tag"/></label>
                  <div class="bg_input floatL"><input type="text" size="35" maxlength="40"/></div>
                  <div class="clear"></div>
                  <p class="hint"><spring:message code="cms.channel_management.msg.camma_seperated"/></p><br/>
                  -->
                  <div class="epBtns" >
                    <a href="javascript:" class="btnDisable floatL ep_savebutton"><spring:message code="cms.channel_management.btn.save"/></a>
                    <a href="javascript:" class="btnCancel floatL ep_cancelbutton"><spring:message code="cms.channel_management.btn.cancel"/></a>
                  </div>
                </fieldset>
              </form>
            </div>
          </li>
        </ul>
        <a href="javascript:" class="addNew" id="continue_add_new_program_button"><spring:message code="cms.channel_management.btn.continue_creating_program"/></a>
      </div>
    </div>
    <!-- upload channel = 9x9 channel s episode list
         1. first li is normal status , second is active status , when user click a  li, switch status to this "epItemFocus"  and epItemFocusTitle than switch into chInfo div
         -->
    <div class="epList" style="display:none" id="program_list">
      <div class="right_title"><div class="floatL">小灰熊的大愛劇場</div> - <spring:message code="cms.channel_management.title.program_management"/> </div>
      <div class="createEpList2">
        <a class="btnCreate create_program_button" href="javascript:"><spring:message code="cms.channel_management.btn.create_program"/></a>
        <ul id="program_list_ul">
          <li style="display:none">
            <div class="epItem program_info_block" id="program_info_block">
              <div class="epInfoTitle program_info_title"><div>何處是我家</div> <a href="javascript:" class="btnDel program_info_removebutton"></a></div>
              <div class="epInfoImg program_info_image"></div>
              <div class="floatL epInfo">
                <a href="#" class="floatL program_info_promoteurl">http://www.9x9.tv/channel/3958</a>
                <a class="floatL program_info_addthis"><img src="http://cache.addthiscdn.com/icons/v1/thumbs/addthis.gif"/></a>
                <a href="#" class="iconStatistics" title="觀看數據"></a>
                <div class="clear"></div>
                <p class="program_info_type"><spring:message code="cms.channel_management.label.program_type"/> : <span>9x9</span></p>
                <p class="program_info_updatedate"><spring:message code="cms.channel_management.label.update_time"/> : <span>2011/04/15 14:45</span></p>
              </div>
              <ul class="floatL">
                <li><a class="chUnPublic program_info_publish"></a></li>
                <li><a class="btnGray program_info_detailbutton"><span><spring:message code="cms.channel_management.btn.program_info"/></span></a></li>
              </ul>
              <div class="clear"></div>
            </div>
          </li>
          <li style="display:none"><!-- not used -->
            <div class="epItemFocus">
              <div class="epItemFocusTitle">何處是我家 <a href="#" class="btnDel"></a></div>
              <div class="epInfoImg"></div>
              <div class=" floatL epInfo" >
                <a href="#" class="floatL">http://www.9x9.tv/channel/3958</a>
                <div class="addthis_toolbox addthis_default_style floatL">
                  <a class="addthis_button_compact"></a>
                </div>
                <a href="#" class="iconStatistics" title="觀看數據"></a>
                <div class="clear"></div>
                <p>頻道類型 : <span>9x9</span></p>
                <p>節目數量 : <span>0</span></p>
                <p>訂閱人數 : <span>0</span></p>
                <p>更新時間 : <span>2011/04/15 14:45</span></p>
              </div>
              <div class="clear"></div>
            </div>
          </li>
        </ul>
      </div>
    </div>
    <!-- upload channel  = 9x9 channel episode information -->
    <div class="createChoose" style="display:none" id="program_detail">
      <div class="right_title"><div class="floatL">小灰熊的大愛劇場</div> - <spring:message code="cms.channel_management.title.program_info"/><a href="javascript:" class="floatR ep_return"><spring:message code="cms.channel_management.title.return_program_management"/>&nbsp;&gt;&gt;</a></div>
      <div class="createEpList">
        <div class="chStep2">
          <p class="hint_title"><span class="red">＊</span><spring:message code="cms.channel_management.msg.neccessary_info"/></p>
          <form>
            <fieldset class="setAlbum">
              <label class="floatL"><spring:message code="cms.channel_management.label.program_source"/></label>
              <p class="ep_source"><a target="_player" href="javascript:">9x9</a></p><br/>
              <label class="floatL"><spring:message code="cms.channel_management.label.program_url"/></label>
              <p><a target="_player" href="#" class="ep_url">http://www.9x9.tv/share/3958</a><p><br/>
              <label class="floatL"><spring:message code="cms.channel_management.label.create_time"/></label>
              <p class="ep_createdate">2011/04/25 14:30</p><br/>
              <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_management.label.logo"/></label>
              <div class="uploadImg">
                <img alt="" src="/images/cms/upload_img.jpg" class="floatL ep_image"/><input type="hidden" class="ep_image_updated" value="fase"/>
                <div class="floatL imgBtn">
                  <p class="gray"><spring:message code="cms.channel_management.msg.best_resolution"/></p>
                  <span class="upload_button_place"><!-- 
                  <a href="javascript:" class="uploadBtn ep_upload_image"></a>
                  --></span>
                  <span style="display:none" class="ep_uploading_image"><spring:message code="cms.channel_management.msg.uploading"/></span>
                </div>
                <div class="clear"></div>
              </div>
              <div class="clear"></div><br/>
              <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_management.label.name"/></label>
              <div class="bg_input floatL">
                <input type="text" size="27" maxlength="40" class="ep_name"/>
              </div>
              <div class="clear"></div>
              <p class="hint"><spring:message code="cms.channel_management.msg.fourty_characters"/></p>
              <br/>
              <label class="floatL"><spring:message code="cms.channel_management.label.introduction"/></label>
              <div class="bg_textarea floatL"><textarea name="" cols="30" rows="5" class="ep_intro"></textarea></div>
              <div class="clear"></div>
              <p class="hint"><spring:message code="cms.channel_management.msg.two_hundred_characters"/></p>
              <br/><!-- 
              <label class="floatL"><spring:message code="cms.channel_management.label.tag"/></label>
              <div class="bg_input floatL"><input type="text" size="40" maxlength="40" disabled="disabled"/></div>
              <div class="clear"></div>
              <p class="hint"><spring:message code="cms.channel_management.msg.camma_seperated"/></p>
              <br/>
              <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_management.label.system_category"/></label>
              <div class="floatL">
                <select name="" class="sys_directory" disabled="disabled">
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
              <p class="hint"><spring:message code="cms.channel_management.msg.system_category_explanation"/></p>
              -->
              <div class="commitPlace">
                <a href="javascript:" class="btn btnStep2 floatL ep_savebutton"><span><spring:message code="cms.channel_management.btn.save"/></span></a><a href="javascript:" class="floatL btn_cancel ep_cancel"><span><spring:message code="cms.channel_management.btn.cancel"/></span></a>
              </div>
            </fieldset>
          </form>
        </div>
      </div>
    </div>
    <!-- the channel is create by import other content source -->
    <div class="epList" style="display:none" id="program_list_readonly">
      <div class="right_title"><div class="floatL">小灰熊的大愛劇場</div> - <spring:message code="cms.channel_management.title.program_management"/> </div>
      <div class="createEpList">
        <ul id="program_list_ul_readonly">
          <li style="display:none">
            <div class="epItem program_info_block_readonly" id="program_info_block_readonly">
              <div class="epInfoTitle program_info_title"><span>何處是我家 </span></div>
              <div class="epInfoImg program_info_image"></div>
              <div class=" floatL epInfo" >
                <a href="#" class="floatL program_info_promoteurl">http://www.9x9.tv/channel/3958</a>
                <a class="floatL program_info_addthis"><img src="http://cache.addthiscdn.com/icons/v1/thumbs/addthis.gif"/></a>
                <a href="#" class="iconStatistics" title="觀看數據"></a>
                <div class="clear"></div>
                <p class="program_info_type"><spring:message code="cms.channel_management.label.program_type"/> : <span>9x9</span></p>
                <p class="program_info_updatedate"><spring:message code="cms.channel_management.label.update_time"/> : <span>2011/04/15 14:45</span></p>
              </div>
              <ul class="floatL">
                <li><a class="chUnPublic program_info_publish"></a></li>
                <li><a class="btnGray program_info_detailbutton"><span><spring:message code="cms.channel_management.btn.program_info"/></span></a></li>
              </ul>
              <div class="clear"></div>
            </div>
          </li>
          <li style="display:none"><!-- not used -->
            <div class="epItemFocus">
              <div class="epItemFocusTitle">何處是我家 </div>
              <div class="epInfoImg"></div>
              <div class=" floatL epInfo">
                <a href="#" class="floatL">http://www.9x9.tv/channel/3958</a>
                <div class="addthis_toolbox addthis_default_style floatL">
                  <a class="addthis_button_compact"></a>
                </div>
                <a href="#" class="iconStatistics" title="觀看數據"></a>
                <div class="clear"></div>
                <p>頻道類型 : <span>9x9</span></p>
                <p>節目數量 : <span>0</span></p>
                <p>訂閱人數 : <span>0</span></p>
                <p>更新時間  : <span>2011/04/15 14:45</span></p>
              </div>
              <div class="clear"></div>
            </div>
          </li>
        </ul>
      </div>
    </div>
    <!-- the channel is create by import other content source  - episode info -->			
    <div class="createChoose" style="display:none" id="program_detail_readonly">
      <div class="right_title"><div class="floatL">小灰熊的大愛劇場</div> - <spring:message code="cms.channel_management.title.program_info"/><a href="javascript:" class="floatR ep_return"><spring:message code="cms.channel_management.title.return_program_management"/>&nbsp;&gt;&gt;</a></div>
      <div class="createEpList">
        <div class="chStep2">
          <p class="hint_title"></p>
          <form>
            <fieldset class="setAlbum">
              <label class="floatL"><spring:message code="cms.channel_management.label.program_source"/></label>
              <p><a target="_player" class="ep_source" href="#">http://blip.tv/file/get/Qtv-JulianSchnabelOnQTV732.m4v</a></p><br/><br/>
              <label class="floatL"><spring:message code="cms.channel_management.label.program_url"/></label>
              <p><a target="_player" class="ep_url" href="#">http://www.9x9.tv/share/3958</a><p><br/><br/>
              <label class="floatL"><spring:message code="cms.channel_management.label.create_time"/></label>
              <p class="ep_createdate">2011/04/25 14:30</p><br/><br/>
              <label class="floatL"><spring:message code="cms.channel_management.label.logo"/></label>
              <div class="uploadImg">
                <img class="ep_image" alt="" src="/images/cms/upload_img.jpg" />
              </div>
              <div class="clear"></div><br/><br/>
              <label class="floatL"><spring:message code="cms.channel_management.label.name"/></label>
              <p class="floatL ep_name">20110326《慈濟新聞深度報導》十噸賑災愛心物資 親手送給日本災民</p><br/><br/>
              <div class="clear"></div>
              <br/>
              <label class="floatL"><spring:message code="cms.channel_management.label.introduction"/></label>
              <p class="floatL ep_intro">慈濟前往日本福島地區協助災民重建工作</p><br/><br/>
              <div class="clear"></div>
            </fieldset>
          </form>
        </div>
      </div>
    </div>
    <!-- create channel by import podcast / youtube channel / youtube playlist-->
    <div class="createChoose" style="display:none" id="channel_import_detail">
      <div class="right_title"><spring:message code="cms.channel_management.title.create_channel_info"/></div>
      <div class="createEpList">
        <div class="chStep2">
          <p class="hint_title"><span class="red">＊</span><spring:message code="cms.channel_management.msg.neccessary_info"/></p>
          <form>
            <fieldset class="setAlbum">
              <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_management.label.channel_source"/></label>
              <div class="bg_input floatL"><input type="text" size="25" maxlength="100" name="ch_import_url"/></div>
              <a href="javascript:" class="btnCreate floatL" name="ch_import_button"><spring:message code="cms.channel_management.btn.import"/></a>
              <div class="clear"></div><br/>
              <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_management.label.logo"/></label>
              <div class="uploadImg">
                <img name="ch_image" alt="" src="/images/cms/upload_img.jpg" class="floatL"/><input type="hidden" name="ch_image_updated" value="false"/>
                <div class="floatL imgBtn">
                  <p class="gray"><spring:message code="cms.channel_management.msg.best_resolution"/></p>
                  <span name="upload_button_place">
                    <a href="javascript:" class="uploadBtn"></a>
                  </span>
                  <span name="ch_uploading_image" style="display:none"><spring:message code="cms.channel_management.msg.uploading"/></span>
                </div>
                <div class="clear"></div>
              </div>
              <div class="clear"></div><br/>
              <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_management.label.name"/></label>
              <div class="bg_input floatL"><input name="ch_name" type="text" size="25" maxlength="40"/></div>
              <div class="clear"></div>
              <p class="hint"><spring:message code="cms.channel_management.msg.fourty_characters"/></p>
              <br/>
              <label class="floatL"><spring:message code="cms.channel_management.label.introduction"/></label>
              <div class="bg_textarea floatL"><textarea name="ch_intro" cols="30" rows="5"></textarea></div>
              <div class="clear"></div>
              <p class="hint"><spring:message code="cms.channel_management.msg.two_hundred_characters"/></p>
              <br/><!--
              <label class="floatL"><spring:message code="cms.channel_management.label.tag"/></label>
              <div class="bg_input floatL"><input type="text" size="40" maxlength="40"/></div>
              <div class="clear"></div>
              <p class="hint"><spring:message code="cms.channel_management.msg.camma_seperated"/></p>
              <br/>
              -->
              <label class="floatL"><span class="red">＊</span><spring:message code="cms.channel_management.label.system_category"/></label>
              <div class="floatL">
                <select name="ch_category" class="sys_directory">
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
              <p class="hint"><spring:message code="cms.channel_management.msg.system_category_explanation"/></p>
              <div class="commitPlace">
                <a href="javascript:" name="ch_savebutton" class="btn btnStep2 floatL"><span><spring:message code="cms.channel_management.btn.save"/></span></a><a href="javascript:" name="ch_cancelbutton" class="floatL btn_cancel"><span><spring:message code="cms.channel_management.btn.cancel"/></span></a>
              </div>
            </fieldset>
          </form>
        </div>
      </div>
    </div>
  </div>
  <div class="right_footer"></div>
  <div class="clear"></div>
</div>
<!-- language tags for javascript -->
<label class="lang" id="lang_view_statistics"><spring:message code="cms.channel_management.msg.view_statistics"/></label>
<label class="lang" id="lang_upload_finished"><spring:message code="cms.warning.upload_finished"/></label>
<label class="lang" id="lang_upload_failed"><spring:message code="cms.warning.upload_failed"/></label>
<label class="lang" id="lang_confirm_removing_program"><spring:message code="cms.warning.confirm_removing_program"/></label>
<label class="lang" id="lang_channel_source_is_empty"><spring:message code="cms.warning.channel_source_is_empty"/></label>
<label class="lang" id="lang_select_category"><spring:message code="cms.channel_management.msg.select_category"/></label>
<label class="lang" id="lang_confirm_removing_channel"><spring:message code="cms.warning.confirm_removing_channel"/></label>
<label class="lang" id="lang_warning_empty_name"><spring:message code="cms.warning.empty_name"/></label>
<label class="lang" id="lang_warning_error_occurs"><spring:message code="cms.warning.error_occurs"/></label>
<label class="lang" id="lang_update_successfully"><spring:message code="cms.warning.update_successfully"/></label>
<label class="lang" id="lang_warning_select_category"><spring:message code="cms.warning.select_system_category"/></label>
<label class="lang" id="lang_confirm_cancel"><spring:message code="cms.warning.confirm_cancel"/></label>
<label class="lang" id="lang_confirm_leaving_program_creation"><spring:message code="cms.warning.confirm_leaving_program_creation"/></label>
<label class="lang" id="lang_warning_import_channel_source"><spring:message code="cms.warning.import_channel_source"/></label>
<label class="lang" id="lang_channel_source_is_wrong"><spring:message code="cms.warning.channel_source_is_wrong"/></label>
</body>
</html>
