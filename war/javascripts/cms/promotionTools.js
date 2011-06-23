/**
 * 
 */

var autosharingSettings =
{
  initChannelSet: function(channelSetId, channelSetName)
  {
    $('.sns_checkbox').attr('checked', false)
    // load autosharing info
    var parameters = {
      'channelSetId': channelSetId,
      'msoId': $('#msoId').val()
    }
    $.get('/CMSAPI/listChannelSetAutosharing', parameters, function(autosharings)
    {
      for (i in autosharings) {
        switch(autosharings[i].type) {
          case 1:
          $('input[name="sns_facebook"]').attr('checked', true);
          break;
          case 2:
          $('input[name="sns_twitter"]').attr('checked', true);
          break;
          case 3:
          $('input[name="sns_plurk"]').attr('checked', true);
          break;
          case 4:
          $('input[name="sns_sina"]').attr('checked', true);
          break;
          default:
        }
      }
      if (!$('input[name="sns_facebook"]').attr('disabled')) {
        $('input[name="sns_facebook"]').unbind('change').change(function()
        {
          var parameters = {
            'msoId': $('#msoId').val(),
            'channelSetId': channelSetId,
            'type': 1
          };
          if ($(this).attr('checked') == 'checked') {
            $.get('/CMSAPI/createChannelSetAutosharing', parameters);
          } else {
            $.get('/CMSAPI/removeChannelSetAutosharing', parameters);
          }
        });
      }
      if (!$('input[name="sns_twitter"]').attr('disabled')) {
        $('input[name="sns_twitter"]').unbind('change').change(function()
        {
          var parameters = {
            'msoId': $('#msoId').val(),
            'channelSetId': channelSetId,
            'type': 2
          };
          if ($(this).attr('checked') == 'checked') {
            $.get('/CMSAPI/createChannelSetAutosharing', parameters);
          } else {
            $.get('/CMSAPI/removeChannelSetAutosharing', parameters);
          }
        });
      }
      if (!$('input[name="sns_plurk"]').unbind('change').attr('disabled')) {
        $('input[name="sns_plurk"]').change(function()
        {
          var parameters = {
            'msoId': $('#msoId').val(),
            'channelSetId': channelSetId,
            'type': 3
          };
          if ($(this).attr('checked') == 'checked') {
            $.get('/CMSAPI/createChannelSetAutosharing', parameters);
          } else {
            $.get('/CMSAPI/removeChannelSetAutosharing', parameters);
          }
        });
      }
      if (!$('input[name="sns_sina"]').unbind('change').attr('disabled')) {
        $('input[name="sns_sina"]').change(function()
        {
          var parameters = {
            'msoId': $('#msoId').val(),
            'channelSetId': channelSetId,
            'type': 3
          };
          if ($(this).attr('checked') == 'checked') {
            $.get('/CMSAPI/createChannelSetAutosharing', parameters);
          } else {
            $.get('/CMSAPI/removeChannelSetAutosharing', parameters);
          }
        });
      }
      $('.right_title > div').text(channelSetName);
      $('#promotion_content').show();
    }, 'json');
  },
  initChannel: function(channelId, channelName)
  {
    $('.sns_checkbox').attr('checked', false)
    // load autosharing info
    var parameters = {
      'channelId': channelId,
      'msoId': $('#msoId').val()
    }
    $.get('/CMSAPI/listChannelAutosharing', parameters, function(autosharings)
    {
      for (i in autosharings) {
        switch(autosharings[i].type) {
          case 1:
          $('input[name="sns_facebook"]').attr('checked', true);
          break;
          case 2:
          $('input[name="sns_twitter"]').attr('checked', true);
          break;
          case 3:
          $('input[name="sns_plurk"]').attr('checked', true);
          break;
          case 4:
          $('input[name="sns_sina"]').attr('checked', true);
          break;
          default:
        }
      }
      if (!$('input[name="sns_facebook"]').attr('disabled')) {
        $('input[name="sns_facebook"]').unbind('change').change(function()
        {
          var parameters = {
            'msoId': $('#msoId').val(),
            'channelId': channelId,
            'type': 1
          };
          if ($(this).attr('checked') == 'checked') {
            $.get('/CMSAPI/createChannelAutosharing', parameters);
          } else {
            $.get('/CMSAPI/removeChannelAutosharing', parameters);
          }
        });
      }
      if (!$('input[name="sns_twitter"]').attr('disabled')) {
        $('input[name="sns_twitter"]').unbind('change').change(function()
        {
          var parameters = {
            'msoId': $('#msoId').val(),
            'channelId': channelId,
            'type': 2
          };
          if ($(this).attr('checked') == 'checked') {
            $.get('/CMSAPI/createChannelAutosharing', parameters);
          } else {
            $.get('/CMSAPI/removeChannelAutosharing', parameters);
          }
        });
      }
      if (!$('input[name="sns_plurk"]').attr('disabled')) {
        $('input[name="sns_plurk"]').unbind('change').change(function()
        {
          var parameters = {
            'msoId': $('#msoId').val(),
            'channelId': channelId,
            'type': 3
          };
          if ($(this).attr('checked') == 'checked') {
            $.get('/CMSAPI/createChannelAutosharing', parameters);
          } else {
            $.get('/CMSAPI/removeChannelAutosharing', parameters);
          }
        });
      }
      if (!$('input[name="sns_sina"]').attr('disabled')) {
        $('input[name="sns_sina"]').unbind('change').change(function()
        {
          var parameters = {
            'msoId': $('#msoId').val(),
            'channelId': channelId,
            'type': 3
          };
          if ($(this).attr('checked') == 'checked') {
            $.get('/CMSAPI/createChannelAutosharing', parameters);
          } else {
            $.get('/CMSAPI/removeChannelAutosharing', parameters);
          }
        });
      }
      $('.right_title > div').text(channelName);
      $('#promotion_content').show();
    }, 'json');
  }
};

