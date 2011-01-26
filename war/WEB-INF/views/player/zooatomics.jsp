<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<!DOCTYPE html>
<head>
<meta charset="UTF-8" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<!-- $Revision$ -->

<!-- FB Sharing meta data -->
<meta name="title" content="My 9x9 Channel Guide <%= (new SimpleDateFormat("MM.dd.yyyy")).format(new Date()) %>" />
<meta name="description" content="My 9x9 Channel Guide. Easily browse your favorite video podcasts on the 9x9 Player! Podcasts automatically download and update for you, bringing up to 81 channels of new videos daily." />
<link rel="image_src" href="http://www.cksinfo.com/clipart/toys/abc-blocks.png" />

<link rel="stylesheet" href="http://zoo.atomics.org/video/9x9playerV29/stylesheets/main.css" />

<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.3/jquery.min.js"></script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.8/jquery-ui.min.js"></script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.8/i18n/jquery-ui-i18n.min.js"></script>

<script type="text/javascript" src="http://static.ak.fbcdn.net/connect/en_US/core.debug.js"></script>
<script type="text/javascript" src="http://zoo.atomics.org/video/cssanim.js"></script>
<script type="text/javascript" src="http://zoo.atomics.org/video/swfobject.js"></script>
<script type="text/javascript" src="http://zoo.atomics.org/video/9x9playerV29/javascripts/jquery.swfobject.1-1-1.min.js"></script>
<script type="text/javascript" src="http://zoo.atomics.org/video/flowplayer-3.2.4.min.js"></script>
<script type="text/javascript" src="http://zoo.atomics.org/video/9x9playerV29/javascripts/whatsnew.js"></script>

<script>

/* players */

var current_tube = '';

var ytplayer;
var yt_video_id;
var yt_timex = 0;
var yt_previous_state = -2;

var jwplayer;
var jw_video_file = 'nothing.flv';
var jw_timex = 0;
var jw_previous_state = '';
var jw_position = 0;

var fp_player = 'player1';
var fp_preloaded = '';
var start_preload = 0;

var fp = {  player1: { file: '', duration: 0, timex: 0, mute: false },
            player2: { file: '', duration: 0, timex: 0, mute: false }  };

var language = 'en';

var activated = false;
var remembered_pause = false;

var current_category = 0;
var current_program = '';
var current_url = '';

var thumbing = '';
var dragging = false;

var channelgrid = {};
var channels_by_id = {}
var channel_line = {};
var n_channel_line = 0;
var channel_cursor = 1;

var programgrid = {};
var program_line = [];
var n_program_line = 0;
var program_cursor = 1;
var program_first = 1;

var ipg_cursor;
var ipg_timex = 0;
var ipg_delayed_stop_timex = 0;
var ipg_preload_timex = 0;
var ipg_mode = '';

var clips_played = 0;

/* cache this for efficiency */
var loglayer;

/* browse */
var browser_x;
var browser_y;
var browser_mode = 'category';
var browse_content = {};
var browse_list = {};
var n_browse_list = 0;
var browse_list_first = 1;
var browsables = {};
var browser_cat = 0;
var browser_cat_cursor = 1;
var browser_first_cat = 1;
var max_browse = 16;
var n_browse = 0;
var saved_thumbing = '';
var control_saved_thumbing = '';
var browse_cursor = 1;
var max_programs_in_line = 9;

/* what's new */
whatsnew = [];

/* timeout for program or channel index */
var osd_timex = 0;

/* workaround for Chrome not firing 'ended' video event */
var fake_timex = 0;

/* timeout for msg-layer */
var msg_timex = 0;

/* when end message enters whatsnew after 20 seconds */
var edge_of_world_timex = 0;

var dirty_delay;
var dirty_channels = [];
var dirty_timex;

var control_buttons = [ 'btn-replay', 'btn-rewind', 'btn-play', 'btn-forward', 'btn-volume', 'btn-facebook', 'btn-screensaver', 'btn-close' ];
var control_cursor = 2;

var user = '';
var username = '';

/* if we entered via a shared IPG, and have not upconverted */
var via_shared_ipg = false;

/* reduced functionality if there is a valid user but he is visiting a shared ipg */
var readonly_ipg = false;

var root = 'http://zoo.atomics.org/video/9x9playerV29/images/';

$(document).ready (function()
 {
 log ('begin execution');
 init();
 pre_login();
 $(window).resize (function() { elastic(); });
 });

function elastic()
  {
  log ('elastic');
  elastic_innards();
  }

function elastic_innards()
  {
  var newWidth  = $(window).width()  / 16;
  var newHeight = $(window).height() / 16;

  var xtimes = newWidth  / 64 * 100;
  var ytimes = newHeight / 36 * 100;

  $("body").css ("font-size", ((xtimes >= ytimes) ? ytimes : xtimes) + "%");

  var vh = $(window).height();
  var vw = $(window).width();

  var h = document.getElementById ("v");
  h.style.height = vh + "px";
  var h = document.getElementById ("fp1");
  h.style.height = vh + "px";
  var h = document.getElementById ("fp2");
  h.style.height = vh + "px";
  var h = document.getElementById ("yt1");
  h.style.height = vh + "px";
  h.style.width = vw + "px";

  ipg_fixup_margin();
  ipg_fixup_middle();

  whatsnew_fixup_middle();
  episode_end_layer_fixup();
  dir_waiting_fixup();
  align_center();

  if (thumbing == 'ipg')
    extend_ipg_timex();
  }

function episode_end_layer_fixup()
  {
  var wh = $(window).height();
  var eh = $("#epend-layer").height();
  var et = (wh-eh)/2;
  $("#epend-layer").css("top",et);
  }

function ipg_fixup_margin()
  {
  var gridH = $("#ipg-grid").height();
  var listH = 0;

  $(".ipg-list").each (function() { listH += $(this).height() + 2; });

  var gap = (gridH - listH) / 8;

  if (gap >= 0)
    $(".ipg-list").css ("margin-bottom", gap);
  else
    $(".ipg-list").css ("margin-bottom", "0");

  $(".ipg-list:last-child").css ("margin-bottom","0");
  }

function ipg_fixup_middle()
  {
  try { $("#ipg-holder").css ("top", ($(window).height() - $("#ipg-holder").height()) / 2); } catch (error) {};
  try { $("#dir-holder").css ("top", ($(window).height() - $("#dir-holder").height()) / 2); } catch (error) {};
  try { $("#list-holder").css ("top", ($("#ipg-grid").height() - $("#list-holder").height()) / 2); } catch (error) {};
  }

function whatsnew_fixup_middle()
  {
  try { $("#new-holder").css ("top", ($(window).height() - $("#new-holder").height()) / 2); } catch (error) {};
  }

function dir_waiting_fixup()
  {
  var offset = $(".content-panel").offset();
  $("#dir-waiting").css ({ "left": offset.left, "top": offset.top });
  }

function align_center()
  {
  var ww = $(window).width();
  var wh = $(window).height();

  $("#ipg-holder").css  ("left", (ww - $("#ipg-holder").width())  / 2);
  $("#dir-holder").css  ("left", (ww - $("#dir-holder").width())  / 2);
  $("#new-holder").css  ("left", (ww - $("#new-holder").width())  / 2);
  $("#ep-layer").css    ("left", (ww - $("#ep-layer").width())    / 2);
  $("#epend-layer").css ("left", (ww - $("#epend-layer").width()) / 2);
  $("#msg-layer").css   ("left", (ww - $("#msg-layer").width())   / 2);
  $("#waiting").css     ("left", (ww - $("#waiting").width())     / 2);
//$("#ipg-hit").css     ("left", (ww - $("#ipg-hit").width())     / 2);
//$("#ep-hit").css      ("left", (ww - $("#ep-hit").width())      / 2);
//$("#error-layer").css ("left", (ww - $("#error-layer").width()) / 2);

  $("#splash").css      ("top",  (wh - $("#splash").height())     / 2);
  }

function log (text)
  {
  try
    {
    if (window.console && console.log)
      console.log (text);

    if (!loglayer)
      loglayer = document.getElementById ("log-layer");

    loglayer.innerHTML += text + '<br>';
    }
  catch (error)
    {
    }
  }

function logblob (text)
  {
  var appendage = '';
  var lines = text.split ('\n');

  for (var i = 0; i < lines.length; i++)
    {
    try
      {
      if (window.console && console.log)
        console.log (lines [i]);
      }
    catch (error)
      {
      }

    appendage += lines [i] + '<br>';
    }

  if (!loglayer)
    loglayer = document.getElementById ("log-layer");

  loglayer.innerHTML += appendage;
  return;
  }

function log_and_alert (text)
  {
  panic (text);
  }

function panic (text)
  {
  log (text);
  alert (text);
  }

function init()
  {
  Array.prototype.remove = function (val)
    {
    for (var i = 0; i < this.length; i++)
      {
      if (this [i] == val)
        {
        this.splice (i, 1);
        break;
        }
      }
    }

  setup_ajax_error_handling();

  if (started_from_shared_ipg())
    via_shared_ipg = true;

  /* Initialize FB Javascript SDK */

  FB.init (
    {
    appId: '110847978946712',
    status: false, // check login status
    cookie: false, // enable cookies to allow the server to access the session
    xfbml: false   // parse XFBML
    });
  }

function started_from_shared_ipg()
  {
  var pathname = location.pathname;
  var split = pathname.split ('/');
  return (split.length == 3 && split[2].match(/^[0-9]+$/));
  }

function get_ipg_id()
  {
  var split = location.pathname.split ('/');
  return split[2];
  }

function user_or_ipg()
  {
  return readonly_ipg ? 'ipg=' + get_ipg_id() : 'user=' + user;
  }

function fetch_programs_in (channel)
  {
  log ('obtaining programs for ' + channel);

  var query = "/playerAPI/programInfo?channel=" + channel + '&' + user_or_ipg();

  var d = $.get (query, function (data)
    {
    parse_program_data (data);

    if (thumbing == 'ipg')
      ipg_metainfo();
    });
  }

function fetch_programs()
  {
  log ('obtaining programs');

  var query = "/playerAPI/programInfo?channel=*" + '&' + user_or_ipg();

  var d = $.get (query, function (data)
    {
    if (sanity_check_data ('programInfo', data))
      parse_program_data (data);
    else
      log ('*** programInfo: DATA RETURNED BY SERVER FAILS SANITY CHECK');

    fetch_channels();
    });
  }

function parse_program_data (data)
  {
  // 0=channelId, 1=programId, 2=programName, 3=description(max length=256),
  // 4=programType, 5=duration,
  // 6=programThumbnailUrl, 7=programLargeThumbnailUrl,
  // 8=url1(mpeg4/slideshow), 9=url2(webm), 10=url3(flv more likely), 11=url4(audio),
  // 12=timestamp

  var lines = data.split ('\n');

  log ('number of programs obtained: ' + lines.length);

  if (lines.length > 0)
    {
    var fields = lines[0].split ('\t');
    if (fields [0] != '0')
      {
      log_and_alert ('server error: ' + lines [0]);
      return;
      }

    for (var i = 1; i < lines.length; i++)
      {
      if (lines [i] != '')
        {
        var fields = lines[i].split ('\t');
        programgrid [fields [1]] = { 'channel': fields[0], 'type': fields[3], 'url1': 'fp:' + fields[8], 
                     'url2': 'fp:' + fields[9], 'url3': 'fp:' + fields[10], 'url4': 'fp:' + fields[11], 
                     'name': fields[2], 'desc': fields [3], 'type': fields[4], 'thumb': fields[6], 
                     'snapshot': fields[7], 'timestamp': fields[12], 'duration': fields[5] };
        }
      }

    log ('finished parsing program data');
    }
  else
    log_and_alert ('server returned nothing');
  }

function fetch_channels()
  {
  log ('obtaining channels');

  var query;

  if (readonly_ipg)
    query = "/playerAPI/loadIpg?ipg=" + get_ipg_id();
  else
    query = "/playerAPI/channelLineup?user=" + user;

  var d = $.get (query, function (data)
    {
    var n = 0;
    var conv = {};

    for (var y = 1; y <= 9; y++)
      for (var x = 1; x <= 9; x++)
        conv [++n] = "" + y + "" + x;

    var lines = data.split ('\n');
    log ('number of channels obtained: ' + lines.length);

    var fields = lines[0].split ('\t');
    if (fields [0] != '0')
      {
      log_and_alert ('server error: ' + lines [0]);
      return;
      }

    for (var i = 1; i < lines.length; i++)
      {
      if (lines [i] != '')
        {
        var fields = lines[i].split ('\t');
        log ("channel line " + i + ": " + conv [fields[0]] + ' = ' + lines [i]);
        channelgrid [conv [fields[0]]] = { 'id': fields[1], 'name': fields[2], 'desc': fields[3], 'thumb': fields[4], 'count': fields[5], 'type': fields[6], 'status': fields[7] };
        channels_by_id [fields[1]] = conv [fields[0]];
        }
      else
        log ("ignoring channels line " + i + ": " + lines [i]);
      }
    if (!activated)
      activate();
    else
      {
      redraw_ipg();
      elastic();
      }
    });
  }

function browser_support()
  {
  if (jQuery.browser.msie && !jQuery.browser.version.match (/^[789]/))
    {
    $("#blue").html ('<p>&nbsp;<p>&nbsp;<p>Please use the Chrome browser for this application:<p>&nbsp; &nbsp;<a href="http://www.google.com/chrome">www.google.com/chrome</a><p>');
    return false;
    }
  return true;
  }

function activate()
  {
  log ('activate');

  $("#opening").hide();

  if (!browser_support())
    return;

  activated = true;
  enter_category (first_category(), 'b');
  elastic();

  //play_first_program_in (first_channel());
  program_cursor = 1;
  program_first = 1;
  current_program = first_program_in (first_channel());

  enter_channel();

  $("#ep-layer").hide();
  document.onkeydown=kp;
  redraw_ipg();

  log ('activate: ipg');
  switch_to_ipg();

  //$("#notblue").show();
  $("#blue").hide();
  preload_control_images()

  jw_play_nothing();

  $("body").focus();
  }

function preload_control_images()
  {
  var html = '';

  for (var i in { 'bg_controler':'', 'btn_rewind':'', 'btn_pause':'', 'btn_play':'', 'btn_forward':'', 'btn_volume':'', 'btn_close':'', 'btn_on':'', 'btn_off':'', 'btn_facebook':'', 'btn_replay':'', 'btn_screensaver':'', 'bg_ep':'', 'bg_film':'' })
    html += '<img src="' + root + i + '.png">';

  $("#preload-control-images").html (html);
  }

function best_url (program)
  {
  var desired;

  if (! (program in programgrid))
    {
    log ('program not in programgrid!');
    return '';
    }

  if (current_tube == 'jw' || current_tube == 'fp')
    desired = '(mp4|m4v|flv)';

  else if (navigator.userAgent.match (/(GoogleTV|Droid Build)/i))
    desired = '(mp4|m4v)';

  else if (navigator.userAgent.match (/(Opera|Firefox)/))
    desired = 'webm';

  else if (navigator.userAgent.match (/(Safari|Chrome)/))
    desired = '(mp4|m4v)';

  ext = new RegExp ('\.' + desired + '$');

  if (programgrid [program]['url1'].match (desired))
    {
    return programgrid [program]['url1'];
    }
  else if (programgrid [program]['url2'].match (desired))
    {
    return programgrid [program]['url2'];
    }
  else if (programgrid [program]['url3'].match (desired))
    {
    return programgrid [program]['url3'];
    }
  else if (programgrid [program]['url4'].match (desired))
    {
    return programgrid [program]['url4'];
    }
  else
    {
    for (var f in { url1:'', url2:'', url3:'', url4:'' })
      {
      if (! (programgrid [program][f].match (/^(|null|jw:null|jw:|fp:null|fp:)$/)))
        return programgrid [program][f];
      }
    return '';
    }
  }

function play_first_program_in (chan)
  {
  program_cursor = 1;
  program_first = 1;

  prepare_channel();

  current_program = first_program_in (chan);
  log ('playing first program in ' + chan + ': ' + current_program);

  play();
  }

function clear_msg_timex()
  {
  if (msg_timex != 0)
    {
    clearTimeout (msg_timex);
    msg_timex = 0;
    }
  if (edge_of_world_timex != 0)
    {
    clearTimeout (edge_of_world_timex);
    edge_of_world_timex = 0;
    }
  $("#msg-layer").hide();
  $("#epend-layer").hide();
  }

function message (text, duration)
  {
  $("#msg-layer").html ('<p>' + text + '</p>');
  $("#msg-layer").show();

  if (duration > 0)
    msg_timex = setTimeout ("empty_channel_timeout()", duration);
  }

function hide_layers()
  {
  $("#ep-layer").hide();
  $("#control-layer").hide();
  $("#msg-layer").hide();
  $("#msg-layer").hide();
  $("#epend-layer").hide();
  }

function end_message (duration)
  {
  $("#loading").hide();

  if (thumbing != 'program' && thumbing != 'channel' && thumbing != 'control')
    return;

  log ('end!');
  hide_layers();

  var square = parseInt (channel_line [channel_cursor]);

  var prev = previous_channel_square (square);
  var next = next_channel_square (square);

  $("#left-tease").attr ("src", channelgrid [prev]['thumb']);
  $("#right-tease").attr ("src", channelgrid [next]['thumb']);

  $("#epend-layer").show();

  if (duration > 0)
    msg_timex = setTimeout ("empty_channel_timeout()", duration);

  edge_of_world_timex = setTimeout ("edge_of_world_idle()", 45000);

  thumbing = 'end';
  }

