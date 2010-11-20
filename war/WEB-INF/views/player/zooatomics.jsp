<!DOCTYPE html>
<head>
<meta charset="utf-8" />

<link rel="stylesheet" href="http://zoo.atomics.org/video/9x9playerV9/stylesheets/main.css" />
<link rel="stylesheet" href="http://zoo.atomics.org/video/stylesheets/ipg.css" />

<style>

#browse {
  width: 38em;
  height: 100%;
  /* background-color:rgba(0,0,0,0.7); */
  background: black;
  position: absolute;
  left: 0;
  top: 0;
  overflow: hidden;
  font-size: 0.5em;
}

#browse div {
  height: 6em;
  padding: 0.625em;
  margin: 0.625em;
  border-width: 3px;
  border-style: solid;
  border-color: #555;
}

#browse img {
  height: 4.5em;
}

</style>

<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
<script type="text/javascript" src="http://zoo.atomics.org/video/cssanim.js"></script>


<script>

var current_category = 0;
var current_program = '';

var thumbing = 'channel';

var channelgrid = {};
var channels_by_id = {}
var channel_line = {};
var n_channel_line = 0;
var channel_cursor = 1;

var programgrid = {};
var program_line = {};
var n_program_line = 0;

var ipg_cursor;

/* browse */
var browsables = {};
var n_browse = 0;
var saved_thumbing = '';
var browse_cursor = 1;

var bubble_timex;

var user = "aghubmUydm1zb3IMCxIGTm5Vc2VyGBoM";

$(document).ready (function()
 {
 log ('begin execution');
 setup_ajax_error_handling();
 //elastic();
 login();
 $(window).resize (function() { elastic(); });
 });

function elastic()
  {
  log ('elastic');

  var newWidth = $(window).width() / 16 ;
  var newHeight = $(window).height() / 16;

  var oriWidth = 64;
  var times = newWidth / oriWidth * 100 + "%";
  $("body").css("font-size",times);

  var v = document.getElementById ("v");
  var vh = $(window).height();
  v.style.height = (vh) + "px";

  var i = document.getElementById ("ipg-layer");
  i.style.height = vh + "px";
  }

function log (text)
  {
  try
    {
    if (window.console && console.log)
      console.log (text);

    var loglayer = document.getElementById ("log-layer");
    loglayer.innerHTML += text + '<br>';
    }
  catch (error)
    {
    }
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
  // 0=channel-id 1=program-id 2=program-name 3=program-type 4=program-thumb-url 5=program-url1 6=program-url2

    var lines = data.split ('\n');
    log ('number of programs obtained: ' + lines.length);
    for (var i = 0; i < lines.length; i++)
      {
      if (lines [i] != '' && lines [i].substring (0,1) != '#')
        {
        var fields = lines[i].split ('\t');
        log ("program line " + i + ": " + fields[0] + ' = ' + lines [i]);

        if (fields [5] == 'null')
          fields [5] = '';

        if (fields.length <= 6 || (fields.length > 6 && fields [6] == 'null'))
          fields [6] = '';

        if (fields [3] == 'slideshow')
          fields [5] = 'slideshow:' + fields [5];

        //if (navigator.userAgent.match (/(GoogleTV|Droid Build)/i))
        //fields[5] = fields[5].replace (/webm$/, 'mp4');

        programgrid [fields [1]] = { 'channel': fields[0], 'url1': fields[5], 'url2': fields[6], 'name': fields[2], 'type': fields[3], 'thumb': fields[4] };
        }
      else
        log ("ignoring program line " + i + ": " + lines [i]);
      }
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
      if (lines [i] != '' && lines [i].substring (0,1) != '#')
        {
        var fields = lines[i].split ('\t');
        log ("channel line " + i + ": " + conv [fields[0]] + ' = ' + lines [i]);
        channelgrid [conv [fields[0]]] = { 'id': fields[1], 'name': fields[2], 'thumb': fields[3] };
        channels_by_id [fields[1]] = conv [fields[0]];
        }
      else
        log ("ignoring channels line " + i + ": " + lines [i]);
      }
    activate();
    });
  }

function activate()
  {
  thumbing = 'channel';
  reset_category_dots();
  enter_category (1, 'b');
  elastic();
  play_first_program_in (first_channel());
  document.onkeydown=kp;
  }