var channelAndChannelSetList =
{
  init: function()
  {
    // load channels
    $.getJSON('/CMSAPI/listOwnedChannels?msoId=' + $('#msoId').val(), function(channels)
    {
      for (i in channels)
      {
        var channelInfoBlock = $('#channel_info_block').clone(true).removeAttr('id').addClass('channel_info_block_cloned');
        var channelId = channels[i].key.id;
        
        channelInfoBlock.find('.iconStatistics').attr('title', $('#lang_view_statistics').text());
        channelInfoBlock.find('.channel_info_title div').text(channels[i].name);
        $('<img/>').attr('src', channels[i].imageUrl).appendTo(channelInfoBlock.find('.channel_info_image'));
        var contentType = 'Unknown';
        switch(channels[i].contentType) {
          case 1:
          contentType = 'System';
          break;
          case 2:
          contentType = 'Podcast';
          break;
          case 3:
          contentType = 'YouTube Channel';
          break;
          case 4:
          contentType = 'YouTube Playlist';
          break;
          case 5:
          contentType = 'Facebook';
          break;
          case 6:
          contentType = '9x9';
          break;
          default:
        }
        channelInfoBlock.find('.channel_info_contenttype span').text(contentType);
        channelInfoBlock.find('.channel_info_programcount span').text(channels[i].programCount);
        channelInfoBlock.find('.channel_info_subscribers span').text(channels[i].subscriptionCount);
        channelInfoBlock.find('.channel_info_updatedate span').text(formatDate(channels[i].updateDate));
        // add this
        var promoteUrl = 'http://' + location.host + '/channel/' + channelId;
        var addthis_share = 
        {
          'title': channels[i].name,
          'description': channels[i].intro,
          'url': promoteUrl
        }
        addthis_config['pubid'] = $('#msoId').val();
        addthis_config['ui_click'] = true;
        addthis.button(channelInfoBlock.find('.channel_info_addthis').get(0), null, addthis_share);
        var switchObject = channelInfoBlock.find('.channel_info_publish');
        if (channels[i]['public']) {
          switchObject.removeClass('chUnPublic').addClass('chPublic');
        } else {
          switchObject.removeClass('chPublic').addClass('chUnPublic');
        }
        channelInfoBlock.find('.channel_info_promoteurl').text(promoteUrl).attr('href', promoteUrl);
        $('<li></li>').append(channelInfoBlock).appendTo('#channel_list_ul');
        // channel info block click event
        channelInfoBlock.click({ 'channelId': channelId, 'channelName': channels[i].name}, function(event)
        {
          $('.channel_info_block').removeClass('chFocus').addClass('chUnFocus');
          $('.channel_info_title').removeClass('chFocusTitle').addClass('chUnFocusTitle');
          $('.channel_info_image').removeClass('chFocusImg').addClass('chUnFocusImg');
          $(this).removeClass('chUnFocus').addClass('chFocus');
          $('.channel_info_title', this).removeClass('chUnFocusTitle').addClass('chFocusTitle');
          $('.channel_info_image', this).removeClass('chUnFocusImg').addClass('chFocusImg');
          
          // load autosharing info
          autosharingSettings.initChannel(event.data.channelId, event.data.channelName);
        });
      }
      //$('#channel_list').show();
    });
    // load channel sets
    $.getJSON('/CMSAPI/listOwnedChannelSets?msoId=' + $('#msoId').val(), function(channelSets)
    {
      for (i in channelSets)
      {
        var channelSetInfoBlock = $('#channel_info_block').clone(true).removeAttr('id').addClass('channel_info_block_cloned');
        var channelSetId = channelSets[i].key.id;
        
        channelSetInfoBlock.find('.iconStatistics').attr('title', $('#lang_view_statistics').text());
        channelSetInfoBlock.find('.channel_info_title div').text(channelSets[i].name);
        $('<img/>').attr('src', channelSets[i].imageUrl).appendTo(channelSetInfoBlock.find('.channel_info_image'));
        channelSetInfoBlock.find('.channel_info_contenttype span').text($('#lang_label_channel_set').text());
        channelSetInfoBlock.find('.channel_info_programcount span').text('N/A');
        channelSetInfoBlock.find('.channel_info_subscribers span').text(channelSets[i].subscriptionCount);
        channelSetInfoBlock.find('.channel_info_updatedate span').text(formatDate(channelSets[i].updateDate));
        // add this
        var promoteUrl = 'http://' + location.host + '/';
        promoteUrl += ((channelSets[i].beautifulUrl != null) ? channelSets[i].beautifulUrl : channelSets[i].defaultUrl);
        var addthis_share =
        {
          'title': channelSets[i].name,
          'description': channelSets[i].intro,
          'url': promoteUrl
        }
        addthis_config['pubid'] = $('#msoId').val();
        addthis_config['ui_click'] = true;
        addthis.button(channelSetInfoBlock.find('.channel_info_addthis').get(0), null, addthis_share);
        var switchObject = channelSetInfoBlock.find('.channel_info_publish');
        if (channelSets[i]['public']) {
          switchObject.removeClass('chUnPublic').addClass('chPublic');
        } else {
          switchObject.removeClass('chPublic').addClass('chUnPublic');
        }
        channelSetInfoBlock.find('.channel_info_promoteurl').text(promoteUrl).attr('href', promoteUrl);
        $('<li></li>').append(channelSetInfoBlock).appendTo('#channel_set_list_ul');
        // channel info block click event
        channelSetInfoBlock.click({ 'channelSetId': channelSetId, 'channelSetName': channelSets[i].name}, function(event)
        {
          $('.channel_info_block').removeClass('chFocus').addClass('chUnFocus');
          $('.channel_info_title').removeClass('chFocusTitle').addClass('chUnFocusTitle');
          $('.channel_info_image').removeClass('chFocusImg').addClass('chUnFocusImg');
          $(this).removeClass('chUnFocus').addClass('chFocus');
          $('.channel_info_title', this).removeClass('chUnFocusTitle').addClass('chFocusTitle');
          $('.channel_info_image', this).removeClass('chUnFocusImg').addClass('chFocusImg');
          
          autosharingSettings.initChannelSet(event.data.channelSetId, event.data.channelSetName);
          
        });
      }
      $('#channel_set_list_ul').append('<div class="clear"/>');
    });
  }
}

