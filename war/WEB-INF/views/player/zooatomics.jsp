<!DOCTYPE html>
<head>
<meta charset="utf-8" />
<style>
* {
  margin: 0;
  padding: 0;
  outline: none;
}

body {
  text-align: center;
  font-family: Arial, Helvetica, sans-serif;
  font-size: 100%;
  color: #fff;
  background-color: black;
  overflow: hidden;
}

#ipg {
  padding: 0.625em 0;
  /* width: 34.875em; */
  width: 38em;
  text-align: center;
  /* background: #000; */
  background-color:rgba(0,0,0,0.7);
  position: absolute;
  left: 5em;
  top: 0em;
  overflow: hidden;
}

#container {
  /* padding: 0.625em 0; */
  padding: 0.3em 0;
  width: 45em;
  text-align: center;
  /* background: #000; */
  background-color:rgba(0,0,0,0.7);
  position: absolute;
  left: 0;
  bottom: 0;
  overflow: hidden;
}

.thumbline {
  list-style: none;
  margin-left: 0;
  display: block;
}

.thumbline li {
  margin: 0 0.25em 0 0;
  padding: 0.125em;
  /* width: 3.75em;
     height: 2.25em; */
  width: 3.0em;
  height: 1.8em;
  /* line-height: 2.25em; */
  background: #666;
  border: #999 0.0625em solid;
  -moz-border-radius: 0.3125em; /* FF1+ */
  -webkit-border-radius: 0.3125em; /* Saf3+, Chrome */
  border-radius: 0.3125em; /* Opera 10.5, IE 9 */
  display: inline-block;
}

.thumbline li:last-child {
  /* margin: 0; */
}

.thumbline li img {
  /* width: 3.75em;
     height: 2.25em; */
  width: 3.0em;
  height: 1.8em;
}

.thumbline li a {
  font-size: 0.75em;
  color: #fff;
  text-decoration: none;
}

.subthumb {
  list-style: none;
  display: block;
}

.subthumb li {
  margin: 0 0.25em 0 0;
  padding: 0.125em;
  /* width: 3.75em;
     height: 2.25em; */
  width: 3.0em;
  height: 0.5em;
  /* line-height: 2.25em; */
  /* background: none; */
  /* border: #999 0.0625em solid; */
  border: transparent 0.0625em solid;
  display: inline-block;
}

.subthumb li:last-child {
  margin: 0;
}

.subthumb li a {
  font-size: 0.75em;
  color: #fff;
  text-decoration: none;
}

.ipgthumb {
  list-style: none;
}

.ipgthumb li {
  margin: 0 0.50em 0.50em 0;
  padding: 0.125em;
  /* width: 3.75em;
     height: 2.25em; */
  width: 3.0em;
  height: 1.8em;
  /* line-height: 2.25em; */
  background: #666;
  border: #999 0.0625em solid;
  -moz-border-radius: 0.3125em; /* FF1+ */
  -webkit-border-radius: 0.3125em; /* Saf3+, Chrome */
  border-radius: 0.3125em; /* Opera 10.5, IE 9 */
  display: block;
  float: left;
}

.ipgthumb li:last-child {
  /* margin: 0; */
}

.ipgthumb li img {
  /* width: 3.75em;
     height: 2.25em; */
  width: 3.0em;
  height: 1.8em;
}

.ipgthumb li a {
  font-size: 0.75em;
  color: #fff;
  text-decoration: none;
}

#v {
  /* padding: 0.625em 0 0 0.625em; */
  width: 45em;
  position: absolute;
  top: 0;
  background-color: black;
  text-align: left;
}

.circle
  {
  width: 0.3em;
  height: 0.3em;
  background: #999;
  -moz-border-radius: 0.15em;
  -webkit-border-radius: 0.15em;
  border-radius: 0.15em;
  display: inline-block;
  margin:.2em;
  }

.fix-circle
  {
  width: 10px;
  height: 10px;
  background: #999;
  -moz-border-radius: 5px;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  display: inline-block;
  margin:0;
  }

.css-arrow-left {
  border-color:transparent #333333 transparent transparent;
  border-style:solid;
  border-width:10px 20px 10px 0;
  float: left;
  height:0;
  margin:0 10px;
  width:0;
  }