function edge_of_world_idle()
  {
  edge_of_world_timex = 0;
  if (thumbing == 'end')
    switch_to_whats_new();
  }

function play()
  {
  clear_msg_timex();

  var url = best_url (current_program);

  if (url == '')
    {
    log ('current program ' + current_program + ' has no URL, assuming empty channel, displaying notice for 3 seconds')
    $("#ep-layer").hide();
    end_message (10000);
    return;
    }

  log ('Playing ' + current_program + ': ' + programgrid [current_program]['name'] + ' :: ' + url);
  physical_start_play (url);

  clips_played++;
  }

function start_play_html5 (url)
  {
  current_tube = 'v1';

  var v = document.getElementById ("vvv");
  v.src = url;

  fake_timex = 0;

  $("#loading").show();

  // v.addEventListener ('loadstart', function() { loadstart_callback(); }, false);
  v.addEventListener ('play', function () { play_callback(); }, false);
  v.addEventListener ('ended', function () { ended_callback(); }, false);
  v.addEventListener ('timeupdate', function () { update_progress_bar(); }, false);
  v.addEventListener ('pause', function () { pause_callback(); }, false);

  v.addEventListener ('error', function () { notify ("error"); }, false);
  v.addEventListener ('stalled', function () { $("#loading").show(); notify ("stalled"); }, false);
  v.addEventListener ('waiting', function () { $("#loading").show(); notify ("waiting"); }, false);
  v.addEventListener ('seeking', function () { notify ("seeking"); }, false);
  v.addEventListener ('seeked', function () { notify ("seeked"); }, false);
  v.addEventListener ('suspend', function () { notify ("suspend"); }, false);
  v.addEventListener ('playing', function () { $("#loading").hide(); notify ("playing"); }, false);
  v.addEventListener ('abort', function () { notify ("abort"); }, false);
  v.addEventListener ('emptied', function () { notify ("emptied"); }, false);

  try { log ('play'); v.play(); } catch (error) { }

  log ('Playing: ' + url);

  update_bubble();
  }


/* html5 video event callbacks */

function notify (text)
  {
  log ('** video event: ' + text);
  }

function loadstart_callback()
  {
  log ('loadstart callback');
  $("#loading").show();
  }

function play_callback()
  {
  log ('play callback');
  $("#loading").hide();
  var v = document.getElementById ("vvv");
  // v.addEventListener ('ended', function () { channel_right(); }, false);
  $("#btn-play").hide();
  $("#btn-pause").show();
  }

function ended_callback()
  {
  if (fake_timex)
    {
    log ('** cleared fake timex');
    clearTimeout (fake_timex);
    fake_timex = 0;
    }

  var type = thumbing;
  if (type == 'control') type = control_saved_thumbing;

  if (type == 'program' || type == 'channel')
    {
    log ('** ended event fired, moving program right (cursor at ' + program_cursor + ')');
    program_right();
    }
  else
    log ('** ended event fired, staying put');
  }

function fake_ended_event()
  {
  fake_timex = 0;
  log ('** ended event not fired, but reached end of video');
  channel_right();
  }

function pause_callback()
  {
  log ('** pause event fired');
  $("#btn-pause").hide();
  $("#btn-play").show();
  }

/* end of html5 video event callbacks */


function empty_channel_timeout()
  {
  msg_timex = 0;
  $("#msg-layer").hide();
  $("#epend-layer").hide();
  log ('auto-switching from empty channel');
  channel_right();
  }

function play_program()
  {
  current_program = program_line [program_cursor];
  play();
  }

function update_bubble()
  {
  var program_name = 'No programs in this channel';

  if (current_program in programgrid)
    program_name = programgrid [current_program]['name'];

  if (program_name.match (/^\s*$/))
    program_name = '[no title]';

  var channel = channel_line [channel_cursor];

  if (channel in channelgrid)
    {
    var channel_name = channelgrid [channel]['name'];
    if (channel_name.match (/^\s*$/)) { channel_name = '[no channel name]'; }

    $("#ep-layer-ch-title").html (channel_name);
    }
  else
    $("#ep-layer-ch-title").html ('');

  $("#ep-layer-ep-title").html (program_name);
  }

function prepare_channel()
  {
  program_line = [];

  var channel = channel_line [channel_cursor];
  log ('prepare channel ' + channel);

  if (channelgrid.length == 0)
    {
    alert ('You have no channels');
    return;
    }

  if (channel in channelgrid)
    var real_channel = channelgrid [channel]['id'];
  else
    {
    log ('not in channelgrid: ' + channel);
    return;
    }

  if (programs_in_channel (channel) < 1)
    {
    log ('no programs in channel');
    return;
    }

  n_program_line = 0;

  for (var p in programgrid)
    {
    if (programgrid [p]['channel'] == real_channel)
      program_line [n_program_line++] = p;

    // var image = new Image();
    // image.src = programgrid [p]['thumb'];
    }

  program_line = program_line.sort (function (a,b) { return Math.floor (programgrid [b]['timestamp']) - Math.floor (programgrid [a]['timestamp']) });
  program_line.unshift ('');

  $("#ep-swish").show();
  $("#ep-layer").show();
  $("#ep-list").html (ep_html());
  $("#ep-list img").error(function () { $(this).unbind("error").attr("src", "http://zoo.atomics.org/video/images-x1/no_images.png"); });
  $("#ep-list .clickable").bind ('click', function() { ep_click ($(this).attr('id')); });

  if (thumbing == 'ipg' && ipg_mode == 'episodes')
    $("#ep-list .clickable").hover (ipg_episode_hover_in, ipg_episode_hover_out);

  update_bubble();
  redraw_program_line();
  }

function enter_channel()
  {
  $("#epend-layer").hide();

  prepare_channel();

  //if (jQuery.browser.msie && jQuery.browser.version == '8.0')
  if (true)
    {
    $("#control-layer").hide();
    redraw_program_line();
    $("#ep-layer").show();
    thumbing = 'program';
    enter_channel_failsafe();
    reset_osd_timex();
    return;
    }
  }

function enter_channel_failsafe()
  {
  // prepare_channel();

  $("#ep-layer").css ("opacity", "1");
  $("#ep-swish").css ("top", "1.4275em");
  $("#ep-swish").css ("display", "block");

  $("#control-layer").css ("opacity", "1");

  if (thumbing == 'program')
    $("#ep-layer").show();

  turn_off_ancillaries();
  }

function ep_html()
  {
  var html = '';
  var now = new Date();

  var bad_thumbnail = 'http://zoo.atomics.org/video/images-x1/no_images.png';

  log ('(program html) program_first: ' + program_first + ' n_program_line: ' + n_program_line + ' program_cursor: ' + program_cursor);
  for (var i = program_first; i <= n_program_line && i < program_first + max_programs_in_line; i++)
    {
    var program = programgrid [program_line [i]];

    var age = ageof (program ['timestamp']);
    var duration = durationof (program ['duration']);

    var classes = (i == program_cursor) ? 'on clickable' : 'clickable';

    var thumbnail = program ['thumb']
    if (thumbnail == '' || thumbnail == 'null' || thumbnail == 'false')
      thumbnail = bad_thumbnail;

    html += '<li class="' + classes + '" id="p-li-' + i + '"><img src="' + root + 'bg_ep_off.png" class="bg-ep"><img src="' + thumbnail + '" class="thumbnail"></li>'
    }

  return html;
  }

function durationof (duration)
  {
  if (duration == '' || duration == 'null' || duration == undefined)
    return '0:00';

  if (duration.match (/^00:\d\d:\d\d/))
    duration = duration.replace (/^00:/, '');

  if (duration.match (/\.\d\d$/))
    duration = duration.replace (/\.\d\d$/, '');

  return duration;
  }

function ageof (timestamp)
  {
  var age = '';
  var now = new Date();
  var ago_or_hence = 'ago';

  if (timestamp != '')
    {
    var d = new Date (Math.floor (timestamp));

    var minutes = Math.floor ((now.getTime() - d.getTime()) / 60 / 1000);
    ago_or_hence = minutes < 0 ? "hence" : "ago";
    minutes = Math.abs (minutes);

    if (minutes > 59)
      {
      var hours = Math.floor ((minutes + 1) / 60);
      if (hours >= 24)
        {
        var days = Math.floor ((hours + 1) / 24);
        if (days > 30)
          {
          var months = Math.floor ((days + 1) / 30);
          if (months > 12)
            {
            var years = Math.floor ((months + 1) / 12);
            age = years + (years == 1 ? ' year' : ' years');
            }
          else
            age = months + (months == 1 ? ' month' : ' months');
          }
        else
          age = days + (days == 1 ? ' day' : ' days');
        }
      else
        age = hours + (hours == 1 ? ' hour' : ' hours');
      }
    else
      age = minutes + (minutes == 1 ? ' minute' : ' minutes');
    }
  else
    age = 'long'

  return age + ' ' + ago_or_hence;
  }

var old_cline;
var new_cline;

function enter_category (cat, positioning)
  {
  log ('enter category: ' + cat + ', thumbing: ' + thumbing);

  /* temporary code, if channel mode is removed */
  if (true)
    {
    channel_line = {};

    $("#ipg-layer").hide();
    $("#control-layer").hide();
    $("#ch-list-" + cat).html (ch_html (cat));
    $("#row-number").html ('<p>' + cat + '</p>');

    current_category = cat;

    for (var y = 1; y <= 9; y++)
      $("#ch-swish-" + y).css ("display", y == cat ? "block" : "none");

    /* position at beginning or ending */
    if (positioning == 'b')
      channel_cursor = 1;
    else if (positioning == 'e')
      channel_cursor = n_channel_line;

    redraw_channel_line();
    prepare_channel();

    thumbing = 'channel';

    turn_off_ancillaries();
    return;
    }
  }

function ch_html (cat)
  {
  var html = "";
  var bad_thumbnail = 'http://zoo.atomics.org/video/images-x1/no_images.png';

  n_channel_line = 0;

  for (var x = 1; x <= 9; x++)
    {
    var chan = "" + cat + "" + x;
  
    if (channelgrid [chan])
      {
      var thumbnail = channelgrid [chan]['thumb'];
      if (thumbnail == '' || thumbnail == 'null' || thumbnail == 'false')
        thumbnail = bad_thumbnail;

      channel_line [++n_channel_line] = chan;
      // html += '<li id="c-' + current_category + '-li-' + n_channel_line + '"><img id="c-th-' + n_channel_line + '" src="' + channelgrid [chan]['thumb'] + '"></li>';
      html += '<li id="c-' + cat + '-li-' + n_channel_line;
      html += (channel_cursor == n_channel_line) ? '" class="on">' : '">';

      var programs = programs_in_channel (chan);
      if (programs == 0)
        programs = "no episodes";
      else
        programs += (programs == 1) ? " episode" : " episodes";

      html += '<img src="' + thumbnail + '"><p class="number"><span>' + programs + ' episodes</span></p></li>';
      log ('channel ' + channelgrid [chan]['id'] + ': ' + channelgrid [chan]['name']);
      }
    else
      html += '<li></li>';
    }

  return html;
  }

function next_channel_square (channel)
  {
  for (var i = channel + 1; i <= 99; i++)
    {
    if (i in channelgrid)
      return i;
    }

  for (var i = 11; i <= channel; i++)
    {
    if (i in channelgrid)
      return i;
    }

  panic ("No next channel!")
  }

function channels_in_category (cat)
  {
  var num_channels = 0;

  for (var c = 1; c <= 9; c++)
    {
    if ("" + cat + "" + c in channelgrid)
      num_channels++;
    }

  log ('channels in category ' + cat + ': ' + num_channels);
  return num_channels;
  }

function programs_in_channel (channel)
  {
  var num_programs = 0;

  if (channel in channelgrid)
    {
    var real_channel = channelgrid [channel]['id'];

    for (p in programgrid)
      {
      if (programgrid [p]['channel'] == real_channel)
        num_programs++;
      }
    }

  return num_programs;
  }

function first_category()
  {
  for (var cat = 1; cat <= 9; cat++)
    {
    if (channels_in_category (cat) > 0)
      return cat;
    }
  }

function next_category()
  {
  log ('next category');

  for (var cat = parseInt (current_category) + 1; cat <= 9; cat++)
    {
    if (channels_in_category (cat) > 0)
      return cat;
    }

  for (var cat = 1; cat < parseInt (current_category); cat++)
    {
    if (channels_in_category (cat) > 0)
      return cat;
    }

  return current_category;
  }

function previous_category()
  {
  log ('previous category');

  for (var cat = parseInt (current_category) - 1; cat >= 1; cat--)
    {
    if (channels_in_category (cat) > 0)
      return cat;
    }

  for (var cat = 9; cat > parseInt (current_category); cat--)
    {
    if (channels_in_category (cat) > 0)
      return cat;
    }

  return current_category;
  }

function previous_channel_square (channel)
  {
  for (var i = channel - 1; i > 10; i--)
    {
    if (i in channelgrid)
      return i;
    }

  for (var i = 99; i >= channel; i--)
    {
    if (i in channelgrid)
      return i;
    }

  panic ("No previous channel!")
  }

function first_channel()
  {
  for (var y = 1; y <= 9; y++)
    for (var x = 1; x <= 9; x++)
      {
      if (("" + y + "" + x) in channelgrid)
        return "" + y + "" + x;
      }
  panic ("no channels");
  }

function first_program_in (channel)
  {
  var programs = [];
  var n_programs = 0;

  if (! (channel in channelgrid))
    {
    log ('channel ' + channel + ' not in channelgrid');
    return;
    }

  var real_channel = channelgrid [channel]['id'];

  for (p in programgrid)
    {
    if (programgrid [p]['channel'] == real_channel)
      programs [n_programs++] = p;
    }

  if (programs.length < 1)
    {
    log ('No programs in channel: ' + channel + "(" + real_channel + ")");
    return undefined;
    }

  // unshift here is to match what is in program_line
  programs = programs.sort (function (a,b) { return Math.floor (programgrid [b]['timestamp']) - Math.floor (programgrid [a]['timestamp']) });
  programs.unshift ('');

  return programs [1];
  }

function escape()
  {
  var layer;

  if (thumbing == 'browse-wait')
    return;

  if (thumbing == 'ipg' && ipg_mode == 'episodes')
    {
    ipg_exit_episode_mode();
    return;
    }

  switch (thumbing)
    {
    case 'program': layer = $("#ep-layer");
                    break;

    case 'channel': return;

    case 'ipg':     layer = $("#ipg-layer");
                    break;

    case 'user':    layer = $("#signin-layer");
                    break;

    case 'browse':  layer = $("#ch-directory");
                    break;

    case 'control': layer = $("#control-layer");
                    break;

    case 'end':     return;
    }

  layer.css ("display", layer.css ("display") == "block" ? "none" : "block");

  if (thumbing == 'ipg')
    {
    try
      {
      clearTimeout (ipg_timex);
      clearTimeout (ipg_delayed_stop_timex);
      }
    catch (error)
      {
      }
    }

  if (thumbing == 'ipg' || thumbing == 'user')
    { /* resume(); */ }

  if (thumbing == 'user' || thumbing == 'browse')
    {
    thumbing = saved_thumbing;
    if (thumbing == 'control')
      $("#control-layer").show();
    }

  else if (thumbing == 'ipg' || thumbing == 'control' || thumbing == 'channel')
    {
    thumbing = 'program';
    prepare_channel();
    }

  $("#mask").hide();
  $("#log-layer").hide();
  $("#msg-layer").hide();
  $("#epend-layer").hide();
  $("#body").removeClass ("on");

  if (thumbing == 'channel' || thumbing == 'program')
    {
    if (layer.css ("display") == "none")
      clear_osd_timex();
    }
  }

function turn_off_ancillaries()
  {
  for (var v in { 'signin-layer':'', 'control-layer':'', 'podcast-layer':'', 'ipg-layer':'' })
    $("#" + v).hide();
  }

function kp (e)
  {
  var ev = e || window.event;
  log (ev.type + " keycode=" + ev.keyCode);

  keypress (ev.keyCode);
  }