$(function()
{
  $('.pro_check').click(function()
  {
    var confirmMessage = $('#lang_confirm_goto_setting_page').text();
    if (confirm(confirmMessage.replace('{sns}', $(this).text())) == true) {
      $('#setup').click();
    }
  });
  $('#9x9_rss_tutorial').click(function()
  {
    alert('9x9 rss feed was not provided');
  });
  $('.promoteInfo').hover(function()
  {
    $('#pro_hint').show();
  }, function()
  {
    $('#pro_hint').hide();
  });
  $.get('/CMSAPI/listSnsAuth?msoId=' + $('#msoId').val(), function(snsAuths)
  {
    for (i in snsAuths) {
      switch (snsAuths[i].type) {
        case 1:
        $('input[name="sns_facebook"]').attr('disabled', false).parent().unbind('click').css('color', 'black');
        break;
        case 2:
        $('input[name="sns_twitter"]').attr('disabled', false).parent().unbind('click').css('color', 'black');
        break;
        case 3:
        $('input[name="sns_plurk"]').attr('disabled', false).parent().unbind('click').css('color', 'black');
        break;
        case 4:
        $('input[name="sns_sina"]').attr('disabled', false).parent().unbind('click').css('color', 'black');
        break;
        default:
      }
    }
    //$('input[name="sns_facebook"]').attr('disabled', false).parent().unbind('click').css('color', 'black'); // Qoo
  })
  channelAndChannelSetList.init();
});
