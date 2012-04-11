var lang = null;
var fileUrl = null;
function install(lang2) {
  lang = lang2;
  getFileUrl();
}
function setFileLink() {

var chromeRegex = / Chrome\/([0-9]+)[\.[0-9]+]* /;
var safariRegex = / Version\/([0-9]+)[\.[0-9]+]* /;
var firefoxRegex = / Firefox\/([0-9]+)[\.[0-9]+]*/;
var msieRegex = / MSIE ([0-9]+)[\.[0-9]+]*/;
var operaRegex = /Opera\/([0-9]+)[\.[0-9]+]* /;

var redirectPage=new Array();
if(lang=="zh") {
  redirectPage[0]=fileUrl;//9x9FLIPr-tc
  redirectPage[1]=fileUrl;//9x9FLIPr-chrome-tc
} else {
  redirectPage[0]=fileUrl;//9x9FLIPr-en
  redirectPage[1]=fileUrl;//9x9FLIPr-chrome-en
}

var detectUserAgent = window.navigator.userAgent;
if ($.browser.msie) {
  detectUserAgent = detectUserAgent.match(msieRegex);
  if((detectUserAgent[1]=="8")||(detectUserAgent[1]=="9")) {
    $("#installer").attr("onclick","window.open('"+redirectPage[0]+"','','width=800,height=600');");
  } else {
    $("#installer").attr("onclick","window.open('"+redirectPage[1]+"','','width=800,height=600');");
  }
} else if ($.browser.mozilla) {
  detectUserAgent = detectUserAgent.match(firefoxRegex);
  if((detectUserAgent[1]=="6")||(detectUserAgent[1]=="7")||(detectUserAgent[1]=="8")) {
    $("#installer").attr("onclick","window.open('"+redirectPage[0]+"','','width=800,height=600');");
  } else {
    $("#installer").attr("onclick","window.open('"+redirectPage[1]+"','','width=800,height=600');");
  }
} else if ($.browser.opera) {
  $("#installer").attr("onclick","window.open('"+redirectPage[1]+"','','width=800,height=600');");
} else if (navigator.userAgent.match(chromeRegex)) {
  $("#installer").attr("onclick","window.open('"+redirectPage[0]+"','','width=800,height=600');");
} else if (navigator.userAgent.match(safariRegex)) {
  detectUserAgent = detectUserAgent.match(safariRegex);
  if(detectUserAgent[1]=="5") {
    $("#installer").attr("onclick","window.open('"+redirectPage[0]+"','','width=800,height=600');");
  } else {
    $("#installer").attr("onclick","window.open('"+redirectPage[1]+"','','width=800,height=600');");
  }
} else {
  $("#installer").attr("onclick","window.open('"+redirectPage[1]+"','','width=800,height=600');");
}
}
function sendEmail(lang) {
  var from = $("#content_m").val();
  var to = "bartonboy@pixnet.net";//the default should be flipr@9x9cloud.tv
  var subject = $("#content_t").val();
  var msgBody = $("#content_w").val();

  if(from=='')
    from = "Anonymous@gmail.com";
  if((subject == '') && (msgBody == '')) {
    if(lang=='zh') {
      alert("尚未填寫內容");
    }else{
      alert("you should type something before send");
    }
    return;
  }
  if(subject=='')
    subject = "none";
  if(msgBody=='')
    msgBody = "none";
    	
  $.post('/CMSAPI/sendEmail', { 'from': from, 'to': to, 'subject': subject, 'msgBody': msgBody }, function(response) {
    if(response != "OK") {
      if(lang=='zh') {
        alert("信件已送出"); 
      }else{
        alert("the mail has been sent"); 
      }
      
      return;
    }
    
    if(lang=='zh') {
      alert("信件已送出");	
    }else{
      alert("the mail has been sent")
    }
    
  }, 'text');
}
function erase() {
  $("#content_t").val(null);
  $("#content_m").val(null);
  $("#content_w").val(null);
}
function getFileUrl() {
  var targetUrl = "http://202.5.224.30/update.php?callback=?";
  var parameters = "";

  $.ajax({    
    type: "GET",
    contentType: "application/x-www-form-urlencoded",
    dataType: "jsonp",
    jsonp: "jsonpcallback",
    async: true,
    url: targetUrl,
    data: parameters,
    success: function(param1, param2){
      fileUrl = param1;
      setFileLink();
    },
    error: function(param1, param2){
      alert("error by ajax call: http://202.5.224.30/update.php");
    }
  });
  
}