function keypress (keycode)
  {
  /* if in rss field entry and down key, exit field */

  if (document.activeElement.id == 'podcastRSS' && keycode == 40)
    document.getElementById ('podcastRSS').blur();

  /* entering a form */
  if (thumbing == 'user' && keycode != 27)
    return;

  /* special case, channel browser + navigation key */
  if (thumbing == 'browse' && keycode != 27 && keycode != 37 && keycode != 38 && keycode != 39 && keycode != 40 && keycode != 13 && keycode != 33 && keycode != 34)
    return;

  if (thumbing == 'ipg')
    extend_ipg_timex();

  /* ensure osd is up */

  if (keycode == 37 || keycode == 39 || keycode == 38 || keycode == 40)
    {
    if (thumbing == 'program')
      {
      if ($("#ep-layer").css ('display') == 'none')
        {
        log ('program osd was off');
        extend_ep_layer();
        return;
        }
      else
        reset_osd_timex();
      }
    }

  switch (keycode)
    {
    case 27:
      /* esc */
      if (thumbing == 'whatsnew')
        exit_whats_new();
      else if (thumbing == 'ipg' && ipg_mode == 'episodes')
        escape();
      else if (thumbing == 'ipg' && clips_played == 0)
        { /* do nothing */ }
      else
        escape();
      break;

    case 32:
      /* space */
    case 178:
      /* google TV play/pause */
      if (thumbing == 'channel' || thumbing == 'program')
        pause();
      break;

    case 13:
      /* enter */
      if (thumbing == 'ipg')
        ipg_play();
      else if (thumbing == 'browse')
        browse_enter();
      else if (thumbing == 'channel' || thumbing == 'program')
        switch_to_control_layer();
      else if (thumbing == 'control')
        control_enter();
      else if (thumbing == 'whatsnew')
        whatsnew_enter()
      break;

    case 37:
      /* left arrow */
      if (thumbing == 'end')
        {
        $("#epend-layer").hide();
        channel_left();
        enter_channel();
        }
      else if (thumbing == 'program')
        program_left();
      else if (thumbing == 'ipg')
        ipg_left();
      else if (thumbing == 'control')
        control_left();
      else if (thumbing == 'whatsnew')
        PrevEp();
      else if (thumbing == 'browse')
        browse_left();
      break;

    case 39:
      /* right arrow */
      if (thumbing == 'end')
        {
        $("#epend-layer").hide();
        channel_right();
        enter_channel();
        }
      else if (thumbing == 'program')
        program_right();
      else if (thumbing == 'ipg')
        ipg_right();
      else if (thumbing == 'control')
        control_right();
      else if (thumbing == 'whatsnew')
        NextEp();
      else if (thumbing == 'browse')
        browse_right();
      break;

    case 38:
      /* up arrow */
      if (thumbing == 'program')
        switch_to_ipg();
      else if (thumbing == 'end')
        {
        $("#epend-layer").hide();
        switch_to_ipg();
        }
      else if (thumbing == 'channel')
        enter_channel();
      else if (thumbing == 'control')
        switch_to_ipg();
      else if (thumbing == 'browse')
        browse_up();
      else if (thumbing == 'ipg')
        ipg_up();
      break;

    case 40:
      /* down arrow */
      if (thumbing == 'control')
        enter_channel();
      else if (thumbing == 'end')
        {
        $("#epend-layer").hide();
        enter_channel();
        thumbing = 'program';
        }
      else if (thumbing == 'browse')
        browse_down();
      else if (thumbing == 'ipg')
        ipg_down();
      break;

    case 33:
      /* PgUp */
      if (thumbing == 'browse')
        browse_page_up();
      break;

    case 34:
      /* PgDn */
      if (thumbing == 'browse')
        browse_page_down();
      break;

    case 45:
      /* Ins */
      // if (thumbing == 'ipg')
      //   ipg_preload (ipg_cursor);
      break;

    case 8:
      /* Backspace */
    case 68:
      /* D */
    case 46:
      /* Del */
      if (thumbing == 'ipg')
        unsubscribe_channel();
      break;

    case 82:
      /* R */
      if (thumbing == 'ipg')
        {
        redraw_ipg();
        elastic();
        }
      else if (thumbing == 'program')
        prepare_channel();
      break;

    case 49:
    case 50:
    case 51:
    case 52:
    case 53:
    case 54:
    case 55:
    case 56:
    case 57:
      /* 1, 2, 3... */
      break;

    case 71:
      /* G */
      break;

    case 79:
      /* O */
      $("#log-layer").show();
      break;

    case 66:
      /* B */
      break;

    case 67:
      /* C */
      break;

    case 73:
      /* I */
      if (thumbing == 'channel' || thumbing == 'program')
        dump_configuration_to_log();
      break;

    case 85:
      /* U */
      if (thumbing == 'channel' || thumbing == 'program')
        login_screen();
      break;

    case 87:
      /* W */
      switch_to_whats_new();
      break;

    case 88:
      /* X */
      physical_stop();
      break;
    }
  }

function dump_configuration_to_log()
  {
  log ('PROGRAMS');
  for (var p in programgrid)
    {
    var program = programgrid [p];
    log ('#' + p + ' ch:' + program ['channel'] + ' grid:' + channels_by_id [program ['channel']] + ' ' + program ['name'] + ' time:' + program ['timestamp'] + ' url: ' + best_url (p))
    }
  }

function switch_to_whats_new()
  {
  whatsnew = [];

  log ('whats new');
  var bad_thumbnail = '<img src="http://zoo.atomics.org/video/images-x1/no_images.png">';

  var query = "/playerAPI/whatsNew?user=" + user;

  var d = $.get (query, function (data)
    {
    var lines = data.split ('\n');

    var fields = lines[0].split ('\t');
    if (fields [0] != '0')
      {
      log ('[whatsNew] server error: ' + lines [0]);
      return;
      }

    log ('number of new programs obtained: ' + lines.length);

    var wn = {};
    var wn_count = 0;

    for (var i = 1; i < lines.length; i++)
      {
      var program = lines [i].replace (/\s+$/, '');
      if (program != '')
        {
        if (program in programgrid)
          {
          var real_channel = programgrid [program]['channel'];

          if (! (real_channel in channels_by_id))
            {
            log ('program ' + program + ' is known but channel ' + real_channel + ' is not!');
            continue;
            }

          if (! (real_channel in wn))
            {
            wn_count++;
            wn [real_channel] = [];
            }

          wn [real_channel].push (program);

          /* fixups */
          programgrid [program]['age'] = ageof (programgrid [program]['timestamp']);
          programgrid [program]['duration'] = durationof (programgrid [program]['duration']);

          if (programgrid [program]['thumb'].match (/^(null|false)$/))
            programgrid [program]['thumb'] = '';

          if (programgrid [program]['snapshot'].match (/^(null|false)$/))
            programgrid [program]['snapshot'] = '';

          if (programgrid [program]['snapshot'] != '')
            programgrid [program]['screenshot'] = programgrid [program]['snapshot'];
          else
            programgrid [program]['screenshot'] = programgrid [program]['thumb']

          //log ('whatsnew ' + program + ' (ch: ' + real_channel + '): ' + programgrid [program]['name']);
          }
        else
          log ('program ' + program + ' not known');
        }
      }

    if (wn_count == 0)
      {
      log ('nothing new');
      return;
      }

    for (var y = 1; y <= 9; y++)
      for (var x = 1; x <= 9; x++)
        {
        if (("" + y + "" + x) in channelgrid)
          {
          var channel = channelgrid ["" + y + "" + x]['id'];
          if (channel in wn)
            {
            var grid = channels_by_id [channel];
            log ('whatsnew :: ch:' + channel + ' grid: ' + grid + ' episodes:' + wn [channel].join());
            whatsnew.push ({ 'channel': channel, 'grid': grid, 'episodes': wn [channel] });
            }
          }
        }

    if (whatsnew.length == 0)
      {
      log ('nothing new...');
      return;
      }
    else
      {
      log ('channels with new things: ' + whatsnew.length);
      }

    try { force_pause(); } catch (error) { log ('exception in force_pause!'); };

    escape();
    hide_layers();
    thumbing = 'whatsnew';

    stop_preload();

    log ('what is new?');

    var html = '<p id="whatsnew-title">What\'s New</p>';

    for (var y = 1; y <= 9; y++)
      {
      html += '<ul class="new-list">';

      for (var x = 1; x <= 9; x++)
        {
        if ("" + y + "" + x in channelgrid)
          {
          var thumb = channelgrid ["" + y + "" + x]['thumb'];
          var real_channel = channelgrid ["" + y + "" + x]['id'];

          if (! (real_channel in wn))
            html += '<li></li>';
          else if (thumb == '' || thumb == 'null' || thumb == 'false')
            html += '<li>' + bad_thumbnail + '</li>';
          else
            html += '<li><img src="' + channelgrid ["" + y + "" + x]['thumb'] + '"></li>';
          }
        else
          html += '<li></li>';
        }
      html += '</ul>';
      }

    $("#new-holder").html (html);

    $("body").addClass ("on");

    elastic();
    $("#all-players").hide();
    PlayWhatsNew();
    });
  }

function exit_whats_new()
  {
  StopWhatsNew();
  $("body").removeClass ("on");
  //$("body").css ("background-image", "none");
  thumbing = 'program';
  switch_to_ipg();
  // $("#all-players").show();
  }

function whatsnew_enter()
  {
  StopWhatsNew();
  $("body").removeClass ("on");
  // $("#all-players").show();

  var grid = whatsnew [i]['grid'];
  var episode = whatsnew [i]['episodes'][n];
  var channel = whatsnew [i]['channel'];

  log ('whatsnew enter: want to play episode ' + episode + ' in channel ' + channel + ' at grid location ' + grid)
  enter_category ((""+grid).substring (0, 1));

  for (var c in channel_line)
    {
    if (channel_line [c] == grid)
      {
      channel_cursor = c;
      redraw_channel_line()
      enter_channel();
      /* select episode, but for now, play first */
      for (var p = 1; p <= n_program_line; p++)
        if (episode == program_line [p])
          {
          program_first = 1;
          program_cursor = p;
          current_program = episode;
          prepare_channel();
          play();
          return;
          }
      log ('episode ' + episode + ' not found in channel ' + grid);
      return;
      }
    }
   }

function clear_osd_timex()
  {
  if (osd_timex != 0)
    {
    clearTimeout (osd_timex);
    osd_timex = 0;
    }
  }

function reset_osd_timex()
  {
  clear_osd_timex();
  osd_timex = setTimeout ("osd_timex_expired()", 10000);
  }

function osd_timex_expired()
  {
  osd_timex = 0;
  log ('osd timex expired');

  $("#ep-layer").hide();

  if (thumbing == 'channel')
    {
    thumbing = 'program';
    prepare_channel();
    }
  }

function extend_ep_layer()
  {
  $("#ep-layer").show();
  elastic();
  reset_osd_timex();
  }

function delayed_video_stop()
  {
  log ('delayed video stop: ' + thumbing);
  if ((thumbing == 'ipg' || thumbing == 'browse' || thumbing == 'browse-wait') && current_tube == 'fp')
    {
    try { log ('flowplayer state: ' + flowplayer(fp_player).getState()); } catch (error) {};
    physical_stop();
    }
  }

function switch_to_ipg()
  {
  log ('ipg');

  clear_msg_timex();
  clear_osd_timex()

  // force_pause();
  physical_stop();
  ipg_delayed_stop_timex = setTimeout ("delayed_video_stop()", 5000);

  ipg_cursor = parseInt (channel_line [channel_cursor]);

  if (! (ipg_cursor in channelgrid))
    ipg_cursor = '11';

  redraw_ipg();

  $("#ipg-signin-btn").removeClass ("on");
  $("#ipg-return-btn").removeClass ("on");

  stop_preload();
  $("#loading").hide();

  //if (jQuery.browser.msie && (jQuery.browser.version == '7.0' || jQuery.browser.version == '8.0'))
  if (true)
    {
    $("#control-layer").hide();
    // $("#ep-layer").hide();
    ipg_program_tip();
    $("#ipg-layer").show();
    $("#body").addClass ("on");
    elastic();
    extend_ipg_timex();
    thumbing = 'ipg';
    start_preload_timer();
    return;
    }

  if (thumbing == 'control')
    $("#control-layer").animateWithCss ({ opacity: "0" }, 500, "ease-in-out", function() { $("#control-layer").hide(); $("#control-layer").css ("opacity", "1"); });

  $("#ipg-layer").hide();
  $("#ipg-layer").css ("opacity", "0");
  $("#ipg-layer").show();

  var phase_out_ch =
    {
    opacity: "0",
    bottom: "-3em"
    };

  setTimeout ("outt()", 500);

  $("#ep-layer").animateWithCss ({ opacity: 0 }, 500, "ease-in-out", function()
    {
    $("#ep-layer").css ("display", "none");
    $("#ep-layer").css ("opacity", "1");
    elastic(); /* fixups */
    });

  thumbing = 'ipg';
  start_preload_timer();
  }

function outt()
  {
  $("#ipg-layer").animateWithCss ({ opacity: "1" }, 500, "ease-in", function() {});
  setTimeout ("ipg_failsafe()", 500);
  }

function ipg_failsafe()
  {
  $("#ipg-layer").css ("opacity", "1");
  $("#ipg-layer").show();
  $("#body").addClass ("on");
  elastic();
  extend_ipg_timex();
  }

function ipg_idle()
  {
  ipg_timex = 0;
  if (thumbing == 'ipg')
    switch_to_whats_new();
  }

function extend_ipg_timex()
  {
  if (ipg_timex)
    clearTimeout (ipg_timex);
  ipg_timex = setTimeout ("ipg_idle()", 65000);
  }

function redraw_ipg()
  {
  var html = "";
  
  var bad_thumbnail = '<img src="http://zoo.atomics.org/video/images-x1/no_images.png">';

  for (var y = 1; y <= 9; y++)
    {
    html += '<ul class="ipg-list" id="row-' + y + '">';

    for (var x = 1; x <= 9; x++)
      {
      if ("" + y + "" + x in channelgrid)
        {
        var thumb = channelgrid ["" + y + "" + x]['thumb'];
        if (thumb == '' || thumb == 'null' || thumb == 'false')
          html += '<li class="clickable draggable" id="ipg-' + y + '' + x + '">' + bad_thumbnail + '</li>';
        else
          html += '<li class="clickable draggable" id="ipg-' + y + '' + x + '"><img src="' + channelgrid ["" + y + "" + x]['thumb'] + '"></li>';
        }
      else
        html += '<li class="clickable droppable" id="ipg-' + y + '' + x + '"><img src="' + root + 'add_channel.png" class="add-ch"></li>';
      }

    html += '</ul>';
    }

  $("#list-holder").html (html);
  $("#list-holder img").error(function () { $(this).unbind("error").attr("src", "http://zoo.atomics.org/video/images-x1/no_images.png"); });
  $("#list-holder .clickable").bind ('click', function () { ipg_click ($(this).attr ('id')); });

  $(function()
    {
    $( "#list-holder .draggable" ).draggable({ zIndex: 9999, opacity: 1 });
    $( "#list-holder .droppable" ).droppable
         ({
             revert: "invalid",
             activate: function() { if (!dragging) { dragging = true; log ('dragstart'); } },
             deactivate: function() { setTimeout ("drag_cleanup()", 200); },
             accept: ".draggable",
             activeClass: "ui-state-hover",
             hoverClass: "ui-state-active",
             drop: function (event, ui)
                      { setTimeout ('move_channel ("' + ui.draggable.attr('id') + '", "' + $(this).attr('id') + '")', 20); }
         });
    });

  // ipg_cursor = parseInt (channel_line [channel_cursor]);

  if (ipg_cursor > 0)
    $("#ipg-" + ipg_cursor).addClass ("on");

  ipg_metainfo();

  ipg_fixup_margin();
  ipg_fixup_middle();
  }

function drag_cleanup()
  {
  if (dragging)
    {
    dragging = false;
    log ('drag cleanup');
    redraw_ipg();
    elastic();
    }
  }

function move_channel (src, dst)
  {
  dragging = false;

  stop_preload();

  src = src.replace (/^ipg-/, '');
  dst = dst.replace (/^ipg-/, '');

  log ('MOVE CHANNEL: ' + src + ' TO ' + dst + ' (id: ' + channelgrid [src] + ')');

  channelgrid [dst] = channelgrid [src];
  channels_by_id [channelgrid [dst]['id']] = dst;

  ipg_cursor = dst;

  delete (channelgrid [src]);

  redraw_ipg();
  elastic();

  start_preload_timer();
  }

function ipg_metainfo()
  {
  if (ipg_cursor in channelgrid)
    {
    var thumbnail = channelgrid [ipg_cursor]['thumb'];

    if (thumbnail == '' || thumbnail == 'null' || thumbnail == 'false')
      thumbnail = 'http://zoo.atomics.org/video/images-x1/no_images.png'

    var name = channelgrid [ipg_cursor]['name'];
    if (name == '')
      name = '[no title]';

    $("#ch-thumb-img").attr ("src", thumbnail);
    $("#ch-name").html ('<p>' + name + '</p>');
    $("#ep-name").html ('');

    var desc = channelgrid [ipg_cursor]['desc'];
    if (desc == undefined || desc == 'null')
      desc = '';

    $("#description").html ('<p>' + desc + '</p>');

    var display_eps;
    var n_eps = programs_in_channel (ipg_cursor);

    if (channelgrid [ipg_cursor]['count'] == undefined)
      {
      /* brackets quietly indicate a data inconsistency */
      display_eps = '[' + n_eps + ']';
      }
    else if (n_eps != channelgrid [ipg_cursor]['count'])
      {
      display_eps = channelgrid [ipg_cursor]['count'] + ' [' + n_eps + ']';
      if (! ('refetched' in channelgrid [ipg_cursor]))
        {
        channelgrid [ipg_cursor]['refetched'] = true;
        fetch_programs_in (channelgrid [ipg_cursor]['id']);
        }
      }
    else
      display_eps = n_eps;

    if (n_eps > 0)
      {
      var first = first_program_in (ipg_cursor);
      $("#update-date").html (ageof (programgrid [first]['timestamp']));
      $("#update").show();
      }
    else
      $("#update").hide();

    $("#ch-episodes").html (display_eps);
    $("#ep-number").show();
    }
  else
    {
    if (ipg_cursor < 0)
      {
      $("#ch-thumb-img").attr ("src", "");
      $("#ch-name").html ('<p></p>');
      }
    else
      {
      $("#ch-thumb-img").attr ("src", "http://zoo.atomics.org/video/images-x1/default_channel.png");
      if (readonly_ipg)
        $("#ch-name").html ('<p></p>');
      else
        $("#ch-name").html ('<p>Add Channel</p>');
      }

    $("#ep-name").html ('<p></p>');
    $("#description").html ('<p></p>');
    $("#ep-number").hide();
    $("#update").hide();
    }
  }

