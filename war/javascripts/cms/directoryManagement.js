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

var loadCategoryChannelsAndChannelSets = function(categoryId) {
  // load channels
  $.get('/CMSAPI/listCategoryChannels?categoryId=' + categoryId, function(channels)
  {
    //var steal = $('#treeview').bind('create.jstree');
    //$('#treeview').unbind('create.jstree');
    for (i in channels)
    {
      var channelId = channels[i].key.id;
      $('#treeview').jstree('create', $('#' + categoryId), 'last', {
        attr: {
          rel: 'file',
          id: channelId
        },
        data: channels[i].name
      }, null, true);
      $('.ch_normal input[name="id"][value="'+channelId+'"]')
        .parent()
        .removeClass('ch_normal')
        .removeClass('jstree-draggable')
        .addClass('ch_disable')
        //.draggable('disable')
        .RemoveBubblePopup();
    }
    //$('#treeview').bind('create.jstree', steal);
  }, 'json');
  // load channel sets
  $.get('/CMSAPI/listCategoryChannelSets?categoryId=' + categoryId, function(channelSets)
  {
    //var steal = $('#treeview').bind('create.jstree');
    //$('#treeview').unbind('create.jstree');
    for (i in channelSets)
    {
      var channelSetId = channelSets[i].key.id;
      $('#treeview').jstree('create', $('#' + categoryId), 'last', {
        attr: {
          rel: 'set',
          id: channelSetId
        },
        data: channelSets[i].name
      }, null, true);
      $('.ch_normal input[name="id"][value="'+channelSetId+'"]')
        .parent()
        .removeClass('ch_normal')
        .removeClass('jstree-draggable')
        .addClass('ch_disable')
        //.draggable('disable')
        .RemoveBubblePopup();
    }
    //$('#treeview').bind('create.jstree', steal);
  }, 'json');
};

var composeCategoryTreeData = function(categories, all)
{
  var result = $('<div/>');
  var ul = $('<ul/>')
  for (var i = 0; i < categories.length; i++)
  {
    var categoryId = categories[i].key.id;
    var item = $('<li/>').attr('rel', 'folder').attr('id', categoryId);//.addClass('jstree-drop');
    var a = $('<a/>').attr('href', 'javascript:').text(categories[i].name);
    item.append(a);
    var sub = [];
    for (var j = 0; j < all.length; j++)
    {
      if (all[j].parentId == categoryId)
      {
        sub.push(all[j]);
      }
    }
    if (sub.length > 0)
    {
      item.append(composeCategoryTreeData(sub, all));
    }
    //loadCategoryChannelSets(categoryId);
    //loadCategoryChannels(categoryId);
    loadCategoryChannelsAndChannelSets(categoryId);
    ul.append(item);
  }
  result.append(ul);
  return result.html();
};