function old_reset_category_dots()
  {
  for (var i = 1; i <= 9; i++)
    {
    if (channels_in_category (i) > 0)
      $("#c" + i).css ("background", "#999");
    else
      $("#c" + i).css ("background", "#484848");
    }
  }

function best_url (program)
  {
  var desired;

  if (navigator.userAgent.match (/(GoogleTV|Droid Build)/i))
    desired = 'mp4';

  else if (navigator.userAgent.match (/(Opera|Chrome|Firefox)/))
    desired = 'webm';

  else if (navigator.userAgent.match (/(Safari)/))
    desired = 'mp4';

  ext = new RegExp ('\.' + desired + '$');

  if (programgrid [current_program]['url1'].match (desired))
    {
    log ('url1 preference');
    return programgrid [current_program]['url1'];
    }
  else if (programgrid [current_program]['url2'].match (desired))
    {
    log ('url2 preference');
    return programgrid [current_program]['url2'];
    }
  else
    {
    log ('fallback to url1');
    return programgrid [current_program]['url1'];
    }
  }

function play_first_program_in (chan)
  {
  program_cursor = 1;

  current_program = first_program_in (chan);
  log ('playing first program in ' + chan + ': ' + current_program);

  play();
  }

function play()
  {
  var url = best_url (current_program)

  var v = document.getElementById ("vvv");
  v.src = url;

  log ('playing: ' + url);

  update_bubble();

  if (bubble_timex)
    clearTimeout (bubble_timex);

  $("#bubble").show();
  bubble_timex = setTimeout ('$("#bubble").hide()', 3000);
  }

function play_program()
  {
  current_program = program_line [program_cursor];
  play();
  }

function update_bubble()
  {
  var channel = channel_line [channel_cursor];
  var program = programgrid [current_program];
  $("#ch-title").html (channelgrid [channel]['name']);
  $("#ep-title").html (program ['name']);
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

  log ('enter channel');

  var channel = channel_line [channel_cursor];
  var real_channel = channelgrid [channel]['id'];

  var html = "";
  n_program_line = 0;

  // NEW FORMAT
  // <li class="on"><img src="images-x1/bg_ep.png" class="ebg"><img src="thumb/14.jpeg" class="thumb"></li>
  // <li><img src="images-x1/bg_ep.png" class="ebg"><img src="thumb/06.jpg" class="thumb"></li>
  // <li class="empty"><img src="images-x1/bg_ep.png" class="ebg"></li>

  for (p in programgrid)
    {
    if (programgrid [p]['channel'] == real_channel)
      {
      program_line [++n_program_line] = p;
      // html += '<li id="p-li-' + n_program_line + '"><img id="p-th-"' + n_program_line + '" src="' + programgrid [p]['thumb'] + '"></li>';
      var onoff = n_program_line == program_cursor ? 'class="on" ' : '';
      onoff = '';
      html += '<li ' + onoff + 'id="p-li-' + n_program_line + '"><img src="http://zoo.atomics.org/video/images-x1/bg_ep.png" class="ebg"><img src="' + programgrid [p]['thumb'] + '" class="thumb"></li>';
      }
    }

    $("#ep-layer").css ("opacity", "0");
    $("#ep-layer").css ("display", "block");

    // $("#ep-swish").css ("opacity", "0");
    $("#ep-swish").css ("top", "5.125em");
    $("#ep-swish").css ("display", "block");

    var phase_in_pp =
      {
      //opacity: "1",
      top: "0em"
      };

    var phase_out_cc =
      {
      //opacity: "0",
      top: "-6.1875em" 
      };

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

  $("#ep-list").html (html);

  setTimeout ("enter_channel_failsafe()", 500);

  redraw_program_line();
  }

function enter_channel_failsafe()
  {
  $("#ep-layer").css ("display", "block");
  $("#ep-layer").css ("opacity", "1");
  $("#ep-swish").css ("top", "0");
  $("#ep-swish").css ("display", "block");
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

    $("#ep-swish").css ("top", "0em");

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
      $("#ep-swish").css ("top", "0em");
      });
    }

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

  reset_category_dots();

  for (var y = 1; y <= 9; y++)
    $("#ch-swish-" + y).css ("display", y == cat ? "block" : "none");

  $("#notblue").css ("display", "block");
  $("#blue").css ("display", "none");

  /* position at beginning or ending */
  if (positioning == 'b')
    channel_cursor = 1;
  else if (positioning == 'e')
    channel_cursor = n_channel_line;

  redraw_channel_line();
  }

