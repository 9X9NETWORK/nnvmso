<!DOCTYPE html>
<head>
<meta charset="UTF-8" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="root" value="http://9x9ui.s3.amazonaws.com/9x9playerV68"/>

<link rel="stylesheet" href="http://9x9ui.s3.amazonaws.com/9x9mini/stylesheets/main.css" />


<!-- $Revision: 1726 $ -->

<script type="text/javascript" charset="utf-8" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.3/jquery.min.js"></script>

<script type="text/javascript" charset="utf-8" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.8/jquery-ui.min.js"></script>
<script type="text/javascript" charset="utf-8" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.8/i18n/jquery-ui-i18n.min.js"></script>

<script type="text/javascript" charset="utf-8" src="http://9x9ui.s3.amazonaws.com/scripts/swfobject.js"></script>
<script type="text/javascript" charset="utf-8" src="${root}/javascripts/jquery.swfobject.1-1-1.min.js"></script>
<script type="text/javascript" charset="utf-8" src="http://9x9ui.s3.amazonaws.com/scripts/flowplayer-3.2.4.min.js"></script>

<script type="text/javascript" charset="utf-8" src="http://9x9ui.s3.amazonaws.com/mini2.js"></script>

</head>
<body>

<div id="video" style="position: absolute; z-Index: 2; height: 100%; width: 100%; background-color: black">
</div>

<div id="sync-layer">
  <div id="sync-holder">
    <p>Ready to Sync</p>
  </div>
</div>

<div id="sg-layer">
  <div id="sg-holder">
    <div id="sg-content">
      <p id="sg-title">
        <span id="sg-user">Jeff's</span>
        <span id="smart-guide">Smart Guide</span>
        <span id="device-id-label">This Device</span>
        <span id="device-id">#0</span>
      </p>
      <div id="sg-grid"></div>
      <div id="channel-info">
        <p id="section-title"><span>Current Channel</span></p>
        <p id="channel-title"><span>Golden Drama</span></p>
        <p id="channel-description"><span>Last installment from my "Making Money from Podcasting" series...</span></p>
        <p>
          <span id="eps-number">Episodes: 12</span><br>
          <span id="updates">Updated: Today</span>
        </p>
        <p id="ch-set"><span>Channel set: <br>Da Ai March Golden Selection Set</span></p>
      </div>
    </div>
  </div>
</div>


<div id="relaydiv" style="z-index: 1; position: absolute; top: 0px; left: 0px">
<object id="relay" width=481 height=86>
<param name=movie value="relay.swf">
<embed play=false swliveconnect="true" name="relay" 
src="http://50.17.15.33/relay.swf" quality=high bgcolor=#FFFFFF 
width=500 height=500 type="application/x-shockwave-flash" allowScriptAccess="always" allowScripting="on" wmode="transparent">
</embed>
</object>
</div>

</body>
</html>