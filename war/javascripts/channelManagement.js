/**
 * 
 */

var formatDate = function(timestamp)
{
  var updateDate = new Date(timestamp);
  var year = updateDate.getFullYear();
  var month = updateDate.getMonth();
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

var overallLayout =
{
  destroyRightSideContent: function()
  {
    // hide right side content
    channelDetail.destroy();
    programDetail.destroy();
    programList.destroy();
    $('#choose_channel_type').hide();
  }
}

var skeletonCreation =
{
  create9x9Channel: function()
  {
    $.getJSON('/CMSAPI/createChannelSkeleton?msoId=' + $('#msoId').val(), function(channelId)
    {
      $('<li/>').append('<div class="chShadow channel_info_block_cloned"><div class="chShadowTitle"></div><div class="chImg"></div></div>')
        .prependTo('#channel_list_ul');
      channelDetail.init(channelId);
    });
  },
  create9x9Program: function(channelId)
  {
    $.getJSON('/CMSAPI/createProgramSkeleton?msoId=' + $('#msoId').val(), function(programId)
    {
      programDetail.init(programId);
    });
  }
};

var programDetail =
{
  swfObjectImage: null,
  swfObjectVideo: null,
  destroy: function()
  {
    if (programDetail.swfObjectImage != null)
    {
      programDetail.swfObjectImage.destroy();
      programDetail.swfObjectImage = null;
    }
    if (programDetail.swfObjectVideo != null)
    {
      programDetail.swfObjectVideo.destroy();
      programDetail.swfObjectVideo = null;
    }
    $('#program_detail').hide();
    $('#program_detail_readonly').hide();
  },
  init: function(programId, readonly)
  {
    $.get('/CMSAPI/programInfo?programId=' + programId, function(program)
    {
      if (readonly) {
        $('#program_list_readonly').hide();
        programDetail.displayProgramReadonly(program);
      } else {
        $('#program_list').hide();
        programDetail.displayProgram(program);
      }
    });
  },
  displayProgram: function(program)
  {
    var programDetailBlock = $('#program_detail');
    var programId = program.key.id;
    programDetailBlock.find('.ep_name').val(program.name);
    programDetailBlock.find('.ep_intro').val(program.intro);
    var promoteUrl = 'http://' + location.host + '/episode/' + programId;
    programDetailBlock.find('.ep_url').attr('href', promoteUrl).text(promoteUrl);
    programDetailBlock.find('.ep_image').attr('src', program.imageUrl);
    programDetailBlock.find('.ep_createdate').text(formatDate(program.createDate));
    programDetailBlock.find('.ep_return').unbind().click(function()
    {
      programDetail.destroy();
      $('#program_list').show();
    });
    programDetailBlock.find('.ep_cancel').unbind().click(function()
    {
      programDetail.init(programId, false);
    });
    programDetailBlock.find('.ep_savebutton').unbind().click(function()
    {
      if (programDetailBlock.find('.ep_name').val == "") {
        alert('名稱不可以為空');
        return;
      }
      var parameters =
      {
        'programId': programId,
        'imageUrl':  programDetailBlock.find('.ep_image').attr('src'),
        'name':      programDetailBlock.find('.ep_name').val(),
        'intro':     programDetailBlock.find('.ep_intro').val()
      };
      $.post('/CMSAPI/saveProgram', parameters, function(response)
      {
        if (response != 'OK') {
          alert('發生錯誤');
        }else {
          alert('儲存成功');alert($('#program_list .right_title span').text());
          programList.init(program.channelId, false, $('#program_list .right_title span').text());
        }
      }, 'text');
    });
    $('#program_detail').show();
  },
  displayProgramReadonly: function(program)
  {
    var programDetailBlock = $('#program_detail_readonly');
    var programId = program.key.id;
    programDetailBlock.find('.ep_name').text(program.name);
    programDetailBlock.find('.ep_intro').text(program.intro);
    var promoteUrl = 'http://' + location.host + '/episode/' + programId;
    programDetailBlock.find('.ep_url').attr('href', promoteUrl).text(promoteUrl);
    programDetailBlock.find('.ep_image').attr('src', program.imageUrl);
    programDetailBlock.find('.ep_createdate').text(formatDate(program.createDate));
    programDetailBlock.find('.ep_return').unbind().click(function()
    {
      programDetail.destroy();
      $('#program_list_readonly').show();
    });
    var source = "N/A";
    if (program.mpeg4FileUrl != "")
      source = program.mpeg4FileUrl;
    else if (program.webMFileUrl != "")
      source = program.webMFileUrl;
    else if (program.otherFileUrl !="")
      source = program.otherFileUrl;
    else if (program.audioFileUrl != "")
      source = program.audioFileUrl;
    var source_truncated = source;
    if (source_truncated.length > 50)
      source_truncated = source_truncated.substring(0, 50) + '...';
    var dom = programDetailBlock.find('.ep_source').text(source_truncated).attr('href', source);
    $('#program_detail_readonly').show();
  }
}

var programList =
{
  destroy: function()
  {
    $('#program_list_empty').hide();
    $('.program_info_block_cloned').each(function() {
      $(this).parent().remove();
    });
    $('#program_list').hide();
  },
  init: function(channelId, readonly, channelName)
  {
    overallLayout.destroyRightSideContent();
    
    $.get('/CMSAPI/programList?channelId=' + channelId, function(programs)
    {
      if (programs.length == 0) {
        if (readonly) {
          $('#program_list_empty_readonly .right_title span').text(channelName);
          $('#program_list_empty_readonly').show();
        } else {
          $('#program_list_empty .right_title span').text(channelName);
          $('#program_list_empty').show();
          $('.create_program_button').unbind().click({ 'channelId': channelId }, function(event)
          {
            alert('create program');
          });
        }
        return;
      }
      if (readonly) {
        $('#program_list_readonly .right_title span').text(channelName);
        programList.displayProgramListReadonly(programs);
      } else {
        $('#program_list .right_title span').text(channelName);
        programList.displayProgramList(programs);
      }
    });
    
  },
  displayProgramList: function(programs)
  {
    for (i in programs)
    {
      var programInfoBlock = $('#program_info_block').clone(true).removeAttr('id').addClass('program_info_block_cloned');
      var programId = programs[i].key.id;
      
      programInfoBlock.find('.program_info_title span').text(programs[i].name);
      $('<img/>').attr('src', programs[i].imageUrl).appendTo(programInfoBlock.find('.program_info_image'));
      var type = 'Unknown';
      switch(programs[i].type) {
        case 1:
        type = 'Video';
        break;
        case 2:
        type = 'Audio';
        break;
        default:
      }
      programInfoBlock.find('.program_info_type span').text(type);
      programInfoBlock.find('.program_info_updatedate span').text(formatDate(programs[i].updateDate));
      // add this
      var promoteUrl = 'http://' + location.host + '/episode/' + programId;
      var addthis_share =
      {
        'title': programs[i].name,
        'description': programs[i].intro,
        'url': promoteUrl
      }
      addthis_config['pubid'] = $('#msoId').val();
      addthis.button(programInfoBlock.find('.program_info_addthis').get(0), null, addthis_share);
      var switchObject = programInfoBlock.find('.program_info_publish');
      if (programs[i]['public']) {
        switchObject.removeClass('chUnPublic').addClass('chPublic');
      } else {
        switchObject.removeClass('chPublic').addClass('chUnPublic');
      }
      programInfoBlock.find('.program_info_removebutton').click({ 'programId': programId, 'programInfoBlock': programInfoBlock }, function(event)
      {
        if (confirm('你確定要將節目移除嗎？') == false)
          return false;
        var parameters = {
          'programId': event.data.programId
        };
        $.post('/CMSAPI/removeProgram', parameters, function()
        {
          event.data.programInfoBlock.parent().remove();
        });
        return false;
      });
      programInfoBlock.find('.program_info_publish').click({ 'programId': programId, 'switchObject': switchObject }, function(event)
      {
        $.getJSON('/CMSAPI/switchProgramPublicity?programId=' + event.data.programId, function(response)
        {
          if (response) {
            event.data.switchObject.removeClass('chUnPublic').addClass('chPublic');
          } else {
            event.data.switchObject.removeClass('chPublic').addClass('chUnPublic');
          }
        });
        return false;
      });
      programInfoBlock.find('.program_info_detailbutton').click({ 'programId': programId }, function(event)
      {
        programDetail.init(event.data.programId, true);
        return false;
      });
      programInfoBlock.find('.program_info_promoteurl').text(promoteUrl).attr('href', promoteUrl);
      $('<li></li>').append(programInfoBlock).appendTo('#program_list_ul');
      programInfoBlock.click({ 'programId': programId }, function(event)
      {
        $('.program_info_block').removeClass('epItemFocus').addClass('epItem');
        $('.program_info_title').removeClass('epItemFocusTitle').addClass('epInfoTitle');
        $(this).removeClass('epItem').addClass('epItemFocus');
        $('.program_info_title', this).removeClass('epInfoTitle').addClass('epItemFocusTitle');
      });
    }
    $('#program_list').show();
  },
  displayProgramListReadonly: function(programs)
  {
    for (i in programs)
    {
      var programInfoBlock = $('#program_info_block_readonly').clone(true).removeAttr('id').addClass('program_info_block_cloned');
      var programId = programs[i].key.id;
      
      programInfoBlock.find('.program_info_title span').text(programs[i].name);
      $('<img/>').attr('src', programs[i].imageUrl).appendTo(programInfoBlock.find('.program_info_image'));
      var type = 'Unknown';
      switch(programs[i].type) {
        case 1:
        type = 'Video';
        break;
        case 2:
        type = 'Audio';
        break;
        default:
      }
      programInfoBlock.find('.program_info_type span').text(type);
      programInfoBlock.find('.program_info_updatedate span').text(formatDate(programs[i].updateDate));
      // add this
      var promoteUrl = 'http://' + location.host + '/episode/' + programId;
      var addthis_share =
      {
        'title': programs[i].name,
        'description': programs[i].intro,
        'url': promoteUrl
      }
      addthis_config['pubid'] = $('#msoId').val();
      addthis.button(programInfoBlock.find('.program_info_addthis').get(0), null, addthis_share);
      var switchObject = programInfoBlock.find('.program_info_publish');
      if (programs[i]['public']) {
        switchObject.removeClass('chUnPublic').addClass('chPublic');
      } else {
        switchObject.removeClass('chPublic').addClass('chUnPublic');
      }
      /*
      programInfoBlock.find('.program_info_removebutton').click({ 'programId': programId, 'programInfoBlock': programInfoBlock }, function(event)
      {
        if (confirm('你確定要將頻道移除嗎？') == false)
          return false;
        var parameters = {
          'programId': event.data.programId,
          'msoId': $('#msoId').val()
        };
        $.post('/CMSAPI/removeChannelFromList', parameters, function()
        {
          event.data.programInfoBlock.parent().remove();
          overallLayout.destroyRightSideContent();
        });
        return false;
      });
      programInfoBlock.find('.program_info_publish').click({ 'programId': programId, 'switchObject': switchObject }, function(event)
      {
        $.getJSON('/CMSAPI/switchChannelPublicity?programId=' + event.data.programId, function(response)
        {
          if (response) {
            event.data.switchObject.removeClass('chUnPublic').addClass('chPublic');
          } else {
            event.data.switchObject.removeClass('chPublic').addClass('chUnPublic');
          }
        });
        return false;
      });
      */
      programInfoBlock.find('.program_info_detailbutton').click({ 'programId': programId }, function(event)
      {
        programDetail.init(event.data.programId, true);
        return false;
      });
      programInfoBlock.find('.program_info_promoteurl').text(promoteUrl).attr('href', promoteUrl);
      $('<li></li>').append(programInfoBlock).appendTo('#program_list_ul_readonly');
      programInfoBlock.click({ 'programId': programId }, function(event)
      {
        $('.program_info_block').removeClass('epItemFocus').addClass('epItem');
        $('.program_info_title').removeClass('epItemFocusTitle').addClass('epInfoTitle');
        $(this).removeClass('epItem').addClass('epItemFocus');
        $('.program_info_title', this).removeClass('epInfoTitle').addClass('epItemFocusTitle');
      });
    }
    $('#program_list_readonly').show();
  }
};

var channelDetail =
{
  swfObject: null,
  destroy: function()
  {
    if (channelDetail.swfObject != null)
    {
      channelDetail.swfObject.destroy();
      channelDetail.swfObject = null;
    }
    $('#channel_detail').hide();
  },
  init: function(channelId)
  {
    overallLayout.destroyRightSideContent();
    
    $.getJSON('/CMSAPI/channelInfo?channelId=' + channelId, function(channel)
    {
      $('#ch_id').val(channel.key.id);
      $('#ch_name').val('').val(channel.name);
      $('#ch_image').attr('src', channel.imageUrl);
      $('#ch_intro').val('').val(channel.intro);
      $('#ch_tag').val('');
      if (channelDetail.swfObject != null) {
        channelDetail.swfObject.destroy();
      }
      $('<spane/>').attr('id', 'ch_upload_image').appendTo('#upload_button_place');
      var swfupload_settings =
      {
        flash_url:          '/javascripts/swfupload/swfupload.swf',
        upload_url:         'http://9x9tmp.s3.amazonaws.com/',
        file_size_limit:    '10240',
        file_types:         '*.jpg;*.png',
        file_types_description: 'Image Files',
        file_post_name:     'file',
        button_placeholder: document.getElementById('ch_upload_image'),
        button_action:       SWFUpload.BUTTON_ACTION.SELECT_FILE,
        button_image_url:   '/images/cms/btn_upload.png',
        button_width:       '95',
        button_height:      '32',
        button_cursor:      SWFUpload.CURSOR.HAND,
        debug:              false,
        http_success :      [201],
        upload_success_handler: function(file, serverData, recievedResponse)
        {
          $('#ch_uploading').hide();
          $('#ch_image').attr('src', 'http://9x9tmp.s3.amazonaws.com/' + 'ch_logo_' + channel.key.id + file.type);
        },
        upload_error_handler: function(file, code, message)
        {
          $('#uploading').hide();
          alert('error: ' + message);
        },
        file_queued_handler: function(file)
        {
          var post_params =
          {
            "AWSAccessKeyId": $('#s3_id').val(),
            "key":            'ch_logo_' + channel.key.id + file.type,
            "acl":            "public-read",
            "policy":         $('#s3_policy').val(),
            "signature":      $('#s3_signature').val(),
            "content-type":   (file.type == '.jpg') ? "image/jpeg" : "image/png",
            "success_action_status": "201"
          };
          this.setPostParams(post_params);
          this.startUpload(file.id);
          $('#ch_uploading').show();
        }
      };
      channelDetail.swfObject = new SWFUpload(swfupload_settings);
      $('#channel_detail').show();
    });
    $.getJSON('/CMSAPI/systemCategories', function(categories)
    {
      $('#channel_detail .sys_directory').html('<option value="0">請選擇分類</option>');
      for (i in categories)
      {
        $('<option></option>')
            .attr('value', categories[i].key.id)
            .text(categories[i].name)
            .appendTo('#channel_detail .sys_directory');
      }
      $.getJSON('/CMSAPI/channelCategory?channelId=' + channelId, function(category)
      {
        if (category != null) {
          $('#channel_detail .sys_directory').val(category.key.id);
        }
      });
    });
    $('#channel_detail_cancel').unbind().click(function()
    {
      channelDetail.init(channelId);
    });
    $('#channel_detail_savebutton').unbind().click(function()
    {
      if ($('#ch_name').val() == "") {
        alert('名稱不可以為空');
        return;
      }
      if ($('#ch_category').val() == 0) {
        alert('請選擇一個系統分類');
        return;
      }
      var parameters =
      {
        'channelId':  $('#ch_id').val(),
        'imageUrl':   $('#ch_image').attr('src'),
        'name':       $('#ch_name').val(),
        'intro':      $('#ch_intro').val(),
        'tag':        $('#ch_tag').val(),
        'categoryId': $('#ch_category').val()
      };
      $.post('/CMSAPI/saveChannel', parameters, function(response)
      {
        if (response != 'OK') {
          alert('發生錯誤');
        }else {
          alert('儲存成功');
          channelList.init();
        }
      }, 'text');
    });
  }
}

var channelList =
{
  destroy: function()
  {
    $('.channel_info_block_cloned').each(function()
    {
      $(this).parent().remove();
    });
  },
  init: function()
  {
    channelList.destroy();
    
    // load channels
    $.getJSON('/CMSAPI/listOwnedChannels?msoId=' + $('#msoId').val(), function(channels)
    {
      if (channels.length == 0) {
        $('#channel_list_empty').show();
        return;
      }
      for (i in channels)
      {
        var channelInfoBlock = $('#channel_info_block').clone(true).removeAttr('id').addClass('channel_info_block_cloned');
        var channelId = channels[i].key.id;
        
        channelInfoBlock.find('.channel_info_title span').text(channels[i].name);
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
        addthis.button(channelInfoBlock.find('.channel_info_addthis').get(0), null, addthis_share);
        var switchObject = channelInfoBlock.find('.channel_info_publish');
        if (channels[i]['public']) {
          switchObject.removeClass('chUnPublic').addClass('chPublic');
        } else {
          switchObject.removeClass('chPublic').addClass('chUnPublic');
        }
        channelInfoBlock.find('.channel_info_removebutton').click({ 'channelId': channelId, 'channelInfoBlock': channelInfoBlock }, function(event)
        {
          if (confirm('你確定要將頻道移除嗎？') == false)
            return false;
          var parameters = {
            'channelId': event.data.channelId,
            'msoId': $('#msoId').val()
          };
          $.post('/CMSAPI/removeChannelFromList', parameters, function()
          {
            event.data.channelInfoBlock.parent().remove();
            overallLayout.destroyRightSideContent();
          });
          return false;
        });
        channelInfoBlock.find('.channel_info_publish').click({ 'channelId': channelId, 'switchObject': switchObject }, function(event)
        {
          $.getJSON('/CMSAPI/switchChannelPublicity?channelId=' + event.data.channelId, function(response)
          {
            if (response) {
              event.data.switchObject.removeClass('chUnPublic').addClass('chPublic');
            } else {
              event.data.switchObject.removeClass('chPublic').addClass('chUnPublic');
            }
          });
          return false;
        });
        channelInfoBlock.find('.channel_info_detailbutton').click({ 'channelId': channelId }, function(event)
        {
          channelDetail.init(event.data.channelId);
          return false;
        });
        channelInfoBlock.find('.channel_info_promoteurl').text(promoteUrl).attr('href', promoteUrl);
        $('<li></li>').append(channelInfoBlock).appendTo('#channel_list_ul');
        // channel info block click event
        if (channels[i].contentType == 6)
          var readonly = false;
        else
          var readonly = true;
        channelInfoBlock.click({ 'channelId': channelId, 'readonly':  readonly, 'channelName': channels[i].name}, function(event)
        {
          $('.channel_info_block').removeClass('chFocus').addClass('chUnFocus');
          $('.channel_info_title').removeClass('chFocusTitle').addClass('chUnFocusTitle');
          $('.channel_info_image').removeClass('chFocusImg').addClass('chUnFocusImg');
          $(this).removeClass('chUnFocus').addClass('chFocus');
          $('.channel_info_title', this).removeClass('chUnFocusTitle').addClass('chFocusTitle');
          $('.channel_info_image', this).removeClass('chUnFocusImg').addClass('chFocusImg');
          programList.init(event.data.channelId, event.data.readonly, event.data.channelName);
        });
      }
      $('#channel_list').show();
    });
    $('.create_channel_button').unbind().click(function()
    {
      overallLayout.destroyRightSideContent();
      $('#create_9x9_channel_button').unbind().click(skeletonCreation.create9x9Channel);
      $('#choose_channel_type').show();
    });
  }
}

$(function()
{
  channelList.init();
});