function enter_category_failsafe()
  {
  $("#ch-layer").css ("opacity", "1");
  $("#ch-layer").css ("display", "block");
  $("#ch-swish-" + current_category).css ("display", "block");
  $("#ch-swish-" + current_category).css ("top", "1.4375em");
  }

function ch_html (cat)
  {
  var html = "";

  // NEW FORMAT
  // <li class="on"><img src="thumb/01.jpg"><img src="http://zoo.atomics.org/video/images-x1/arrow_down.png" class="arrow-down"><span class="number">9</span></li>

  n_channel_line = 0;

  for (var x = 1; x <= 9; x++)
    {
    var chan = "" + cat + "" + x;
  
    if (channelgrid [chan])
      {
      channel_line [++n_channel_line] = chan;
      // html += '<li id="c-' + current_category + '-li-' + n_channel_line + '"><img id="c-th-' + n_channel_line + '" src="' + channelgrid [chan]['thumb'] + '"></li>';
      html += '<li id="c-' + cat + '-li-' + n_channel_line;
      html += (channel_cursor == n_channel_line) ? '" class="on">' : '">';
      html += '<img src="' + channelgrid [chan]['thumb'] + '"><img src="http://zoo.atomics.org/video/images-x1/arrow_down.png" class="arrow-down"><span class="number">' + programs_in_channel (chan) + '</span></li>';
      log ('channel ' + channelgrid [chan]['id'] + ': ' + channelgrid [chan]['name']);
      }
    else
      html += '<li><img src="http://zoo.atomics.org/video/images-x1/arrow_down.png" class="arrow-down"><span class="number">9</span></li>';
    }

  return html;
  }

function reset_category_dots (cat)
  {
  var dotsize;
  var html = "";

  for (var y = 1; y <= 9; y++)
    {
    if (channels_in_category (y) == 0)
      dotsize = ' class="empty"';
    else if (y == current_category)
      dotsize = ' class="on"';
    else if (y == current_category - 1 || y == current_category + 1)
      dotsize = ' class="next"';
    else
      dotsize = '';

    html += '<li' + dotsize + '><span class="dot"></span></li>';
    }

  $("#cg-tabs").html (html);
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

function programs_in_channel (channel)
  {
  var num_programs = 0;
  var real_channel = channelgrid [channel]['id'];

  for (p in programgrid)
    {
    if (programgrid [p]['channel'] == real_channel)
      num_programs++;
    }

  return num_programs;
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
    log_and_alert ('channel ' + channel + ' not in channelgrid');
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

    case 'throw':   layer = $("#throw-layer");
                    break;

    case 'browse':  layer = $("#podcast-layer");
                    break;
    }

  layer.css ("display", layer.css ("display") == "block" ? "none" : "block");

  if (thumbing == 'user' || thumbing == 'throw' || thumbing == 'browse')
    thumbing = saved_thumbing;

  else if (thumbing == 'ipg')
    {
    thumbing = 'channel';
    }

  $("#mask").hide();
  $("#log-layer").hide();
  }

function keypress (keycode)
  {
  /* entering a form */
  if ((thumbing == 'user' || thumbing == 'throw') && keycode != 27)
    return;

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
      else if (thumbing == 'browse')
        browse_accept();
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
      else if (thumbing == 'browse')
        browse_up();
      else if (thumbing == 'ipg')
        ipg_up();
      break;

    case 40:
      /* down arrow */
      if (thumbing == 'channel')
        switch_to_program_thumbs();
      else if (thumbing == 'browse')
        browse_down();
      else if (thumbing == 'ipg')
        ipg_down();
      break;

    case 45:
      /* insert */
    case 84:
      /* T */
      if (thumbing == 'ipg')
        throw_screen();
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
      browse();
      break;

    case 73:
      /* I */
      break;

    case 85:
      /* U */
      if (thumbing == 'channel')
        login_screen();
      break;
    }
  }

function switch_to_ipg()
  {
  $("#ipg-layer").css ("display", "none");
  redraw_ipg();

  $("#ipg-layer").css ("opacity", "0");
  // $("#mask").show();
  $("#ipg-layer").css ("display", "block");

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
    });

  thumbing = 'ipg';
  }