.css-arrow-down {
  border-color:#333333 transparent transparent;
  border-style:solid;
  border-width:.6em .6em 0;
  /* float:left; */
  height:0;
  /* margin:0 10px; */
  display: block;
  width:0;
}

.css-arrow-down-equivalent {
  border-color:transparent transparent transparent;
  border-style:solid;
  border-width:.6em .6em 0;
  /* float:left; */
  height:0;
  /* margin:0 10px; */
  display: block;
  width:0;
}

.css-arrow-up {
  border-color:transparent transparent #333333 transparent;
  border-style:solid;
  border-width:0 .6em .6em;
  /* float:left;  */
  height:0;
  /* margin:0 10px; */
  margin: 0 auto;
  display: block;
  width:0;
  }

.css-arrow-up-box {
  /* float:left;  */
  height:20px;
  /* margin:0 10px; */
  margin: 0 auto;
  display: block;
  width:20px;
  background-color: red;
  }

.css-arrow-right {
border-color: transparent transparent transparent #333333;
border-style:solid;
border-width:10px 0 10px 20px;
float:left;
height:0;
margin:0 10px;
width:0;
}

.swish {
  position: absolute;
  text-align: center;
  top: 2.8em;
  left: 0px;
  height: 2.8em;
  width: 45em;
  opacity: 1;
  display: none;
}

</style>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
<script type="text/javascript" src="http://zoo.atomics.org/video/cssanim.js"></script>


<script>

var current_category = 0;
var current_program = '';

var thumbing = 'channel';

var channelgrid = {};
var channel_line = {};
var n_channel_line = 0;

var programgrid = {};
var program_line = {};
var n_program_line = 0;

var ipg_cursor;

var user = "aghubmUydm1zb3IMCxIGTm5Vc2VyGBoM";

$(document).ready (function()
 {
 //elastic(); 
 fetch_programs();
 $(window).resize (function() { elastic(); });
 });

function elastic()
  {
  var newWidth = $(window).width() / 16 ;
  var newHeight = $(window).height() / 16;

  var oriWidth = 45;
  var times = newWidth / oriWidth * 100 + "%";
  $("body").css("font-size",times);

  var v = document.getElementById ("v");
  var b = document.getElementById ("body");
  var c = document.getElementById ("container");
  var nb = document.getElementById ("notblue");
  //var vh = $(window).height() - c.offsetHeight;
  var vh = $(window).height();
  b.style.height = vh + "px";
  v.style.height = (vh-10) + "px";
  nb.style.height = vh + "px";
  /*
  var offset = $("#video").offset();
  */

  /*
  var aloft = document.getElementById ("aloft");
  aloft.style.left = offset.left + "px";
  aloft.style.top = offset.top + "px";
  aloft.style.height = $("#video").height() + "px";
  aloft.style.width = $("#video").width() + "px";
  aloft.style.display = "block";
  */
  }

function log (text)
  {
  if (window.console)
    console.log (text);
  }

function panic (text)
  {
  log (text);
  alert (text);
  }

function fetch_programs()
  {
  log ('obtaining programs');

  var query = "/player/programInfo?channel=*" + String.fromCharCode(38) + "user=" + user;

  // 0=channel-id 1=program-id 2=program-name 3=program-type 4=program-thumb-url 5=program-url

  var d = $.get (query, function (data)
    {
    var lines = data.split ('\n');
    log ('number of programs obtained: ' + lines.length);
    for (var i = 0; i < lines.length; i++)
      {
      if (lines [i] != '' && lines [i].substring (0,1) != '#')
        {
        var fields = lines[i].split ('\t');
        log ("program line " + i + ": " + fields[0] + ' = ' + lines [i]);

        if (fields [3] == 'slideshow')
          fields [5] = 'slideshow:' + fields [5];

        if (navigator.userAgent.match (/(GoogleTV|Droid Build)/i))
          fields[5] = fields[5].replace (/webm$/, 'mp4');

        programgrid [fields [1]] = { 'channel': fields[0], 'url': fields[5], 'name': fields[2], 'type': fields[3], 'thumb': fields[4] };
        }
      else
        log ("ignoring program line " + i + ": " + lines [i]);
      }
    fetch_channels();
    });
  }

