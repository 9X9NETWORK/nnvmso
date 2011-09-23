/**
 * 
 */

var page$ = {
  autosharingSettings: {
    initChannelSet: function(channelSetId, channelSetName) {
      $('.sns_checkbox').attr('checked', false)
      // load autosharing info
      var parameters = {
        'channelSetId': channelSetId,
        'msoId': $('#msoId').val()
      }
      cms.post('/CMSAPI/listChannelSetAutosharing', parameters, function(autosharings) {
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
        if ($('input[name="sns_facebook"]').attr('disabled') != 'disabled') {
          $('input[name="sns_facebook"]').unbind('change').change(function() {
            var parameters = {
              'msoId': $('#msoId').val(),
              'channelSetId': channelSetId,
              'type': 1
            };
            if ($(this).attr('checked') == 'checked') {
              cms.post('/CMSAPI/createChannelSetAutosharing', parameters);
            } else {
              cms.post('/CMSAPI/removeChannelSetAutosharing', parameters);
            }
          });
        }
        if ($('input[name="sns_twitter"]').attr('disabled') != 'disabled') {
          $('input[name="sns_twitter"]').unbind('change').change(function() {
            var parameters = {
              'msoId': $('#msoId').val(),
              'channelSetId': channelSetId,
              'type': 2
            };
            if ($(this).attr('checked') == 'checked') {
              cms.post('/CMSAPI/createChannelSetAutosharing', parameters);
            } else {
              cms.post('/CMSAPI/removeChannelSetAutosharing', parameters);
            }
          });
        }
        if ($('input[name="sns_plurk"]').unbind('change').attr('disabled') != 'disabled') {
          $('input[name="sns_plurk"]').change(function() {
            var parameters = {
              'msoId': $('#msoId').val(),
              'channelSetId': channelSetId,
              'type': 3
            };
            if ($(this).attr('checked') == 'checked') {
              cms.post('/CMSAPI/createChannelSetAutosharing', parameters);
            } else {
              cms.post('/CMSAPI/removeChannelSetAutosharing', parameters);
            }
          });
        }
        if ($('input[name="sns_sina"]').unbind('change').attr('disabled') != 'disabled') {
          $('input[name="sns_sina"]').change(function() {
            var parameters = {
              'msoId': $('#msoId').val(),
              'channelSetId': channelSetId,
              'type': 3
            };
            if ($(this).attr('checked') == 'checked') {
              cms.post('/CMSAPI/createChannelSetAutosharing', parameters);
            } else {
              cms.post('/CMSAPI/removeChannelSetAutosharing', parameters);
            }
          });
        }
        $('.right_title > div').text(channelSetName);
        $('#promotion_content').show();
      }, 'json');
    },
    initChannel: function(channelId, channelName) {
      $('.sns_checkbox').attr('checked', false)
      // load autosharing info
      var parameters = {
        'channelId': channelId,
        'msoId': $('#msoId').val()
      }
      cms.post('/CMSAPI/listChannelAutosharing', parameters, function(autosharings) {
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
        if ($('input[name="sns_facebook"]').attr('disabled') != 'disabled') {
          $('input[name="sns_facebook"]').unbind('change').change(function() {
            var parameters = {
              'msoId': $('#msoId').val(),
              'channelId': channelId,
              'type': 1
            };
            if ($(this).attr('checked') == 'checked') {
              cms.post('/CMSAPI/createChannelAutosharing', parameters);
            } else {
              cms.post('/CMSAPI/removeChannelAutosharing', parameters);
            }
          });
        }
        if ($('input[name="sns_twitter"]').attr('disabled') != 'disabled') {
          $('input[name="sns_twitter"]').unbind('change').change(function() {
            var parameters = {
              'msoId': $('#msoId').val(),
              'channelId': channelId,
              'type': 2
            };
            if ($(this).attr('checked') == 'checked') {
              cms.post('/CMSAPI/createChannelAutosharing', parameters);
            } else {
              cms.post('/CMSAPI/removeChannelAutosharing', parameters);
            }
          });
        }
        if ($('input[name="sns_plurk"]').attr('disabled') != 'disabled') {
          $('input[name="sns_plurk"]').unbind('change').change(function() {
            var parameters = {
              'msoId': $('#msoId').val(),
              'channelId': channelId,
              'type': 3
            };
            if ($(this).attr('checked') == 'checked') {
              cms.post('/CMSAPI/createChannelAutosharing', parameters);
            } else {
              cms.post('/CMSAPI/removeChannelAutosharing', parameters);
            }
          });
        }
        if ($('input[name="sns_sina"]').attr('disabled') != 'disabled') {
          $('input[name="sns_sina"]').unbind('change').change(function() {
            var parameters = {
              'msoId': $('#msoId').val(),
              'channelId': channelId,
              'type': 3
            };
            if ($(this).attr('checked') == 'checked') {
              cms.post('/CMSAPI/createChannelAutosharing', parameters);
            } else {
              cms.post('/CMSAPI/removeChannelAutosharing', parameters);
            }
          });
        }
        $('.right_title > div').text(channelName);
        $('#promotion_content').show();
      }, 'json');
    }
  },
  channelAndChannelSetList: {
    init: function() {
      // load channels
      cms.loadJSON('/CMSAPI/listOwnedChannels?msoId=' + $('#msoId').val(), function(channels) {
        for (i in channels) {
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
          channelInfoBlock.find('.channel_info_updatedate span').text(cms.formatDate(channels[i].updateDate));
          // add this
          var promoteUrl = 'http://' + location.host + '/view?channel=' + channelId;
          channelInfoBlock.find('.addthis_button_expanded').attr('addthis:url', promoteUrl);
          var switchObject = channelInfoBlock.find('.channel_info_publish');
          if (channels[i]['public']) {
            switchObject.removeClass('chUnPublic').addClass('chPublic');
          } else {
            switchObject.removeClass('chPublic').addClass('chUnPublic');
          }
          var promoteUrlTruncated = promoteUrl;
          if (promoteUrl.length > 40) {
            promoteUrlTruncated = promoteUrl.substring(0, 36) + '...';
          }
          channelInfoBlock.find('.channel_info_promoteurl').text(promoteUrlTruncated).attr('href', promoteUrl);
          $('<li></li>').append(channelInfoBlock).appendTo('#channel_list_ul');
          // channel info block click event
          channelInfoBlock.click({ 'channelId': channelId, 'channelName': channels[i].name}, function(event) {
            $('.channel_info_block').removeClass('chFocus').addClass('chUnFocus');
            $('.channel_info_title').removeClass('chFocusTitle').addClass('chUnFocusTitle');
            $('.channel_info_image').removeClass('chFocusImg').addClass('chUnFocusImg');
            $(this).removeClass('chUnFocus').addClass('chFocus');
            $('.channel_info_title', this).removeClass('chUnFocusTitle').addClass('chFocusTitle');
            $('.channel_info_image', this).removeClass('chUnFocusImg').addClass('chFocusImg');
            
            // load autosharing info
            page$.autosharingSettings.initChannel(event.data.channelId, event.data.channelName);
          });
        }
        //$('#channel_list').show();
        cms.initAddthis();
      });
      /*
      // load channel sets
      cms.loadJSON('/CMSAPI/listOwnedChannelSets?msoId=' + $('#msoId').val(), function(channelSets) {
        for (i in channelSets) {
          var channelSetInfoBlock = $('#channel_info_block').clone(true).removeAttr('id').addClass('channel_info_block_cloned');
          var channelSetId = channelSets[i].key.id;
          
          channelSetInfoBlock.find('.iconStatistics').attr('title', $('#lang_view_statistics').text());
          channelSetInfoBlock.find('.channel_info_title div').text(channelSets[i].name);
          $('<img/>').attr('src', channelSets[i].imageUrl).appendTo(channelSetInfoBlock.find('.channel_info_image'));
          channelSetInfoBlock.find('.channel_info_contenttype span').text($('#lang_label_channel_set').text());
          channelSetInfoBlock.find('.channel_info_programcount span').text('N/A');
          channelSetInfoBlock.find('.channel_info_subscribers span').text(channelSets[i].subscriptionCount);
          channelSetInfoBlock.find('.channel_info_updatedate span').text(cms.formatDate(channelSets[i].updateDate));
          // add this
          var promoteUrl = 'http://' + location.host + '/';
          promoteUrl += ((channelSets[i].beautifulUrl != null) ? channelSets[i].beautifulUrl : channelSets[i].defaultUrl);
          channelSetInfoBlock.find('.addthis_button_expanded').attr('addthis:url', promoteUrl);
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
            
            page$.autosharingSettings.initChannelSet(event.data.channelSetId, event.data.channelSetName);
            
          });
        }
        $('#channel_set_list_ul').append('<div class="clear"/>');
      });
      */
    }
  },
  init: function(){
    var css = '<style> .chPublic { background:url(' + $('#image_ch_public').text() + ') no-repeat; }\n.chUnPublic { background:url(' + $('#image_ch_unpublic').text() + ') no-repeat; } </style>';
    $(css).appendTo('head');
    
    $('.pro_check').click(function() {
      var confirmMessage = $('#lang_confirm_goto_setting_page').text();
      if (confirm(confirmMessage.replace('{sns}', $(this).text())) == true) {
        $('#setup').click();
      }
    });
    $('#9x9_rss_tutorial').click(function() {
      alert('9x9 rss feed was not provided');
    });
    $('.promoteInfo').hover(function() {
      $('#pro_hint').show();
    }, function() {
      $('#pro_hint').hide();
    });
    cms.post('/CMSAPI/listSnsAuth?msoId=' + $('#msoId').val(), function(snsAuths) {
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
    }, 'json');
    page$.channelAndChannelSetList.init();
  }
};