function outt()
  {
  $("#ipg-layer").animateWithCss ({ opacity: "1" }, 500, "ease-in", function() {});
  }

function redraw_ipg()
  {
  var html = "";
  
  for (var y = 1; y <= 9; y++)
    {
    html += '<ul class="ipg-list">';

    for (var x = 1; x <= 9; x++)
      {
      if ("" + y + "" + x in channelgrid)
        html += '<li id="ipg-' + y + '' + x + '"><img src="' + channelgrid ["" + y + "" + x]['thumb'] + '"></li>';
      else
        html += '<li id="ipg-' + y + '' + x + '"><img src="http://zoo.atomics.org/video/images-x1/add_channel.png" class="add-ch"></li>';
      }
    html += '</ul>';
    }

  $("#ipg-grid").html (html);

  ipg_cursor = parseInt (channel_line [channel_cursor]);

  $("#ipg-" + ipg_cursor).addClass ("on");
  ipg_metainfo();
  }

function ipg_metainfo()
  {
  if (ipg_cursor in channelgrid)
    {
    $("#ch-thumb-img").attr ("src", channelgrid [ipg_cursor]['thumb']);
    $("#ch-name").html ('<p>' + channelgrid [ipg_cursor]['name'] + '</p>');
    $("#ep-name").html ('<p>An episode name would go here?</p>');
    $("#description").html ('<p>A description of something is supposed to go here, but I have nothing to put in this spot.</p>');
    $("#ch-episodes").html (programs_in_channel (ipg_cursor));
    $("#ep-number").show();
    $("#update").show();
    }
  else
    {
    $("#ch-thumb-img").attr ("src", "http://zoo.atomics.org/video/images-x1/add_channel.png");
    $("#ch-name").html ('<p></p>');
    $("#ch-name").html ('<p></p>');
    $("#ep-name").html ('<p></p>');
    $("#description").html ('<p></p>');
    $("#ep-number").hide();
    $("#update").hide();
    }
  }

function ipg_right()
  {
  log ("IPG RIGHT: old ipg cursor: " + ipg_cursor);
  $("#ipg-" + ipg_cursor).removeClass ("on");

  // ipg_cursor = next_channel_square (ipg_cursor)

  if (ipg_cursor == 99)
    ipg_cursor = 11;
  else if (ipg_cursor % 10 == 9)
    ipg_cursor += 2; /* 39 -> 41 */
  else
    ipg_cursor++;

  log ("new ipg cursor: " + ipg_cursor);

  $("#ipg-" + ipg_cursor).addClass ("on");
  ipg_metainfo();
  }

function ipg_left()
  {
  log ("IPG LEFT: old ipg cursor: " + ipg_cursor);
  $("#ipg-" + ipg_cursor).removeClass ("on");

  // ipg_cursor = previous_channel_square (ipg_cursor)

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
  $("#ipg-" + ipg_cursor).removeClass ("on");

  if (ipg_cursor > 19)
    ipg_cursor -= 10;
  else
    ipg_cursor += 80;
  log ("new ipg cursor: " + ipg_cursor);

  $("#ipg-" + ipg_cursor).addClass ("on");
  ipg_metainfo();
  }