function fetch_channels()
  {
  log ('obtaining channels');

  var query = "/player/channelLineup?user=" + user;

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
      if (lines [i] != '' && lines [i].substring (0,1) != '#')
        {
        var fields = lines[i].split ('\t');
        log ("channel line " + i + ": " + conv [fields[0]] + ' = ' + lines [i]);
        channelgrid [conv [fields[0]]] = { 'id': fields[1], 'name': fields[2], 'thumb': fields[3] };
        }
      else
        log ("ignoring channels line " + i + ": " + lines [i]);
      }
    activate();
    });
  }

function activate()
  {
  reset_category_dots();
  enter_category (1, 'b');
  elastic();
  play_first_program_in (first_channel());
  document.onkeydown=kp;
  }

function reset_category_dots()
  {
  for (var i = 1; i <= 9; i++)
    {
    if (channels_in_category (i) > 0)
      $("#c" + i).css ("background", "#999");
    else
      $("#c" + i).css ("background", "#484848");
    }
  }

function play_first_program_in (chan)
  {
  program_cursor = 1;

  current_program = first_program_in (chan);
  log ('playing first program in ' + chan + ': ' + current_program);

  var url = programgrid [current_program]['url'];

  var v = document.getElementById ("vvv");
  v.src = url;

  log ('playing: ' + url);
  }

function play_program()
  {
  current_program = program_line [program_cursor];

  var url = programgrid [current_program]['url'];

  var v = document.getElementById ("vvv");
  v.src = url;

  log ('playing: ' + url);
  }

function switch_to_channel_thumbs()
  {
  enter_category (current_category, '');
  thumbing = 'channel';
  }

function switch_to_program_thumbs()
  {
  enter_channel();
  thumbing = 'program';
  }

function enter_channel()
  {
  program_line = {};

  var channel = channel_line [channel_cursor];
  var real_channel = channelgrid [channel]['id'];

  var html = "";
  n_program_line = 0;

  for (p in programgrid)
    {
    if (programgrid [p]['channel'] == real_channel)
      {
      program_line [++n_program_line] = p;
      html += '<li id="p-li-' + n_program_line + '"><img id="p-th-"' + n_program_line + '" src="' + programgrid [p]['thumb'] + '"></li>';
      }
    }

  /* known to be same size as sline */
  stml = '<li><div class="css-arrow-down-equivalent" style="margin: 0 auto 0.4em auto"></div></li>';
  $("#dummy").html (stml);

    $("#pp").css ("opacity", "0");
    $("#pp").css ("top", "5.6em");
    $("#pp").css ("display", "block");

    var phase_in_pp =
      {
      opacity: "1",
      top: "2.8em"
      };

    var phase_out_cc =
      {
      opacity: "0",
      top: "0em"
      };

    $("#pp").animateWithCss (phase_in_pp, 500, "ease-in-out", function() {});

    $("#cc" + current_category).animateWithCss (phase_out_cc, 500, "ease-in-out", function() 
      {
      $("#cc" + current_category).css ("display", "none");
      $("#cc" + current_category).css ("top", "2.8em");
      $("#cc" + current_category).css ("opacity", "1");
      });

  $("#pline").html (html);

  $("#cat-up").css ("display", "none");
  $("#prog-up").css ("display", "inline-block");

  $("#container").css ("display", "block");

  redraw_program_line();
  }

var old_cline;
var new_cline;

