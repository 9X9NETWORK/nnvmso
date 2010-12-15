<!DOCTYPE html>
<head>
<meta charset="UTF-8" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<link rel="stylesheet" href="http://zoo.atomics.org/video/9x9playerV19/stylesheets/main.css" />

<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
<script type="text/javascript" src="http://zoo.atomics.org/video/cssanim.js"></script>
<script type="text/javascript" src="http://zoo.atomics.org/video/swfobject.js"></script>
<script type="text/javascript" src="http://zoo.atomics.org/video/flowplayer-3.2.4.min.js"></script>
<script type="text/javascript" src="http://zoo.atomics.org/video/9x9playerV19/javascripts/whatsnew.js"></script>

<script>

/* players */

var current_tube = 'v1';

var ytplayer;
var yt_video_id;

var jwplayer;
var jw_video_file = 'nothing.flv';
var jw_timex = 0;
var jw_previous_state = '';
var jw_position = 0;

var fp_video_file;
var fp_duration;


var activated = false;
var remembered_pause = false;

var current_category = 0;
var current_program = '';
var current_url = '';

var thumbing = '';

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

/* cache this for efficiency */
var loglayer;

/* browse */
var browsables = {};
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

var user = "aghubmUydm1zb3IMCxIGTm5Vc2VyGBoM";
var root = 'http://zoo.atomics.org/video/9x9playerV19/images/';

$(document).ready (function()
 {
 log ('begin execution');
 init();
 //elastic();
 login();
 $(window).resize (function() { elastic(); });
 });

function elastic()
  {
  log ('elastic');
  elastic_innards();
  // ShowArrows();
  if (thumbing == 'whatsnew')
    {
    GetAnchor();
    GetFilmPos2();
    }
  }

function elastic_innards()
  {
  var newWidth = $(window).width() / 16 ;
  var newHeight = $(window).height() / 16;

  var oriWidth = 64;
  var times = newWidth / oriWidth * 100 + "%";
  $("body").css("font-size",times);

  var v = document.getElementById ("v");
  var vh = $(window).height();
  v.style.height = (vh) + "px";

  // var h = document.getElementById ("jw");
  // h.style.height = (vh) + "px";
  // var h = document.getElementById ("jw2");
  // h.style.height = (vh) + "px";
  var h = document.getElementById ("fp");
  h.style.height = (vh) + "px";

  // var i = document.getElementById ("ipg-layer");
  // i.style.height = vh + "px";

  ipg_fixup_margin();
  ipg_fixup_middle();

  whatsnew_fixup_middle();
  episode_end_layer_fixup();

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
  var wh = $(window).height();
  var lh = $("#ipg-layer").height();

  var margin = (wh - lh) / 2;
  // log ('align_middle :: wh: ' + wh + ' lh: ' + lh + ' margin: ' + margin);

  $("#ipg-layer").css ("top", margin);
  $("#podcast-layer").css ("top", margin);
  }

function whatsnew_fixup_middle()
  {
  var wh = $(window).height();
  var nh = $("#new-layer").height();
  var nt = (wh-nh)/2;
  $("#new-layer").css("top",nt);
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

  //var playertext = $("#jw-template").html();
  //playertext = playertext.replace (/%ID%/g, 'player2');
  //$("#jw2").html (playertext);
  //$("#jw2").show();
  }

