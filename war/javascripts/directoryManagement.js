/**
 * 
 */

var draggableProperties =
{
  'start': function()
  {
    //$('.ch_exist').RemoveBubblePopup();
    $('.ch_normal').RemoveBubblePopup();
  },
  'stop': function()
  {
    channelAndSetPool.initBubbles();
    //channelPool.manageControls();
  },
  'appendTo': '#directory_list_ul',
  'disabled': false,
  'opacity':  0.5,
  'helper':   "clone",
  'scroll':   false,
  'revert':   'invalid'
  };

var populateBubbleChannelSetContent = function(channelSet)
{
  var label_channel_set = $('#lang_label_channel_set').text();
  var label_update_time = $('#lang_label_update_time').text();
  var span = $('<span></span>');
  var h2 = $('<h2 class="popTitle"></h2>').text('[' + label_channel_set + ']' + channelSet.name).textTruncate(20, '...').appendTo(span);
  
  var innerHtml = span.html();
  innerHtml += '<br/>' + label_update_time + ' ：' + formatDate(channelSet.updateDate);
  
  return innerHtml;
}

var populateBubbleChannelContent = function(channel)
{
  var label_program_count = $('#lang_label_program_count').text();
  var label_update_time = $('#lang_label_update_time').text();
  var span = $('<span></span>');
  var h2 = $('<h2 class="popTitle"></h2>').text(channel.name).textTruncate(20, '...').appendTo(span);
  
  var innerHtml = span.html();
  innerHtml += '<br/>' + label_program_count + ' ：' + channel.programCount;
  innerHtml += '<br/>' + label_update_time + ' ：' + formatDate(channel.updateDate);
  
  return innerHtml;
}

var directoryArea =
{
  init: function()
  {
    $('#btn_delete_directory').button().unbind().click(function()
    {
      alert('delete directory');
    });
    $('#btn_create_directory').button().unbind().click(function()
    {
      alert('create directory');
    });
  }
};

var channelAndSetPool =
{
  initBubbles: function()
  {
    $('.ch_normal').each(function()
      {
        bubblePopupProperties['innerHtml'] = $(this).find('span').html();
        $(this).CreateBubblePopup(bubblePopupProperties);
      });
  },
  init: function()
  {
    $.get('/CMSAPI/listOwnedChannelSets?msoId=' + $('#msoId').val(), function(channelSets)
    {
      for (var i = 0; i < channelSets.length; i++)
      {
        var item = $('<li class="ch_normal"/>');
        var img = $('<img/>').attr('src', channelSets[i].imageUrl);
        var p = $('<p class="ch_name"></p>').text(channelSets[i].name).textTruncate(20, '...');
        var type = $('<input type="hidden" name="type"/>').val('channelSet');
        var hidden = $('<input type="hidden" name="id"/>').val(channelSets[i].key.id);
        item.append(img).append(p).append(type).append(hidden).appendTo('#directory_list_ul');
        
        var innerHtml = populateBubbleChannelSetContent(channelSets[i]);
        $('<span></span>').html(innerHtml).hide().appendTo(item);
        
        item.draggable(draggableProperties);
        
      }
      
      $.get('/CMSAPI/listOwnedChannels?msoId=' + $('#msoId').val(), function(channels)
      {
        for (var i = 0; i < channels.length; i++)
        {
          var item = $('<li class="ch_normal"/>');
          var img = $('<img/>').attr('src', channels[i].imageUrl);
          var p = $('<p class="ch_name"></p>').text(channels[i].name).textTruncate(20, '...');
          var type = $('<input type="hidden" name="type"/>').val('channel');
          var hidden = $('<input type="hidden" name="id"/>').val(channels[i].key.id);
          item.append(img).append(p).append(type).append(hidden).appendTo('#directory_list_ul');
          
          var innerHtml = populateBubbleChannelContent(channels[i]);
          $('<span></span>').html(innerHtml).hide().appendTo(item);
          
          item.draggable(draggableProperties);
        }
        $('#directory_list_ul').append('<div style="clear:both"/>');
        channelAndSetPool.initBubbles();
      });
    }, 'json');
  }
};

$(function()
{
  /*
  $('.ch_normal').CreateBubblePopup(
  {
    position:  'top',
    align:     'center',
    innerHtml: '<h2 class="popTitle">頻道標題</h2><br/> 節目數量 : 10 <br/> 更新時間 ：2010/10/24 &nbsp;&nbsp; 16:34:30 ',
    innerHtmlStyle:
    {
      'color':      '#292929',
      'text-align': 'left',
      'font-size':  '0.8em'
    },
    themeName: 'all-black',
    themePath: '/images/cms'
  });
  */
  channelAndSetPool.init();
  directoryArea.init();
  $('#treeview').jstree({});
});