function enter_category (cat, positioning)
  {
  channel_line = {};

  //$("#container").css ("display", "none");

  /* FIX - enter category from program mode */

  if (thumbing == 'program')
    {
    $("#cc" + current_category).css ("display", "block");
    $("#cc" + current_category).css ("opacity", "0");
    $("#cc" + current_category).css ("top", "-2.8em");

    // $("#pp").css ("top", "2.8m");

    var phase_out_pp =
      {
      opacity: "0",
      top: "5.6em"
      };

    var phase_in_cc =
      {
      opacity: "1",
      top: "2.8em"
      };

    $("#pp").animateWithCss (phase_out_pp, 500, "ease-in-out", function() 
      {
      $("#pp").css ("display", "none");
      $("#pp").css ("opacity", "1");
      $("#pp").css ("top", "2.8em");
      });

    $("#cc" + current_category).animateWithCss (phase_in_cc, 500, "ease-in-out", function() {});
    }

  if (current_category > 0)
    $("#cc" + current_category).css ("display", "block");

  if (current_category > 0 && cat != current_category)
    {
    old_cline = $("#cc" + current_category);
    new_cline = $("#cc" + cat);

    new_cline.css ("opacity", "0"); 
    new_cline.css ("display", "block"); 

    var phase_out =
      {
      opacity: "0",
      left: positioning == 'b' ? "-45em" : "45em"
      };

    var phase_in =
      {
      opacity: "1"
      };

    old_cline.animateWithCss (phase_out, 500, "ease-in-out", function() 
      {
      old_cline.css ("display", "none");
      old_cline.css ("opacity", "1");
      old_cline.css ("left", "0px");
      });

    new_cline.animateWithCss (phase_in, 700, "ease-in-out", function() 
      {
      });
    }

  $("#cline" + cat).html ("");
  $("#sline" + cat).html ("");

  if (cat != current_category)
    {
    $("#c" + current_category).css ("background", "#999");
    current_category = cat;
    log ('setting new category: ' + cat);
    }
  else
    log ('already in category: ' + cat);

  $("#c" + current_category).css ("background", "orange");

  var html = "";
  var stml = "";

  n_channel_line = 0;

  for (x = 1; x <= 9; x++)
    {
    var chan = "" + cat + "" + x;
    if (channelgrid [chan])
      {
      channel_line [++n_channel_line] = chan;
      html += '<li id="c-' + current_category + '-li-' + n_channel_line + '"><img id="c-th-' + n_channel_line + '" src="' + channelgrid [chan]['thumb'] + '"></li>';
      stml += '<li id="s-' + current_category + '-li-' + n_channel_line + '"><div id="s-' + current_category + '-ar-' + n_channel_line + '" class="css-arrow-down" style="margin: 0 auto 0.4em auto"></div></li>';
      log ('channel ' + channelgrid [chan]['id'] + ': ' + channelgrid [chan]['name']);
      }
    }

  $("#cline" + cat).html (html);
  $("#sline" + cat).html (stml);
  $("#cat-up").css ("display", "inline-block");
  $("#prog-up").css ("display", "none");
  $("#container").css ("display", "block");

  $("#notblue").css ("display", "block");
  $("#blue").css ("display", "none");

  /* position at beginning or ending */
  if (positioning == 'b')
    channel_cursor = 1;
  else if (positioning == 'e')
    channel_cursor = n_channel_line;
  
  redraw_channel_line();
  }

