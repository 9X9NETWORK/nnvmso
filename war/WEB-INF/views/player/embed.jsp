<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<form id="embed-form">
  <div class="container">
    <div id="embed-content" class="content">
      <p class="titlebar">Embed</p>
      <div class="formholder">
        <ul class="thumbnail">
          <li>Logo:</li>
          <li><img src="../images/thumb_noImage.jpg" id="msoLogo"></li>
          <li><span class="guide">Recommended upload size under 2MB</span></li>
          <li>
            <input name="btn-upload" type="image" src="../images/btn_upload.png" id="btn-upload">
          </li>
        </ul>
        <ul class="form">
          <li><label for="dimension">Dimension:</label></li>
          <li>
            <span class="dimensiontxt">Width:</span>
            <span class="dimensionbox"><input name="v-width" id="v-width" type="text" class="dimensionfield"/></span>
            <span class="dimensiontxt">pixel</span>
          </li>
          <li>
            <span class="dimensiontxt">Height:</span>
            <span class="dimensionbox"><input name="v-height" id="v-height" type="text" class="dimensionfield"/></span>
            <span class="dimensiontxt">pixel</span>
          </li>
        </ul>
      </div>
      <div id="embedcode">
        <p class="control">
          <span class="box-title">Embed code:</span>
          <a href="javascript:;" class="btnblue" id="btn-generateCode">Generate code</a>
        </p>
        <p class="textareabox">
          <textarea name="embed-code" id="embed-code" cols="10" rows="6" class="textarea"></textarea>
        </p>
      </div>
    </div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
