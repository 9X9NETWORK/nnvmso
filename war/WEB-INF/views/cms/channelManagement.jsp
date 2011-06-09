<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link href="/stylesheets/cms.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#pubid=ra-4dcccc98718a5dbe"></script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.0/jquery.min.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.12/jquery-ui.min.js"></script>
<script type="text/javascript" src="/javascripts/jquery.textTruncate.js"></script>
<script type="text/javascript" src="/javascripts/channelManagement.js"></script>
<script type="text/javascript" src="http://www.netgrow.com.au/assets/files/jquery_plugins/jquery.dump.js"></script>
<script type="text/javascript" src="/javascripts/swfupload/swfupload.js"></script>
<title>頻道管理</title>
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
      <li><a href="#" class="menuA_active"></a></li>
      <li><a href="channelSetManagement" class="menuB"></a></li>
      <li><a href="#" class="menuC"></a></li>
      <li><a href="#" class="menuD"></a></li>
      <li><a href="#" class="menuE"></a></li>
    </ul>
    <div class="clear"></div>
    <div class="left_body">
      <!-- channel management - never create channel yet -->
      <div class="createCh" style="display:none" id="channel_list_empty">
        <div ><a href="javascript:" class="btnCreate create_channel_button">建立頻道</a></div>
        <p>您尚無任何頻道<br/>迅速建立頻道並新增節目，即可立即行銷您的影視內容</p>
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
        <div class="left_title">頻道清單</div>
        <a href="javascript:" class="btnCreate create_channel_button">建立頻道</a>
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
                <div class="chUnFocusTitle channel_info_title"><span>小灰熊的大愛劇場</span> <a href="javascript:" class="btnDel channel_info_removebutton"></a></div>
                <div class="chUnFocusImg channel_info_image"></div>
                <div class="floatL chInfo">
                  <a href="#" target="_player" class="floatL channel_info_promoteurl">http://www.9x9.tv/channel/3958</a>
                  <a class="floatL channel_info_addthis"><img src="http://cache.addthiscdn.com/icons/v1/thumbs/addthis.gif"/></a>
                  <a href="#" class="iconStatistics" title="觀看數據"></a>
                  <div class="clear"></div>
                  <div class="floatL">
                    <p class="channel_info_contenttype">頻道類型 : <span>9x9</span></p>
                    <p class="channel_info_programcount">節目數量 : <span>0</span></p>
                    <p class="channel_info_subscribers">訂閱人數 : <span>0</span></p>
                    <p class="channel_info_updatedate">更新時間 : <span>2011/04/15 14:45</span></p>
                  </div>
                  <ul class="floatL">
                    <li><a class="chUnPublic channel_info_publish"></a></li>
                    <li><a class="btnGray channel_info_detailbutton"><span>頻道資訊</span></a></li>
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
      <div class="right_title">頻道管理 - 建立頻道</div>
      <div class="createEpList">
        <div class="chStep1">
          <ul>
            <li class="createChItem">
              <div class="floatL createBtn"><a href="javascript:" id="create_9x9_channel_button" class="btn btnStep1"><span>建立全新9x9頻道</span></a></div>
              <ul class="floatL createAbout">
                <li>轉檔、儲存、管理、行銷</li>
                <li>絕佳的播放速度</li>
                <li>雲端多螢幕播放處理</li>
                <li>進階的自訂化管理功能</li>
                <li>現在完全免費!</li>
              </ul>
              <div class="clear"></div>
            </li>
            <li class="createChItem">
              <div class="floatL createBtn"><a href="#" class="btn btnStep1"><span>匯入Podcast</span></a></div>
              <ul class="floatL createAbout">
                <li>直接同步既有Podcast</li>
                <li>絕佳的播放速度</li>
                <li>基本的管理功能</li>
              </ul>
              <div class="clear"></div>
            </li>
            <li class="createChItem">
              <div class="floatL createBtn"><a href="#" class="btn btnStep1"><span>匯入YouTube 頻道</span></a></div>
              <ul class="floatL createAbout">
                <li>直接同步既有YouTube頻道</li>
                <li>基本的管理功能</li>
              </ul>
              <div class="clear"></div>
              <p class="hint">範例 www.youtube.com/user/MIT</p>
            </li>
            <li class="createChItem">
              <div class="floatL createBtn"><a href="#" class="btn btnStep1"><span>匯入YouTube 播放清單</span></a></div>
              <ul class="floatL createAbout">
                <li>直接同步既有YouTube播放清單</li>
                <li>基本的管理功能</li>
              </ul>
              <div class="clear"></div>
              <p class="hint">範例 http://www.youtube.com/user/MIT#grid/user/6535748F59DCA484</p>
            </li>
          </ul>
        </div>
      </div>
    </div>
    <!-- channel management - create ch step2 (create new channel)-->
    <div class="createChoose" style="display:none" id="channel_detail">
      <div class="right_title">頻道管理 - 建立頻道資訊</div>
      <div class="createEpList">
        <div class="chStep2">
          <p class="hint_title"><span class="red">＊</span>為必填資訊</p>
          <form>
            <fieldset class="setAlbum">
              <label class="floatL"><span class="red">＊</span>圖示</label>
              <div class="uploadImg">
                <img id="ch_image" alt="" src="/images/cms/upload_img.jpg" class="floatL"/>
                <div class="floatL imgBtn">
                  <p class="gray">多螢幕最佳顯示品質建議<br/>解析度至少為720x480</p>
                  <span id="upload_button_place"><!--
                    <a href="#" id="ch_upload_image" class="uploadBtn"></a>
                    -->
                  </span>
                  <span id="ch_uploading" style="display:none">上傳中....</span>
                  <input type="hidden" id="s3_policy" value="${s3Policy}"/>
                  <input type="hidden" id="s3_signature" value="${s3Signature}"/>
                  <input type="hidden" id="s3_id" value="${s3Id}"/>
                </div>
                <div class="clear"></div>
              </div>
              <input type="hidden" id="ch_id" value="0"/>
              <div class="clear"></div><br/>
              <label class="floatL"><span class="red">＊</span>名稱</label>
              <div class="bg_input floatL"><input id="ch_name" type="text" size="40" maxlength="40"/></div>
              <div class="clear"></div>
              <p class="hint">限40字元</p>
              <br/>
              <label class="floatL">介紹</label>
              <div class="bg_textarea floatL"><textarea id="ch_intro" name="" cols="37" rows="3"></textarea></div>
              <div class="clear"></div>
              <p class="hint">限200字元</p>
              <br/>
              <label class="floatL">標籤</label>
              <div class="bg_input floatL"><input id="ch_tag" type="text" size="40" maxlength="40" disabled="disabled"/></div>
              <div class="clear"></div>
              <p class="hint">請用" , "分開</p>
              <br/>
              <label class="floatL"><span class="red">＊</span>系統分類</label>
              <div class="floatL">
                <select name="" id="ch_category" class="sys_directory"></select>
              </div>
              <div class="clear"></div>
              <p class="hint">選擇分類後，頻道網將被收錄至9x9.tv的系統目錄中，供觀眾瀏覽，<br/>您也可以至「目錄管理」編輯您自訂的目錄，匯錄您的所有內容。</p>
              <div class="commitPlace">
                <a href="javascript:" class="btn btnStep2 floatL" id="channel_detail_savebutton"><span>儲存</span></a><a href="javascript:" class="floatL btn_cancel" id="channel_detail_cancel"><span>取消</span></a>
              </div>
            </fieldset>
          </form>
        </div>
      </div>
    </div>
    <!-- channel management - create ch step3 end (created a new channel , choose to create episode)-->
    <div class="createEp" style="display:none" id="program_list_empty">
      <div class="right_title"><span>小灰熊的大愛劇場</span> - 節目管理</div>
      <div class="createEpList">
        <div class="createEpStep1">
          <div ><a href="javascript:" class="btnCreate create_program_button">建立節目</a></div>
          <p>您尚無任何節目<br/>迅速建立節目，即可立即行銷您的影視內容</p>
        </div>
      </div>
    </div>
    <div class="createEp" style="display:none" id="program_list_empty_readonly">
      <div class="right_title"><span>小灰熊的大愛劇場</span> - 節目管理</div>
      <div class="createEpList">
        <div class="createEpStep1">
          <p>無任何節目</p>
        </div>
      </div>
    </div>
    <!-- channel management - create episode step1 (after press “”create channel button)
         1. class="addEpSection" please add vertical scrollbar on it, 
         2. class="addNew" this a tag will show up when the last uploadSection has already choosed a file to upload , and also fill up the episode title, if one of these did not satified  , the a tag wont show
         3. the button  "save"   will keep disable status (class="btnDisable" ), until the upload progress bar has been finished then switch to class="epSave"
         -->
    <div class="createEp" style="display:none">
      <div class="right_title"><span>小灰熊的大愛劇場</span> - 建立節目 <a href="#" class="floatR">回到節目管理&nbsp;&gt;&gt;</a></div>
      <div class="createEpList">
        <p class="hint_title"><span class="red">＊</span>為必填資訊</p>
        <ul class="addEpSection">
          <li>
            <div class="uploadSection">
              <form>
                <fieldset class="setAlbum">
                  <label class="floatL"><span class="red">＊</span>上傳檔案</label>
                  <div class="floatL epImport" >
                    <a href="#" class="btnCreate floatL">從硬碟</a>
                    <a href="#" class="btnCreate floatL">從URL載點</a>
                  </div>
                  <!--  upload by URL link -->
                  <div class="floatL uploadURL" style="display:none">
                    <div class="bg_input floatL"><input type="text" size="40" maxlength="40"/></div>&nbsp;<a href="#">取消</a>
                    <div class="clear"></div>
                    <p class="hint">網址結尾必須為影片格式的附檔名<br/>範例：http://blip.tv/file/get/QtvTV732.wmv</p><br/>
                  </div>
                  <label class="floatL"><span class="red">＊</span>節目圖示</label>
                  <div class="uploadImg">
                    <img alt="" src="/images/cms/upload_img.jpg" class="floatL"/>
                    <div class="floatL imgBtn">
                      <p class="gray">多螢幕最佳顯示品質建議<br/>解析度至少為720x480</p>
                      <a href="#" class="uploadBtn"></a>
                    </div>
                    <div class="clear"></div>
                  </div>
                  <label class="floatL"><span class="red">＊</span>節目名稱</label>
                  <div class="bg_input floatL"><input type="text" size="35" maxlength="40"/></div>
                  <div class="clear"></div>
                  <p class="hint">限40字元</p>
                  <br/>
                  <label class="floatL">節目介紹</label>
                  <div class="bg_textarea floatL"><textarea name="" cols="37" rows="3"></textarea></div>
                  <div class="clear"></div>
                  <p class="hint">限200字元</p>
                  <br/>
                  <label class="floatL">標籤</label>
                  <div class="bg_input floatL"><input type="text" size="35" maxlength="40"/></div>
                  <div class="clear"></div>
                  <p class="hint">請用" , "分開</p><br/>
                  <div class="epBtns" >
                    <a href="#" class="btnDisable floatL">儲存</a>
                    <a href="#"  class="btnCancel  floatL">取消</a>
                  </div>
                </fieldset>
              </form>
            </div>
            <a href="#" class="addNew">繼續新增節目</a>
          </li>
        </ul>
      </div>
    </div>
    <!-- upload channel = 9x9 channel s episode list
         1. first li is normal status , second is active status , when user click a  li, switch status to this "epItemFocus"  and epItemFocusTitle than switch into chInfo div
         -->
    <div class="epList" style="display:none" id="program_list">
      <div class="right_title"><span>小灰熊的大愛劇場</span> - 節目管理 </div>
      <div class="createEpList2">
        <a class="btnCreate create_program_button" href="javascript:">建立節目</a>
        <ul id="program_list_ul">
          <li style="display:none">
            <div class="epItem program_info_block" id="program_info_block">
              <div class="epInfoTitle program_info_title"><span>何處是我家</span> <a href="#" class="btnDel program_info_removebutton"></a></div>
              <div class="epInfoImg program_info_image"></div>
              <div class=" floatL epInfo" >
                <a href="#" class="floatL program_info_promoteurl">http://www.9x9.tv/channel/3958</a>
                <a class="floatL program_info_addthis"><img src="http://cache.addthiscdn.com/icons/v1/thumbs/addthis.gif"/></a>
                <a href="#" class="iconStatistics" title="觀看數據"></a>
                <div class="clear"></div>
                <p class="program_info_type">節目類型 : <span>9x9</span></p>
                <p class="program_info_updatedate">更新時間  : <span>2011/04/15 14:45</span></p>
              </div>
              <ul class="floatL">
                <li><a class="chUnPublic program_info_publish"></a></li>
                <li><a class="btnGray program_info_detailbutton"><span>節目資訊</span></a></li>
              </ul>
              <div class="clear"></div>
            </div>
          </li>
          <li style="display:none">
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
                <p>更新時間  : <span>2011/04/15 14:45</span></p>
              </div>
              <div class="clear"></div>
            </div>
          </li>
        </ul>
      </div>
    </div>
    <!-- upload channel  = 9x9 channel episode information -->
    <div class="createChoose" style="display:none" id="program_detail">
      <div class="right_title"><span>小灰熊的大愛劇場</span> - 節目資訊<a href="javascript:" class="floatR ep_return">回到節目管理&nbsp;&gt;&gt;</a></div>
      <div class="createEpList">
        <div class="chStep2">
          <p class="hint_title"><span class="red">＊</span>為必填資訊</p>
          <form>
            <fieldset class="setAlbum">
              <label class="floatL">節目來源</label>
              <p class="ep_source">9x9</p><br/>
              <label class="floatL">節目網址</label>
              <p><a target="_player" href="#" class="ep_url">http://www.9x9.tv/share/3958</a><p><br/>
              <label class="floatL">建立時間</label>
              <p class="ep_createdate">2011/04/25 14:30</p><br/>
              <label class="floatL"><span class="red">＊</span>圖示</label>
              <div class="uploadImg">
                <img alt="" src="/images/cms/upload_img.jpg" class="floatL ep_image"/>
                <div class="floatL imgBtn">
                  <p class="gray">多螢幕最佳顯示品質建議<br/>解析度至少為720x480</p>
                  <a href="#" class="uploadBtn ep_upload_image"></a>
                </div>
                <div class="clear"></div>
              </div>
              <div class="clear"></div><br/>
              <label class="floatL"><span class="red">＊</span>名稱</label>
              <div class="bg_input floatL">
                <input type="text" size="30" maxlength="40" class="ep_name"/>
              </div>
              <div class="clear"></div>
              <p class="hint">限40字元</p>
              <br/>
              <label class="floatL">介紹</label>
              <div class="bg_textarea floatL"><textarea name="" cols="30" rows="3" class="ep_intro"></textarea></div>
              <div class="clear"></div>
              <p class="hint">限200字元</p>
              <br/><!-- 
              <label class="floatL">標籤</label>
              <div class="bg_input floatL"><input type="text" size="40" maxlength="40" disabled="disabled"/></div>
              <div class="clear"></div>
              <p class="hint">請用" , "分開</p>
              <br/>
              <label class="floatL"><span class="red">＊</span>系統分類</label>
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
              <p class="hint">選擇分類後，頻道網將被收錄至9x9.tv的系統目錄中，供觀眾瀏覽，<br/>您也可以至「目錄管理」編輯您自訂的目錄，匯錄您的所有內容。</p>
              -->
              <div class="commitPlace">
                <a href="javascript:" class="btn btnStep2 floatL ep_savebutton"><span>儲存修改</span></a><a href="javascript:" class="floatL btn_cancel ep_cancel"><span>取消</span></a>
              </div>
            </fieldset>
          </form>
        </div>
      </div>
    </div>
    <!-- the channel is create by import other content source -->
    <div class="epList" style="display:none" id="program_list_readonly">
      <div class="right_title"><span>小灰熊的大愛劇場</span> - 節目管理 </div>
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
                <p class="program_info_type">節目類型 : <span>9x9</span></p>
                <p class="program_info_updatedate">更新時間 : <span>2011/04/15 14:45</span></p>
              </div>
              <ul class="floatL">
                <li><a class="chUnPublic program_info_publish"></a></li>
                <li><a class="btnGray program_info_detailbutton"><span>節目資訊</span></a></li>
              </ul>
              <div class="clear"></div>
            </div>
          </li>
          <li style="display:none">
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
      <div class="right_title"><span>小灰熊的大愛劇場</span> - 節目資訊<a href="javascript:" class="floatR ep_return">回到節目管理&nbsp;&gt;&gt;</a></div>
      <div class="createEpList">
        <div class="chStep2">
          <p class="hint_title"></p>
          <form>
            <fieldset class="setAlbum">
              <label class="floatL">節目來源</label>
              <p><a target="_player" class="ep_source" href="#">http://blip.tv/file/get/Qtv-JulianSchnabelOnQTV732.m4v</a></p><br/><br/>
              <label class="floatL">節目網址</label>
              <p><a target="_player" class="ep_url" href="#">http://www.9x9.tv/share/3958</a><p><br/><br/>
              <label class="floatL">建立時間</label>
              <p class="ep_createdate">2011/04/25 14:30</p><br/><br/>
              <label class="floatL">圖示</label>
              <div class="uploadImg">
                <img class="ep_image" alt="" src="/images/cms/upload_img.jpg" />
              </div>
              <div class="clear"></div><br/><br/>
              <label class="floatL">名稱</label>
              <p class="floatL ep_name">20110326《慈濟新聞深度報導》十噸賑災愛心物資 親手送給日本災民</p><br/><br/>
              <div class="clear"></div>
              <br/>
              <label class="floatL">介紹</label>
              <p class="floatL ep_intro">慈濟前往日本福島地區協助災民重建工作</p><br/><br/>
              <div class="clear"></div>
            </fieldset>
          </form>
        </div>
      </div>
    </div>
    <!-- create channel by import podcast / youtube channel / youtube playlist-->
    <div class="createChoose" style="display:none">
      <div class="right_title">頻道管理 - 建立頻道資訊</div>
      <div class="createEpList">
        <div class="chStep2">
          <p class="hint_title"><span class="red">＊</span>為必填資訊</p>
          <form>
            <fieldset class="setAlbum">
              <label class="floatL"><span class="red">＊</span>頻道來源</label>
              <div class="bg_input floatL"><input type="text" size="40" maxlength="40"/></div>
              <a href="#" class="btnCreate floatL">匯入</a>
              <div class="clear"></div><br/>
              <label class="floatL"><span class="red">＊</span>圖示</label>
              <div class="uploadImg">
                <img alt="" src="/images/cms/upload_img.jpg" class="floatL"/>
                <div class="floatL imgBtn">
                  <p class="gray">多螢幕最佳顯示品質建議<br/>解析度至少為720x480</p>
                  <a href="#" class="uploadBtn"></a>
                </div>
                <div class="clear"></div>
              </div>
              <div class="clear"></div><br/>
              <label class="floatL"><span class="red">＊</span>名稱</label>
              <div class="bg_input floatL"><input type="text" size="40" maxlength="40"/></div>
              <div class="clear"></div>
              <p class="hint">限40字元</p>
              <br/>
              <label class="floatL">介紹</label>
              <div class="bg_textarea floatL"><textarea name="" cols="37" rows="3"></textarea></div>
              <div class="clear"></div>
              <p class="hint">限200字元</p>
              <br/>
              <label class="floatL">標籤</label>
              <div class="bg_input floatL"><input type="text" size="40" maxlength="40"/></div>
              <div class="clear"></div>
              <p class="hint">請用" , "分開</p>
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
              <p class="hint">選擇分類後，頻道網將被收錄至9x9.tv的系統目錄中，供觀眾瀏覽，<br/>您也可以至「目錄管理」編輯您自訂的目錄，匯錄您的所有內容。</p>
              <div class="commitPlace">
                <a href="#" class="btn btnStep2 floatL"><span>儲存</span></a><a href="#" class="floatL btn_cancel"><span>取消</span></a>
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
</body>
</html>