function stop_preload()
  {
  clearTimeout (ipg_preload_timex);

  if (fp_preloaded == 'yt')
    {
    try { ytplayer.stopVideo(); } catch (error) {};
    try { ytplayer.unMute(); } catch (error) {};
    log ('cleared preload: ' + fp_preloaded);
    }
  else if (fp_preloaded != '')
    {
    fp [fp_preloaded]['mute'] = false;

    flowplayer (fp_preloaded).stop();
    flowplayer (fp_preloaded).unmute();

    log ('cleared preload: ' + fp_preloaded);
    }

  fp_preloaded = '';
  $("#preload").html ('None');
  }

function start_preload_timer()
  {
  if (thumbing == 'ipg' && ipg_cursor in channelgrid)
    {
    ipg_preload_timex = setTimeout ("preload_this_square()", 1000);
    $("#preload").html ('Timer...');
    }
  }

function preload_this_square()
  {
  if (thumbing == 'ipg' && ipg_cursor in channelgrid)
    ipg_preload (ipg_cursor);
  }


function ipg_exit_episode_mode()
  {
  log ('ipg mode: episodes -> tip');
  $("#ipg-content").removeClass ("fade");
  ipg_program_tip();
  ipg_mode = 'tip';
  }

function ipg_sync()
  {
  if (ipg_cursor in channelgrid)
    {
    ipg_set_channel (ipg_cursor);
    ipg_program_index();
    $("#ipg-content").addClass("fade");
    $("#ep-list .clickable").bind ('click', function() { ep_click ($(this).attr('id')); });
    $("#ep-list .clickable").hover (ipg_episode_hover_in, ipg_episode_hover_out);
    ipg_mode = 'episodes';
    }
  else
    {
    ipg_program_tip();
    $("#ipg-content").removeClass("fade");
    ipg_mode = 'tip';
    }
  }

function ipg_right()
  {
  log ("IPG RIGHT: old ipg cursor: " + ipg_cursor);

  if (ipg_mode == 'episodes')
    {
    physical_mute();
    program_right();
    return;
    }

  if (ipg_cursor < 0)
    {
    $("#ipg-signin-btn").removeClass ("on");
    $("#ipg-return-btn").removeClass ("on");
    ipg_cursor = parseInt (channel_line [channel_cursor]);
    }
  else
    {
    $("#ipg-" + ipg_cursor).removeClass ("on");

    // ipg_cursor = next_channel_square (ipg_cursor)

    if (parseInt (ipg_cursor) == 99)
      ipg_cursor = 11;
    else if (parseInt (ipg_cursor) % 10 == 9)
      ipg_cursor = parseInt (ipg_cursor) + 2; /* 39 -> 41 */
    else
      ipg_cursor = parseInt (ipg_cursor) + 1;
    }

  log ("new ipg cursor: " + ipg_cursor);

  $("#ipg-" + ipg_cursor).addClass ("on");
  ipg_metainfo();

  stop_preload();
  start_preload_timer();

  ipg_program_tip();
  }

function ipg_left()
  {
  log ("IPG LEFT: old ipg cursor: " + ipg_cursor);

  if (ipg_mode == 'episodes')
    {
    program_left();
    return;
    }

  if (ipg_cursor < 0)
    return;

  $("#ipg-" + ipg_cursor).removeClass ("on");

  // ipg_cursor = previous_channel_square (ipg_cursor)

  if (parseInt (ipg_cursor) % 10 == 1)
    {
    log ('switching to ipg left panel');
    ipg_cursor = -2;
    $("#ipg-signin-btn").addClass ("on");
    return;
    }

  if (parseInt (ipg_cursor) == 11)
    ipg_cursor = 99;
  else if (parseInt (ipg_cursor) % 10 == 1)
    ipg_cursor = parseInt (ipg_cursor) - 2; /* 41 -> 39 */
  else
    ipg_cursor = parseInt (ipg_cursor) - 1;

  log ("new ipg cursor: " + ipg_cursor);

  $("#ipg-" + ipg_cursor).addClass ("on");
  ipg_metainfo();

  stop_preload();
  start_preload_timer();

  ipg_program_tip();
  }

function ipg_up()
  {
  log ("IPG UP: old ipg cursor: " + ipg_cursor);

  if (ipg_mode == 'episodes')
    {
    ipg_exit_episode_mode();
    return;
    }

  if (ipg_cursor == -1)
    {
    $("#ipg-return-btn").removeClass ("on");
    $("#ipg-signin-btn").addClass ("on");
    ipg_cursor = -2;
    return;
    }
  else if (ipg_cursor == -2)
    {
    $("#ipg-signin-btn").removeClass ("on");
    ipg_cursor = parseInt (channel_line [channel_cursor]);
    /* fall through */
    }
  else
    {
    $("#ipg-" + ipg_cursor).removeClass ("on");

    if (parseInt (ipg_cursor) > 19)
      ipg_cursor = parseInt (ipg_cursor) - 10;
    else
      {
      // disabled wraparound for now
      // ipg_cursor += 80;
      }
    }

  log ("new ipg cursor: " + ipg_cursor);

  $("#ipg-" + ipg_cursor).addClass ("on");
  ipg_metainfo();

  stop_preload();
  start_preload_timer();

  ipg_program_tip();
  }

function ipg_down()
  {
  log ("IPG DOWN: old ipg cursor: " + ipg_cursor);

  if (ipg_mode == 'episodes')
    return;

  if (ipg_cursor < 0)
    {
    if (ipg_cursor == -2)
      {
      $("#ipg-signin-btn").removeClass ("on");
      $("#ipg-return-btn").addClass ("on");
      ipg_cursor = -1;
      return;
      }
    else if (ipg_cursor == -1)
      {
      return;
      }
    }
  else if (ipg_cursor > 90)
    {
    // disable this for now
    // escape();
    // enter_category (current_category, '');
    // enter_channel();
    // return;
    }

  $("#ipg-" + ipg_cursor).removeClass ("on");

  if (parseInt (ipg_cursor) < 90)
    ipg_cursor = parseInt (ipg_cursor) + 10;
  else
    {
    // disabled wraparound for now
    // ipg_cursor -= 80;
    }
  log ("new ipg cursor: " + ipg_cursor);

  $("#ipg-" + ipg_cursor).addClass ("on");
  ipg_metainfo();

  stop_preload();
  start_preload_timer();

  ipg_program_tip();
  }

function ipg_resume()
  {
  log ('ipg resume');

  /* this may have received focus */
  $('#ipg-return-btn').blur();

  if (current_tube == '')
    {
    log_and_alert ('nothing was playing');
    return
    }

  clearTimeout (ipg_timex);
  clearTimeout (ipg_delayed_stop_timex);

  escape();
  enter_category (current_category, '');
  enter_channel();

  stop_preload();
  play_program();
  }

function ipg_set_channel (grid)
  {
  enter_category (Math.floor (grid/10), '');

  program_cursor = 1;
  program_first = 1;

  current_program = first_program_in (grid);

  for (var c in channel_line)
    if (channel_line [c] == grid)
      {
      channel_cursor = c;
      enter_channel();
      /* and then yucky fixups (temporary) */
      thumbing = 'ipg';
      $("#ipg-layer").show();
      clear_osd_timex();
      return;
      }

  log_and_alert ('was unable to enter channel: ' + grid);
  }

function ipg_program_tip()
  {
  $("#ep-container").hide();
  $("#ep-tip").show();
  $("#ep-layer").show();
  $("#ipg-content").removeClass ("fade");
  $("#ep-panel").attr ("src", root + '/ep_panel_off.png');

  ipg_mode = 'tip';

  if (ipg_cursor in channelgrid && !channelgrid [ipg_cursor]['thumbcache'])
    {
    channelgrid [ipg_cursor]['thumbcache'] = 1;
    setTimeout ("ipg_episode_thumbs(" + ipg_cursor + ")", 400);
    }
  }

function ipg_episode_thumbs (id)
  {
  if (thumbing == 'ipg' && ipg_cursor == id)
    {
    if (channelgrid [id]['thumbcache'] == 2)
      {
      /* thumbs presently being loaded */
      return;
      }

    channelgrid [id]['thumbcache'] = 2;

    log ('preload episode thumbs');
    ipg_set_channel (id);

    for (var p in program_line)
      {
      if (program_line [p] in programgrid)
        {
        var image = new Image();
        image.src = programgrid [program_line[p]]['thumb'];
        }
      }
    }
  }

function ipg_program_index()
  {
  $("#ep-list").html (ep_html());
  $("#ep-layer").show();
  $("#ep-tip").hide();
  $("#ep-container").show(); //.fadeIn()
  $("#ep-panel").attr ("src", root + '/ep_panel_on.png');
  }

function ipg_click (id)
  {
  if (dragging)
    {
    log ('eating apparent false click');
    return;
    }
  else
    log ('CLICK');

  if (thumbing == 'ipg')
    {
    id = id.replace (/^ipg-/, '');
    log ('ipg_click: ' + id);

    var previous_cursor = ipg_cursor;
    ipg_cursor = id;

    ipg_sync();

    if (ipg_cursor != previous_cursor)
      {
      $("#ipg-" + previous_cursor).removeClass ("on");
      $("#ipg-" + id).addClass ("on");

      ipg_metainfo();
      current_program = first_program_in (id);
      update_bubble();

      stop_preload();
      start_preload_timer();
      }
    else
      {
      log ('PLAY. ipg cursor: ' + ipg_cursor + ', id: ' + id);
      ipg_play();
      }
    }
  }

function ipg_play()
  {
  log ('ipg play: ' + ipg_cursor);

  if (ipg_cursor < 0)
    {
    if (ipg_cursor == -1)
      {
      ipg_resume();
      }
    else if (ipg_cursor == -2)
      {
      login_screen();
      }

    clearTimeout (ipg_timex);
    return;
    }

  if (! (ipg_cursor in channelgrid))
    {
    if (readonly_ipg)
      {
      log_and_alert ('You cannot add channels while viewing a shared IPG');
      return;
      }
    clearTimeout (ipg_timex);
    browse();
    return;
    }

  if (ipg_mode == 'tip')
    {
    ipg_mode = 'episodes';
    ipg_sync();
    return;
    }

  if (programs_in_channel (ipg_cursor) < 1)
    {
    log_and_alert ('no programs in channel!');
    return;
    }

  clearTimeout (ipg_timex);
  clearTimeout (ipg_delayed_stop_timex);

//xyz
  if (ipg_mode == 'episodes' && program_cursor != 1)
    {
    fp_preloaded = '';
    physical_seek (0);
    physical_unmute();
    physical_play();
    unhide_player (current_tube);
    $("#ipg-layer").hide();
    thumbing = 'program';
    stop_all_other_players();
    return;
    }

  if (current_tube == '')
    current_tube = 'fp';

  if (fp_preloaded != '')
    {
    ipg_preload_play();
    return;
    }

  $("#ipg-layer").hide();



  enter_category ((""+ipg_cursor).substring (0, 1));

  for (var c in channel_line)
    {
    if (channel_line [c] == ipg_cursor)
      {
      channel_cursor = c;
      enter_channel();

      current_program = first_program_in (ipg_cursor);
      update_bubble();
      redraw_channel_line()

      thumbing = 'channel';
      $("#body").removeClass ("on");
      play_first_program_in (channel_line [channel_cursor]);
      enter_channel();
      return;
      }
    }

  log_and_alert ('ipg_play: no channel');
  }

function channel_right()
  {
  if (channel_cursor < n_channel_line)
    channel_cursor++;
  else
    {
    log ('channel_right: current category: ' + current_category + ', next category: ' + next_category());
    enter_category (next_category(), 'b');
    }
  redraw_channel_line();
  prepare_channel();

  play_first_program_in (channel_line [channel_cursor]);
  }

function channel_left()
  {
  if (channel_cursor > 1)
    channel_cursor--;
  else
    enter_category (previous_category(), 'e');

  redraw_channel_line();
  prepare_channel();

  play_first_program_in (channel_line [channel_cursor]);
  }

function redraw_channel_line()
  {
  for (y = 1; y <= 9; y++)
    {
    var c = $("#c-" + current_category + "-li-" + y);

    if (y == channel_cursor)
      {
      if (!c.hasClass ("on"))
        $("#c-" + current_category + "-li-" + y).addClass ("on");
      }
    else
      {
      if (c.hasClass ("on"))
        $("#c-" + current_category + "-li-" + y).removeClass ("on");
      }
    }

  // old way, replace all html inside div:
  // $("#ch-list-" + current_category).html (ch_html (current_category));
  }

function ep_click (id)
  {
log ("EP CLICK");
  if (thumbing == 'program' || thumbing == 'ipg')
    {
    id = id.replace (/^p-li-/, '');
    log ('ep_click: ' + id);
    program_cursor = id;
    redraw_program_line();
    physical_stop();
    if (thumbing == 'ipg')
      {
      thumbing = 'program';
      $("#ipg-layer").hide();
      }
    if (tube() == 'fp') play_program();
    }
  }

function ipg_episode_hover_in()
  {
  $(this).addClass ("hover");
  $("#p-li-" + program_cursor).removeClass ("on");

  var id = $(this).attr('id').replace (/^p-li-/, '');
  var program = programgrid [program_line [id]];

  $("#ep-layer-ep-title").html (program ['name']);
  $("#ep-age").html (ageof (program ['timestamp']));
  $("#ep-duration").html (durationof (program ['duration']));
  }

function ipg_episode_hover_out()
  {
  $(this).removeClass ("hover");
  $("#p-li-" + program_cursor).addClass ("on");

  var program = programgrid [program_line [program_cursor]];

  $("#ep-layer-ep-title").html (program ['name']);
  $("#ep-age").html (ageof (program ['timestamp']));
  $("#ep-duration").html (durationof (program ['duration']));
  }

function program_right()
  {
  log ('program right');

  if (program_cursor < n_program_line)
    {
    program_cursor++;
    redraw_program_line();
    physical_stop();
    if (tube() == 'fp' || tube() == 'yt') play_program();
    }
  else
    {
    physical_stop();
    end_message (0);
    }
  }

function program_left()
  {
  log ('program left');

  if (program_cursor > 1)
    program_cursor--;
  else
    return;

  redraw_program_line();
  play_program();
  }

function redraw_program_line()
  {
  log ('redraw program line');

  while (program_cursor < program_first)
    {
    --program_first;
    $("#ep-list").html (ep_html());
    $("#ep-list img").error(function () { $(this).unbind("error").attr("src", "http://zoo.atomics.org/video/images-x1/no_images.png"); });
    $("#ep-list .clickable").bind ('click', function() { ep_click ($(this).attr('id')); });
    }

  while (program_cursor >= program_first + max_programs_in_line)
    {
    ++program_first;
    $("#ep-list").html (ep_html());
    $("#ep-list img").error(function () { $(this).unbind("error").attr("src", "http://zoo.atomics.org/video/images-x1/no_images.png"); });
    $("#ep-list .clickable").bind ('click', function() { ep_click ($(this).attr('id')); });
    }

  log ('redraw program line');
  for (var i = program_first; i <= n_program_line && i < program_first + max_programs_in_line; i++)
    {
    if (i == program_cursor)
      {
      if (! $("#p-li-" + i).hasClass ("on"))
        $("#p-li-" + i).addClass ("on");
      }
    else
      {
      if ($("#p-li-" + i).hasClass ("on"))
        $("#p-li-" + i).removeClass ("on");
      }
    }
  }

function setup_ajax_error_handling()
  {
  $.ajaxSetup ({ error: function (x, e)
    {
    $("#loading").hide();
    $("#msg-layer").hide();

    if (x.status == 0)
      {
      log_and_alert ('No network!');
      }
    else if (x.status == 404)
      {
      log_and_alert ('404 Not Found');
      }
    else if (x.status == 500)
      {
      log_and_alert ('500 Internal Server Error');
      }
    else if (e == 'timeout')
      {
      log_and_alert ('Network request timed out!');
      }
    else
      {
      log_and_alert ('Unknown error: ' + x.responseText);
      }
    }});
  }

function server_grid (coord)
  {
  var n = 0;
  var conv = {};

  for (var y = 1; y <= 9; y++)
    for (var x = 1; x <= 9; x++)
      conv ["" + y + "" + x] = ++n;

  return conv [coord];
  }

function getcookie (id)
  {
  log ('getcookie: ' + document.cookie);

  var fields = document.cookie.split (/; */);

  for (var i in fields)
    {
    try
      {
      var kv = fields[i].split ('=');
      // log ('k: ' + kv[0] + ' v: ' + kv[1]);
      if (kv [0] == id)
        return kv [1];
      // log ('nope');
      }
    catch (err)
      {
      // this catch is necessary because of a bug in Google TV
      log ('some error occurred: ' + err.description);
      }
    }

  return undefined;
  }

