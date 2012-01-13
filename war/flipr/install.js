function install(lang) {
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
    //window.location = redirectPage[0];
  } else {
    window.open(redirectPage[1],"","width=800,height=600");
    //window.location = redirectPage[1];
  }
} else if ($.browser.mozilla) {
  detectUserAgent = detectUserAgent.match(firefoxRegex);
  if((detectUserAgent[1]=="6")||(detectUserAgent[1]=="7")||(detectUserAgent[1]=="8")) {
    window.open(redirectPage[0],"","width=800,height=600");
    //window.location = redirectPage[0];
  } else {
    window.open(redirectPage[1],"","width=800,height=600");
    //window.location = redirectPage[1];
  }
} else if ($.browser.opera) {
  window.open(redirectPage[1],"","width=800,height=600");
  //window.location = redirectPage[1];
} else if (navigator.userAgent.match(chromeRegex)) {
  window.open(redirectPage[0],"","width=800,height=600");
  //window.location = redirectPage[0];
} else if (navigator.userAgent.match(safariRegex)) {
  detectUserAgent = detectUserAgent.match(safariRegex);
  if(detectUserAgent[1]=="5") {
    window.open(redirectPage[0],"","width=800,height=600");
    //window.location = redirectPage[0];
  } else {
    window.open(redirectPage[1],"","width=800,height=600");
    //window.location = redirectPage[1];
  }
} else {
  window.open(redirectPage[1],"","width=800,height=600");
  //window.location = redirectPage[1];
}
}
