<!DOCTYPE html>
<head>
<meta charset="UTF-8" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="root" value="http://9x9ui.s3.amazonaws.com/9x9miniV9a"/>

<link rel="stylesheet" href="${root}/stylesheets/main.css" />


<!-- $Revision: 1726 $ -->

<script type="text/javascript" charset="utf-8" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.3/jquery.min.js"></script>

<script type="text/javascript" charset="utf-8" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.8/jquery-ui.min.js"></script>
<script type="text/javascript" charset="utf-8" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.8/i18n/jquery-ui-i18n.min.js"></script>

<script type="text/javascript" charset="utf-8" src="http://9x9ui.s3.amazonaws.com/scripts/swfobject.js"></script>
<!--script type="text/javascript" charset="utf-8" src="${root}/javascripts/jquery.swfobject.1-1-1.min.js"></script-->
<script type="text/javascript" charset="utf-8" src="http://9x9ui.s3.amazonaws.com/scripts/flowplayer-3.2.4.min.js"></script>
<script type="text/javascript" charset="utf-8" src="http://9x9ui.s3.amazonaws.com/vertical.slider.V2.js"></script>
<script type="text/javascript" charset="utf-8" src="http://9x9ui.s3.amazonaws.com/jquery.mousewheel.min.js"></script>

<script type="text/javascript" charset="utf-8" src="http://9x9ui.s3.amazonaws.com/mini9.js"></script>

</head>
<body>

<div id="bg-layer"></div>

<div id="video" style="position: absolute; z-Index: 2; height: 100%; width: 100%; background-color: black">
</div>

<div id="sync-layer">
  <div id="sync-holder">
    <p><span>Please initiate account binding.<br>The name of this device is:</span></p>
    <p><span class="device-name"></span></p>
    <p><span class="note">Need help? Please visit 9x9.tv or email feedback@9x9.tv</span></p>
  </div>
</div>

<div id="welcome-layer">
  <div id="welcome-holder">
    <p><span>Welcome, </span><span class="user-name">Jeff</span></p>
  </div>
</div>

<div id="msg-layer">
  <div id="msg-holder">
    <p><span></span></p>
  </div>
</div>

<div id="yesno-layer">
  <div id="yesno-holder">
    <p id="question"><span>Are you sure you want to remove</span><span id="removee"></span><span>from the user list?</span></p>
    <ul class="action-list">
      <li><p class="btn" id="btn-yesno-yes"><span>Yes</span></p></li>
      <li><p class="btn" id="btn-yesno-no"><span>No</span></p></li>
    </ul>
  </div>
</div>

<div id="player-layer">
  <div id="osd-layer">
    <div id="osd-holder">
      <p id="ch-info"><span class="head">Channel: </span><span id="ch-title"></span></p>
      <p id="ep-info"><span class="head">Episode: </span><span id="ep-title"></span><span class="dash"> &#8212; </span><span id="ep-age"></span></p>
      <p id="key-hints"><img src="${root}/images/key_hints.png"></p>
    </div>
  </div>
  <div id="player-holder">
    <div id="video-layer"></div>
  </div>
</div>

<div id="sg-layer">
  <div id="sg-header">
    <img src="${root}/images/logo.png" id="logo">
    <p id="slogan"><span>Your Personal Channel Browser</span></p>
    <p id="device">
      <span>Device:</span>
      <span id="device-name">Not connected</span>
    </p>
  </div>
  <div id="sg-holder">
      <div id="user">
        <p class="section-title"><span>User</span></p>
        <p id="selected-user"><span>Guest</span><img src="${root}/images/icon_expand.png" class="icon-expand"></p>
        <ul id="user-list">
        </ul>
      </div>
      <div id="sg-grid">
      </div>
      <div id="channel-info">
        <p class="section-title"><span>Current Channel</span></p>
        <p id="channel-title"><span>Golden Drama</span></p>
        <p id="channel-description"><span>Last installment from my "Making Money from Podcasting" series...</span></p>
        <p id="channel-meta">
          <span id="eps-number">Episodes: 12</span><br>
          <span id="updates">Updated: Today</span>
        </p>
        <p id="ch-set"><span>Channel set: <br>Da Ai March Golden Selection Set</span></p>
      </div>
  </div>
</div>

<div id="volume-layer" style="display: none">
  <p id="volume-up"><img src="${root}/images/btn_volume_up.png" title="Volume Up"></p>
  <ul id="volume-bars">
    <li></li>
    <li></li>
    <li></li>
    <li class="on"></li>
    <li class="on"></li>
    <li class="on"></li>
    <li class="on"></li>
  </ul>
  <p id="volume-down"><img src="${root}/images/btn_volume_down.png" title="Volume Down"></p>
</div>

<div id="relaydiv" style="z-index: 1; position: absolute; top: 0px; left: 0px">
</div>

<div id="help-layer">
  <div id="help-holder">
    <p id="btn-help-close"><img src="${root}/images/btn_delete_off.png" class="off"><img src="${root}/images/btn_delete_on.png" class="on"></p>
    <ul id="help-tabs">
      <li id="p1" class="on"><span>Panel 1</span></li>
      <li id="p2"><span>Panel 2</span></li>
      <li id="p3"><span>Panel 3</span></li>
      <li id="p4"><span>Panel 4</span></li>
    </ul>
    <div id="p1-panel" class="input-panel">
      <div id="p1-slider" class="slider-wrap"><div class="slider-vertical"></div></div>
      <div id="p1-content" class="constrain">
        <div id="p1-list" class="list">
        </div>
      </div>
    </div>
    <div id="p2-panel" class="input-panel">
      <div id="p2-slider" class="slider-wrap"><div class="slider-vertical"></div></div>
      <div id="p2-content" class="constrain">
        <div id="p2-list" class="list">
        </div>
      </div>
    </div>
    <div id="p3-panel" class="input-panel">
      <div id="p3-slider" class="slider-wrap"><div class="slider-vertical"></div></div>
      <div id="p3-content" class="constrain">
        <div id="p3-list" class="list">
        </div>
      </div>
    </div>
    <div id="p4-panel" class="input-panel">
      <div class="constrain">
        <p id="problem-input">
          <textarea class="textfield">Enter your problem</textarea> 
        </p>
        <p id="btn-report" class="btn"><span>Submit</span></p>
      </div>
    </div>
  </div>
</div>

</body>
</html>
