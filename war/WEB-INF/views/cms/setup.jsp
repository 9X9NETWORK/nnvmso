<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<div class="setup_context">
  <a href="javascript:" class="btnClose jqmClose"></a>
  <div class="setCon">
    <div class="setTitle">
      <ul class="setupTab">
        <li id="tabA" class="setupTabItem link_Act"><a href="javascript:"><spring:message code="cms.setup.label.account_sync"/></a></li>
        <li id="tabB" class="setupTabItem link_Normal"><a href="javascript:"><spring:message code="cms.setup.label.admin_group"/></a></li>
        <li id="tabC" class="setupTabItem link_Normal"><a href="javascript:"><spring:message code="cms.setup.label.change_password"/></a></li>
        <div class="clear"></div>
      </ul>
    </div>
    <div id="syncAcc" class="syncAcc setupContent">
      <spring:message code="cms.setup.msg.account_synchronization_explanation"/>
      <ul>
        <li class="syncFb">
          <span><spring:message code="cms.setup.label.facebook"/></span>&nbsp;&nbsp;
          <a href="javascript:"><spring:message code="cms.setup.button.account_connect"/></a>
          <!--
          <a href="#"><spring:message code="cms.setup.button.account_disconnect"/></a>&nbsp; | &nbsp;<a href="#"><spring:message code="cms.setup.button.disable_autosharing"/> </a>
          -->
          <a href="javascript:" class="syncInfo"></a>
          <div class="sync_hint" id="fb_hint">
            <spring:message code="cms.setup.msg.fb_sync_hint"/>
          </div>
        </li>
        <li class="syncTwitter">
          <span><spring:message code="cms.setup.label.twitter"/></span>&nbsp;&nbsp;
          <a href="javascript:"><spring:message code="cms.setup.button.account_connect"/></a>
          <!--
          <a href="#"><spring:message code="cms.setup.button.account_disconnect"/></a>&nbsp; | &nbsp;<a href="#"><spring:message code="cms.setup.button.disable_autosharing"/> </a>
          -->
        </li>
        <li class="syncPlurk">
          <span><spring:message code="cms.setup.label.plurk"/></span>&nbsp;&nbsp;
          <a href="javascript:"><spring:message code="cms.setup.button.account_connect"/></a>
          <!--
          <a href="#"><spring:message code="cms.setup.button.account_disconnect"/></a>&nbsp; | &nbsp;<a href="#"><spring:message code="cms.setup.button.disable_autosharing"/> </a>
          -->
        </li>
        <li class="syncSina">
          <span><spring:message code="cms.setup.label.sina"/></span>&nbsp;&nbsp;
          <a href="javascript:"><spring:message code="cms.setup.button.account_connect"/></a>
          <!--
          <a href="#"><spring:message code="cms.setup.button.account_disconnect"/></a>&nbsp; | &nbsp;<a href="#"><spring:message code="cms.setup.button.disable_autosharing"/> </a>
          -->
        </li>
      </ul>
    </div>
    <div id="addAcc" class="addAcc setupContent" style="display:none;">
      <table border="0" cellpadding="0" cellspacing="0" class="addForm">
        <tr>
          <th align="left"><spring:message code="cms.setup.title.add_admin_account"/></th>
          <th align="left"><spring:message code="cms.setup.title.comment"/></th>
          <th></th>
        </tr>
        <tr>
          <td><input type="text"/></td>
          <td><input type="text"/></td>
          <td><button><big><spring:message code="cms.setup.button.add_account"/></big></button></td>
        </tr>
        <tr>
          <td colspan="3"><spring:message code="cms.setup.msg.add_account_explanation"/></td>
        </tr>
      </table>
      <table border="0" cellpadding="0" cellspacing="0" class="mainAcc">
        <tr>
          <th align="left" width="200"><spring:message code="cms.setup.title.admin_account"/></th>
          <th align="left"><spring:message code="cms.setup.title.comment"/></th>
        </tr>
        <tr>
          <td><span>daai@9x9.tv</span></td>
          <td><span><spring:message code="cms.setup.msg.master_admin"/></span></td>
        </tr>
      </table>
      <div class="subAcc">
        <table border="0" cellpadding="0" cellspacing="0">
          <tr>
            <th align="left" width="200"><spring:message code="cms.setup.title.sub_admin"/></th>
            <th align="left"><spring:message code="cms.setup.title.comment"/></th>
          </tr>
          <tr>
            <td><span>daai1@9x9.tv</span></td>
            <td><span>commet</span></td>
          </tr>
          <tr>
            <td><span>daai2@9x9.tv</span></td>
            <td><span>commet</span></td>
          </tr>
          <tr>
            <td><span>daai3@9x9.tv</span></td>
            <td><span>commet</span></td>
          </tr>
          <tr>
            <td><span>daai4@9x9.tv</span></td>
            <td><span>commet</span></td>
          </tr>
          <tr>
            <td><span>daai5@9x9.tv</span></td>
            <td><span>commet</span></td>
          </tr>
          <tr>
            <td><span>daai6@9x9.tv</span></td>
            <td><span>commet</span></td>
          </tr>
        </table>
      </div>
    </div>
    <div id="setPs" class="setPs setupContent" style="display:none;">
      <table border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td><spring:message code="cms.setup.label.reset_password"/></td>
          <td><input type="text"/></td>
        </tr>
        <tr>
          <td><spring:message code="cms.setup.label.retype_password"/></td>
          <td><input type="text"/></td>
        </tr>
        <tr>
          <td></td>
          <td><button><big><spring:message code="cms.setup.button.save"/></big></button>&nbsp;&nbsp;<a href="javascript:"><spring:message code="cms.setup.button.cancel"/></a></td>
        </tr>
      </table>
    </div>
  </div>
</div>
<label class="lang" id="lang_warning_not_9x9_user"><spring:message code="cms.warning.not_9x9_user"/></label>
<label class="lang" id="lang_warning_remove_admin_member"><spring:message code="cms.warning.remove_admin_member"/></label>
