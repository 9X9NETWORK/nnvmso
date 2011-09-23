/**
 * 
 */

var log = function(text) {
  if (window.console && console.log)
    console.log(text);
};

var cms = {
  debug: false,
  isGeneric: function() {
    return ($('#msoType').val() == '4');
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
          }
        });
      },
      onHide: function() {
        location.reload();
      }
    });
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
    
    $.ajaxSetup ({
      cache: false // Disable caching of AJAX responses
    });
    
    cms.loadScript('/javascripts/plugins/jquery.getCSS.js', function() {
      $.getCSS('/stylesheets/jquery.jqModal.css', function() {
        cms.loadScript('/javascripts/plugins/jquery.jqModal.js', cms.initSetupButton);
      });
    });
    cms.loadScript('/javascripts/plugins/jquery.textTruncate.js');
  }
};

var addthis_config = {
  'data_use_cookies':     false,
  'data_use_flash':       false,
  'data_track_clickback': false,
  'services_expanded':    'email,facebook,twitter,tumblr,sinaweibo,funp',
  'pubid':                'ra-4dcccc98718a5dbe'
};

// everything is start from here
$(function() {
  
  if (cms.debug) {
    cms.loadScript('http://www.netgrow.com.au/assets/files/jquery_plugins/jquery.dump.js');
  }
  
  cms.init();
  
  if (typeof (page$) != 'undefined' && typeof (page$.init) == 'function') {
    page$.init();
    if (cms.isGeneric() && typeof (page$.initGenericOne) == 'function') {
      page$.initGenericOne();
    }
  }
  
  if (cms.isGeneric()) {
    cms.initGenericOne();
  }
  
});