var directoryArea =
{
  init: function()
  {
    $('#btn_delete_directory').button().unbind('click').click(function()
    {
      var selected = $('#treeview').jstree('get_selected');
      if ($(selected).attr('rel') != 'folder') {
        alert('你必須選擇一個目錄');
        return;
      } else if ($(selected).parent().parent().attr('id') == 'treeview') {
        alert('根目錄將無法被移除');
        return;
      }
      var categoryId = $(selected).attr('id');
      var categoryName = $(selected).find('> a').text();
      if (confirm('你確定要移除目錄 "'+categoryName+'" 所有的子目錄也會一併被移除') == false) {
        return;
      }
      $.get('/CMSAPI/removeCategory?categoryId=' + categoryId, function(response)
      {
        if (response != 'OK') {
          alert('發生錯誤');
          return;
        }
        $('#treeview').jstree('remove', selected);
      }, 'text');
    });
    $('#btn_create_directory').button().unbind('click').click(function()
    {
      var selected = $('#treeview').jstree('get_selected');
      if ($(selected).attr('rel') != 'folder') {
        alert('你必須選擇一個目錄');
        return;
      }
      var name = new Date().getTime().toString();
      var parameters = {
        msoId: $('#msoId').val(),
        parentId: $(selected).attr('id'),
        name: name
      };
      $.post('/CMSAPI/createCategory', parameters, function(categoryId)
      {
        //alert(categoryId);
        if (!categoryId.toString().match(/^[0-9]+$/) || categoryId == 0) {
          alert('發生錯誤');
          return;
        }
        $('#treeview').jstree('create', selected, 'first', {
          attr: {
            rel: 'folder',
            id: categoryId
          },
          data: name
        }, function(data)
        {
        }, false);
      });
    });
    $('#btn_rename_directory').button().unbind('click').click(function()
    {
      var selected = $('#treeview').jstree('get_selected');
      if ($(selected).attr('rel') != 'folder') {
        alert('你必須選擇一個目錄');
        return;
      }
      $('#treeview').jstree('rename');
      //alert($(selected).html());
      //alert('rename directory');
    });
    $.get('/CMSAPI/listCategories?msoId=' + $('#msoId').val(), function(categories)
    {
      var root = [];
      for (var i = 0; i < categories.length; i++)
      {
        if (categories[i].parentId == 0)
        {
          root.push(categories[i]);
        }
      }
      var treeData = composeCategoryTreeData(root, categories);
      //alert(treeData);
      $('#treeview').jstree({
        'dnd': {
          'drop_finish': function(data)
          {
            var categoryId = data.o.parent().parent().attr('id');
            var objectId = data.o.attr('id');
            var type = data.o.attr('rel');
            
            var url;
            var parameters = {
              'categoryId': categoryId
            };
            if (type == 'file') {
              url = '/CMSAPI/removeCategoryChannel';
              parameters['channelId'] = objectId;
            } else if (type == 'set') {
              url = '/CMSAPI/removeCategoryChannelSet';
              parameters['channelSetId'] = objectId;
            } else {
              alert('拖拽目錄無效');
              return;
            }
            $.post(url, parameters, function(response)
            {
              if (response != 'OK') {
                alert('發生錯誤');
                return;
              }
              //alert('Qoo');
              $('#treeview').jstree('remove', data.o);
              var ch = $('.ch_disable input[name="id"][value="'+objectId+'"]')
                         .parent()
                         .removeClass('ch_disable')
                         .addClass('ch_normal')
                         //.draggable('enable')
                         .addClass('jstree-draggable');
              bubblePopupProperties['innerHtml'] = ch.find('span').html();
              ch.CreateBubblePopup(bubblePopupProperties);
            }, 'text')
          },
          'drag_finish': function(data)
          {
            var categoryId = data.r.attr('id');
            var obj;
            if (data.o.tagName != 'LI')
              obj = $(data.o).parent();
            else
              obj = $(data.o);
            var type = obj.find('input[name="type"]').val();
            var objectId = obj.find('input[name="id"]').val();
            var url;
            var parameters = {
              'categoryId': categoryId
            };
            if (type == 'channelSet') {
              url = '/CMSAPI/createCategoryChannelSet';
              parameters['channelSetId'] = objectId;
            } else {
              url = '/CMSAPI/createCategoryChannel';
              parameters['channelId'] = objectId;
            }
            $.post(url, parameters, function(response)
            {
              if (response != 'OK') {
                alert('發生錯誤');
                return;
              }
              $(data.r).find('ul > li[rel!="folder"]').remove();
              loadCategoryChannelsAndChannelSets(categoryId);
            }, 'text');
          },
          'drag_check': function(data)
          {
            if (data.r.attr('rel') == 'folder') {
              return {
                before: false,
                after: false,
                inside: true
              }
            } else {
              return {
                before: false,
                after: false,
                inside: false
              }
            }
          },
          'drag_target': 'li.jstree-draggable'
        },
        'html_data': {
          'data': treeData
        },
        'types': {
          'valid_children': ['folder'],
          'types': {
            'folder': {
              'valid_children': ['folder', 'file', 'set'],
              'icon': { 'image': '/javascripts/themes/default/folder.png' }
            },
            'file': {
              'valid_children': ['none'],
              'max_depth': 1,
              'icon': { 'image': '/javascripts/themes/default/file.png' }
            },
            'set': {
              'valid_children': ['none'],
              'max_depth': 1,
              'icon': { 'image': '/javascripts/themes/default/set.png' }
            },
            'default': {
           
            }
          }
        },
        'crrm': {
          'move': {
            'check_move': function(data)
            {
              var parent = $(data.o).parent().parent();
              var dest = $(data.r);
              if (parent.attr('id') == 'treeview') { // root can not be moved
                return false;
              }
              if (dest.attr('rel') != 'folder') { // destination must be a folder
                return false;
              }
              if (data.cr == -1) {
                return false
              }
              return true;
            }
          }
        },
        'plugins': ['themes', 'html_data', 'types', 'ui', 'crrm', 'dnd'] 
      });
      $('#treeview').bind('rename.jstree', function(event, data)
      {
        var selected = $('#treeview').jstree('get_selected');
        var categoryId = $(selected).attr('id');
        //alert(categoryId);
        var newName = data.rslt.new_name;
        var oldName = data.rslt.old_name;
        if (newName != oldName)
        {
          var parameter = {
            'categoryId': categoryId,
            'name': newName
          };
          $.post('/CMSAPI/renameCategory', parameter, function(response)
          {
            if (response != 'OK')
              alert('發生錯誤');
          }, 'text');
        }
      });
      $('#treeview').bind('create.jstree', function(event, data)
      {
        //alert(data.rslt.obj.attr('rel'));
        if (data.rslt.obj.attr('rel') != 'folder') {
          return;
        }
        var categoryId = data.rslt.obj.attr('id');
        var name = data.rslt.name;
        var parameter = {
          'categoryId': categoryId,
          'name': name
        };
        $.post('/CMSAPI/renameCategory', parameter, function(response)
        {
          if (response != 'OK')
            alert('發生錯誤');
        }, 'text');
      });
      $('#treeview').bind('move_node.jstree', function(event, data)
      {
        var toCategoryId = $(data.rslt.np).attr('id');
        var type = $(data.rslt.o).attr('rel');
        var objectId = $(data.rslt.o).attr('id');
        var fromCategoryId = $(data.rslt.op).attr('id');
        
        var url;
        var parameters = {
          'toCategoryId': toCategoryId,
          'fromCategoryId': fromCategoryId
        }
        if (type == 'file') {
          url = '/CMSAPI/moveCategoryChannel';
          parameters['channelId'] = objectId;
        } else if (type == 'set') {
          url = '/CMSAPI/moveCategoryChannelSet';
          parameters['channelSetId'] = objectId;
        } else if (type == 'folder') {
          url = '/CMSAPI/moveCategory';
          parameters['categoryId'] = objectId;
        } else {
          return;
        }
        $.post(url, parameters, function(response)
        {
          if (response != 'OK') {
            alert('發生錯誤');
          }
        }, 'text');
        
      });
    });
  }
};

var channelAndSetPool =
{
  onload: null,
  initilized: false,
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
        item.addClass('jstree-draggable').css('z-index', 100);
        item.children().each(function()
        {
          $(this).css('z-index', 1);
        });
        //item.draggable(draggableProperties);
        
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
          item.addClass('jstree-draggable').css('z-index', 100);
          item.children().each(function()
          {
            $(this).css('z-index', 1);
          });
          //item.draggable(draggableProperties);
        }
        $('#directory_list_ul').append('<div style="clear:both"/>');
        channelAndSetPool.initBubbles();
        channelAndSetPool.initilized = true;
        if (channelAndSetPool.onload != null && typeof channelAndSetPool.onload == 'function') {
          channelAndSetPool.onload();
        }
      });
    }, 'json');
  }
};

$(function()
{
  channelAndSetPool.onload = directoryArea.init;
  channelAndSetPool.init();
});
