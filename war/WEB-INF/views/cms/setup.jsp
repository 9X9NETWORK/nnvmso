<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="setup_context">
  <a href="javascript:" class="btnClose jqmClose"></a>
  <div class="setCon">
    <div class="setTitle">
      <ul class="setupTab">
        <li id="tabA" class="link_Act"><a href="javascript:">帳號連結</a></li>
        <li id="tabB" class="link_Normal"><a href="javascript:">管理員設定</a></li>
        <li id="tabC" class="link_Normal"><a href="javascript:">修改密碼</a></li>
        <div class="clear"></div>
      </ul>
    </div>
    <div id="syncAcc" class="syncAcc">
      <p>9x9 將會自動同步發佈您更新的節目至您設定連結帳號的平台</p>
      <p>設定步驟 : </p>
      <p>1. 選擇您欲同步發佈的平台，選擇「連結帳號」進行設定。</p>
      <p>2. 至「推廣工具」選取3x3或頻道，勾選同步發佈至哪些平台。</p>
      <p>3. 自動同步發佈功能設定完成！</p>
      <p>未來，每當您的3x3或頻道內發佈新節目時，9x9即會自動幫您同步發佈至您連結帳號的平台！</p>
      <ul>
        <li class="syncFb">
          <span>facebook</span>&nbsp;&nbsp;
          <a href="javascript:">連結帳號</a>
          <!--
          <a href="#">中斷帳戶連結</a>&nbsp; | &nbsp;<a href="#">停用自動分享 </a>
          -->
          <a href="javascript:" class="syncInfo"></a>
          <div class="sync_hint" id="fb_hint">
            若內容更新非常頻繁，使用同步發佈功能有可能造成您社群粉絲接收資訊上的困擾，請審慎設定。
          </div>
        </li>
        <li class="syncPlurk">
          <span>plurk</span>&nbsp;&nbsp;
          <a href="javascript:">連結帳號</a>
          <!--
          <a href="#">中斷帳戶連結</a>&nbsp; | &nbsp;<a href="#">停用自動分享 </a>
          -->
        </li>
        <li class="syncTwitter">
          <span>twitter</span>&nbsp;&nbsp;
          <a href="javascript:">連結帳號</a>
          <!--
          <a href="#">中斷帳戶連結</a>&nbsp; | &nbsp;<a href="#">停用自動分享 </a>
          -->
        </li>
        <li class="syncSina">
          <span>新浪微博</span>&nbsp;&nbsp;
          <a href="javascript:">連結帳號</a>
          <!--
          <a href="#">中斷帳戶連結</a>&nbsp; | &nbsp;<a href="#">停用自動分享 </a>
          -->
        </li>
      </ul>
    </div>
    <div id="addAcc" class="addAcc" style="display:none;">
      <table border="0" cellpadding="0" cellspacing="0" class="addForm">
        <tr>
          <th align="left">新增子管理員帳號</th>
          <th align="left">備註</th>
          <th></th>
        </tr>
        <tr>
          <td><input type="text"/></td>
          <td><input type="text"/></td>
          <td><input type="button" value="確定新增"/></td>
        </tr>
        <tr>
          <td colspan="3">請使用9x9帳號邀請其他9x9用戶成為子管理員。<br/>子管理員可以使用原先註冊的9x9帳號與密碼登入管理，如同雲端群組協作。</td>
        </tr>
      </table>
      <table border="0" cellpadding="0" cellspacing="0" class="mainAcc">
        <tr>
          <th align="left" width="200">管理員帳號</th>
          <th align="left">備註</th>
        </tr>
        <tr>
          <td><span>daai@9x9.tv</span></td>
          <td><span>主要管理員使用</span></td>
        </tr>
      </table>
      <div class="subAcc">
        <table border="0" cellpadding="0" cellspacing="0">
          <tr>
            <th align="left" width="200">子管理員帳號</th>
            <th align="left">備註</th>
          </tr>
          <tr>
            <td><span>daai@9x9.tv</span></td>
            <td><span>備註文字</span></td>
          </tr>
          <tr>
            <td><span>daai@9x9.tv</span></td>
            <td><span>備註文字</span></td>
          </tr>
          <tr>
            <td><span>daai@9x9.tv</span></td>
            <td><span>備註文字</span></td>
          </tr>
          <tr>
            <td><span>daai@9x9.tv</span></td>
            <td><span>備註文字</span></td>
          </tr>
          <tr>
            <td><span>daai@9x9.tv</span></td>
            <td><span>備註文字</span></td>
          </tr>
          <tr>
            <td><span>daai@9x9.tv</span></td>
            <td><span>備註文字</span></td>
          </tr>
        </table>
      </div>
    </div>
    <div id="setPs" class="setPs" style="display:none;">
      <table border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td>重新設定密碼</td>
          <td><input type="text"/></td>
        </tr>
        <tr>
          <td>請再次輸入密碼</td>
          <td><input type="text"/></td>
        </tr>
        <tr>
          <td></td>
          <td><input type="button" value="儲存修改"/>&nbsp;&nbsp;<a href="#">取消</a></td>
        </tr>
      </table>
    </div>
  </div>
</div>
