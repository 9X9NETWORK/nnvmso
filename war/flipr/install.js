function install(lang) {

return;

var chromeRegex = / Chrome\/([0-9]+)[\.[0-9]+]* /;
var safariRegex = / Version\/([0-9]+)[\.[0-9]+]* /;
var firefoxRegex = / Firefox\/([0-9]+)[\.[0-9]+]*/;
var msieRegex = / MSIE ([0-9]+)[\.[0-9]+]*/;
var operaRegex = /Opera\/([0-9]+)[\.[0-9]+]* /;

var redirectPage=new Array();
if(lang=="zh") {
  redirectPage[0]="../install/a.html";//9x9FLIPr-tc
  redirectPage[1]="../install/b.html";//9x9FLIPr-chrome-tc
} else {
  redirectPage[0]="../install/c.html";//9x9FLIPr-en
  redirectPage[1]="../install/d.html";//9x9FLIPr-chrome-en
}

var detectUserAgent = window.navigator.userAgent;
if ($.browser.msie) {
  detectUserAgent = detectUserAgent.match(msieRegex);
  if((detectUserAgent[1]=="8")||(detectUserAgent[1]=="9")) {
    window.open(redirectPage[0],"","width=800,height=600");
  } else {
    window.open(redirectPage[1],"","width=800,height=600");
  }
} else if ($.browser.mozilla) {
  detectUserAgent = detectUserAgent.match(firefoxRegex);
  if((detectUserAgent[1]=="6")||(detectUserAgent[1]=="7")||(detectUserAgent[1]=="8")) {
    window.open(redirectPage[0],"","width=800,height=600");
  } else {
    window.open(redirectPage[1],"","width=800,height=600");
  }
} else if ($.browser.opera) {
  window.open(redirectPage[1],"","width=800,height=600");
} else if (navigator.userAgent.match(chromeRegex)) {
  window.open(redirectPage[0],"","width=800,height=600");
} else if (navigator.userAgent.match(safariRegex)) {
  detectUserAgent = detectUserAgent.match(safariRegex);
  if(detectUserAgent[1]=="5") {
    window.open(redirectPage[0],"","width=800,height=600");
  } else {
    window.open(redirectPage[1],"","width=800,height=600");
  }
} else {
  window.open(redirectPage[1],"","width=800,height=600");
}
}
function sendEmail() {
  var from = null;
  var to = "bartonboy@pixnet.net";//the default should be flipr@9x9cloud.tv
  var subject = $("#content_t").val();
  var msgBody = $("#content_w").val();

  if(from==null)
    from = "Anonymous@gmail.com";
  if((subject==null)&&(msgBody==null)) {
    alert("you should type something before send");
    return;
  }
  if (subject == '' && msgBody == '')
    return;
  if(subject=='')
    subject = "none";
  if(msgBody=='')
    msgBody = "none";
    	
  $.post('/CMSAPI/sendEmail', { 'from': from, 'to': to, 'subject': subject, 'msgBody': msgBody }, function(response) {
    if (response != "OK") {
      alert("send fail");
      return;
    }
    alert("send success");
  }, 'text');

}
function erase() {
  $("#content_t").val(null);
  $("#content_w").val(null);
}