function next_channel_square (channel)
  {
  for (var i = channel + 1; i <= 99; i++)
    {
log ("trying: " + i);
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

function next_category()
  {
  log ('nxcat');
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
  if (! (channel in channelgrid))
    {
    alert ('channel ' + channel + ' not in channelgrid');
    return;
    }

  var real_channel = channelgrid [channel]['id'];

  for (p in programgrid)
    {
    if (programgrid [p]['channel'] == real_channel)
      return p;
    }

  panic ('No programs in channel: ' + channel + "(" + real_channel + ")");
  }

function kp (e)
  {
  var ev = e || window.event;
  log (ev.type + " keycode=" + ev.keyCode);

  keypress (ev.keyCode);
  }

function escape()
  {
  if ($("#container").css ("display") == "block")
    $("#container").css ("display", "none");
  else
    $("#container").css ("display", "block");

  if ($("#ipg").css ("display") == "block")
    {
    $("#ipg").css ("display", "none");
    thumbing = 'channel';
    }
  }

function keypress (keycode)
  {
  switch (keycode)
    {
    case 27:
      /* esc */
      escape();
      break;

    case 32:
      /* space */
    case 178:
      /* google TV play/pause */
      break;

    case 13:
      /* enter */
      if (thumbing == 'ipg')
        ipg_play();
      break;

    case 37:
      /* left arrow */
      if (thumbing == 'channel')
        channel_left();
      else if (thumbing == 'program')
        program_left();
      else if (thumbing == 'ipg')
        ipg_left();
      break;

    case 39:
      /* right arrow */
      if (thumbing == 'channel')
        channel_right();
      else if (thumbing == 'program')
        program_right();
      else if (thumbing == 'ipg')
        ipg_right();
      break;

    case 38:
      /* up arrow */
      if (thumbing == 'program')
        switch_to_channel_thumbs();
      else if (thumbing == 'channel')
        switch_to_ipg();
      break;

    case 40:
      /* down arrow */
      if (thumbing == 'channel')
        switch_to_program_thumbs();
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
      $("#body").css ("background-color", "orange");
      break;
    }
  }

function switch_to_ipg()
  {
  $("#ipg").css ("display", "none");
  redraw_ipg();

  //$("#container").css ("display", "none");
  //$("#ipg").css ("display", "block");

  $("#ipg").css ("opacity", "0");
  //$("#ipg").css ("top", "-3em");
  $("#ipg").css ("top", "0");
  $("#ipg").css ("display", "block");


    var phase_out_container =
      {
      opacity: "0",
      bottom: "-3em"
      };

   setTimeout ("outt()", 500);

    $("#container").animateWithCss (phase_out_container, 500, "ease-in-out", function() 
      {
      $("#container").css ("display", "none");
      $("#container").css ("bottom", "0");
      $("#container").css ("opacity", "1");
      });

  thumbing = 'ipg';
  }

function outt()
  {
    var phase_in_ipg =
      {
      opacity: "1"
      };
  $("#ipg").animateWithCss (phase_in_ipg, 500, "ease-in", function() {});
  }

function redraw_ipg()
  {
  var html = "";

  for (var y = 1; y <= 9; y++)
    for (var x = 1; x <= 9; x++)
      {
      if ("" + y + "" + x in channelgrid)
        html += '<li id="ipg-' + y + '' + x + '"><img src="' + channelgrid ["" + y + "" + x]['thumb'] + '"></li>';
      else
        html += '<li><span style="color: gray">' + y + '-' + x + '</span></li>';
      }

  $("#iline").html (html);

  ipg_cursor = parseInt (channel_line [channel_cursor]);

  $("#ipg-" + ipg_cursor).css ("border-color", "orange");
  $("#ipg-" + ipg_cursor).css ("background", "orange");
  }

function ipg_right()
  {
  log ("IPG RIGHT: old ipg cursor: " + ipg_cursor);
  $("#ipg-" + ipg_cursor).css ("border-color", "#999");
  $("#ipg-" + ipg_cursor).css ("background", "#666");

  ipg_cursor = next_channel_square (ipg_cursor)
  log ("new ipg cursor: " + ipg_cursor);

  $("#ipg-" + ipg_cursor).css ("border-color", "orange");
  $("#ipg-" + ipg_cursor).css ("background", "orange");
  }

function ipg_left()
  {
  log ("IPG LEFT: old ipg cursor: " + ipg_cursor);
  $("#ipg-" + ipg_cursor).css ("border-color", "#999");
  $("#ipg-" + ipg_cursor).css ("background", "#666");

  ipg_cursor = previous_channel_square (ipg_cursor)
  log ("new ipg cursor: " + ipg_cursor);

  $("#ipg-" + ipg_cursor).css ("border-color", "orange");
  $("#ipg-" + ipg_cursor).css ("background", "orange");
  }

function ipg_play()
  {
  //alert ("play: " + (""+ipg_cursor).substring (0, 1) + "-" + (""+ipg_cursor).substring (1,2));

  enter_category ((""+ipg_cursor).substring (0, 1));

  for (var c in channel_line)
    {
    if (channel_line [c] == ipg_cursor)
      {
      channel_cursor = c;
      thumbing = 'channel';
      $("#container").css ("display", "block");
      $("#ipg").css ("display", "none");
      play_first_program_in (channel_line [channel_cursor]);
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
  play_first_program_in (channel_line [channel_cursor]);
  }

function channel_left()
  {
  if (channel_cursor > 1)
    channel_cursor--;
  else
    enter_category (previous_category(), 'e');

  redraw_channel_line();
  play_first_program_in (channel_line [channel_cursor]);
  }

function redraw_channel_line()
  {
  for (var i = 1; i <= n_channel_line; i++)
    {
    log ('i: ' + i);
    var li = document.getElementById ("c-" + current_category + "-li-" + i);
    var sa = document.getElementById ("s-" + current_category + "-ar-" + i);
    if (i == channel_cursor)
      {
      li.style.borderColor = "orange";
      li.style.background = "orange";
      sa.style.display = "inline-block";
      }
    else
      {
      li.style.borderColor = "#999";
      li.style.background = "#666";
      sa.style.display = "none";
      }
    }
  }

function program_right()
  {
  if (program_cursor < n_program_line)
    program_cursor++;
  else
    program_cursor = 1;

  redraw_program_line();
  play_program();
  }

function program_left()
  {
  if (program_cursor > 1)
    program_cursor--;
  else
    program_cursor = n_program_line;

  redraw_program_line();
  play_program();
  }

function redraw_program_line()
  {
  for (var i = 1; i <= n_program_line; i++)
    {
    var li = document.getElementById ("p-li-" + i);
    if (i == program_cursor)
      {
      li.style.borderColor = "orange";
      li.style.background = "orange";
      }
    else
      {
      li.style.borderColor = "green";
      li.style.background = "green";
      }
    }
  }

</script>

<title>Elastic Layout</title>
</head>

<body id="body">

<div id="blue" style="background: black; width: 100%; height: 100%; display: block; position: absolute">
One moment...
</div>

<div id="notblue" style="width: 100%; display: none; position: absolute; top: 0; margin: 0">

  <div id="v" style="display: block; padding: 0">
    <video id="vvv" autoplay="false" preload="metadata" loop="false" height="100%" width="100%" volume="0"></video></div>

  <div id="container" display="block">
    <div id="cat-up" class="css-arrow-up" style="margin: 0 auto -0.4em auto"></div>

    <div id="prog-up" class="css-arrow-up" style="margin: 0 auto -0.4em auto"></div>
    <!--div id="prog-up" class="css-arrow-up" style="margin: 0 auto 0.0em auto"></div-->
    <div id="dots" style="display: block; margin: 0; padding:0"><span class="circle" id="c1"></span><span class="circle" id="c2"></span><span class="circle" id="c3"></span><span class="circle" id="c4"></span><span class="circle" id="c5"></span><span class="circle" id="c6"></span><span class="circle" id="c7"></span><span class="circle" id="c8"></span><span class="circle" id="c9"></span></div>

    <div style="height: 3em; width: 45em"></div>

    <div>
      <div id="cc1" class="swish" style="display: block">
        <ul id="cline1" class="thumbline"></ul>
        <ul id="sline1" class="subthumb"></ul>

      </div>
      <div id="cc2" class="swish">
        <ul id="cline2" class="thumbline"></ul>
        <ul id="sline2" class="subthumb"></ul>
      </div>
      <div id="cc3" class="swish">
        <ul id="cline3" class="thumbline"></ul>
        <ul id="sline3" class="subthumb"></ul>
      </div>

      <div id="cc4" class="swish">
        <ul id="cline4" class="thumbline"></ul>
        <ul id="sline4" class="subthumb"></ul>
      </div>
      <div id="cc5" class="swish">
        <ul id="cline5" class="thumbline"></ul>
        <ul id="sline5" class="subthumb"></ul>
      </div>
      <div id="cc6" class="swish">

        <ul id="cline6" class="thumbline"></ul>
        <ul id="sline6" class="subthumb"></ul>
      </div>
      <div id="cc7" class="swish">
        <ul id="cline7" class="thumbline"></ul>
        <ul id="sline7" class="subthumb"></ul>
      </div>
      <div id="cc8" class="swish">
        <ul id="cline8" class="thumbline"></ul>

        <ul id="sline8" class="subthumb"></ul>
      </div>
      <div id="cc9" class="swish">
        <ul id="cline9" class="thumbline"></ul>
        <ul id="sline9" class="subthumb"></ul>
      </div>
      <div id="pp" class="swish">
        <ul id="pline" class="thumbline"></ul>
        <ul id="dummy" class="subthumb"></ul>

      </div>
    </div>

    </div>
  </div>

  <div id="ipg" style="position: absolute; top: 0; display: none">
    <ul id="iline" class="ipgthumb" style="display: block">
    </ul>
  </div>

</div>
</body>
</html>
