/**
 * 
 */

var formatDate = function(timestamp)
{
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
};

var getContentTypeByFileExtention = function(ext)
{
  switch(ext)
  {
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
};

var bubblePopupProperties = 
{
  'position':  'top',
  'align':     'center',
  //'innerHtml': innerHtml,
  'innerHtmlStyle':
  {
    'color':      '#292929',
    'text-align': 'left',
    'font-size':  '0.8em'
  },
  'themeName': 'all-black',
  'themePath': '/images/cms'
};

var initSetupPage = function()
{
  $('#setup_page').jqm(
  {
    zIndex: 1000,
    toTop: true,
    modal: true,
    ajax: 'setup',
    trigger: '#setup',
    onLoad: function()
    {
      $.getScript('/javascripts/setup.js');
    }
  });
};

$(function()
{
  initSetupPage();
});