function fetch_programs()
  {
  log ('obtaining programs');

  var query = "/playerAPI/programInfo?channel=*" + String.fromCharCode(38) + "user=" + user;

  var d = $.get (query, function (data)
    {
    parse_program_data (data);
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

  var logtext = '';
  var now = new Date();

  log ('splitting')
  var lines = data.split ('\n');
  log ('number of programs obtained: ' + lines.length);
  for (var i = 0; i < lines.length; i++)
    {
    // log (lines [i]);
    if (lines [i] != '')
      {
      var fields = lines[i].split ('\t');
      // logtext += "program line " + i + ": " + fields[0] + ' = ' + lines [i] + '\n';
      programgrid [fields [1]] = { 'channel': fields[0], 'type': fields[3], 'url1': 'fp:' + fields[8], 
                   'url2': 'fp:' + fields[9], 'url3': 'fp:' + fields[10], 'url4': 'fp:' + fields[11], 
                   'name': fields[2], 'desc': fields [3], 'type': fields[4], 'thumb': fields[6], 
                   'snapshot': fields[7], 'timestamp': fields[12], 'duration': fields[5] };
      }
    }

  log ('finished parsing program data');
  // logblob (logtext);
  }

function fetch_channels()
  {
  log ('obtaining channels');

  var query = "/playerAPI/channelLineup?user=" + user;

  // 1  570     Channel One     /thumb/01.jpg
  // 0=grid-location 1=channel-id 2=channel-name 3=channel-thumb

  var d = $.get (query, function (data)
    {
    var n = 0;
    var conv = {};

    for (var y = 1; y <= 9; y++)
      for (var x = 1; x <= 9; x++)
        conv [++n] = "" + y + "" + x;

    var lines = data.split ('\n');
    log ('number of channels obtained: ' + lines.length);
    for (var i = 0; i < lines.length; i++)
      {
      if (lines [i] != '')
        {
        var fields = lines[i].split ('\t');
        log ("channel line " + i + ": " + conv [fields[0]] + ' = ' + lines [i]);
        channelgrid [conv [fields[0]]] = { 'id': fields[1], 'name': fields[2], 'thumb': fields[3] };
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

function activate()
  {
  activated = true;
  enter_category (1, 'b');
  elastic();
log ('activate0');
  //play_first_program_in (first_channel());
  program_cursor = 1;
  program_first = 1;
  current_program = first_program_in (first_channel());

log ('activate2');
  enter_channel();
log ('activate3');
  $("#ep-layer").hide();
  document.onkeydown=kp;
  redraw_ipg();

  log ('activate: ipg');
  switch_to_ipg();
  $("#notblue").show();
  $("#blue").hide();
  preload_control_images()

  jw_play_nothing();
  }

function preload_control_images()
  {
  var html = '';

  for (var i in { 'bg_controlbar':'', 'btn_rewind':'', 'btn_pause':'', 'btn_play':'', 'btn_forward':'', 'btn_volume':'', 'btn_close':'', 'btn_signin':'', 'btn_handler':'', 'bg_msgup':'', 'bg_msgdown':'', 'btn_on':'', 'btn_off':'', 'btn_facebook':'', 'btn_replay':'', 'btn_screensaver':'', 'bg_ep':'', 'bg_podcastlist':'', 'bg_film':'' })
    html += '<img src="' + root + i + '.svg">';

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
    if (programgrid [program]['url1'].match (/(null|jw:null|jw:|fp:null|fp:)/))
      return '';
    return programgrid [program]['url1'];
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
  $("#ch-layer").hide();
  $("#ep-layer").hide();
  $("#control-layer").hide();
  $("#msg-layer").hide();
  $("#msg-layer").hide();
  $("#epend-layer").hide();
  }

function end_message (duration)
  {
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
    {
    escape();
    switch_to_whats_new();
    }
  }

function play()
  {
  clear_msg_timex();

  var url = best_url (current_program);

  if (url == '')
    {
    log ('empty channel, displaying notice for 3 seconds')
    $("#ep-layer").hide();
    $("#ch-layer").hide();
    end_message (10000);
    return;
    }

  log ('Playing ' + current_program + ': ' + programgrid [current_program]['name']);
  physical_start_play (url);
  }

function start_play_html5 (url)
  {
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
  $("#btn-play").css ("background-image", "url(" + root + "btn_pause.svg)");
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

  // if (type == 'channel')
  //  {
  //  log ('** ended event fired, moving channel right');
  //  channel_right();
  //  }

  if (type == 'program' || type == 'channel')
    {
    log ('** ended event fired, moving program right');
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
  $("#btn-play").css ("background-image", "url(" + root + "btn_play.svg)");
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

  var channel_name = channelgrid [channel]['name'];
  if (channel_name.match (/^\s*$/)) { channel_name = '[no channel name]'; }

  $("#ch-layer-ch-title").html (channel_name);
  $("#ch-layer-ep-title").html (program_name);

  $("#ep-layer-ch-title").html (channel_name);
  $("#ep-layer-ep-title").html (program_name);

  // $("#left-row-num").html (previous_category (current_category));
  // $("#right-row-num").html (next_category (current_category));

  $("#left-row-num").html (Math.floor (current_category) - 1 == 0 ? 9 : Math.floor (current_category) - 1);
  $("#right-row-num").html (Math.floor (current_category) + 1 == 10 ? 1 : Math.floor (current_category) + 1);
  }

function switch_to_channel_thumbs()
  {
  enter_category (current_category, '');
  thumbing = 'channel';
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
    log ('not in channelgrid: ' + channel);

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

  redraw_program_line();
  }

function enter_channel()
  {
  $("#epend-layer").hide();

  prepare_channel();

  $("#ep-layer").css ("opacity", "0");
  $("#ep-layer").css ("display", "block");

  $("#ep-swish").css ("top", "5.125em");
  $("#ep-swish").css ("display", "block");

  var phase_in_pp =
    {
    //opacity: "1",
    //top: "0em"
    top: "1.4275em"
    };

  var phase_out_cc =
    {
    //opacity: "0",
    top: "-6.1875em" 
    };

  if (thumbing == 'control')
    $("#control-layer").animateWithCss ({ opacity: "0" }, 500, "ease-in-out", function() { $("#control-layer").hide(); $("#control-layer").css ("opacity", "1"); });

  $("#ep-layer").animateWithCss ({ opacity: "1" }, 500, "ease-in-out", function() {});
  $("#ch-layer").animateWithCss ({ opacity: "0" }, 500, "ease-in-out", function() { $("#ch-layer").css ("opacity", "1"); });

  $("#ep-swish").animateWithCss (phase_in_pp, 500, "ease-in-out", function() {});

  $("#ch-swish-" + current_category).animateWithCss (phase_out_cc, 500, "ease-in-out", function()
    {
    $("#ch-swish-" + current_category).css ("display", "none");
    $("#ch-swish-" + current_category).css ("top", "1.4375em");
    //$("#ch-swish-" + current_category).css ("opacity", "1");
    $("#ch-layer").css ("display", "none");
    });

  setTimeout ("enter_channel_failsafe()", 500);

  thumbing = 'program';

  redraw_program_line();
  // ShowArrows();

  reset_osd_timex();
  }

function enter_channel_failsafe()
  {
  // prepare_channel();

  $("#ep-layer").css ("opacity", "1");
  $("#ep-swish").css ("top", "1.4275em");
  $("#ep-swish").css ("display", "block");

  $("#control-layer").css ("opacity", "1");

  if (thumbing == 'program')
    {
    $("#ch-layer").hide();
    $("#ep-layer").show();
    }
  turn_off_ancillaries();
  // ShowArrows();
  }

function ep_html()
  {
  var html = '';
  var now = new Date();

  log ('(program html) program_first: ' + program_first + ' n_program_line: ' + n_program_line);
  for (var i = program_first; i <= n_program_line && i < program_first + max_programs_in_line; i++)
    {
    log ('--> program ' + i);
    var program = programgrid [program_line [i]];
    var age = ageof (program ['timestamp']);

    // html += '<li id="p-li-' + i + '"><img id="p-th-"' + i + '" src="' + program ['thumb'] + '"></li>';
    var onoff = (i == program_cursor) ? 'class="on" ' : '';
    onoff = '';
    html += '<li ' + onoff + 'id="p-li-' + i + '"><img src="' + program ['thumb'] + '">'
    html += '<p class="timestamp unseen">' + age + '</p><p class="duration">0:00</p></li>';
    }

  log ('PROGRAM HTML: ' + html);
  return html;
  }

function ageof (timestamp)
  {
  var age = '';
  var now = new Date();

  if (timestamp != '')
    {
    var d = new Date (Math.floor (timestamp));
    var minutes = Math.floor ((now.getTime() - d.getTime()) / 60 / 1000);
    if (minutes > 59)
      {
      var hours = Math.floor ((minutes + 1) / 60);
      if (hours >= 24)
        {
        var days = Math.floor ((hours + 1) / 24);
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

  return age + ' ago';
  }

var old_cline;
var new_cline;

function enter_category (cat, positioning)
  {
  log ('enter category: ' + cat + ', thumbing: ' + thumbing);

  channel_line = {};

  if (thumbing == 'program')
    {
    var ch = $("#ch-swish-" + current_category);
    var ep = $("#ep-swish");

    $("#ch-swish-" + current_category).css ("display", "block");
    $("#ch-layer").css ("opacity", "0");
    $("#ch-swish-" + current_category).css ("top", "-4.0625em");
    $("#ch-layer").css ("display", "block");

    $("#ep-swish").css ("top", "1.4275em");

    var phase_out_pp =
      {
      //opacity: "0",
      top: "5.125em"
      };

    var phase_in_cc =
      {
      //opacity: "1",
      top: "1.4375em" 
      };

    log ('animating channel to category ' + current_category);

    // GAH! Why do I have to put this in a timer to get the animation to work?
    // $("#ch-swish-" + current_category).animateWithCss (phase_in_cc, 500, "ease-in-out", function() {});
    //
    setTimeout ("$('#ch-swish-' + current_category).animateWithCss ({ top: '1.4375em' }, 500, 'ease-in-out', function() {});", 0);

    setTimeout ("$('#ch-layer').animateWithCss ({ opacity: '1' }, 600, 'ease-in-out', function() {});", 0);
    setTimeout ("$('#ep-layer').animateWithCss ({ opacity: '0' }, 500, 'ease-in-out', function() { $('#ep-layer').css ('display', 'none'); $('#ep-layer').css ('opacity', '1'); });", 0);

    ep.animateWithCss (phase_out_pp, 500, "ease-in-out", function()
      {
      $("#ep-swish").css ("display", "none");
      //$("#ep-swish").css ("opacity", "1");
      $("#ep-layer").css ("opacity", "1");
      $("#ep-swish").css ("top", "1.4275em");
      });
    }

  thumbing = 'channel';
  $("#control-layer").hide();

  if (current_category > 0)
    $("#ch-swish-" + current_category).css ("display", "block");

  if (current_category > 0 && cat != current_category)
    {
    old_cline = $("#ch-swish-" + current_category);
    new_cline = $("#ch-swish-" + cat);

    new_cline.css ("opacity", "0");
    new_cline.css ("display", "block");

    var phase_out =
      {
      opacity: "0",
      // left: positioning == 'b' ? "-45em" : "45em"
      };

    var phase_in =
      {
      opacity: "1"
      };

    old_cline.animateWithCss (phase_out, 1000, "ease-in-out", function()
      {
      old_cline.css ("display", "none");
      old_cline.css ("opacity", "1");
      // old_cline.css ("left", "0px");
      });

    new_cline.animateWithCss (phase_in, 700, "ease-in-out", function()
      {
      });
    }

  setTimeout ("enter_category_failsafe()", 700);

  $("#ch-list-" + cat).html ("");

  if (cat != current_category)
    {
    current_category = cat;
    log ('setting new category: ' + cat);
    }
  else
    log ('already in category: ' + cat);

  var html = ch_html (cat);

  $("#ch-list-" + cat).html (html);
  $("#ch-layer").css ("display", "block");

  for (var y = 1; y <= 9; y++)
    $("#ch-swish-" + y).css ("display", y == cat ? "block" : "none");

  /* position at beginning or ending */
  if (positioning == 'b')
    channel_cursor = 1;
  else if (positioning == 'e')
    channel_cursor = n_channel_line;

  redraw_channel_line();
  // ShowArrows();
  reset_osd_timex();

  prepare_channel();
  }

function enter_category_failsafe()
  {
  $("#ch-swish-" + current_category).css ("top", "1.4375em");
  $("#ch-swish-" + current_category).css ("display", "block");

  if (thumbing == 'channel')
    {
    $("#ep-layer").hide();
    $("#ch-layer").css ("display", "block");
    $("#ch-layer").css ("opacity", "1");
    }

  turn_off_ancillaries();
  // ShowArrows();
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

function ShowArrows()
  {
  elastic_innards();

  if (thumbing == 'channel')
    {
    pos1 = $(".ch-list li.on").offset();
    if (pos1 != null)
      {
      d1 = ((94-21)/2)* $(window).width()/16/64;
      l1 = pos1.left + d1;
      $("#ch-layer .arrow-up, #ch-layer .arrow-down").show().css("left",l1);
      }
    }
  else if (thumbing == 'program')
    {
    pos2 = $(".ep-list li.on").offset();
    if (pos2 != null)
      {
      d2 = ((90-21)/2)* $(window).width()/16/64;
      l2 = pos2.left + d2;
      $("#ep-layer .arrow-up").show().css("left",l2);
      }
    }
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

function next_category()
  {
  log ('next category');

  for (var cat = current_category + 1; cat <= 9; cat++)
    {
    if (channels_in_category (cat) > 0)
      return cat;
    }

  for (var cat = 1; cat < current_category; cat++)
    {
    if (channels_in_category (cat) > 0)
      return cat;
    }

  return current_category;
  }

function previous_category()
  {
  log ('previous category');

  for (var cat = current_category - 1; cat >= 1; cat--)
    {
    if (channels_in_category (cat) > 0)
      return cat;
    }

  for (var cat = 9; cat > current_category; cat--)
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
    log_and_alert ('channel ' + channel + ' not in channelgrid');
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
  // log ('AX: ' + programs);
  programs = programs.sort (function (a,b) { return Math.floor (programgrid [b]['timestamp']) - Math.floor (programgrid [a]['timestamp']) });
  programs.unshift ('');
  // log ('AY: ' + programs);

  return programs [1];
  }

function escape()
  {
  var layer;

  switch (thumbing)
    {
    case 'program': layer = $("#ep-layer");
                    break;

    case 'channel': layer = $("#ch-layer");
                    break;

    case 'ipg':     layer = $("#ipg-layer");
                    break;

    case 'user':    layer = $("#signin-layer");
                    break;

    case 'browse':  layer = $("#podcast-layer");
                    break;

    case 'control': layer = $("#control-layer");
                    break;

    case 'end':     return;
    }

  layer.css ("display", layer.css ("display") == "block" ? "none" : "block");

  if (thumbing == 'ipg' || thumbing == 'user')
    resume();

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
  if (thumbing == 'browse' && keycode != 27 && keycode != 38 && keycode != 40 && keycode != 13 && keycode != 33 && keycode != 34)
    return;

  if (thumbing == 'ipg')
    extend_ipg_timex();

  /* ensure osd is up */

  if (keycode == 37 || keycode == 39 || keycode == 38 || keycode == 40)
    {
    if (thumbing == 'channel')
      {
      if ($("#ch-layer").css ('display') == 'none')
        {
        log ('channel osd was off');
        extend_ch_layer();
        return;
        }
      else
        reset_osd_timex();
      }
    else if (thumbing == 'program')
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
        browse_accept();
      else if (thumbing == 'channel' || thumbing == 'program')
        switch_to_control_layer();
      else if (thumbing == 'control')
        control_enter();
      else if (thumbing == 'whatsnew')
        whatsnew_enter()
      break;

    case 37:
      /* left arrow */
      if (thumbing == 'channel')
        channel_left();
      else if (thumbing == 'end')
        {
        $("#epend-layer").hide();
        channel_left();
        thumbing = 'program';
        }
      else if (thumbing == 'program')
        program_left();
      else if (thumbing == 'ipg')
        ipg_left();
      else if (thumbing == 'control')
        control_left();
      else if (thumbing == 'whatsnew')
        PrevEp();
      break;

    case 39:
      /* right arrow */
      if (thumbing == 'channel')
        channel_right();
      else if (thumbing == 'end')
        {
        $("#epend-layer").hide();
        channel_right();
        thumbing = 'program';
        }
      else if (thumbing == 'program')
        program_right();
      else if (thumbing == 'ipg')
        ipg_right();
      else if (thumbing == 'control')
        control_right();
      else if (thumbing == 'whatsnew')
        NextEp();
      break;

    case 38:
      /* up arrow */
      if (thumbing == 'program')
        // switch_to_channel_thumbs();
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
      if (thumbing == 'channel' || thumbing == 'control')
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
      else if (thumbing == 'program')
        switch_to_channel_thumbs();
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
      enter_category (keycode - 48, 'b');
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
      // $("#epend-layer").show();
      end_message (10000);
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

  hide_layers();
  force_pause();

  thumbing = 'whatsnew';
  var bad_thumbnail = '<img src="http://zoo.atomics.org/video/images-x1/no_images.png">';
  var desc = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, ...";

  var query = "/playerAPI/whatsNew?user=" + user;

  var d = $.get (query, function (data)
    {
    var lines = data.split ('\n');
    log ('number of new programs obtained: ' + lines.length);

    wn = {};

    for (var i = 0; i < lines.length; i++)
      {
      var program = lines [i].trim();
      if (program != '')
        {
        if (program in programgrid)
          {
          var real_channel = programgrid [program]['channel'];

          if (! (real_channel in wn))
            wn [real_channel] = [];

          wn [real_channel].push (program);

          /* fakes */
          programgrid [program]['desc'] = desc;
          programgrid [program]['age'] = ageof (programgrid [program]['timestamp']);

          if (programgrid [program]['snapshot'] != '')
            programgrid [program]['screenshot'] = programgrid [program]['snapshot'];
          else
            programgrid [program]['screenshot'] = programgrid [program]['thumb']

          //log ('whatsnew ' + program + ' (ch: ' + real_channel + '): ' + programgrid [program]['name']);
          }
        else
          log ('program ' + program + ' not in a subscribed channel');
        }
      }

    for (var channel in wn)
      {
      var grid = channels_by_id [channel];
      log ('whatsnew :: ch:' + channel + ' grid: ' + grid + ' episodes:' + wn [channel].join());
      whatsnew.push ({ 'channel': channel, 'grid': grid, 'episodes': wn [channel] });
      }

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

    $("#new-layer").html (html);

    $("body").addClass ("on");
    // $("body").css ("background-image", "url(" + root + "bg.jpg)");

    //$("#new-layer").show();
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
  $("#all-players").show();
  enter_channel();
  }

function whatsnew_enter()
  {
  StopWhatsNew();
  $("body").removeClass ("on");
  $("#all-players").show();

  var grid = whatsnew [i]['grid'];
  var episode = whatsnew [i]['episodes'][n];
  var channel = whatsnew [i]['channel'];

  enter_category ((""+grid).substring (0, 1));

  for (var c in channel_line)
    {
    if (channel_line [c] == grid)
      {
      channel_cursor = c;
      redraw_channel_line()
      enter_channel();
      /* select episode, but for now, play first */
      play_first_program_in (channel_line [channel_cursor]);
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

  $("#ch-layer").hide();
  $("#ep-layer").hide();

  if (thumbing == 'channel')
    {
    thumbing = 'program';
    prepare_channel();
    }
  }

function extend_ch_layer()
  {
  $("#ch-layer").show();
  elastic();
  reset_osd_timex();
  }

function extend_ep_layer()
  {
  $("#ep-layer").show();
  elastic();
  reset_osd_timex();
  }

function switch_to_ipg()
  {
  log ('ipg');

  clear_msg_timex();
  force_pause();

  if (thumbing == 'control')
    $("#control-layer").animateWithCss ({ opacity: "0" }, 500, "ease-in-out", function() { $("#control-layer").hide(); $("#control-layer").css ("opacity", "1"); });

  $("#ipg-layer").hide();

  ipg_cursor = parseInt (channel_line [channel_cursor]);
  // ipg_cursor = -1;

  if (! (ipg_cursor in channelgrid))
    ipg_cursor = '11';

  redraw_ipg();

  $("#ipg-layer").css ("opacity", "0");
  $("#ipg-layer").show();

  var phase_out_ch =
    {
    opacity: "0",
    bottom: "-3em"
    };

  setTimeout ("outt()", 500);

  $("#ch-layer").animateWithCss (phase_out_ch, 500, "ease-in-out", function()
    {
    $("#ch-layer").css ("display", "none");
    $("#ch-layer").css ("bottom", "0");
    $("#ch-layer").css ("opacity", "1");
    elastic(); /* fixups */
    });

  thumbing = 'ipg';

  $("#ipg-signin-btn").removeClass ("on");
  $("#ipg-return-btn").removeClass ("on");
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
  elastic();
  extend_ipg_timex();
  }

function ipg_idle()
  {
  ipg_timex = 0;
  if (thumbing == 'ipg')
    {
    escape();
    switch_to_whats_new();
    }
  }

function extend_ipg_timex()
  {
  if (ipg_timex)
    clearTimeout (ipg_timex);
  ipg_timex = setTimeout ("ipg_idle()", 45000);
  }

function redraw_ipg()
  {
  var html = "";
  
  //var bad_thumbnail = '<span style="position: absolute; top: 0; left: 0; padding: 5px; font-size: 0.6em; text-align: left; color: white">BAD<br>THUMBNAIL</span>';
  var bad_thumbnail = '<img src="http://zoo.atomics.org/video/images-x1/no_images.png">';

  for (var y = 1; y <= 9; y++)
    {
    //if (y >= 8)
    //  html += '<ul class="ipg-list private" id="row-' + y + '">';
    // else
    html += '<ul class="ipg-list" id="row-' + y + '">';

    html += '<li class="rowNum"><span>' + y + '</span></li>';

    for (var x = 1; x <= 9; x++)
      {
      if ("" + y + "" + x in channelgrid)
        {
        var thumb = channelgrid ["" + y + "" + x]['thumb'];
        if (thumb == '' || thumb == 'null' || thumb == 'false')
          html += '<li id="ipg-' + y + '' + x + '">' + bad_thumbnail + '</li>';
        else
          html += '<li id="ipg-' + y + '' + x + '"><img src="' + channelgrid ["" + y + "" + x]['thumb'] + '"></li>';
        }
      else
        html += '<li id="ipg-' + y + '' + x + '"></li>';
      }
    html += '</ul>';
    }

  $("#ipg-grid").html (html);
  $("#ipg-grid img").error(function () { $(this).unbind("error").attr("src", "http://zoo.atomics.org/video/images-x1/no_images.png"); });

  // ipg_cursor = parseInt (channel_line [channel_cursor]);

  if (ipg_cursor > 0)
    $("#ipg-" + ipg_cursor).addClass ("on");

  ipg_metainfo();
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
    $("#ep-name").html ('<p>An episode name would go here?</p>');
    $("#description").html ('<p>A description of something is supposed to go here, but I have nothing to put in this spot.</p>');
    $("#ch-episodes").html (programs_in_channel (ipg_cursor));
    $("#ep-number").show();
    $("#update").show();
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
      $("#ch-name").html ('<p>Add Channel</p>');
      }

    $("#ep-name").html ('<p></p>');
    $("#description").html ('<p></p>');
    $("#ep-number").hide();
    $("#update").hide();
    }
  }

function ipg_right()
  {
  log ("IPG RIGHT: old ipg cursor: " + ipg_cursor);

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

    if (ipg_cursor == 99)
      ipg_cursor = 11;
    else if (ipg_cursor % 10 == 9)
      ipg_cursor += 2; /* 39 -> 41 */
    else
      ipg_cursor++;
    }

  log ("new ipg cursor: " + ipg_cursor);

  $("#ipg-" + ipg_cursor).addClass ("on");
  ipg_metainfo();
  }

function ipg_left()
  {
  log ("IPG LEFT: old ipg cursor: " + ipg_cursor);

  if (ipg_cursor < 0)
    return;

  $("#ipg-" + ipg_cursor).removeClass ("on");

  // ipg_cursor = previous_channel_square (ipg_cursor)

  if (ipg_cursor % 10 == 1)
    {
    log ('switching to ipg left panel');
    ipg_cursor = -2;
    $("#ipg-signin-btn").addClass ("on");
    return;
    }

  if (ipg_cursor == 11)
    ipg_cursor = 99;
  else if (ipg_cursor % 10 == 1)
    ipg_cursor -= 2; /* 41 -> 39 */
  else
    ipg_cursor--;

  log ("new ipg cursor: " + ipg_cursor);

  $("#ipg-" + ipg_cursor).addClass ("on");
  ipg_metainfo();
  }

function ipg_up()
  {
  log ("IPG UP: old ipg cursor: " + ipg_cursor);

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

    if (ipg_cursor > 19)
      ipg_cursor -= 10;
    else
      ipg_cursor += 80;
    }

  log ("new ipg cursor: " + ipg_cursor);

  $("#ipg-" + ipg_cursor).addClass ("on");
  ipg_metainfo();
  }

function ipg_down()
  {
  log ("IPG DOWN: old ipg cursor: " + ipg_cursor);

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
      escape();
      switch_to_channel_thumbs()
      return;
      }
    }
  else if (ipg_cursor > 90)
    {
    escape();
    switch_to_channel_thumbs()
    enter_channel();
    return;
    }

  $("#ipg-" + ipg_cursor).removeClass ("on");

  if (ipg_cursor < 90)
    ipg_cursor += 10;
  else
    ipg_cursor -= 80;
  log ("new ipg cursor: " + ipg_cursor);

  $("#ipg-" + ipg_cursor).addClass ("on");
  ipg_metainfo();
  }

function ipg_play()
  {
  log ('ipg play: ' + ipg_cursor);

  if (ipg_cursor < 0)
    {
    if (ipg_cursor == -1)
      {
      escape();
      switch_to_channel_thumbs();
      enter_channel();
      }
    else if (ipg_cursor == -2)
      {
      login_screen();
      }
    return;
    }

  if (! (ipg_cursor in channelgrid))
    {
    browse();
    return;
    }

  if (programs_in_channel (ipg_cursor) < 1)
    {
    log_and_alert ('no programs in channel!');
    return;
    }

  enter_category ((""+ipg_cursor).substring (0, 1));

  for (var c in channel_line)
    {
    if (channel_line [c] == ipg_cursor)
      {
      channel_cursor = c;
      redraw_channel_line()
      thumbing = 'channel';
      $("#ch-layer").css ("display", "block");
      $("#ipg-layer").css ("display", "none");
      play_first_program_in (channel_line [channel_cursor]);
      enter_channel();
      return;
      }
    }
  }

function channel_right()
  {
  if (channel_cursor < n_channel_line)
    channel_cursor++;
  else
    enter_category (next_category(), 'b');

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

  //log ('redrawn: ' + $("#ch-list-" + current_category).html());

  // old way, replace all html inside div:
  // $("#ch-list-" + current_category).html (ch_html (current_category));

  // ShowArrows();
  }

function program_right()
  {
  if (program_cursor < n_program_line)
    {
    program_cursor++;
    redraw_program_line();
    physical_stop();
    if (tube() == 'fp') play_program();
    }
  else
    {
    physical_stop();
    end_message (0);
    }
  }

function program_left()
  {
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
    }

  while (program_cursor >= program_first + max_programs_in_line)
    {
    ++program_first;
    $("#ep-list").html (ep_html());
    }

  log ('redraw program line');
  for (var i = program_first; i <= n_program_line && i < program_first + max_programs_in_line; i++)
    {
    var dd = document.getElementById ("p-li-" + i);
    if (i == program_cursor)
      {
      if (!(dd.className == 'on'))
        {
        dd.className = 'on';
        // log ("#" + i + " on");
        }
      }
    else
      {
      if (dd.className == 'on')
        {
        dd.className = '';
        // log ("#" + i + " off");
        }
      }
    }

  // ShowArrows();
  }

function setup_ajax_error_handling()
  {
  $.ajaxSetup ({ error: function (x, e)
    {
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
    var fields = data.split ('\t');
    user = fields [1];
    if (fields [0] == "0")
      {
      /* wipe out the current guest account program+channel data */
      channelgrid = {};
      programgrid = {};
      escape();
      log_and_alert ('logged in as user: ' + user);
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
    var fields = data.split ('\t');
    user = fields [1];
    if (fields [0] == "0")
      {
      /* wipe out the current guest account program+channel data */
      channelgrid = {};
      programgrid = {};
      escape();
      log_and_alert ('signed up as user: ' + user);
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

  var serialized = $("#throw").serialize() + '&' + 'user=' + user + '&' + 'grid=' + server_grid (ipg_cursor);
  log ('throw: ' + serialized);

  $.post ("/playerAPI/podcastSubmit", serialized, function (data)
    {
    var fields = data.split ('\t');
    if (fields [0] == "0")
      {
      escape();
      log_and_alert ('podcast thrown!')
      // fields: 0=status 1=channel-id 2=channel-name 3=channel-thumb
      channelgrid [ipg_cursor] = { 'id': fields[1], 'name': fields[2], 'thumb': fields[3] };
      channels_by_id [fields[1]] = ipg_cursor;
      redraw_ipg();
      add_dirty_channel (fields[1]);
      }
    else
      log_and_alert ("PODCAST THROW FAIL: " + fields [1]);
    })
  }

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

    var lines = data.split();
    for (var i = 0; i < lines.length; i++)
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

function login()
  {
  log ('login')
  var u = getcookie ("user");

  if (u)
    {
    log ('user cookie exists, checking');

    var d = $.get ("/playerAPI/userTokenVerify?token=" + u, function (data)
      {
      log ('response to userTokenVerify: ' + data);

      var fields = data.split ('\t');

      if (fields[0] == '0')
        {
        log ('user token was valid');
        user = u;
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

    var d = $.get ("/playerAPI/guestRegister", function (data)
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
        fetch_programs();
        }
      else
        panic ("was not able to get a user cookie");
      });
    }
  }

function browse()
  {
  n_browse = 0;
  browse_first = 1;

  saved_thumbing = thumbing;

  // why doesn't this work?
  document.getElementById("podcastRSS").value = "";

  log ('obtaining browse information');

  var query = "/playerAPI/channelBrowse";

  // 1  570     Channel One     /thumb/01.jpg
  // 0=grid-location(ignore) 1=channel-id 2=channel-name 3=channel-thumb

  var d = $.get (query, function (data)
    {
    // <li class="on"><img src="thumb/abc.jpg"><span>ABC Entertainment</span></li>
    // <li><img src="thumb/abc.jpg"><span>ABC Entertainment</span></li>

    sanity_check_data ('channelBrowse', data);

    var lines = data.split ('\n');
    log ('number of browse channels obtained: ' + lines.length);
    for (var i = 0; i < lines.length; i++)
      {
      if (lines [i] != '')
        {
        var fields = lines[i].split ('\t');
        if (fields [1] in channels_by_id)
          {
          // log ('channel ' + fields [1] + ' already in lineup: ' + fields [2]);
          }
        else
          {
          // log ('browse ' + fields [1] + ': ' + lines [i]);

          n_browse++;
          browsables [n_browse] = { 'id': fields [1], 'thumb': fields [3], 'name': fields [2], 'count': fields [4] };
          }
        }
      }

    log ('displaying browse');

    browse_cursor = 1;
    redraw_browse();
    
    $("#mask").show();
    $("#podcast-layer").show();

    thumbing = 'browse';

    if (n_browse > 0)
      $("#pod-" + browse_cursor).addClass ("on");

    document.getElementById("podcastRSS").value = "";
    });
  }

function redraw_browse()
  {
  var html = "";

  for (var i = browse_first; i <= n_browse && i <= browse_first + 9; i++)
    {
    var count = browsables [i]['count'] >= 0 ? ' <span style="color: orange">(' + browsables [i]['count'] + ')</span>' : '';
    var thumb = browsables [i]['thumb'];
    if (thumb == '' || thumb == 'null' || thumb == 'false')
      thumb = 'http://zoo.atomics.org/video/images-x1/no_images.png';
    html += '<li id="pod-' + i + '"><img src="' + thumb + '"><span>' + browsables [i]['name'] + '</span>' + count + '</li>';
    }

  $("#podcast-list").html (html);
  $("#podcast-list img").error(function () { $(this).unbind("error").attr("src", "http://zoo.atomics.org/video/images-x1/no_images.png"); });

  if (n_browse > 0)
    $("#pod-" + browse_cursor).addClass ("on");
  }

function browse_up()
  {
  if (n_browse > 0 && browse_cursor > 1)
    {
    if (browse_cursor - 1 < browse_first)
      {
      browse_first--;
      redraw_browse();
      }
    $("#pod-" + browse_cursor).removeClass ("on");
    browse_cursor--;
    $("#pod-" + browse_cursor).addClass ("on");
    // log ('browser now at: ' + browsables [browse_cursor]['id']);
    }
  }

function browse_down()
  {
  if (n_browse > 0 && browse_cursor < n_browse)
    {
    if (browse_cursor + 1 > browse_first + 9)
      {
      browse_first++;
      redraw_browse();
      }
    $("#pod-" + browse_cursor).removeClass ("on");
    browse_cursor++;
    $("#pod-" + browse_cursor).addClass ("on");
    // log ('browser now at: ' + browsables [browse_cursor]['id']);
    }
  }

function browse_page_up()
  {
  if (n_browse > 0 && browse_cursor > 1)
    {
    if (browse_cursor - 10 < browse_first)
      {
      browse_first -= 10;
      if (browse_first < 1) browse_first = 1;
      redraw_browse();
      }

    $("#pod-" + browse_cursor).removeClass ("on");
    browse_cursor -= 10;
    if (browse_cursor < 1) browse_cursor = 1;
    $("#pod-" + browse_cursor).addClass ("on");
    // log ('browser now at: ' + browsables [browse_cursor]['id']);
    }
  }

function browse_page_down()
  {
  if (n_browse > 0 && browse_cursor < n_browse)
    {
    if (browse_cursor + 10 > browse_first + 9)
      {
      browse_first += 10;
      if (browse_first > n_browse) browse_first = n_browse - 9;
      if (browse_first < 1) browse_first = 1;
      redraw_browse();
      }
    $("#pod-" + browse_cursor).removeClass ("on");
    browse_cursor += 10;
    if (browse_cursor > n_browse) browse_cursor = n_browse;
    $("#pod-" + browse_cursor).addClass ("on");
    // log ('browser now at: ' + browsables [browse_cursor]['id']);
    }
  }

function browse_accept()
  {
  if (browse_cursor in browsables)
    {
    var new_channel_id = browsables [browse_cursor]['id'];

    log ('browser accepts: ' + new_channel_id +  ' (' + server_grid (ipg_cursor) + ')');

    var cmd = "/playerAPI/subscribe?user=" + user + '&' + "channel=" + new_channel_id + '&' + "grid=" + server_grid (ipg_cursor);
    var d = $.get (cmd, function (data)
      {
      continue_acceptance();
      });
    }
  }

function continue_acceptance()
  {
  log ('continuing');

  /* insert channel */

  var name = browsables [browse_cursor]['name'];
  var thumb = browsables [browse_cursor]['thumb'];
  var new_channel_id = browsables [browse_cursor]['id'];

  channelgrid [ipg_cursor] = { 'id': new_channel_id, 'name': name, 'thumb': thumb };
  channels_by_id [new_channel_id] = ipg_cursor;

  redraw_ipg();
  elastic();

  /* obtain programs */

  log ('obtaining programs for: ' + new_channel_id);

  var cmd = "/playerAPI/programInfo?channel=" + new_channel_id + '&' + "user=" + user;

  var d = $.get (cmd, function (data)
    {
    sanity_check_data ('programInfo', data);
    parse_program_data (data);
    escape();
    redraw_ipg();
    elastic();
    // for now, stay in IPG
    // ipg_play()
    });
  }

function unsubscribe_channel()
  {
  if (ipg_cursor in channelgrid)
    {
    var grid = server_grid (ipg_cursor);
    var channel = channelgrid [ipg_cursor]['id'];

    var cmd = "/playerAPI/unsubscribe?user=" + user + '&' + "channel=" + channel + '&' + "grid=" + grid;
    var d = $.get (cmd, function (data)
      {
      delete (channelgrid [ipg_cursor]);
      redraw_ipg();
      elastic();
      });
    }
  }

function sanity_check_data (what, data)
  {
  log ('sanity check ' + what);

  var lines = data.split ('\n');

  if (lines.length > 9 && lines [0] == '' && lines [1] == '')
    {
    log_and_alert ('very bad data returned from ' + what + ' API');
    return true;
    }

  return false;
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
    $("#btn-play").css ("background-image", "url(" + root + "btn_pause.svg)");
    }
  else
    {
    physical_pause();
    $("#btn-play").css ("background-image", "url(" + root + "btn_play.svg)");
    }
  }

function unhide_player (player)
  {
  log ('unhide: ' + player);

  switch (player)
    {
    case "jw":

      $("#v").hide();
      $("#fp").hide();
      $("#jw2").show();
      break;

    case "fp":

      $("#v").hide();
      $("#jw2").hide();
      $("#fp").show();
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
  $("#btn-play").css ("background-image", "url(" + root + "btn_pause.svg)");
  }

function start_play_yt (url)
  {
  yt_video_id = url.match (/v=([^&]+)/)[1];
  log ('YouTube video: ' + yt_video_id);
  // setup_yt();
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
                 flowplayer ("player").stop();
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

function start_play_fp (url)
  {
  jw_position = 0;
  current_tube = 'fp';

  // ugh! don't know actual url until player is chosen
  url = best_url (current_program);

  fp_video_file = url.replace (/^fp:/, '');
  unhide_player ("fp");

  log ("FP STREAM: " + fp_video_file);

  flowplayer ("player", {src: 'http://zoo.atomics.org/video/flowplayer-3.2.5.swf', wmode: 'transparent'}, { canvas: { backgroundColor: '#000000', backgroundGradient: 'none' }, clip: { onFinish: fp_ended, onStart: fp_onstart, bufferLength: 1, autoPlay: true, scaling: 'fit' }, plugins: { controls: null }, play: null });
  flowplayer ("player").play (fp_video_file);
  }

function fp_onstart()
  {
  log ('fp onstart')
  var fd = parseInt (this.getClip().fullDuration, 10);
  fp_duration = fd * 1000;
  }

function fp_ended()
  {
  log ('fp ended');
  ended_callback();
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
        {
        log ("FP OFFSET: " +  flowplayer ("player").getTime());
        return flowplayer().getTime() * 1000;
        }
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

      if (ytplayer && ytplayer.getDuration)
        return ytplayer.getDuration();

    case "jw":

      if (jwplayer)
        return jwplayer.getPlaylist()[0]['duration'];
      else
        return 1;

    case "fp":

      if (flowplayer && fp_duration)
        return fp_duration;
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
         flowplayer ("player").pause();
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
        flowplayer ("player").play();
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
        return flowplayer ("player").isPaused();
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
        flowplayer ("player").seek (0);
        flowplayer ("player").resume();
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
  if (diff < 1)
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
  $('#' + control_buttons [control_cursor]).addClass ("on");
  $("#ch-layer").hide();
  $("#ep-layer").hide();
  control_saved_thumbing = thumbing;
  thumbing = 'control';
  $("#btn-play").css ("background-image", "url(" + root + "btn_" + (physical_is_paused() ? "play" : "pause") + ".svg)");
  $("#control-layer").show();
  }

function control_left()
  {
  $('#' + control_buttons [control_cursor]).removeClass ("on");

  if (control_cursor > 0)
   control_cursor--;
  else
   control_cursor = control_buttons.length - 1;

  $('#' + control_buttons [control_cursor]).addClass ("on");
  }

function control_right()
  {
  $('#' + control_buttons [control_cursor]).removeClass ("on");

  if (control_cursor < control_buttons.length - 1)
    control_cursor++;
  else
    control_cursor = 0;

  $('#' + control_buttons [control_cursor]).addClass ("on");
  }

function control_enter()
  {
  switch (control_buttons [control_cursor])
    {
    case 'btn-close':  escape();
                       break;

    case 'btn-play':   pause();
                       break;

    case 'btn-signin': login_screen();
                       break;

    case 'btn-replay': physical_replay();
                       break;
    }
  }

function playerReady (thePlayer)
  {
return;
  log ('jw player ready: ' + thePlayer.id);
  jwplayer = document.getElementById (thePlayer.id);
  jwplayer.sendEvent ('LOAD', 'nothing.flv');
  }

</script>

<title>Elastic 9x9 Player</title>

</head>

<body id="body" style="background: black; overflow: hidden">

<div id="blue" style="background: black; width: 100%; height: 100%; display: block; position: absolute; color: white">
One moment...
</div>

<div id="notblue" style="width: 100%; display: none; position: absolute; top: 0; margin: 0; overflow: hidden">

  <div id="all-players" style="display: block; padding: 0">
    <div id="v" style="display: block; padding: 0">
      <video id="vvv" autoplay="false" preload="metadata" loop="false" height="100%" width="100%" volume="0"></video></div>

<div id="fp" style="width: 100%; height: 100%; display: none">
  <a href="http://e1h13.simplecdn.net/flowplayer/flowplayer.flv" style="display:block;width:100%;height:100%" id="player"></a>
</div>

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

<!--div id="jw-template" style="display: none">
        <embed name="%ID%" id="%ID%"
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

<div id="ch-layer" style="display: block">
  <div id="ch-container">
    <div class="arrow-up"></div><div class="arrow-down"></div>

    <div id="left-piece"><span class="rowNum" id="left-row-num">1</span></div>

    <div id="ch-constrain">
      <div class="ch-strip">
        <ul id="ch-meta">
          <li id="ch-layer-ch-title" class="ch-title"></li>
          <li class="dash">&#8212;</li>
          <li id="ch-layer-ep-title" class="ep-title"></li>
        </ul>

        <div class="ch-swish" id="ch-swish-1" style="display: block"><ul id="ch-list-1" class="ch-list"></ul></div>
        <div class="ch-swish" id="ch-swish-2" style="display: block"><ul id="ch-list-2" class="ch-list"></ul></div>

        <div class="ch-swish" id="ch-swish-3" style="display: block"><ul id="ch-list-3" class="ch-list"></ul></div>
        <div class="ch-swish" id="ch-swish-4" style="display: block"><ul id="ch-list-4" class="ch-list"></ul></div>
        <div class="ch-swish" id="ch-swish-5" style="display: block"><ul id="ch-list-5" class="ch-list"></ul></div>
        <div class="ch-swish" id="ch-swish-6" style="display: block"><ul id="ch-list-6" class="ch-list"></ul></div>
        <div class="ch-swish" id="ch-swish-7" style="display: block"><ul id="ch-list-7" class="ch-list"></ul></div>
        <div class="ch-swish" id="ch-swish-8" style="display: block"><ul id="ch-list-8" class="ch-list"></ul></div>

        <div class="ch-swish" id="ch-swish-9" style="display: block"><ul id="ch-list-9" class="ch-list"></ul></div>
      </div>
      </div>

    </div>
    <div id="right-piece"><span class="rowNum" id="right-row-num">3</span></div>
  </div>
</div>

<div id="ep-layer">

  <div id="ep-container">
    <div class="arrow-up"></div>
    <div id="ep-constrain">
      <ul id="ep-meta">

        <li id="ep-layer-ch-title" class="ch-title"></li>
        <li class="dash">&#8212;</li>
        <li id="ep-layer-ep-title" class="ep-title"></li>
      </ul>

      <div id="ep-swish" class="ep-swish" style="display: block">
        <ul class="ep-list" id="ep-list"></ul>
      </div>
    </div>
  </div>

</div>

<div id="ipg-layer" style="display: none">
  <div id="ipg-pannel">
    <ul id="info-list">

      <li id="ch-thumb"><img id="ch-thumb-img" src=""></li>
      <li id="ch-name"><p>ABC news</p></li>
      <li id="ep-name"><p>An episode name would go here?</p></li>

      <li id="description"><p>A description of something is supposed to go here, but I have nothing to put in this spot.</p></li>

      <li id="ep-number"><p><span class="hilite">Episodes: </span><span id="ch-episodes">9</span></p></li>

      <li id="update"><p><span class="hilite">Updated:</span> 11/09/2010</p></li>
    </ul>
    <ul id="control-list">
      <li><a id="ipg-signin-btn" href="javascript:;" class="btn">Sign in / Sign up</a></li>
      <li><a id="ipg-return-btn" href="javascript:;" class="btn">Return to Channel Mode</a></li>

    </ul> 
    <img src="http://zoo.atomics.org/video/9x9playerV19/images/logo.png" id="logo">
  </div>

  <div id="ipg-grid"></div>
  <div id="menu">
    <p id="pop-play"></p><p id="pop-delete"></p>
  </div>
</div>

<div id="new-layer" style="display: none">
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

<div id="podcast-layer">
  <div id="padcast-pannel">

    <label for="podcast input">Enter Podcast URL:</label>
    <ul id="podcast-input">

      <li class="textfieldbox"><form id="throw"><input id="podcastRSS" name="podcastRSS" type="text" class="textfield" value="" size="30" onkeypress="return event.keyCode != 13"></form></li>
      <li><a href="javascript:submit_throw()" class="btn"><span>Contribute</span></a></li>

    </ul>
    <ul id="podcast-list"></ul>
  </div>
</div>

<div id="browse" style="display: none; z-index: 999"></div>

<div id="preload-control-images" style="display: none"></div>

<div id="control-layer" style="display: none">
  <div id="msg-up"><p>Press <span class="enlarge">&uarr;</span> to see your programming guide</p></div> 
  <ul id="control-bar">

    <li id="btn-replay"></li>
    <li id="btn-rewind"></li>
    <li id="btn-play" class="on"></li>
    <li id="btn-pause"></li>

    <li id="btn-forward"></li>
    <li class="divider"></li>
    <li id="btn-volume"></li>
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
    <li id="btn-facebook"></li>

    <li id="btn-screensaver"></li>
    <li class="divider"></li>
    <li id="btn-close"></li>
    <li id="play-time">00:52 / 01:32</li>
    <li id="progress-bar">
      <p id="loaded"></p>

      <p id="played"></p>
    </li>

  </ul> 
  <div id="msg-down"><p>Press <span class="enlarge">&darr;</span> for more episodes</p></div>
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


<div id="log-layer" style="position: absolute; left: 0; top: 0; height: 100%; width: 100%; background: white; color: black; text-align: left; padding: 20px; overflow: scroll; z-index: 9999; display: none"></div>

<div id="mask"></div>
<div id="loading"></div>

</div>
</body>
</html>