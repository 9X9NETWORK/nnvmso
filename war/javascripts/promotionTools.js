/**
 * 
 */

var channelList =
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
        
        channelInfoBlock.find('.iconStatistics').attr('title', $('#lang_view_statistics'));
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
          //programList.init(event.data.channelId, event.data.readonly, event.data.channelName);
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
        channelSetInfoBlock.find('.channel_info_programcount span').text(channelSets[i].programCount);
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
          //programList.init(event.data.channelSetId, event.data.readonly, event.data.channelName);
        });
      }
      $('#channel_set_list_ul').append('<div class="clear"/>');
    });
  }
}

$(function()
{
  $('.promoteInfo').hover(function()
  {
    $('#pro_hint').show();
  }, function()
  {
    $('#pro_hint').hide();
  });
  channelList.init();
});