function login_screen()
  {
  /* this may have received focus */
  $('#ipg-signin-btn').blur()

  force_pause();
  saved_thumbing = thumbing;
  thumbing = 'user';

  $("#control-layer").hide();
  $("#mask").show();
  $("#signin-layer").show();
  }

function submit_login()
  {
  var things = [];
  var params = { 'L-email': 'email', 'L-password': 'password' };

  // this is broken in Opera, appears to be Javascript bug
  for (var p in params)
    {
    var v = $('#' + p).val();
    log ("value1: " + v);
    v = encodeURIComponent (v);
    log ("value2: " + v);
    things.push ( params [p] + '=' + v );
    }

  var serialized = things.join ('&');
  log ('login: ' + serialized);
  
  $.post ("/playerAPI/login", serialized, function (data)
    {
    var lines = data.split ('\n');
    var fields = lines[0].split ('\t');

    if (fields [0] == "0")
      {
      var fields = lines[1].split ('\t');

      user = fields [1];
      username = fields [2];
      $("#user").html (username);
      log ('[explicit login] welcome ' + username + ', AKA ' + user);

      if (readonly_ipg)
        {
        /* this user has now upconverted */
        readonly_ipg = false;
        via_shared_ipg = false;
        }

      /* wipe out the current guest account program+channel data */
      channelgrid = {};
      programgrid = {};
      escape();

      resume();
      activated = false;

      fetch_programs();
      }
    else
      log_and_alert ("LOGIN FAIL: " + fields [1]);
    })
  }

function submit_signup()
  {
  var things = [];
  var params = { 'S-name': 'name', 'S-email': 'email', 'S-password': 'password' };

  // this is broken in Opera, appears to be Javascript bug
  for (var p in params)
    {
    var v = $('#' + p).val();
    log ("value1: " + v);
    v = encodeURIComponent (v);
    log ("value2: " + v);
    things.push ( params [p] + '=' + v );
    }

  var serialized = things.join ('&') + '&' + 'user=' + user;
  log ('signup: ' + serialized);

  $.post ("/playerAPI/signup", serialized, function (data)
    {
    log ('signup response: ' + data);

    var lines = data.split ('\n');
    var fields = lines[0].split ('\t');

    if (fields [0] == "0")
      {
      fields = lines[1].split ('\t');

      user = fields [1];
      username = fields [2];
      $("#user").html (username);
      log ('[login via signup] welcome ' + username + ', AKA ' + user);

      if (readonly_ipg)
        {
        /* this user has now upconverted */
        readonly_ipg = false;
        via_shared_ipg = false;
        }

      /* wipe out the current guest account program+channel data */
      channelgrid = {};
      programgrid = {};
      escape();

      if (fields.length >= 3)
        {
        //log_and_alert ('signed up as user: ' + fields [2]);
        username = fields [2];
        $("#user").html (username);
        }
      else
        {
        //log_and_alert ('signed up as user: ' + user);
        }

      resume();
      fetch_programs();
      }
    else
      log_and_alert ("SIGNUP FAIL: " + fields [1]);
    });
  }

function submit_throw()
  {
  if ($("#podcastRSS") == '')
    {
    log ('blank podcastRSS submitted, ignoring');
    return;
    }

  /* always called from IPG, use ipg_cursor */
  var categories = '';

  $("#cate-list .selected").each (function()
    {
    var id = $(this).attr("id").replace (/^addcat-/, '');
    if (categories != '')
      { categories += ','; }
    categories += browsables [id]['category'];
    });
//xxzz
  var url = encodeURIComponent ($("#submit-url").val());

  // $("#throw").serialize()
  var serialized =  'url=' + url + '&' + 'user=' + user + '&' + 'grid=' + server_grid (ipg_cursor) + '&' + 'langCode=' + language + '&' + 'category=' + categories;
  log ('throw: ' + serialized);

  $("#feedback").html ('<p>Please wait...</p>');
  $("#feedback").show();

  $.post ("/playerAPI/channelSubmit", serialized, function (data)
    {
    sanity_check_data ('podcastSubmit', data);
    var lines = data.split ('\n');
    var fields = lines[0].split ('\t');
    if (fields [0] == "0")
      {
      $("#feedback").html ('<p>Successful!</p>');
      //escape();
      log ('podcast thrown successfully!')
      log ('channelSubmit returned: ' + lines[1]);
      fields = lines[1].split('\t');
      // fields: 0=channel-id 1=channel-name 2=channel-thumb
      channelgrid [ipg_cursor] = { 'id': fields[0], 'name': fields[1], 'thumb': fields[2] };
      channels_by_id [fields[0]] = ipg_cursor;
      redraw_ipg();
      add_dirty_channel (fields[0]);
      }
    else
      {
      $("#feedback").html ('<p>FAILED: ' + fields[1] + '</p>');
      //log_and_alert ("PODCAST THROW FAIL: " + fields [1]);
      }
    $("#podcastRSS").html ('');
    })
  }

/* podcast channels submitted by the user, which must be polled */

function add_dirty_channel (channel)
  {
  if (dirty_timex)
    clearTimeout (dirty_timex);

  dirty_delay = 15;
  dirty_channels.push (channel);

  log ('next dirty check: ' + dirty_delay + ' seconds');
  dirty_timex = setTimeout ("dirty()", dirty_delay * 1000);
  }

function dirty()
  {
  log ('dirty!');

  dirty_timex = 0;
  var channels = dirty_channels.join();

  if (channels == '')
    {
    log ('dirty(): no dirty channels!');
    return;
    }

  var cmd = "/playerAPI/programInfo?channel=" + channels + '&' + "user=" + user;

  var d = $.get (cmd, function (data)
    {
    parse_program_data (data);

    /* once program data is returned, remove those channels from dirty list */

    var lines = data.split ('\n');
    for (var i = 1; i < lines.length; i++)
      {
      if (lines [i] != '')
        {
        var fields = lines[i].split ('\t');
        dirty_channels.remove (fields[0]);
        }
      }
    });

  fetch_channels();
  redraw_ipg();

  if (dirty_channels.length > 0)
    {
    dirty_delay += 10;
    log ('next dirty check: ' + dirty_delay + ' seconds');
    dirty_timex = setTimeout ("dirty()", dirty_delay * 1000);
    }
  }

function pre_login()
  {
  // key	agg5eDl0dmRldnIKCxIDTXNvGOg-DA
  // name	9x9
  // loglUrl	/WEB-INF/../images/logo_9x9.png
  // jingleUrl	/WEB-INF/../videos/logo2.swf
  // preferredLangCode	en

  log ('pre_login');

  var d = $.get ("/playerAPI/brandInfo", function (data)
    {
    var lines = data.split ('\n');
    var fields = lines[0].split ('\t');
    if (fields[0] == '0')
      {
      for (var i = 1; i < lines.length; i++)
        {
        var fields = lines[i].split ('\t');
        if (fields[0] == 'loglUrl')
          {
          log ('logo: ' + fields[1]);
          $("#logo").attr ('src', fields[1]);
          }
        else if (fields[0] == 'jingleUrl')
          {
          log ('jingle: ' + fields[1]);
          //$("#opening").css ("display", "block");
          //$("#opening").css("z-index", "99999");
          $("#blue").hide();
          $("#splash").flash({ swf: fields[1], width: "100%", height: "100%", wmode: 'transparent' });
          }
        else if (fields[0] == 'preferredLangCode')
          {
          log ('language: ' + fields[1]);
          language = fields[1];
          }
        }
      login();
      }
    else
      {
      alert ('[brandInfo] failure!');
      }
    });
  }

function login()
  {
  log ('login')
  var u = getcookie ("user");

  // "response to userTokenVerify: 0\tsuccess\n 0\tagg5eDl0dmRldnINCxIGTm5Vc2VyGLc_DA\tGuest\t8040"
  if (u)
    {
    log ('user cookie exists, checking');

    var d = $.get ("/playerAPI/userTokenVerify?token=" + u, function (data)
      {
      if (!sanity_check_data ('userTokenVerify', data))
        {
        // xyz
        }

      log ('response to userTokenVerify: ' + data);

      var lines = data.split ('\n');
      var fields = lines[0].split ('\t');

      if (fields[0] == '0')
        {
        log ('user token was valid');
        fields = lines[1].split ('\t');

        user = u;
        username = fields[2];
        $("#user").html (username);
        log ('[login via cookie] welcome ' + username + ', AKA ' + user);

        if (via_shared_ipg)
          readonly_ipg = true;

        fetch_programs();
        }
      else
        {
        log_and_alert ('user token was not valid');
        login();
        }
      });
    }
  else
    {
    log ('user cookie does not exist, obtaining one');

    if (via_shared_ipg)
      log ('jumpstarting from this ipg: ' + get_ipg_id());

    args = via_shared_ipg ? '?ipg=' + get_ipg_id() : '';

    var d = $.get ("/playerAPI/guestRegister" + args, function (data)
      {
      log ('response to guestRegister: ' + data);
      var u = getcookie ("user");

      var fields = data.split ('\t');
      if (!u && fields [0] == '0')
        {
        log ('no "user" cookie, but login was successful: ' + fields [1])
        user = fields [1];
        fetch_programs();
        }
      else if (u)
        {
        log ('user cookie now exists');
        user = u;
        via_shared_ipg = false;
        fetch_programs();
        }
      else
        panic ("was not able to get a user cookie");
      });
    }
  }

function calculate_empties()
  {
  var n = 0;

  for (var y = 1; y <= 9; y++)
    for (var x = 1; x <= 9; x++)
      if (! (["" + y + "" + x] in channelgrid))
        n++;

  var text;

  if (n == 1)
    text = 'You still have one empty channel';
  else if (n == 81)
    text = 'You have no empty channels!'
  else
    text = 'You still have ' + n + ' empty channels';

  $("#ch-vacancy").html (text);
  }

function browse()
  {
  log ('channel directory browse');

  saved_thumbing = thumbing;
  thumbing = 'browse-wait';
  browsables = [];
  browser_cat_cursor = 1;
  browser_first_cat = 1;
  browser_mode = 'category';

  $("#msg-layer").html ('<p>One moment...</p>');
  $("#msg-layer").show();

  var query = "/playerAPI/categoryBrowse?langCode=en";
  var d = $.get (query, function (data)
    {
    $("#msg-layer").hide();
    sanity_check_data ('categoryBrowse', data);

    calculate_empties();

    $("#add-panel").hide();
    $("#category-panel").show();
    $("#ch-directory").show();
    elastic();

    thumbing = 'browse';

    var html = '';
    var add_html = '';

    var lines = data.split ('\n');

    var fields = lines[0].split ('\t');
    if (fields [0] != '0')
      {
      log_and_alert ('[categoryBrowse] server error: ' + lines [0]);
      return;
      }

    n_browse = 0;
    for (var i = 1; i < lines.length; i++)
      {
      if (lines [i] != '')
        {
        n_browse++;
        var fields = lines[i].split ('\t');
        var xclass = (n_browse == 1) ? ' class="selected"' : '';
        if (n_browse <= max_browse)
          html += '<li id="cat-' + n_browse + '"' + xclass + '><p>' + fields[1] + '<span class="arrow">&raquo;</span></li>';
        add_html += '<li id="addcat-' + n_browse + '"><img id="img-addcat-' + n_browse + '" src="' + root + 'check_off.png"><span>' + fields[1] + '</span></li>';
        browsables [n_browse] = { category: fields[0], name: fields[1] };
        }
      }

    $("#ch-catlist").html (html);
    $("#cate-list").html (add_html);

    $("#main-panel li").bind ('click', function () { browse_click (1, $(this).attr ('id')); });
    $("#ch-catlist li").bind ('click', function () { browse_click (2, $(this).attr ('id')); });
    $("#cate-list  li").bind ('click', function () { browse_click (5, $(this).attr ('id')); });

    $("#main-panel li, #ch-catlist li, cate-list li").hover (browse_hover_in, browse_hover_out);

    browse_set_cursor (2, 1);
    });
  }

function redraw_browser_categories()
  {
  var html = '';

  if (browser_y < browser_first_cat)
    browser_first_cat = browser_y;

  while (browser_y >= browser_first_cat + max_browse)
    browser_first_cat++;

  for (var i = browser_first_cat; i <= n_browse && i < browser_first_cat + max_browse; i++)
    {
    var xclass = (browser_y == i) ? ' class="selected"' : '';
    html += '<li id="cat-' + i + '"' + xclass + '><p>' + browsables[i]['name'] + '<span class="arrow">&raquo;</span></li>';
    }

  $("#ch-catlist").html (html);
  $("#ch-catlist li").bind ('click', function () { browse_click (2, $(this).attr ('id')); });
  $("#ch-catlist li").hover (browse_hover_in, browse_hover_out);

  if (browser_x == 1)
    $("#cat-" + browser_y).addClass ("on");
  }

function browse_hover_in()
  {
  $(this).addClass ("hover");
  }

function browse_hover_out()
  {
  $(this).removeClass ("hover");
  }

function browse_set_cursor (x, y)
  {
  log ('browse [' + browser_mode + '] set cursor: ' + x + ', ' + y);

  if (!browser_x || !browser_y)
    {
    browser_x = x;
    browser_y = y;
    browse_category (browsables [browser_y]['category']);
    return;
    }

  if ((browser_x && x != browser_x) || (browser_y && y != browser_y))
    {
    if (browser_x == 1)
      {
      $("#main-" + browser_y).removeClass ("on");
      if (x == 1 && browser_y != y)
        $("#main-" + browser_y).removeClass ("selected");
      }

    else if (browser_mode == 'category')
      {
      if (browser_x == 2)
        $("#cat-" + browser_y).removeClass ("on");

      else if (browser_x == 3)
        $("#content-" + browser_y).removeClass ("on");
      }

    else if (browser_mode == 'add')
      {
      if (browser_x == 2)
        {
        if (browser_y == 1)
          {
          $("#submit-url-box").removeClass ("on");
          $("#submit-url").blur();
          }
        }
      else if (browser_x == 3)
        $("#addcat-" + browser_y).removeClass ("on");

      else if (browser_x == 4)
        $("#add-go").removeClass ("on");
      }
    }

  if (x == 1)
    {
    $("#main-" + y).addClass ("on");
    $("#main-" + y).addClass ("selected");
    }
  else if (browser_mode == 'category')
    {
    if (x == 2)
      $("#cat-" + y).addClass ("on");

    else if (x == 3)
      $("#content-" + y).addClass ("on");
    }
  else if (browser_mode == 'add')
    {
    if (x == 2)
      {
      $("#submit-url-box").addClass ("on");
      $("#submit-url").focus();
      }
    else if (x == 3)
      $("#addcat-" + y).addClass ("on");

    else if (x == 4)
      $("#add-go").addClass ("on");
    }

  log ('setting new cursor: ' + x + ', ' + y);

  browser_x = x;
  browser_y = y;

  if (browser_x == 1)
    {
    if (browser_y == 1)
      {
      $("#add-panel").hide();
      $("#category-panel").show();
      browser_mode = 'category';
      }
    else if (browser_y == 2)
      {
      $("#category-panel").hide();
      $("#add-panel").show();
      browser_mode = 'add';
      }
    }

  if (browser_mode == 'category' && browser_x == 2 && browser_cat != browsables [browser_y]['category'])
    {
    if (browser_y < browser_first_cat || browser_y >= browser_first_cat + max_browse)
      redraw_browser_categories();

    log ('  selected category, cursor: ' + browser_cat_cursor + ', new: ' + browser_y);
    $("#cat-" + browser_cat_cursor).removeClass ("selected");
    browser_cat_cursor = browser_y;
    $("#cat-" + browser_cat_cursor).addClass ("selected");
    browse_category (browsables [browser_y]['category']);
    }

  if (browser_mode == 'category' && browser_x == 3)
    {
    if (browser_y < browse_list_first || browser_y >= browse_list_first + 8)
      {
      log ("CONTENT OUT OF RANGE! REDRAW!");
      redraw_browse_content();
      }
    }
  }

function browse_left()
  {
  if (browser_x > 1)
    {
    if (browser_mode == 'category' && browser_x == 3)
      {
      browse_set_cursor (parseInt (browser_x) - 1, browser_cat_cursor);
      }
    else if (browser_x == 2)
      {
      switch (browser_mode)
        {
        case 'category': browse_set_cursor (1, 1);
                         break;

        case 'add':      browse_set_cursor (1, 2);
                         break;
        }
      }
    else if (browser_mode == 'add')
      {
      if (browser_x == 2)
        browse_set_cursor (1, 2);
      else if (browser_x == 3)
        {
        if ((parseInt (browser_y) % 4) == 1)
          browse_set_cursor (1, 2);
        else
          browse_set_cursor (browser_x, parseInt (browser_y) - 1);
        }
      else if (browser_x == 4)
        browse_set_cursor (1, 2);
      }
    else if (browser_mode == 'category')
      {
      if ((parseInt (browser_y) % 2) == 1)
        browse_set_cursor (parseInt (browser_x) - 1, browser_y);
      else
        browse_set_cursor (browser_x, parseInt (browser_y) - 1);
      }
    }
  }