function ipg_down()
  {
  log ("IPG DOWN: old ipg cursor: " + ipg_cursor);
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
  //alert ("play: " + (""+ipg_cursor).substring (0, 1) + "-" + (""+ipg_cursor).substring (1,2));

  if (! (ipg_cursor in channelgrid))
    {
    browse();
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
  log ('redraw program line');
  for (var i = 1; i <= n_program_line; i++)
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

function throw_screen()
  {
  saved_thumbing = thumbing;
  thumbing = 'throw';
  $("#mask").show();
  $("#throw-layer").show();
  }

function login_screen()
  {
  saved_thumbing = thumbing;
  thumbing = 'user';
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
      escape();
      log_and_alert ('logged in as user: ' + user);
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

  var serialized = things.join ('&');
  log ('signup: ' + serialized);

  $.post ("/playerAPI/signup", serialized, function (data)
    {
    var fields = data.split ('\t');
    user = fields [1];
    if (fields [0] == "0")
      {
      escape();
      log_and_alert ('signed up as user: ' + user);
      fetch_programs();
      }
    else
      log_and_alert ("SIGNUP FAIL: " + fields [1]);
    });
  }

function submit_throw()
  {
  /* always called from IPG, use ipg_cursor */

  var serialized = $("#throw").serialize() + '&' + 'user=' + user + '&' + 'grid=' + server_grid (ipg_cursor);
  log ('throw: ' + serialized);

  $.post ("/playerAPI/podcastSubmit", serialized, function (data)
    {
    var fields = data.split ('\t');
    user = fields [1];
    if (fields [0] == "0")
      {
      escape();
      log_and_alert ('podcast thrown!')
      // fields: 0=status 1=channel-id 2=channel-name 3=channel-thumb
      channelgrid [ipg_cursor] = { 'id': fields[1], 'name': fields[2], 'thumb': fields[3] };
      channels_by_id [fields[1]] = ipg_cursor;
      redraw_ipg();
      }
    else
      log_and_alert ("PODCAST THROW FAIL: " + fields [1]);
    })
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
  saved_thumbing = thumbing;

  log ('obtaining browse information');

  var query = "/playerAPI/channelBrowse";
  var html = "";

  // 1  570     Channel One     /thumb/01.jpg
  // 0=grid-location(ignore) 1=channel-id 2=channel-name 3=channel-thumb

  var d = $.get (query, function (data)
    {
    // <li class="on"><img src="thumb/abc.jpg"><span>ABC Entertainment</span></li>
    // <li><img src="thumb/abc.jpg"><span>ABC Entertainment</span></li>

    var lines = data.split ('\n');
    log ('number of browse channels obtained: ' + lines.length);
    for (var i = 0; i < lines.length; i++)
      {
      if (lines [i] != '')
        {
        var fields = lines[i].split ('\t');
        if (fields [1] in channels_by_id)
          log ('channel ' + fields [1] + ' already in lineup');
        else
          {
          log ('browse ' + fields [1] + ': ' + lines [i]);
          n_browse++;
          // html += '<div id="browse' + n_browse + '" style="color: white"><img src="' + fields[3] + '"><br>' + fields[2] + '</div>';
          html += '<li id="pod-' + n_browse + '"><img src="' + fields[3] + '"><span>' + fields [2] + '</span></li>';
          browsables [n_browse] = { 'id': fields [1], 'thumb': fields [3], 'name': fields [2] };
          }
        }
      }

    $("#mask").show();
    $("#podcast-list").html (html);
    $("#podcast-layer").show();

    thumbing = 'browse';
    browse_cursor = 1;
    $("#pod-" + browse_cursor).addClass ("on");
    });
  }

function browse_up()
  {
  if (browse_cursor > 1)
    {
    $("#pod-" + browse_cursor).removeClass ("on");
    browse_cursor--;
    $("#pod-" + browse_cursor).addClass ("on");
    log ('browser now at: ' + browsables [browse_cursor]['id']);
    }
  }

function browse_down()
  {
  if (browse_cursor < n_browse)
    {
    $("#pod-" + browse_cursor).removeClass ("on");
    browse_cursor++;
    $("#pod-" + browse_cursor).addClass ("on");
    log ('browser now at: ' + browsables [browse_cursor]['id']);
    }
  }

function browse_accept()
  {
  var new_channel_id = browsables [browse_cursor]['id'];

  log ('browser accepts: ' + new_channel_id +  ' (' + server_grid (ipg_cursor) + ')');

  var cmd = "/playerAPI/subscribe?user=" + user + '&' + "channel=" + new_channel_id + '&' + "grid=" + server_grid (ipg_cursor);
  var d = $.get (cmd, function (data)
    {
    continue_acceptance();
    });
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

  /* obtain programs */

  log ('obtaining programs for: ' + new_channel_id);

  var cmd = "/playerAPI/programInfo?channel=" + new_channel_id + '&' + "user=" + user;

  var d = $.get (cmd, function (data)
    {
    parse_program_data (data);
    escape();
    ipg_play()
    });
  }






</script>

<title>Elastic 9x9 Player</title>

</head>

<body id="body" style="background: black">

<div id="blue" style="background: black; width: 100%; height: 100%; display: block; position: absolute; color: white">
One moment...

</div>

<div id="notblue" style="width: 100%; display: none; position: absolute; top: 0; margin: 0; overflow: hidden">

  <div id="v" style="display: block; padding: 0">

    <video id="vvv" autoplay="false" preload="metadata" loop="false" height="100%" width="100%" volume="0"></video></div>

<ul id="bubble">
  <li id="ch-title">CNN News</li>
  <li id="dash">&#8212;</li>
  <li id="ep-title" class="on">Jay Leno's eclectic car collection</li>
  <li id="time">11/09/10</li>
  <li id="divider">|</li>

  <li id="duration">10:24</li>
</ul>

<div id="ch-layer" style="display: block;">
  <img src="http://zoo.atomics.org/video/images-x1/arrow_up.png" id="arrow-up">
  <div id="ch-container">
    <img src="http://zoo.atomics.org/video/images-x1/arrow-left.png" id="arrow-left">
    <div id="ch-constrain">
    <div class="ch-strip">
      <ul id="cg-tabs"><li><span class="dot"></span></li><li class="next"><span class="dot"></span></li><li class="on"><span class="dot"></span></li><li class="next"><span class="dot"></span></li><li><span class="dot"></span></li><li><span class="dot"></span></li><li><span class="dot"></span></li><li class="empty"><span class="dot"></span></li><li class="empty"><span class="dot"></span></li></ul>

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
    <img src="http://zoo.atomics.org/video/images-x1/arrow-right.png" id="arrow-right">
  </div>

</div>

<div id="ep-layer" style="display: none">
  <img src="http://zoo.atomics.org/video/images-x1/arrow_up.png" id="arrow-up">
  <div class="ep-swish" id="ep-swish">

    <ul class="ep-list" id="ep-list"></ul>
  </div>
</div>

<div id="throw-layer" style="display: none; position: absolute; top: 0; background: orange; padding: 20px; z-index: 999; width: 20em">
<form id="throw">
<span style="color: red">Throw Podcast</span>

<hr width=75% style="margin: auto">
<br>
<p>

Postcast URL to throw<br>

<input type=text size=32 name="podcastRSS" value="http://" onkeypress="return event.keyCode != 13"></input>
<p><br><p>
<a href="javascript:submit_throw()">THROW PODCAST</a>
<br>

<p>
</form>
</div>

<div id="user-layer" style="display: none; position: absolute; top: 0; background: black; padding: 20px; z-index: 999">

<div style="display:inline-block; background: orange; width: 20em; padding: 20px">

<form id="login">

<span style="color: red">Returning Users</span>

<hr width=75% style="margin: auto">
<br>
<p>
E-Mail<br>
<input type=text size=32 name="email" value="you@example.com" onkeypress="return event.keyCode != 13"></input>

<p>
Password<br>
<input type=text size=32 name="password" value="swordfish" onkeypress="return event.keyCode != 13"></input>
<p><br><p>
<a href="javascript:submit_login()">LOGIN</a>
<br>

<p>

</form>
</div>

<div style="display:inline-block; background: yellow; width: 20em; border-left: 4px solid black; padding: 20px;">
<form id="signup">
<span style="color: red">New Users</span>

<hr width=75% style="margin: auto">
<br>
<p>
Your Name<br>
<input type=text size=32 name="name" value="Rodney Q. Public" onkeypress="return event.keyCode != 13"></input>
<p>

Your E-Mail<br>

<input type=text size=32 name="email" value="you@example.com" onkeypress="return event.keyCode != 13"></input>
<p>
Password<br>

<input type=text size=32 name="password" value="swordfish" onkeypress="return event.keyCode != 13"></input>
<p>

Password Verify<br>
<input type=text size=32 name="password-again" value="swordfish" onkeypress="return event.keyCode != 13"></input>
<p><br><p>
<a href="javascript:submit_signup()">SIGNUP</a>

<br>

<p>
</form>
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
      <li><a href="javascript:;" class="btn"><span>Button!</span></a></li>

      <li><a href="javascript:;" class="btn"><span>Button!</span></a></li>
    </ul>   
  </div>
  <div id="ipg-grid"></div>
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
      <li class="textfieldbox"><input name="" type="text" class="textfield"></li>
      <li><a href="javascript:;" class="btn"><span>Contribute</span></a></li>
    </ul>
    <ul id="podcast-list"></ul>
  </div>
</div>

<div id="browse" style="display: none; z-index: 999"></div>
<div id="log-layer" style="position: absolute; left: 0; top: 0; height: 100%; width: 100%; background: white; color: black; text-align: left; padding: 20px; overflow: scroll; z-index: 9999; display: none"></div>

<div id="mask"></div>

</div>
</body>
</html>
