/**
 * 
 */

var log = function(text) {
  if (window.console && console.log)
    console.log(text);
};

var cms = {
  debug: false,
  isFacebookLoaded: false,
  loadFacebook: function(callback) {
    if (this.isFacebookLoaded) {
      return (typeof callback == 'function') ? callback() : null;
    }
    cms.isFacebookLoaded = true;
    $('<div id="fb-root"></div>').appendTo('body');
    window.fbAsyncInit = function() {
      FB.init({
        appId:     '110847978946712',
        status:     true, // check login status
        cookie:     true, // enable cookies to allow the server to access the session
        xfbml:      true, // parse XFBML
        oauth:      true, // enable OAth 2.0
        channelURL: 'http://' + location.host + '/facebook.html'
      });
      return (typeof callback == 'function') ? callback() : null;
    };
    cms.loadScript('http://connect.beta.facebook.net/en_US/all.js');
  },
  isNN: function() {
    return ($('#msoType').val() == '1');
  },
  isEnterprise: function() {
    return ($('#msoType').val() == '5');
  },
  isGeneric: function() {
    return ($('#msoType').val() == '4');
  },
  initPlusone: function(callback) {
    if (callback)
      cms.loadScript('https://apis.google.com/js/plusone.js', callback);
    else
      cms.loadScript('https://apis.google.com/js/plusone.js');
  },
  initAddthis: function() {
    window.addthis = null;
    cms.loadScript('http://s7.addthis.com/js/250/addthis_widget.js', function() {
      addthis.init();
    });
  },
  formatDate: function(timestamp) {
    var updateDate = new Date(timestamp);
    var year = updateDate.getFullYear();
    var month = updateDate.getMonth() + 1;
    var date = updateDate.getDate();
    var hour = updateDate.getHours();
    var minute = updateDate.getMinutes().toString();
    var second = updateDate.getSeconds().toString();
    if (minute.length < 2)
      minute = "0" + minute;
    if (second.length < 2)
      second = "0" + second;
    return year + '/' + month + '/' + date + ' ' + hour + ':' + minute + ':' + second;
  },
  bubblePopupProperties: {
    'position':  'top',
    'align':     'center',
    'innerHtmlStyle':
    {
      'color':      '#292929',
      'text-align': 'left',
      'font-size':  '0.8em'
    },
    'themeName': 'all-black',
    'themePath': '/images/cms'
  },
  escapeHtml: function(text) {
    return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  },
  getChannelUrl: function(channelId, programId) {
    var url = 'http://' + location.host + '/view?channel=' + channelId;
    if (programId) {
      url += '&episode=' + programId;
    }
    return url;
  },
  getContentTypeByFileExtention: function(ext) {
    
    switch(ext) {
      
      case '.mpg':
      case '.mpeg':
      return 'video/mpeg';
      break;
      
      case '.m4v':
      case '.mp4':
      return 'video/mp4';
      break;
      
      case '.ogg':
      return 'video/ogg';
      break;
      
      case '.webm':
      return 'video/webm';
      break;
      
      case '.mov':
      return 'video/quicktime';
      break;
      
      case '.wmv':
      return 'video/x-ms-wmv';
      break;
      
      case '.flv':
      return 'video/x-flv';
      break;
      
      case '.avi':
      return 'video/avi';
      break;
      
      case '.rmvb':
      return 'application/vnd.rn-realmedia-vbr';
      break;
      
      case 'rm':
      return 'video/rm';
      break;
      
      default:
      return 'video/unknown';
    }
  },
  get: function(url, parameters, callback, format) {
    if (!format) {
      $.get(url, parameters, callback);
    } else {
      $.get(url, parameters, callback, format);
    }
  },
  post: function(url, parameters, callback, format) {
    if (!callback) {
      $.post(url, parameters);
    } else {
      if (!format)
        $.post(url, parameters, callback);
      else
        $.post(url, parameters, callback, format);
    }
  },
  loadJSON: function(url, callback) {
    var cache = $.ajaxSettings.cache;
    $.ajaxSettings.cache = true;
    $.getJSON(url, callback);
    $.ajaxSettings.cache = cache;
  },
  loadScript: function(url, callback) {
    if ($.browser.msie && url.charAt(0) == '/') {
      log('loadScript: ' + url);
      return (function(url, callback) {
        var head   = document.getElementsByTagName("head")[0];
        var script = document.createElement("script");
        var done   = false; // Handle Script loading
        script.src = url;
        script.onload = script.onreadystatechange = function() { // Attach handlers for all browsers
          if ( !done && (!this.readyState || this.readyState === "loaded" || this.readyState === "complete") ) {
            done = true;
            if (callback) { callback(); }
            script.onload = script.onreadystatechange = null; // Handle memory leak in IE
          }
        };
        head.appendChild(script);
        return undefined; // We handle everything using the script element injection
      })(url, callback);
    }
    var cache = $.ajaxSettings.cache;
    $.ajaxSettings.cache = true;
    if (typeof (callback) == 'function')
      $.getScript(url, callback);
    else
      $.getScript(url);
    $.ajaxSettings.cache = cache;
  },
  initSetupButton: function() {
    $('#setup_page').jqm({
      zIndex:  1000,
      toTop:   true,
      modal:   true,
      ajax:    'setup',
      trigger: '#setup',
      onLoad: function() {
        cms.loadScript('/javascripts/cms/setup.js', function() {
          pageSetup.init();
          if (cms.isGeneric()) {
            pageSetup.initGenericOne();
          } else if (cms.isEnterprise()) {
            pageSetup.initEnterpriseOne();
          }
        });
      },
      onHide: function() {
        location.reload();
      }
    });
  },
  initEnterpriseOne: function() {
    $('.menuA').removeAttr('href');
    $('<style> .menuA:hover { background-position: 0px -59px; cursor: default; } </style>').appendTo('head');
    $('.menuD').removeAttr('href');
    $('<style> .menuD:hover { background-position: -268px -59px; cursor: default; } </style>').appendTo('head');
  },
  initNNOne: function() {
    $('.menuC').attr('href', 'directoryManagement');
    $('<style> .menuC:hover { background-position: -179px 0px; cursor: default; } </style>').appendTo('head');
  },
  initGenericOne: function() {
    $('#blog_link').show();
    $('#mso_logo').wrapAll('<a href="/9x9"></a>');
    $('.menuB').removeAttr('href');
    $('<style> .menuB:hover { background-position: -90px -59px; cursor: default; } </style>').appendTo('head');
  },
  init: function() {
    if (!cms.isGeneric()) {
      $('#mso_logo').wrapAll('<a href="/' + $('#msoName').val() + '"></a>');
    }
    $('.header .logout').css('background', 'url(' + $('#image_header_logout').text() + ') no-repeat');
    $('.header .setup').css('background', 'url(' + $('#image_header_setup').text() + ') no-repeat');
    $('.header .sg').css('background', 'url(' + $('#image_header_sg').text() + ') no-repeat');
    
    var style =
      '<style> ' +
      '.menuA, .menuA:hover, .menuA_active, ' +
      '.menuB, .menuB:hover, .menuB_active, ' +
      '.menuC, .menuC:hover, .menuC_active, ' +
      '.menuD, .menuD:hover, .menuD_active, ' +
      '.menuE, .menuE:hover, .menuE_active {' +
      '  background-image: url(' + $('#image_menu').text() + '); ' +
      '} ' +
      '</style>';
    $(style).appendTo('head'); // IE compatible
    
    $('.menuC').removeAttr('href');
    $('<style> .menuC:hover { background-position: -179px -59px; cursor: default; } </style>').appendTo('head');
    
    $.ajaxSetup ({
      cache: false // Disable caching of AJAX responses
    });
    
    cms.loadScript('/javascripts/plugins/jquery.getCSS.js', function() {
      $.getCSS('/stylesheets/jquery.jqModal.css', function() {
        cms.loadScript('/javascripts/plugins/jquery.jqModal.js', cms.initSetupButton);
      });
    });
  }
};