function browse_right()
  {
  if (browser_x < 4)
    {
    if (browser_mode == 'add')
      {
      if (browser_x == 1)
        {
        browse_set_cursor (parseInt (browser_x) + 1, 1);
        }
      else if (browser_x == 2)
        {
        /* do nothing */
        }
      else if (browser_x == 3)
        {
        if (browser_y < n_browse)
          browse_set_cursor (browser_x, parseInt (browser_y) + 1);
        }
      }
    else if (browser_mode == 'category')
      {
      if (browser_x == 1)
        browse_set_cursor (parseInt (browser_x) + 1, browser_cat_cursor);
      else if (browser_x == 2)
        browse_set_cursor (parseInt (browser_x) + 1, 1);
      else if (browser_x == 3)
        {
        if (browser_y < n_browse_list)
          browse_set_cursor (browser_x, 1 + parseInt (browser_y));
        }
      }
    }
  }

function browse_up()
  {
  if (browser_mode == 'add')
    {
    if (browser_x == 1 && browser_y > 1)
      {
      browse_set_cursor (browser_x, parseInt (browser_y) - 1);
      }
    else if (browser_x == 3)
      {
      if (browser_y <= 4)
        {
        /* top row */
        browse_set_cursor (2, 1);
        }
      else
        browse_set_cursor (3, parseInt (browser_y) - 4);
      }
    else if (browser_x == 4)
      {
      var new_y = n_browse;
      while ((new_y % 4) != 1) new_y--;
      browse_set_cursor (3, new_y);
      }
    }
  else if (browser_mode == 'category')
    {
    if (browser_x == 1 || browser_x == 2)
      {
      if (browser_y > 1)
        browse_set_cursor (browser_x, parseInt (browser_y) - 1);
      }
    else if (browser_x == 3)
      {
      if (browser_y > 2)
        browse_set_cursor (browser_x, parseInt (browser_y) - 2);
      }
    }
  }

function browse_down()
  {
  if (browser_x == 1 && browser_y >= 2)
    {
    /* last menu item */
    }
  else if (browser_mode == 'add')
    {
    if (browser_x == 2)
      {
      browse_set_cursor (3, 1);
      }
    else if (browser_x == 3)
      {
      if (parseInt (browser_y) + 4 <= n_browse)
        browse_set_cursor (3, parseInt (browser_y) + 4);
      else
        browse_set_cursor (4, 1);
      }
    }
  else if (browser_mode == 'category')
    {
    if (browser_x == 2 && browser_y >= n_browse)
      {
      /* at last category */
      }
    else if (browser_x == 3)
      {
      if (browser_y + 2 <= n_browse_list)
        browse_set_cursor (browser_x, parseInt (browser_y) + 2);
      else if (browser_y + 1 <= n_browse_list)
        browse_set_cursor (browser_x, parseInt (browser_y) + 1);
      }
    else
      {
      browse_set_cursor (browser_x, parseInt (browser_y) + 1);
      }
    }
  }

function browse_click (column, id)
  {
  log ('browse click :: ' + column + ', ' + id);

  if (column == 1)
    {
    id = id.replace (/^main-/, '');
    browse_set_cursor (1, id);
    }
  if (column == 2)
    {
    id = id.replace (/^cat-/, '');
    browse_set_cursor (2, id);
    $("#cat-" + id).addClass ("selected");
    }
  else if (column == 3)
    {
    id = id.replace (/^content-/, '');
    log ('browse_click: ' + id);
    if ($("#content-" + id).hasClass ("on"))
      {
      log ('wants to subscribe to: ' + $("#content-" + id).attr ("data-id"));
      browse_accept ($("#content-" + id).attr ("data-id"));
      }
    else
      {
      /* move cursor here */
      var fields = id.split ('-');
      browse_set_cursor (fields [0], fields [1]);
      }
    }
  else if (column == 5)
    {
    browse_add_checkbox (id);
    }
  }

function browse_add_checkbox (id)
  {
  if ($("#" + id).hasClass ("selected"))
    {
    $("#" + id).removeClass ("selected");
    $("#img-" + id).attr ('src', root + 'check_off.png');
    }
  else
    {
    $("#" + id).addClass ("selected");
    $("#img-" + id).attr ('src', root + 'check_on.png');
    }
  }

function browse_category (category_id)
  {
  dir_waiting_fixup()
  $("#dir-waiting").show();

  $("#content-list").html ('');

  browser_cat = category_id;

  var query = "/playerAPI/channelBrowse?category=" + category_id;

  var d = $.get (query, function (data)
    {
    $("#dir-waiting").hide();
    sanity_check_data ('channelBrowse', data);

    browse_content = {};
    browse_list = {};
    n_browse_list = 0;
    browse_list_first = 1;

    // 0=sequence-number 1=channel-id 2=channel-name 3=thumbnail 4=count

    var lines = data.split ('\n');

    var fields = lines[0].split ('\t');
    if (fields [0] != '0')
      {
      log_and_alert ('[channelBrowse] server error: ' + lines [0]);
      return;
      }

    var category = parseInt (lines[1]);

    if (category != browser_cat)
      {
      log ('ignoring obsolete information for category: ' + category + ' (category is now ' + browser_cat + ')');
      return;
      }

    /* remove the status code line */
    lines.shift();
    lines.shift();

    for (var i = 0; i < lines.length; i++)
      {
      if (lines [i] != '')
        {


        var fields = lines[i].split ('\t');
log ('BROWSE: ' + fields[1]);

        if (parseInt (fields[1]) > 0)
          {
          n_browse_list++;

          browse_content [fields[1]] = { 'name': fields[2], 'thumb': fields[3], 'count': fields[4] };
          browse_list [n_browse_list] = { 'id': fields[1], 'name': fields[2], 'thumb': fields[3], 'count': fields[4] };
          }
        }
      }

    redraw_browse_content();
    });

  }

function redraw_browse_content()
  {
  var html = '';

  if (browser_mode == 'category' && browser_x == 3)
    {
    if (browser_y < browse_list_first)
      {
      browse_list_first = browser_y;
      if ((browse_list_first % 2) != 1)
        browse_list_first--;
      }

    while (browser_y >= browse_list_first + 8)
      browse_list_first += 2;
    }

  for (var i = browse_list_first; i <= n_browse_list && i < browse_list_first + 8; i++)
    {
    var content = browse_list [i];

    html += '<li id="content-' + i + '" data-id="' + content['id'] + '"><img src=' + content['thumb'] + ' class="thumbnail">';
    html += '<p class="chdir-title">' + content['name'] + '</p>';

    var eps = (content['count'] == 1) ? (content['count'] + ' episode') : (content['count'] + ' episodes');

    html += '<p class="chdir-meta">' + eps + '<br>0 subscribers</p>';
    html += '<div class="msgbar">';

    if (content['id'] in channels_by_id)
      html += '<p class="status">Subscribed</p></div></li>';
    else
      html += '<p class="tip">Press ENTER to subscribe</p></div></li>';
    }

  $("#content-list").html (html);
  $("#content-list li").bind ('click', function () { browse_click (3, $(this).attr ('id')); });
  $("#content-list li").hover (browse_hover_in, browse_hover_out);

  browse_set_cursor (browser_x, browser_y);

  if (browser_mode == 'category' && browser_x == 3)
    $("#content-" + browser_y).addClass ("on");
  }

function browse_enter()
  {
  if (browser_mode == 'category')
    {
    var id = $("#content-" + browser_x + '-' + browser_y).attr ("data-id");
    if (id)
      browse_accept (id);
    }
  else if (browser_mode == 'add')
    {
    if (browser_x == 3)
      browse_add_checkbox ("addcat-" + browser_y);
    }
  }

function browse_accept (channel)
  {
  if (channel in channels_by_id)
    {
    log ('wants to unsubscribe: ' + channel);
    }
  else
    {
    log ('subscribe: ' + channel +  ' (at ' + server_grid (ipg_cursor) + ')');

    var cmd = "/playerAPI/subscribe?user=" + user + '&' + "channel=" + channel + '&' + "grid=" + server_grid (ipg_cursor);
    var d = $.get (cmd, function (data)
      {
      continue_acceptance (channel);
      });
    }
  }

function continue_acceptance (new_channel_id)
  {
  log ('accepting new channel ' + new_channel_id);
  stop_preload();

  /* insert channel */

  var name  = browse_content [new_channel_id]['name'];
  var thumb = browse_content [new_channel_id]['thumb'];
  var count = browse_content [new_channel_id]['count'];

  channelgrid [ipg_cursor] = { 'id': new_channel_id, 'name': name, 'thumb': thumb, 'count': count };
  channels_by_id [new_channel_id] = ipg_cursor;

  redraw_ipg();
  elastic();

  /* obtain programs */

  log ('obtaining programs for: ' + new_channel_id);

  var cmd = "/playerAPI/programInfo?channel=" + new_channel_id;

  var d = $.get (cmd, function (data)
    {
    sanity_check_data ('programInfo', data);
    parse_program_data (data);
    escape();
    redraw_ipg();
    elastic();
    start_preload_timer();
    });
  }

function unsubscribe_channel()
  {
  if (readonly_ipg)
    {
    log_and_alert ('You cannot unsubscribe channels in a shared IPG');
    return;
    }

  if (ipg_cursor in channelgrid)
    {
    var grid = server_grid (ipg_cursor);
    var channel = channelgrid [ipg_cursor]['id'];

    if (channelgrid [ipg_cursor]['type'] == 'SYSTEM')
      {
      log_and_alert ('Cannot unsubscribe a system channel');
      return;
      }

    stop_preload();

    var cmd = "/playerAPI/unsubscribe?user=" + user + '&' + "channel=" + channel + '&' + "grid=" + grid;
    var d = $.get (cmd, function (data)
      {
      delete (channelgrid [ipg_cursor]);
      delete (channels_by_id [channel]);
      redraw_ipg();
      elastic();
      });
    }
  }

function sanity_check_data (what, data)
  {
  log ('sanity check ' + what);

  if (data.match (/\!DOCTYPE/))
    {
    log ('sanity check: a !DOCTYPE was found in results from ' + what + ' API');
    return false;
    }

  var lines = data.split ('\n');

  if (lines.length > 9 && lines [0] == '' && lines [1] == '')
    {
    log_and_alert ('very bad data returned from ' + what + ' API');
    return false;
    }

  return true;
  }

function tube()
  {
  /* will be more complicated when there is preloading */
  return current_tube;
  }

function force_pause()
  {
  remembered_pause = physical_is_paused();

  if (!remembered_pause)
    pause();
  }

function resume()
  {
  log ('resume');
  if (remembered_pause != physical_is_paused())
    {
    pause();
    remembered_pause = physical_is_paused();
    }
  }

function pause()
  {
  if (physical_is_paused())
    {
    physical_play();
    $("#btn-play").hide();
    $("#btn-pause").show();
    }
  else
    {
    physical_pause();
    $("#btn-pause").hide();
    $("#btn-play").show();
    }
  }

function unhide_player (player)
  {
  log ('unhide: ' + player);

  switch (player)
    {
    case "jw":

      $("#v").hide();
      $("#fp1").hide();
      $("#fp2").hide();
      $("#jw2").show();
      break;

    case "fp":

      $("#v").hide();
      $("#jw2").hide();
      $("#yt1").hide();

      if (fp_player == 'player1')
        {
        $("#fp2").css ("visibility", "hidden");
        $("#fp1").css ("visibility", "visible");
        }
      else
        {
        $("#fp1").css ("visibility", "hidden");
        $("#fp2").css ("visibility", "visible");
        }

      $("#fp1").show();
      $("#fp2").show();

      break;

    case "yt":

      $("#v").hide();
      $("#fp1").hide();
      $("#fp2").hide();
      $("#yt1").show();
      $("#yt1").css ("visibility", "visible");
      break;
    }
  }

function physical_start_play (url)
  {
  if (url.match (/youtube\.com/))
    start_play_yt (url);

  else if (url.match (/^http:/))
    start_play_html5 (url);

  else if (url.match (/^jw:/))
    start_play_jw (url);

  else if (url.match (/^fp:/))
    start_play_fp (url);

  update_bubble();
  $("#btn-play").hide();
  $("#btn-pause").show();
  }

function start_play_yt (url)
  {
  yt_video_id = url.match (/v=([^&]+)/)[1];
  log ('YouTube video: ' + yt_video_id);
  setup_yt();
  }

function start_play_jw (url)
  {
  jw_position = 0;
  current_tube = 'jw';

  // ugh! don't know actual url until player is chosen
  url = best_url (current_program);

  jw_video_file = url.replace (/^jw:/, '');

  log ('setting up JW player, video file is: ' + jw_video_file);
  unhide_player ("jw");
  $("#yt1").css ("visibility", "visible");

  if (jwplayer)
    jw_play();
  else
    jw_timex = setInterval ("retry_jw_start()", 50);
  }

function retry_jw_start()
  {
  if (jwplayer)
    {
    clearTimeout (jw_timex);
    jw_play();
    }
  }

function jw_play()
  {
  log ("jw STOP");
  // jwplayer.sendEvent ('STOP');
  physical_stop();
  log ("jw LOAD " + jw_video_file);
  jwplayer.sendEvent ('LOAD', jw_video_file)
  log ("jw PLAY");
  jwplayer.sendEvent ('PLAY');
  jw_previous_state = '';
  jwplayer.addModelListener ('TIME', 'jw_progress' );
  jwplayer.addModelListener ('STATE', "jw_state_change()" );
  }

function jw_play_nothing()
  {
return;
  jw_video_file = "nothing.flv";
  log ("jw LOAD " + jw_video_file);
  jwplayer.sendEvent ('LOAD', jw_video_file)
  }

function physical_stop()
  {
  switch (tube())
    {
    case "jw": log ('jw STOP');
               try { jwplayer.removeModelListener ('STATE'); } catch (error) {};
               try { jwplayer.sendEvent ('STOP'); } catch (error) {};
               break;

    case "fp": log ('fp STOP');
               if (flowplayer)
                 try { flowplayer (fp_player).stop(); } catch (error) {};
               break;

    case "yt": log ('yt STOP');
               if (ytplayer)
                 ytplayer.stopVideo();
               break;

    }
  }

function physical_mute()
  {
  switch (tube())
    {
    case "jw": break;

    case "fp": log ('fp MUTE');
               if (flowplayer)
                 try { flowplayer (fp_player).mute(); } catch (error) {};
               fp [fp_player]['mute'] = true;
               break;

    case "yt": log ('yt STOP');
               if (ytplayer)
                 try { ytplayer.mute(); } catch (error) {};
               break;
    }
  }

function physical_unmute()
  {
  switch (tube())
    {
    case "jw": break;

    case "fp": log ('fp UNMUTE');
               if (flowplayer)
                 try { flowplayer (fp_player).unmute(); } catch (error) {};
               fp [fp_player]['mute'] = false;
               break;

    case "yt": log ('yt STOP');
               if (ytplayer)
                 try { ytplayer.unMute(); } catch (error) {};
               break;
    }
  }

function jw_state_change()
  {
  var state = jwplayer.getConfig()['state'];
  var previous = jw_previous_state;

  jw_previous_state = state;

  log ('jwplayer state is: ' + state + ', previous state was: ' + previous);

  if (state == 'COMPLETED' && previous != 'COMPLETED')
    {
    log ('jw now completed');
    log ("jw STOP");
    //jwplayer.sendEvent ('STOP');
    physical_stop();
    // $("#loading").hide();
    ended_callback();
    }

  if (state == 'IDLE' && previous != 'IDLE')
    {
    log ('jw now idle');
    log ("jw STOP");
    //jwplayer.sendEvent ('STOP');
    physical_stop();
    // $("#loading").hide();
    ended_callback();
    }

  else if (state == 'BUFFERING')
    {
    // $("#loading").show();
    }

  else if (state == 'PLAYING')
    {
    // $("#loading").hide();
    }
  }

function jw_progress (event)
  {
  jw_position = event ['position'];
  update_progress_bar();
  }

function preload_yt (program)
  {
  url = best_url (program);
  fp_preloaded = 'yt';
  start_play_yt (url)
  }

function ipg_preload (grid)
  {
  log ('preload: grid ' + grid)

  if (programs_in_channel (grid) < 1)
    {
    log ('no programs in channel ' + grid + ' to preload');
    $("#preload").html ('No programs');
    return;
    }

  var program = first_program_in (grid);

  if (best_url (program).match (/youtube\.com/))
    {
    current_tube = 'yt';
    preload_yt (program);
    $("#preload").html ('Start YT...');
    return;
    }

  if (current_tube != 'fp')
    {
    log ('preload: flowplayer was not active');
    current_tube = 'fp';
    unhide_player ("fp");
    fp_player = 'player1';
    }

  fp_preloaded = fp_player == 'player1' ? 'player2' : 'player1';

  if (fp_preloaded == 'player1')
    {
    $("#fp1").css ("visibility", "visible");
    $("#fp2").css ("visibility", "hidden");
    try { flowplayer ("player2").stop(); } catch (error) {};
    }
  else
    {
    $("#fp1").css ("visibility", "hidden");
    $("#fp2").css ("visibility", "visible");
    try { flowplayer ("player1").stop(); } catch (error) {};
    }

  try { flowplayer (fp_preloaded).unload(); } catch (error) {};

  try { log ('flowplayer preload state: ' + flowplayer (fp_preloaded).getState()); } catch (error) {};

  flowplayer (fp_preloaded,
      {src: 'http://zoo.atomics.org/video/fc/flowplayer.commercial-3.2.5.swf', wmode: 'transparent', allowfullscreen: 'false', allowscriptaccess: 'always' }, 
      { canvas: { backgroundColor: '#000000', backgroundGradient: 'none', linkUrl: '' },
      clip: { onFinish: fp_ended, onStart: fp_onstart, onBegin: fp_onbegin, bufferLength: 1, autoPlay: true, scaling: 'fit', onBufferEmpty: fp_buffering, onBufferFull: fp_notbuffering }, 
      plugins: { controls: null },
      play: null, onBeforeKeypress: fpkp, onLoad: fp_onpreload,
      onError: function (in_code, in_msg) { log ("ERROR! " + in_code + " TEXT: " + in_msg); },
      key: '#$f469b88194323deb943' });

  start_preload = new Date();
  $("#preload").html ('Starting...');

  var url = best_url (program);
  url = url.replace (/^fp:/, '');

  fp [fp_preloaded]['file'] = url;
  fp [fp_preloaded]['mute'] = true;
  }

