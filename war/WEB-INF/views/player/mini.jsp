<!DOCTYPE html>
<head>
<meta charset="UTF-8" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="root" value="http://9x9ui.s3.amazonaws.com/9x9miniV4"/>

<link rel="stylesheet" href="http://9x9ui.s3.amazonaws.com/9x9miniV4/stylesheets/main.css" />


<!-- $Revision: 1726 $ -->

<script type="text/javascript" charset="utf-8" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.3/jquery.min.js"></script>

<script type="text/javascript" charset="utf-8" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.8/jquery-ui.min.js"></script>
<script type="text/javascript" charset="utf-8" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.8/i18n/jquery-ui-i18n.min.js"></script>

<script type="text/javascript" charset="utf-8" src="http://9x9ui.s3.amazonaws.com/scripts/swfobject.js"></script>
<script type="text/javascript" charset="utf-8" src="${root}/javascripts/jquery.swfobject.1-1-1.min.js"></script>
<script type="text/javascript" charset="utf-8" src="http://9x9ui.s3.amazonaws.com/scripts/flowplayer-3.2.4.min.js"></script>

<script type="text/javascript" charset="utf-8" src="http://9x9ui.s3.amazonaws.com/mini4.js"></script>

</head>
<body>

<div id="bg-layer"></div>

<div id="video" style="position: absolute; z-Index: 2; height: 100%; width: 100%; background-color: black">
</div>

<div id="sync-layer">
  <div id="sync-holder">
    <p>Ready to Sync</p>
  </div>
</div>

<div id="sync-layer">
  <div id="sync-holder">
    <p><span>Please initiate default account binding.<br>The name of this device name is:</span></p>
    <p><span class="device-name"></span></p>
    <p><span class="note">Need help? Please visit 9x9.tv</span></p>
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

<div id="sg-layer">
  <div id="sg-header">
    <img src="${root}/images/beta.png" id="beta">
    <img src="${root}/images/logo.png" id="logo">
    <p id="slogan"><span>Your Personal Video Album</span></p>
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

<div id="player-layer">
  <div id="osd-layer">
    <p id="ch-title"><span class="head">Channel: </span><span>Channel Title</span></p>
    <p id="ep-title"><span class="head">Episode: </span><span>Episode Title</span></p>
  </div>
  <div id="player-holder">
    <div id="video-layer"></div>
    <p id="arrow-left"><img src="${root}/images/arrow_left.png"><span>Prev<br>Episode</span></p>
    <p id="arrow-right"><img src="${root}/images/arrow_right.png"><span>Next<br>Episode</span></p>
    <p id="arrow-up"><img src="${root}/images/arrow_up.png"><span>Prev Ch</span></p>
    <p id="arrow-down"><img src="${root}/images/arrow_down.png"><span>Next Ch</span></p>
  </div>
</div>

<div id="relaydiv" style="z-index: 1; position: absolute; top: 0px; left: 0px">
</div>

</body>
</html>