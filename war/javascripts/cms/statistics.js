/**
 * 
 */

var datepicker_properties =
{
  rangeSelect: true,
  firstDay: 1,
  showOn: 'button',
  buttonImage: '/images/cms/icon_calendar.png',
  buttonImageOnly: true
}

var statisticsReport = {
  destroy: function()
  {
    $('#channel_statistics').hide();
    $('#channel_set_statistics').hide();
  },
  initChannel: function(channelId, channelName)
  {
    statisticsReport.destroy();
    $('.right_title > div').text(channelName);
    $.get('/CMSAPI/channelStatisticsInfo?channelId=' + channelId, function(report)
    {
      $('#ch_subscription_count').text(report.subscriptionCount);
    });
    $('#stasticTabA').click();
    $('#channel_statistics').show();
    statisticsReport.initProgram(channelId);
  },
  initProgram: function(channelId)
  {
    $('#ep_selector').html('');
    $('<option/>').val('0').text($('#lang_label_please_select_program').text()).appendTo('#ep_selector');
    $.get('/CMSAPI/programList?channelId=' + channelId, function(programs)
    {
      for (i in programs) {
        var name = programs[i].name;
        if (name.length > 20) {
          name = name.substr(0, 20) + '...';
        }
        var option = $('<option/>').val(programs[i].key.id).text(name).appendTo('#ep_selector');
        $('#ep_selector').unbind().change(function(event)
        {
          var programId = $(this).val();
          $.get('/CMSAPI/programStatisticsInfo?programId=' + programId, function(report)
          {
            $('#ep_share_count').text(report.shareCount);
          })
        });
      }
    });
  },
  initChannelSet: function(channelSetId, channelSetName)
  {
    statisticsReport.destroy();
    $('.right_title > div').text(channelSetName + ' - ' + $('#lang_title_set_statistics').text());
    $.get('/CMSAPI/channelSetStatisticsInfo?channelSetId=' + channelSetId, function(report)
    {
      $('#set_subscription_count').text(report.subscriptionCount);
    });
    $('#channel_set_statistics').show();
  }
}

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
          statisticsReport.initChannel(event.data.channelId, event.data.channelName);
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
          
          statisticsReport.initChannelSet(event.data.channelSetId, event.data.channelSetName);
          
        });
      }
      $('#channel_set_list_ul').append('<div class="clear"/>');
    });
  }
}

$(function()
{
  var css = '.chPublic { background:url(' + $('#image_ch_public').text() + ') no-repeat; }\n.chUnPublic { background:url(' + $('#image_ch_unpublic').text() + ') no-repeat; }';
  $('<style/>').text(css).appendTo('head');
  
  $('#stasticTabA').click(function()
  {
    $('#ch_stastics').show();
    $('#ep_stastics').hide();
    $('#stasticTabA').addClass('tab_focus').removeClass('tab_unfocus');
    $('#stasticTabB').addClass('tab_unfocus').removeClass('tab_focus');
  });
  $('#stasticTabB').click(function()
  {
    $('#ch_stastics').hide();
    $('#ep_stastics').show();
    $('#stasticTabB').addClass('tab_focus').removeClass('tab_unfocus');
    $('#stasticTabA').addClass('tab_unfocus').removeClass('tab_focus');
  });
  channelAndChannelSetList.init();
  $('.datePick input').datepicker(datepicker_properties);
});