function fp_buffering()
  {
  var id = this.id();
  log ('fp ' + id + ' buffering')
  if (id == fp_player)
    $("#loading").show();
  }

function fp_notbuffering()
  {
  var id = this.id();
  log ('fp ' + id + ' no longer buffering')

  if (id == fp_player)
    $("#loading").hide();

  // if (id == fp_preloaded)
  //  {
  //  log ('pausing preload');
  //  flowplayer (fp_preloaded).pause();
  //  }
  }

function fp_onpreload()
  {
  var url = fp [fp_preloaded]['file'];

  log ('onpreload ' + fp_preloaded + ' url: ' + url);
  $("#preload").html ('Waiting...');

  // flowplayer (fp_preloaded).stop();
  flowplayer (fp_preloaded).mute();

  flowplayer (fp_preloaded).play (url);
  flowplayer (fp_preloaded).mute();
  }

function yt_tick()
  {
  // log ('yt_tick');

  if (tube() == "yt")
    update_progress_bar();

  /* cancel ticking if player stopped */

  var state = -2;
  try { state = ytplayer.getPlayerState(); } catch (error) {};

  if (state == -2 || state == -1 || state == 0)
    {
    log ('yt_tick, STATE IS: ' + state);
    clearTimeout (yt_timex);
    }
  }

function stop_all_other_players()
  {
  if (current_tube != 'fp' || fp_player != 'player1')
    try { flowplayer ('player1').stop(); } catch (error) {};

  if (current_tube != 'fp' || fp_player != 'player2')
    try { flowplayer ('player2').stop(); } catch (error) {};

  if (current_tube != 'yt')
    try { ytplayer.stopVideo(); } catch (error) {};
  }

function ipg_preload_play()
  {
  if (fp_preloaded == '')
    {
    log ('no preload running');
    return;
    }

  clearTimeout (ipg_timex);
  clearTimeout (ipg_delayed_stop_timex);

  if (fp_preloaded == 'yt')
    {
    log ('starting preloaded YouTube video');
    fp_preloaded = '';
    current_tube = 'yt';
    try { ytplayer.seekTo (0); } catch (error) {};
    try { ytplayer.unMute(); } catch (error) {};
    $("#yt1").css ("visibility", "visible");
    try { ytplayer.playVideo(); } catch (error) {};
    clearTimeout (yt_timex);
    yt_timex = setInterval ("yt_tick()", 333);
    }
  else
    {
    current_tube = 'fp';
    fp_player = fp_preloaded;
    fp_preloaded = '';

    log ('PRELOAD PLAY: ' + fp_player);

    try { log (fp_player + ' pre1state: ' + flowplayer (fp_player).getState()); } catch (error) {};

    try { flowplayer (fp_player).seek (0); } catch (error) {};

    try { log (fp_player + ' pre2state: ' + flowplayer (fp_player).getState()); } catch (error) {};

    unhide_player ("fp");

    try { log (fp_player + ' pre3state: ' + flowplayer (fp_player).getState()); } catch (error) {};

    fp [fp_player]['mute'] = false;
    try { flowplayer (fp_player).unmute(); } catch (error) {};

    try { log (fp_player + ' pre4state: ' + flowplayer (fp_player).getState()); } catch (error) {};
    }

  stop_all_other_players();

  enter_category ((""+ipg_cursor).substring (0, 1));

  for (var c in channel_line)
    {
    if (channel_line [c] == ipg_cursor)
      {
      channel_cursor = c;
      enter_channel();
      current_program = first_program_in (ipg_cursor);
      update_bubble();
      redraw_channel_line()
      thumbing = 'channel';
      $("#ipg-layer").css ("display", "none");
      // play_first_program_in (channel_line [channel_cursor]);
      program_cursor = 1;
      program_first = 1;
      enter_channel();
      clips_played++;

      try
        {
        if (tube() == 'yt')
          log ('EXIT PRELOAD PLAY, player ' + fp_player);
        else
          log ('EXIT PRELOAD PLAY, player ' + fp_player + ', state ' + flowplayer (fp_player).getState());
        }
      catch (error)
        {
        log ('EXIT PRELOAD PLAY, state unknown');
        }

      if (tube() == 'fp')
        {
        try
          {
          var state = flowplayer (fp_player).getState();
          if (state == -1)
            {
            log ('*** flowplayer was unloaded, trying over');
            $("#loading").show();
            stop_preload();
            ipg_play();
            }
          else if (state == 1)
            {
            log ('*** flowplayer was unexpectedly idle, restarting with: ' + fp [fp_player]['file']);
            $("#loading").show();
            flowplayer (fp_player).play (fp [fp_player]['file']);
            }
          else if (state == 2)
            {
            $("#loading").show();
            }
          }
        catch (error)
          {
          }
        }

      return;
      }
    }
  }

function start_play_fp (url)
  {
  fp_player = 'player1';
  current_tube = 'fp';

  // ugh! don't know actual url until player is chosen
  url = best_url (current_program);

  url  = url.replace (/^fp:/, '');
  fp [fp_player]['file'] = url;

  unhide_player ("fp");
  log ("FP STREAM: " + url);

  try { flowplayer (fp_player).unload(); } catch (error) {};

  $("#loading").show();

  flowplayer (fp_player,
      {src: 'http://zoo.atomics.org/video/fc/flowplayer.commercial-3.2.5.swf', wmode: 'transparent', allowfullscreen: 'false', allowscriptaccess: 'always' }, 
      { canvas: { backgroundColor: '#000000', backgroundGradient: 'none', linkUrl: '' },
      clip: { onFinish: fp_ended, onStart: fp_onstart, bufferLength: 1, autoPlay: true, scaling: 'fit', onBufferEmpty: fp_buffering, onBufferFull: fp_notbuffering }, 
      plugins: { controls: null },
      play: null, onBeforeKeypress: fpkp, onLoad: fp_onload,
      key: '#$f469b88194323deb943' });

  /* hack */
  if (thumbing != 'ipg' || ipg_mode != 'episodes')
    fp [fp_player]['mute'] = false;

  // flowplayer (fp_player).unmute();
  // flowplayer (fp_player).play (url);
  }

function fp_onload()
  {
  log ('fp onload');
  unhide_player ("fp");
  flowplayer (fp_player).unmute();
  flowplayer (fp_player).play (fp [fp_player]['file']);
  }

function fpkp()
  {
  log ('fpkp');
  return false;
  }

function fp_onbegin()
  {
  var id = this.id();
  log ('fp ' + id + ' onbegin')

  if (fp [id]['mute'])
    {
    try { flowplayer (id).mute(); } catch (error) {};
    }
  else
    {
    try { flowplayer (id).unmute(); } catch (error) {};
    }

  var now = new Date();
  var waited = Math.round ((now.getTime() - start_preload.getTime()) / 100) / 10;
  $("#preload").html ('Preloaded ' + waited + 's');
  $("#loading").hide();
  }

function fp_onstart()
  {
  var id = this.id();
  log ('fp ' + id + ' onstart')

  var fd = parseInt (this.getClip().fullDuration, 10);
  fp [id]['duration'] = fd * 1000;

  if (fp [id]['mute'])
    { try { flowplayer (id).mute(); } catch (error) {}; }
  else
    { try { flowplayer (id).unmute(); } catch (error) {}; }

  /* flowplayer provides no progress/tick event */

  var cmd = 'fp_tick("' + id + '")';
  log ('cmd: ' + cmd);
  fp [id]['timex'] = setInterval (cmd, 333);

  if (id == fp_player)
    update_progress_bar();
  else
    {
    if (id == 'player1')
      $("#fp1").css ("visibility", "hidden");
    else
      $("#fp2").css ("visibility", "hidden");
    }
  }

function fp_ended()
  {
  var id = this.id();
  log ('fp ' + id + ' ended');
  ended_callback();
  clearTimeout (fp [id]['timex']);
  }

function fp_tick (id)
  {
  if (id == fp_player)
    update_progress_bar();

  /* cancel ticking if player stopped */

  if (flowplayer (id).getState() == 1)
    clearTimeout (fp [id]['timex']);
  }

function setup_yt()
  {
  log ('setting up youtube');
  unhide_player ("yt");

  current_tube = 'yt';

  if (!ytplayer)
    {
    try { ytplayer.setSize ($(window).width(), $(window).height()) } catch (error) {};

    var params = { allowScriptAccess: "always", wmode: "transparent", disablekb: "1" };
    var atts = { id: "myytplayer" };
    var url = "http://www.youtube.com/apiplayer?version=3&enablejsapi=1";

    swfobject.embedSWF (url, "ytapiplayer", "100%", "100%", "8", null, null, params, atts);
    }
  else
    play_yt();
  }

function onYouTubePlayerReady (playerId)
  {
  ytplayer = document.getElementById ("myytplayer");
  log ("yt ready, id is: " + playerId);
  try { ytplayer.setSize ($(window).width(), $(window).height()) } catch (error) {};
  play_yt();
  }

function play_yt()
  {
  if (ytplayer && yt_video_id)
    {
    log ('yt PLAY');

    if (fp_preloaded == 'yt')
      { try { ytplayer.mute(); } catch (error) {}; }
    else
      {
      try { ytplayer.unMute(); } catch (error) {};
      clearTimeout (yt_timex);
      yt_timex = setInterval ("yt_tick()", 333);
      }

    $("#yt1").css ("visibility", "visible");

    try { ytplayer.addEventListener ('onStateChange', 'yt_state'); } catch (error) {};
    try { ytplayer.addEventListener ('onError', 'yt_error'); } catch (error) {};

    /* small | medium | large | hd720 | hd1080 */
    try { ytplayer.loadVideoById (yt_video_id, 0, "medium"); } catch (error) {};
    }
  else
    alert ("ytplayer not ready");
  }

function yt_state (state)
  {
  // unstarted (-1), ended (0), playing (1), paused (2), buffering (3), video cued (5).
  log ('yt state: ' + state);
  if (fp_preloaded == 'yt' && state == 1)
    {
    ytplayer.pauseVideo();
    $("#yt1").css ("visibility", "hidden");
    $("#preload").html ('YT Preloaded');
    }

  if (fp_preloaded != 'yt' && (state == 1 || state == 2 || state == 3) && yt_previous_state != state)
    {
    log ('restarting yt timex');
    clearTimeout (yt_timex);
    yt_timex = setInterval ("yt_tick()", 333);
    }
  else if (fp_preloaded != 'yt' && state == 0 && (yt_previous_state == 1 || yt_previous_state == 2 || yt_previous_state == 3))
    {
    /* change this as soon as possible */
    yt_previous_state = state;
    log ('yt eof');
    ended_callback();
    return;
    }

  yt_previous_state = state;
  }

function yt_error (code)
  {
  var errtext;

  var errors = { 100: 'Video not found', 101: 'Embedding not allowed', 150: 'Video not found' };

  if (code in errors)
    errtext = 'YouTube error ' + code + ': ' + errors [code];
  else
    errtext = 'YouTube unknown error: ' + code;

  if (fp_preloaded != 'yt' && (thumbing == 'program' || thumbing == 'channel'))
    {
    log (errtext);
    $("#msg-layer").html ('<p>' + errtext + '</p>');
    $("#msg-layer").show();
    msg_timex = setTimeout ("yt_error_timeout()", 3000);
    }
  else
    log (errtext);
  }

function yt_error_timeout()
  {
  clear_msg_timex();
  program_right();
  }

function physical_seek (offset)
  {
  switch (tube())
    {
    case "fp":
      if (flowplayer)
        flowplayer (fp_player).seek (offset);
      break;

    case "yt":
      if (ytplayer)
        try { ytplayer.seekTo (offset); } catch (error) {};
      break;
    }
  }

function physical_offset()
  {
  switch (tube())
    {
    case "yt":

      if (ytplayer && ytplayer.getCurrentTime)
        return ytplayer.getCurrentTime();
      else
        return 0;

    case "jw":

      if (jwplayer)
        return jw_position;
      else
        return 0;

    case "fp":

      if (flowplayer)
        return flowplayer (fp_player).getTime();
      else
        return 0;

    case "v1":

      var video = document.getElementById ("vvv");
      return video.currentTime;
    }
  }

function physical_length()
  {
  switch (tube())
    {
    case "yt":

      var duration = 1;
      if (ytplayer && ytplayer.getDuration)
        try { duration = ytplayer.getDuration(); } catch (error) {};
      return duration;

    case "jw":

      if (jwplayer)
        return jwplayer.getPlaylist()[0]['duration'];
      else
        return 1;

    case "fp":

      if (flowplayer && fp[fp_player]['duration'])
        return fp [fp_player]['duration'] / 1000;
      else
        return 1;

    case "v1": 

      var video = document.getElementById ("vvv");
      return video.duration;
    }
  }

function physical_pause()
  {
  switch (tube())
    {
    case "yt":

      if (ytplayer)
        ytplayer.pauseVideo()
      break;

    case "jw":

      if (jwplayer)
        jwplayer.sendEvent ("PLAY", "false");
      break;

    case "fp":

       if (flowplayer)
         try { flowplayer (fp_player).pause(); } catch (error) {};
       break;

    case "v1": var video = document.getElementById ("vvv");
               video.pause();
               break;
    }
  }

function physical_play()
  {
  switch (tube())
    {
    case "yt":

      if (ytplayer)
        ytplayer.playVideo()
      break;

    case "jw":

      if (jwplayer)
        jwplayer.sendEvent ("PLAY", "true");
      break;

    case "fp":

      if (flowplayer)
        try { flowplayer (fp_player).play(); } catch (error) {};
      break;

    case "v1":

      var video = document.getElementById ("vvv");
      video.play();
      break;
    }
  }

function physical_is_paused()
  {
  switch (tube())
    {
    case "yt":

      if (ytplayer)
        return ytplayer.getPlayerState() == 2;
      else
        return false;

    case "jw":

      if (jwplayer)
        return jwplayer.getConfig()['state'] == 'PAUSED';
      else
        return false;

    case "fp":

      if (flowplayer)
        {
        try
          {
          return flowplayer (fp_player).isPaused();
          }
        catch (error)
          {
          return false;
          }
        }
      else
        return false;

      break;

    case "v1":

      var video = document.getElementById ("vvv");
      return video.paused;
    }
  }

function physical_replay()
  {
  switch (tube())
    {
    case "yt":

      if (ytplayer && ytplayer.seekTo)
        {
        ytplayer.seekTo (0, false);
        ytplayer.playVideo();
        }
      break;

    case "jw":

      if (jwplayer && jwplayer.sendEvent)
        {
        jwplayer.sendEvent ("SEEK", "0");
        jwplayer.sendEvent ("PLAY", "true");
        }
      break;

    case "fp":

      if (flowplayer)
        {
        flowplayer (fp_player).seek (0);
        flowplayer (fp_player).resume();
        }
      break;

    default:

      var video = document.getElementById ("vvv");
      video.currentTime = 0;
      video.play();

      break;
    }
  }

function update_progress_bar()
  {
  // log ('progress');
  var pct = 100 * physical_offset() / physical_length();

  if (pct >= 0)
    $("#played").css ("width", pct + '%');

  var o1 = physical_offset();
  var o2 = physical_length();

  var t1 = formatted_time (physical_offset());
  var t2 = formatted_time (physical_length());

  $("#play-time").html (t1 + " / " + t2);

  var diff = o2 - o1;
  if (diff > 0 && diff < 1)
    log ('diff: ' + diff);

  if (o2 - o1 < 0.2 && tube() == 'v1' && !physical_is_paused() && !fake_timex)
    {
    log ('end of video reached');
    fake_timex = setTimeout ("fake_ended_event()", 200);
    }
  }

function formatted_time (t)
  {
  if (t == '' || t == NaN || t == undefined)
    return '--';

  var m = Math.floor (t / 60);
  var s = Math.floor (t) - m * 60;

  return m + ":" + ("0" + s).substring (("0" + s).length - 2);
  }

function switch_to_control_layer()
  {
  $('#' + control_buttons [control_cursor]).removeClass ("on");
  control_cursor = 2;
  $('#btn-pause').addClass ("on");
  $('#btn-play').addClass ("on");

  $("#ep-layer").hide();
  $("#ipg-layer").hide();
  $("#new-layer").hide();

  control_saved_thumbing = thumbing;
  thumbing = 'control';

  if (physical_is_paused())
    {
    $("#btn-pause").hide();
    $("#btn-play").show();
    }
  else
    {
    $("#btn-play").hide();
    $("#btn-pause").show();
    }

  $("#control-layer").show();
  }