var addthis_config = {
  'data_use_cookies':     false,
  'data_use_flash':       false,
  'data_track_clickback': false,
  'services_expanded':    'email,facebook,twitter,tumblr,sinaweibo,funp,plusone.google.com',
  'services_custom': [
    {
      name: "Google+",
      url:  "https://plusone.google.com/_/+1/confirm?hl=en&url={{URL}}",
      icon: "/images/cms/google-plus.png"
    }
  ],
  'pubid':                'ra-4dcccc98718a5dbe'
};

// everything is start from here
$(function() {
  
  if (cms.debug) {
    cms.loadScript('http://www.netgrow.com.au/assets/files/jquery_plugins/jquery.dump.js');
  }
  cms.loadScript('/javascripts/plugins/jquery.blockUI.js', function() {
    
    $.blockUI.defaults.message = $('#warning_please_wait').html();
    $(document).ajaxStart($.blockUI).ajaxStop($.unblockUI);
    $.blockUI();
    
    cms.init();
    
    if (cms.isGeneric()) {
      cms.initGenericOne();
    } else if (cms.isEnterprise()) {
      cms.initEnterpriseOne();
    } else if (cms.isNN()) {
      cms.initNNOne();
    }
    
    if (typeof (page$) != 'undefined' && typeof (page$.init) == 'function') {
      page$.init();
      if (cms.isGeneric() && typeof (page$.initGenericOne) == 'function') {
        page$.initGenericOne();
      } else if (cms.isEnterprise() && typeof (page$.initEnterpriseOne) == 'function') {
        page$.initEnterpriseOne();
      }
    }
    
  });
  
});