function control_left()
  {
  $('#' + control_buttons [control_cursor]).removeClass ("on");

  if (control_buttons [control_cursor] == 'btn-play')
    $('#btn-pause').removeClass ("on");

  if (control_cursor > 0)
   control_cursor--;
  else
   control_cursor = control_buttons.length - 1;

  $('#' + control_buttons [control_cursor]).addClass ("on");

  if (control_buttons [control_cursor] == 'btn-play')
    $('#btn-pause').addClass ("on");
  }

function control_right()
  {
  $('#' + control_buttons [control_cursor]).removeClass ("on");

  if (control_buttons [control_cursor] == 'btn-play')
    $('#btn-pause').removeClass ("on");

  if (control_cursor < control_buttons.length - 1)
    control_cursor++;
  else
    control_cursor = 0;

  $('#' + control_buttons [control_cursor]).addClass ("on");

  if (control_buttons [control_cursor] == 'btn-play')
    $('#btn-pause').addClass ("on");
  }

function control_enter()
  {
  switch (control_buttons [control_cursor])
    {
    case 'btn-close':       escape();
                            break;

    case 'btn-play':        pause();
                            break;

    case 'btn-signin':      login_screen();
                            break;

    case 'btn-replay':      physical_replay();
                            break;

    case 'btn-facebook':    facebook_share();
                            break;

    case 'btn-screensaver': switch_to_whats_new();
                            break;

    case 'btn-rewind':      rewind();
                            break;

    case 'btn-forward':     fast_forward();
                            break;
    }
  }

function rewind()
  {
  var offset = physical_offset();
  var duration = physical_length();

  log ('rewind, offset is ' + offset + ', duration is ' + duration);

  offset -= 10;

  if (offset < 0)
    offset = 0;

  log ('seeking ' + offset);
  physical_seek (offset);
  }

function fast_forward()
  {
  var offset = physical_offset();
  var duration = physical_length();

  log ('fast forward, offset is ' + offset + ', duration is ' + duration);

  offset += 10;

  if (offset > duration)
    offset = duration;

  log ('seeking ' + offset);
  physical_seek (offset);
  }

function facebook_share()
  {
  log ("facebook share");

  if (!confirm ("You will be sharing the Public section of your Guide with your facebook friends .."))
    return;

  var query = "/playerAPI/saveIpg?user=" + user;
  var d = $.get (query, function (data)
    {
    log ('saveIpg returned: ' + data);

    var fields = data.split ('\t');
    if (fields[0] == "0")
      {
      FB.ui ({ method: "stream.share", u: location.protocol + "//" + location.host + "/share/" + fields[1] });
      }
    });
  }

function state()
  {
  if (tube() == "yt")
    {
    yt_player_state();
    }
  else if (flowplayer)
    {
    log ('current player: ' + fp_player + ', preloaded: ' + fp_preloaded);
    fp_player_state ('player1');
    fp_player_state ('player2');
    }
  else
    log ('flowplayer is not active!');

  log ('layers :: fp1: ' + $("#fp1").css ("display") + ' ' + $("#fp1").css ("visibility") + ', fp2: ' + 
                           $("#fp2").css ("display") + ' ' + $("#fp2").css ("visibility") + ', yt1: ' +
                           $("#yt1").css ("display") + ' ' + $("#yt1").css ("visibility"));
  return '';
  }

function yt_player_state()
  {
  var yt_state = -2;
  var states = { '-2': 'fail', '-1': 'unstarted', '0': 'ended', '1': 'playing', '2': 'paused', '3': 'buffering', '5': 'cued' };

  try { yt_state = ytplayer.getPlayerState(); } catch (error) {};
  log ('youtube state: ' + states [yt_state]);
  }

function fp_player_state (player)
  {
  var fp_state = -2;

  var star = (player == fp_player ? '* ' : '  ') + player + ' ';
  var states = { '-2': 'none', '-1': 'unloaded', '0': 'loaded', '1': 'unstarted', '2': 'buffering', '3': 'playing', '4': 'paused', '5': 'ended' };

  try { fp_state = flowplayer (player).getState(); } catch (error) {};

  log (star + 'flowplayer state: ' + fp_state + ' == "' + states [fp_state] + '"');
  log (star + 'url: ' + fp [player]['file']);

  return ''
  }

function playerReady (thePlayer)
  {
  return;
  log ('jw player ready: ' + thePlayer.id);
  jwplayer = document.getElementById (thePlayer.id);
  jwplayer.sendEvent ('LOAD', 'nothing.flv');
  }

function noop (e)
  {
  log ('video mouse down');
  /* undo the pause damage done by flowplayer */
  // flowplayer (fp_player).pause();
  }

</script>

<title>9x9.tv</title>

</head>

<body id="body" style="overflow: hidden">

<div id="fp1" style="width: 100%; height: 100%; z-index: 1; visibility: hidden; position: absolute; left: 0; top: 0">
  <a href="" style="display:block;width:100%;height:100%" id="player1" onClick="noop(this)"></a>
</div>

<div id="fp2" style="width: 100%; height: 100%; z-index: 2; visibility: hidden; position: absolute; left: 0; top: 0">
  <a href="" style="display:block;width:100%;height:100%" id="player2" onClick="noop(this)"></a>
</div>

<div id="yt1" style="width: 100%; height: 100%; z-index: 1; visibility: visible; position: absolute; left: 0; top: 0">
  <div id="ytapiplayer">
    <!-- You need Flash player 8+ and JavaScript enabled to view this video.-->
  </div>
</div>

<div id="blue" style="background: black; width: 100%; height: 100%; display: block; position: absolute; color: white">
One moment...
</div>

<!--div id="notblue" style="width: 100%; display: none; position: absolute; top: 0; margin: 0; overflow: hidden"-->

  <div id="all-players" style="display: none; padding: 0; display: none">
    <div id="v" style="display: block; padding: 0">
      <video id="vvv" autoplay="false" preload="metadata" loop="false" height="100%" width="100%" volume="0"></video></div>

<div id="jw" style="width: 100%; height: 100%; display: none">
        <embed name="player1" id="player1"
            type="application/x-shockwave-flash"
            pluginspage="http://www.macromedia.com/go/getflashplayer"
            width="100%" height="100%"
            bgcolor="#FFFFFF"
            src="http://zoo.atomics.org/video/player.swf"
            allowfullscreen="true"
            allowscriptaccess="always"
            wmode="transparent"
            flashvars="fullscreen=true&controlbar=none&mute=false&bufferlength=1&allowscriptaccess=always">
        </embed>
</div>
<!--div id="jw2" style="width: 100%; height: 100%">
        <embed name="player2" id="player2"
            type="application/x-shockwave-flash"
            pluginspage="http://www.macromedia.com/go/getflashplayer"
            width="100%" height="100%"
            bgcolor="#FFFFFF"
            src="http://zoo.atomics.org/video/player.swf"
            allowfullscreen="true"
            allowscriptaccess="always"
            wmode="transparent"
            flashvars="fullscreen=true&controlbar=none&mute=false&bufferlength=1&allowscriptaccess=always">
        </embed>

</div-->

  </div>

<div id="ep-layer" style="display: none">
  <img src="http://zoo.atomics.org/video/9x9playerV29/images/ep_panel_off.png" id="ep-panel">
  <div id="ep-tip"><p><span class="hilite">Press ENTER key</span> or <span class="hilite">click</span> on the highlighted channel icon in your IPG to see channel episodes.</p></div>
  <div id="ep-container">
    <div class="ep-swish" style="display: block">
      <ul class="ep-list" id="ep-list"></ul>
    </div>
    <div id="ep-meta"><p><span class="ch-title" id="ep-layer-ch-title">ABC news</span> - <span class="ep-title" id="ep-layer-ep-title">Jay Leno's eclectic car collection</span> - <span class="age" id="ep-age">Today</span> - <span class="duration" id="ep-duration">10:10</span></p></div>
  </div>
</div>

<div id="ipg-layer" style="display: none">
  <div id="ipg-holder">
    <div id="header">
      <img src="http://zoo.atomics.org/video/9x9playerV29/images/logo.png" id="logo">
      <p id="user-name">Hello, <span id="user">Guest</span></p>  
      <ul id="control-list"><li><a href="javascript:login_screen()" class="btn" id="btn-signin"><span>Sign in / Sign up</span></a></li><li><a href="javascript:;" class="btn" id="btn-signout"><span>Sign out</span></a></li><li><a href="javascript:;" class="btn"><span>Resume Watching</span></a></li></ul>
    </div>
    <div id="ipg-content">
      <ul id="info-list">
        <li id="ch-name"></li>
        <!--li id="ch-mtype">
          <img src="images/icon_audio.png">
        </li-->
        <li id="ep-name"></li>
        <li id="description"></li>
        <li id="ep-number">
          <p><span class="hilite">Episodes:</span> <span id="ch-episodes">0</span></p>
        </li>
        <li id="update">
          <p><span class="hilite">Updated:</span> <span id="update-date"></span></p>
        </li>
        <li id="preloading"><p><span class="hilite">Preload:</span> <span id="preload"></span></p></li>
      </ul>
      <div id="ipg-grid">
        <p id="watermark"><img src="http://zoo.atomics.org/video/9x9playerV29/images/watermark.png"></p>
        <div id="list-holder">
        </div>
      </div>     
    </div>
  </div>
</div>

<div id="ch-directory">
  <div id="dir-holder">
  <div id="main-panel">
    <ul>
      <!--<li id="featured"><p>Featured</p><span class="arrow">&raquo;</span></li>-->
      <li id="main-1" class="selected"><p>Category</p><span class="arrow">&raquo;</span></li>
      <!--<li id="most"><p>Most subscribed</p><span class="arrow">&raquo;</span></li>
      <li id="search"><p>Search</p><span class="arrow">&raquo;</span></li>-->
      <li id="main-2"><p>Add RSS / YouTube</p><span class="arrow">&raquo;</span></li>
    </ul>
    <a href="javascript:;" class="btn" id="btn-returnIPG">Return to IPG</a>
  </div>
    <div class="br-panel" id="category-panel">
    <div class="sub-panel">
      <p class="page-up"><img src="http://zoo.atomics.org/video/9x9playerV29/images/arrow_up.png"></p>
      <div class="sub-holder">
        <ul id="ch-catlist"></ul>
      </div>
      <p class="page-down"><img src="http://zoo.atomics.org/video/9x9playerV29/images/arrow_down.png"></p>
    </div>
    <div class="content-panel">
      <p class="page-up"><img src="http://zoo.atomics.org/video/9x9playerV29/images/arrow_up.png"></p>
      <div class="content-holder">
        <ul id="content-list"></ul>
      </div>
      <p id="ch-vacancy"></p>
      <!--a href="javascript:;" class="btn" id="btn-subscribeAll">Subscribe all</a-->
      <p class="page-down"><img src="http://zoo.atomics.org/video/9x9playerV29/images/arrow_down.png"></p>
    </div>
  </div>
  
  <div class="op-panel" id="search-panel">
    <div class="input-area">
      <label for="search input">Enter search term:</label>
        <ul class="search-input">
          <li class="textfieldbox"><input name="" type="text" class="textfield"></li>
          <li><a href="javascript:;" class="btn">Go</a></li>
        </ul>
    </div>
    <p class="page-up"><img src="http://zoo.atomics.org/video/9x9playerV29/images/arrow_up.png"></p>
    <p class="page-down"><img src="http://zoo.atomics.org/video/9x9playerV29/images/arrow_down.png"></p>
  </div>
  
  <div class="op-panel" id="add-panel">
    <div class="input-area">
      <label for="RSS/YouTube input">Contribute a Podcast / YouTube URL:</label>
      <ul class="url-input">
        <li class="textfieldbox" id="submit-url-box"><input name="" type="text" class="textfield" id="submit-url"></li>
      </ul>
    </div>
    <div class="cate-selector">
      <p>Channel category:</p>
      <ul class="cate-list" id="cate-list"></ul>
    </div>
    <div id="feedback" class="success"><p></p></div>
    <a href="javascript:submit_throw()" class="btn" id="add-go">Go</a>
  </div>
  </div>
</div>

<div id="new-layer" style="display: none">
  <div id="new-holder">
  </div>
</div>

<div id="signin-layer" style="display: none">
  <ul id="login-pannel">
    <li><h2>Returning Users</h2></li>
    <li>
      <span>Email:</span>
      <p class="textfieldbox"><input type="text" id="L-email" class="textfield" value="you@example.com"></p>
    </li>
    <li>
      <span>Password:</span>
      <p class="textfieldbox"><input type="password" id="L-password" class="textfield" value="swordfish"></p>
    </li>
    <li><a href="javascript:submit_login()" class="btn"><span>Log in</span></a></li>
  </ul>
  <ul id="signup-pannel">
    <li><h2>New Users</h2></li>
    <li>
      <span>Name:</span>
      <p class="textfieldbox"><input type="text" id="S-name" class="textfield"></p>
    </li>
    <li>
      <span>Email:</span>
      <p class="textfieldbox"><input type="text" id="S-email" class="textfield"></p>
    </li>
    <li>
      <span>Password:</span>
      <p class="textfieldbox"><input type="password" id="S-password" class="textfield"></p>
    </li>
    <li>
      <span>Password verify:</span>
      <p class="textfieldbox"><input type="password" id="S-password2" class="textfield"></p>
    </li>
    <li><a href="javascript:submit_signup()" class="btn"><span>Sign up</span></a></li>
  </ul>
</div>

<div id="browse" style="display: none; z-index: 999"></div>

<div id="preload-control-images" style="display: none"></div>

<div id="control-layer">
  <div id="msg-up">
    <p>Press <span class="enlarge">&uarr;</span> to see your IPG</p>
  </div>
  <div id="controler">
    <img src="http://zoo.atomics.org/video/9x9playerV29/images/bg_controler.png" id="controler-bg">
    <ul id="control-bar">
      <li id="btn-replay"><img src="http://zoo.atomics.org/video/9x9playerV29/images/btn_replay.png"></li>
      <li id="btn-rewind"><img src="http://zoo.atomics.org/video/9x9playerV29/images/btn_rewind.png"></li>
      <li id="btn-play" style="display: none"><img src="http://zoo.atomics.org/video/9x9playerV29/images/btn_play.png"></li>
      <li id="btn-pause"><img src="http://zoo.atomics.org/video/9x9playerV29/images/btn_pause.png"></li>
      <li id="btn-forward"><img src="http://zoo.atomics.org/video/9x9playerV29/images/btn_forward.png"></li>
      <li class="divider"></li>
      <li id="btn-volume"><img src="http://zoo.atomics.org/video/9x9playerV29/images/btn_volume.png"></li>
      <li id="volume-constrain" class="on">
        <ul id="volume-bars">
          <li></li>
          <li></li>
          <li></li>
          <li class="on"></li>
          <li class="on"></li>
          <li class="on"></li>
          <li class="on"></li>
        </ul>
      </li>
      <li class="divider"></li>
      <li id="btn-facebook"><img src="http://zoo.atomics.org/video/9x9playerV29/images/btn_facebook.png"></li>
      <li id="btn-screensaver"><img src="http://zoo.atomics.org/video/9x9playerV29/images/btn_screensaver.png"></li>
      <li class="divider"></li>
      <li id="btn-close"><img src="http://zoo.atomics.org/video/9x9playerV29/images/btn_close.png"></li>
      <li id="play-time">-- / --</li>
      <li id="progress-bar">
        <p id="loaded"></p>
        <p id="played"></p>
      </li>
    </ul>
  </div>
  <div id="msg-down">
    <p>Press <span class="enlarge">&darr;</span> for more episodes</p>
  </div>
</div>

<div id="waiting">
  <div class="waiting-holder">
    <img src="http://zoo.atomics.org/video/9x9playerV29/images/loading.gif">
    <p>One moment...</p>
  </div>
</div>

<div id="buffering">
  <div class="waiting-holder">
    <img src="http://zoo.atomics.org/video/9x9playerV29/images/loading.gif">
    <p>Buffering...</p>
  </div>
</div>

<div id="dir-waiting">
  <div class="waiting-holder">
    <img src="http://zoo.atomics.org/video/9x9playerV29/images/loading.gif">
    <p>One moment...</p>
  </div>
</div>

<div id="msg-layer" style="display: none">
  <p>No programs in this channel</p>
</div>

<div id="epend-layer" style="display: none">
  <div id="go-up">Press <span class="enlarge">&uarr;</span> to go to the IPG</div>
  <div id="go-down">Press <span class="enlarge">&darr;</span> to see all episodes</div>
  <div id="go-left"><img src="" id="left-tease">Press <span class="enlarge">&larr;</span> to watch previous channel</div>
  <div id="go-right"><img src="" id="right-tease">Press <span class="enlarge">&rarr;</span> to watch next channel</div>
</div>

<div id="opening" style="display: block">
  <div class="opening-holder" id="splash"></div>
</div>

<div id="log-layer" style="position: absolute; left: 0; top: 0; height: 100%; width: 100%; background: white; color: black; text-align: left; padding: 20px; overflow: scroll; z-index: 9999; display: none"></div>

<div id="mask"></div>
<div id="loading"><img src="http://zoo.atomics.org/video/9x9playerV29/images/loading.gif"></div>

<!--/div-->
</body>
</html>