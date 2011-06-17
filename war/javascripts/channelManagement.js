/**
 * 
 */

var overallLayout =
{
  destroyRightSideContent: function(cleanup)
  {
    if (cleanup == false) {
      if ($('.program_create_detail_block_cloned').size() > 0) {
        if (confirm($('#lang_confirm_leaving_program_creation').text()) == false) {
          return false;
        }
      }
    }
    // hide right side content
    channelDetail.destroy();
    programDetail.destroy();
    programList.destroy();
    $('#choose_channel_type').hide();
    return true;
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
  create9x9Program: function(channelId, channelName)
  {
    $.getJSON('/CMSAPI/createProgramSkeleton', function(programId)
    {
      if (programId != null)
        programDetail.programCreation(programId, channelId, channelName);
    });
  }
};

var programDetail =
{
  swfObjectImage: [],
  swfObjectVideo: [],
  destroy: function()
  {
    for (i in programDetail.swfObjectImage)
    {
      programDetail.swfObjectImage[i].destroy();
    }
    for (i in programDetail.swfObjectVideo)
    {
      programDetail.swfObjectVideo[i].destroy();
    }
    programDetail.swfObjectVideo = [];
    programDetail.swfObjectImage = [];
    $('#program_detail').hide();
    $('#program_detail_readonly').hide();
    $('#program_create_detail').hide();
    $('.program_create_detail_block_cloned').each(function()
    {
      $(this).parent().remove();
    });
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
    var title = program.name;
    if (title.length > 20) {
      title = title.substring(0, 20) + '...';
    }
    var source = "javascript:";
    if (program.mpeg4FileUrl != null)
      source = program.mpeg4FileUrl;
    else if (program.webMFileUrl != null)
      source = program.webMFileUrl;
    else if (program.otherFileUrl != null)
      source = program.otherFileUrl;
    else if (program.audioFileUrl != null)
      source = program.audioFileUrl;
    programDetailBlock.find('.ep_source a').attr('href', source);
    programDetailBlock.find('.right_title div').text(title);
    programDetailBlock.find('.ep_name').val(program.name);
    programDetailBlock.find('.ep_intro').val(program.intro);
    var promoteUrl = 'http://' + location.host + '/episode/' + programId;
    programDetailBlock.find('.ep_url').attr('href', promoteUrl).text(promoteUrl);
    programDetailBlock.find('.ep_image').attr('src', program.imageUrl);
    programDetailBlock.find('.ep_image_updated').val('false');
    programDetailBlock.find('.ep_createdate').text(formatDate(program.createDate));
    programDetailBlock.find('.ep_return').unbind().click(function()
    {
      programDetail.destroy();
      $('#program_list').show();
    });
    programDetailBlock.find('.ep_cancel').unbind().click(function()
    {
      programDetail.destroy();
      $('#program_list').show();
    });
    programDetailBlock.find('.ep_savebutton').unbind().click(function()
    {
      if (programDetailBlock.find('.ep_name').val() == "") {
        alert($('#lang_warning_empty_name').text());
        return;
      }
      var parameters =
      {
        'programId': programId,
        //'imageUrl':  programDetailBlock.find('.ep_image').attr('src'),
        'name':      programDetailBlock.find('.ep_name').val(),
        'intro':     programDetailBlock.find('.ep_intro').val()
      };
      if (programDetailBlock.find('.ep_image_updated').val() == 'true') {
        parameters['imageUrl'] = programDetailBlock.find('.ep_image').attr('src');
      }
      $.post('/CMSAPI/saveProgram', parameters, function(response)
      {
        if (response != 'OK') {
          alert($('#lang_warning_error_occurs').text());
        }else {
          alert($('#lang_update_successfully').text());
          programList.init(program.channelId, false, $('#program_list .right_title div').text());
        }
      }, 'text');
    });
    $('<span/>').addClass('ep_upload_image').appendTo(programDetailBlock.find('.upload_button_place'));
    var swfupload_settings =
    {
      flash_url:          '/javascripts/swfupload/swfupload.swf',
      upload_url:         'http://9x9tmp.s3.amazonaws.com/',
      file_size_limit:    '10240',
      file_types:         '*.jpg;*.png',
      file_types_description: 'Image Files',
      file_post_name:     'file',
      button_placeholder: programDetailBlock.find('.ep_upload_image').get(0),
      button_action:      SWFUpload.BUTTON_ACTION.SELECT_FILE,
      button_image_url:   '/images/cms/btn_upload.png',
      button_width:       '95',
      button_height:      '32',
      button_cursor:      SWFUpload.CURSOR.HAND,
      debug:              false,
      http_success :      [201],
      upload_success_handler: function(file, serverData, recievedResponse)
      {
        programDetailBlock.find('.ep_uploading_image').hide();
        programDetailBlock.find('.ep_image').attr('src', 'http://9x9tmp.s3.amazonaws.com/prog_logo_' + programId + '_' + file.size + file.type);
        programDetailBlock.find('.ep_image_updated').val('true');
      },
      upload_error_handler: function(file, code, message)
      {
        programDetailBlock.find('.ep_uploading_image').hide();
        alert('error: ' + message);
      },
      file_queued_handler: function(file)
      {
        var post_params =
        {
          "AWSAccessKeyId": $('#s3_id').val(),
          "key":            'prog_logo_' + programId + '_' + file.size + file.type,
          "acl":            "public-read",
          "policy":         $('#s3_policy').val(),
          "signature":      $('#s3_signature').val(),
          "content-type":   (file.type == '.jpg') ? "image/jpeg" : "image/png",
          "success_action_status": "201"
        };
        this.setPostParams(post_params);
        this.startUpload(file.id);
        programDetailBlock.find('.ep_uploading_image').show();
      }
    };
    var swfObjectImage = new SWFUpload(swfupload_settings);
    programDetail.swfObjectImage.push(swfObjectImage);
    $('#program_detail').show();
  },
  displayProgramReadonly: function(program)
  {
    var programDetailBlock = $('#program_detail_readonly');
    var programId = program.key.id;
    var title = program.name;
    if (title.length > 20) {
      title = title.substring(0, 20) + '...';
    }
    programDetailBlock.find('.right_title div').text(title);
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
    if (program.mpeg4FileUrl != null)
      source = program.mpeg4FileUrl;
    else if (program.webMFileUrl != null)
      source = program.webMFileUrl;
    else if (program.otherFileUrl != null)
      source = program.otherFileUrl;
    else if (program.audioFileUrl != null)
      source = program.audioFileUrl;
    var source_truncated = source;
    if (source_truncated.length > 50)
      source_truncated = source_truncated.substring(0, 50) + '...';
    var dom = programDetailBlock.find('.ep_source').text(source_truncated).attr('href', source);
    $('#program_list_readonly').hide();
    $('#program_detail_readonly').show();
  },
  programCreation: function(programId, channelId, channelName)
  {
    $('#program_create_detail .right_title div').text(channelName);
    $('#program_create_detail .ep_return').unbind().click(function()
    {
      programList.init(channelId, false, channelName);
    });
    $('#continue_add_new_program_button').unbind().click(function()
    {
      skeletonCreation.create9x9Program(channelId, channelName);
    });
    $.get('/CMSAPI/programInfo?programId=' + programId, function(program)
    {
      var programDetailBlock = $('#program_create_detail_block')
                                 .clone(true)
                                 .removeAttr('id')
                                 .addClass('program_create_detail_block_cloned');
      var programId = program.key.id;
      
      programDetailBlock.find('.ep_name').val(program.name);
      programDetailBlock.find('.ep_image').attr('src', program.imageUrl);
      programDetailBlock.find('.ep_image_updated').val('false');
      programDetailBlock.find('.ep_intro').val(program.intro);
      programDetailBlock.find('.ep_urlbutton').click(function()
      {
        programDetailBlock.find('.ep_url_block').show();
      });
      programDetailBlock.find('.ep_url_cancel').click(function()
      {
        programDetailBlock.find('.ep_url_input').val('');
        programDetailBlock.find('.ep_url_block').hide();
        programDetailBlock.find('.ep_savebutton').unbind().addClass('btnDisable').removeClass('btnSave');
      });
      programDetailBlock.find('.ep_url_input, .ep_name').focusout(function()
      {
        if (programDetailBlock.find('.ep_url_input').val().length > 0
            && programDetailBlock.find('.ep_name').val().length > 0) {
          programDetailBlock
            .find('.ep_savebutton')
            .unbind()
            .removeClass('btnDisable')
            .addClass('btnSave')
            .click(function()
            {
              if (programDetailBlock.find('.ep_name').val() == '') {
                alert($('#lang_warning_empty_name').text());
                return;
              }
              var parameters =
              {
                'channelId': channelId,
                'programId': programId,
                'sourceUrl': programDetailBlock.find('.ep_url_input').val(),
                //'imageUrl':  programDetailBlock.find('.ep_image').attr('src'),
                'name':      programDetailBlock.find('.ep_name').val(),
                'intro':     programDetailBlock.find('.ep_intro').val()
              };
              if (programDetailBlock.find('.ep_image_updated').val() == 'true') {
                parameters['imageUrl'] = programDetailBlock.find('.ep_image').attr('src');
              }
              $.post('/CMSAPI/saveNewProgram', parameters, function(response)
              {
                if (response == 'OK') {
                  alert($('#lang_update_successfully').text());
                  programDetailBlock.parent().remove();
                  if ($('.program_create_detail_block_cloned').size() == 0) {
                    programList.init(channelId, false, channelName);
                  }
                } else {
                  alert($('#lang_warning_error_occurs').text());
                }
              }, 'text');
            });
        } else {
          programDetailBlock.find('.ep_savebutton').unbind().addClass('btnDisable').removeClass('btnSave');
        }
      });
      $('#program_create_detail').show();
      $('<li></li>').append(programDetailBlock).appendTo('#program_create_ul');
      // video uploading
      var swfupload_settings =
      {
        flash_url:          '/javascripts/swfupload/swfupload.swf',
        upload_url:         'http://9x9tmp.s3.amazonaws.com/',
        file_size_limit:    '1024000',
        file_types:         '*.m4v;*.mp4;*.mpg;*.mpeg;*.mov;*.webm;*.rm;*.rmvb;*.avi;*.wmv;*.flv;*.ogg',
        file_types_description: 'Video Files',
        file_post_name:     'file',
        button_placeholder: programDetailBlock.find('.ep_upload_video').get(0),
        button_action:      SWFUpload.BUTTON_ACTION.SELECT_FILE,
        button_image_url:   '/images/cms/btn_upload.png',
        button_width:       '95',
        button_height:      '32',
        button_text:        '從硬碟',
        button_cursor:      SWFUpload.CURSOR.HAND,
        debug:              false,
        http_success :      [201],
        upload_progress_handler: function(file, completed, total)
        {
          programDetailBlock.find('.ep_uploading_video div').progressbar('value', (completed * 100) / total);
        },
        upload_success_handler: function(file, serverData, recievedResponse)
        {
          programDetailBlock.find('.ep_uploading_video div').progressbar('destroy').text($('#lang_upload_finished').text()).show();
          programDetailBlock.find('.ep_url_input')
            .val('http://9x9tmp.s3.amazonaws.com/' + 'prog_video_' + programId + file.type)
            .focusout();
        },
        upload_error_handler: function(file, code, message)
        {
          programDetailBlock.find('.ep_uploading_video div').progressbar('destroy').text($('#lang_upload_failed').text()).show();
          alert('error: ' + message);
        },
        file_queued_handler: function(file)
        {
          var post_params =
          {
            "AWSAccessKeyId": $('#s3_id').val(),
            "key":            'prog_video_' + programId + file.type,
            "acl":            "public-read",
            "policy":         $('#s3_policy').val(),
            "signature":      $('#s3_signature').val(),
            "content-type":   getContentTypeByFileExtention(file.type),
            "success_action_status": "201"
          };
          this.setPostParams(post_params);
          this.startUpload(file.id);
          programDetailBlock.find('.ep_uploading_video div').text('').show().progressbar();
          programDetailBlock.find('.ep_url_block').hide();
        }
      };
      var swfObject = new SWFUpload(swfupload_settings);
      programDetail.swfObjectVideo.push(swfObject);
      // logo uploading
      var swfupload_settings =
      {
        flash_url:          '/javascripts/swfupload/swfupload.swf',
        upload_url:         'http://9x9tmp.s3.amazonaws.com/',
        file_size_limit:    '10240',
        file_types:         '*.jpg;*.png',
        file_types_description: 'Image Files',
        file_post_name:     'file',
        button_placeholder: programDetailBlock.find('.ep_upload_image').get(0),
        button_action:      SWFUpload.BUTTON_ACTION.SELECT_FILE,
        button_image_url:   '/images/cms/btn_upload.png',
        button_width:       '95',
        button_height:      '32',
        button_cursor:      SWFUpload.CURSOR.HAND,
        debug:              false,
        http_success :      [201],
        upload_success_handler: function(file, serverData, recievedResponse)
        {
          programDetailBlock.find('.ep_uploading_image').hide();
          programDetailBlock.find('.ep_image').attr('src', 'http://9x9tmp.s3.amazonaws.com/prog_logo_' + programId + '_' + file.size + file.type);
          programDetailBlock.find('.ep_image_updated').val('true');
        },
        upload_error_handler: function(file, code, message)
        {
          programDetailBlock.find('.ep_uploading_image').hide();
          alert('error: ' + message);
        },
        file_queued_handler: function(file)
        {
          var post_params =
          {
            "AWSAccessKeyId": $('#s3_id').val(),
            "key":            'prog_logo_' + programId + '_' + file.size + file.type,
            "acl":            "public-read",
            "policy":         $('#s3_policy').val(),
            "signature":      $('#s3_signature').val(),
            "content-type":   (file.type == '.jpg') ? "image/jpeg" : "image/png",
            "success_action_status": "201"
          };
          this.setPostParams(post_params);
          this.startUpload(file.id);
          programDetailBlock.find('.ep_uploading_image').show();
        }
      };
      var swfObjectImage = new SWFUpload(swfupload_settings);
      programDetail.swfObjectImage.push(swfObjectImage);
      programDetailBlock.find('.ep_cancelbutton').click(function()
      {
        if (confirm($('#lang_confirm_cancel').text()) == false)
          return;
        swfObjectImage.destroy();
        swfObject.destroy();
        programDetailBlock.parent().remove();
        if ($('.program_create_detail_block_cloned').size() == 0) {
          programList.init(channelId, false, channelName);
        }
      });
    });
    
  }
}

var programList =
{
  destroy: function()
  {
    $('#program_list_empty').hide();
    $('#program_list_empty_readonly').hide();
    $('.program_info_block_cloned').each(function() {
      $(this).parent().remove();
    });
    $('#program_list').hide();
    $('#program_list_readonly').hide();
  },
  init: function(channelId, readonly, channelName)
  {
    if (overallLayout.destroyRightSideContent(false) == false) return false;
    
    $.get('/CMSAPI/programList?channelId=' + channelId, function(programs)
    {
      $('.create_program_button').unbind().click({ 'channelId': channelId, 'channelName': channelName }, function(event)
      {
        if (overallLayout.destroyRightSideContent(false) == false) return false;
        skeletonCreation.create9x9Program(event.data.channelId, event.data.channelName);
      });
      if (programs.length == 0) {
        if (readonly) {
          $('#program_list_empty_readonly .right_title div').text(channelName);
          $('#program_list_empty_readonly').show();
        } else {
          $('#program_list_empty .right_title div').text(channelName);
          $('#program_list_empty').show();
        }
        return;
      }
      if (readonly) {
        $('#program_list_readonly .right_title div').text(channelName);
        programList.displayProgramListReadonly(programs);
      } else {
        $('#program_list .right_title div').text(channelName);
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
      
      programInfoBlock.find('.iconStatistics').attr('title', $('#lang_view_statistics'));
      programInfoBlock.find('.program_info_title div').text(programs[i].name);
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
      addthis_config['ui_click'] = true;
      addthis.button(programInfoBlock.find('.program_info_addthis').get(0), null, addthis_share);
      var switchObject = programInfoBlock.find('.program_info_publish');
      if (programs[i]['public']) {
        switchObject.removeClass('chUnPublic').addClass('chPublic');
      } else {
        switchObject.removeClass('chPublic').addClass('chUnPublic');
      }
      programInfoBlock.find('.program_info_removebutton').click({ 'programId': programId, 'programName': programs[i].name, 'programInfoBlock': programInfoBlock }, function(event)
      {
        var confirmation = $('#lang_confirm_removing_program').text().replace('{programName}', event.data.programName);
        if (confirm(confirmation) == false)
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
        programDetail.init(event.data.programId, false);
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
      
      programInfoBlock.find('.iconStatistics').attr('title', $('#lang_view_statistics'));
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
    $('#channel_import_detail').hide();
    $('#channel_import_detail [name="ch_import_url"]').val('');
    $('#channel_import_detail [name="ch_name"]').val('');
    $('#channel_import_detail [name="ch_intro"]').val('');
    $('#channel_import_detail [name="ch_image"]').attr('src', '/images/cms/upload_img.jpg');
    $('#channel_import_detail [name="ch_image_updated"]').val('false');
  },
  displayImportDetail: function()
  {
    if (overallLayout.destroyRightSideContent(false) == false) return false;
    
    $.getJSON('/CMSAPI/systemCategories', function(categories)
    {
      $('#channel_import_detail .sys_directory').html('<option value="0">請選擇分類</option>');
      for (i in categories)
      {
        $('<option></option>')
            .attr('value', categories[i].key.id)
            .text(categories[i].name)
            .appendTo('#channel_import_detail .sys_directory');
      }
    });
    $('#channel_import_detail [name="ch_import_button"]').click(function()
    {
      if ($('#channel_import_detail [name="ch_import_url"]').val() == "") {
        alert($('#lang_channel_source_is_empty').text());
        return;
      }
      var sourceUrl = $('#channel_import_detail [name="ch_import_url"]').val();
      var parameters = {
        'sourceUrl': sourceUrl
      }
      $.post('/CMSAPI/importChannelByUrl', parameters, function(channel)
      {
        if (channel == null) {
          alert($('#lang_channel_source_is_wrong').text());
          $('#channel_import_detail [name="ch_import_url"]').focus();
          return;
        }
        var channelId = channel.key.id;
        $('#channel_import_detail [name="ch_import_url"]').val(channel.sourceUrl);
        $('#channel_import_detail [name="upload_button_place"]').html('').append($('<span/>').attr('name', 'ch_upload_image'));
        if (channelDetail.swfObject != null) {
          channelDetail.swfObject.destroy();
        }
        var swfupload_settings =
        {
          flash_url:          '/javascripts/swfupload/swfupload.swf',
          upload_url:         'http://9x9tmp.s3.amazonaws.com/',
          file_size_limit:    '10240',
          file_types:         '*.jpg;*.png',
          file_types_description: 'Image Files',
          file_post_name:     'file',
          button_placeholder: $('#channel_import_detail [name="ch_upload_image"]').get(0),
          button_action:       SWFUpload.BUTTON_ACTION.SELECT_FILE,
          button_image_url:   '/images/cms/btn_upload.png',
          button_width:       '95',
          button_height:      '32',
          button_cursor:      SWFUpload.CURSOR.HAND,
          debug:              false,
          http_success :      [201],
          upload_success_handler: function(file, serverData, recievedResponse)
          {
            $('#channel_import_detail [name="ch_uploading_image"]').hide();
            $('#channel_import_detail [name="ch_image"]').attr('src', 'http://9x9tmp.s3.amazonaws.com/' + 'ch_logo_' + channel.key.id + '_' + file.size + file.type);
            $('#channel_import_detail [name="ch_image_updated"]').val('true');
            },
          upload_error_handler: function(file, code, message)
          {
            $('#channel_import_detail [name="ch_uploading_image"]').hide();
            alert('error: ' + message);
          },
          file_queued_handler: function(file)
          {
            var post_params =
            {
              "AWSAccessKeyId": $('#s3_id').val(),
              "key":            'ch_logo_' + channel.key.id + '_' + file.size + file.type,
              "acl":            "public-read",
              "policy":         $('#s3_policy').val(),
              "signature":      $('#s3_signature').val(),
              "content-type":   (file.type == '.jpg') ? "image/jpeg" : "image/png",
              "success_action_status": "201"
            };
            this.setPostParams(post_params);
            this.startUpload(file.id);
            $('#channel_import_detail [name="ch_uploading_image"]').show();
          }
        };
        channelDetail.swfObject = new SWFUpload(swfupload_settings);
        $('#channel_import_detail [name="ch_name"]').val(channel.name);
        $('#channel_import_detail [name="ch_intro"]').val(channel.intro);
        $('#channel_import_detail [name="ch_image"]').attr('src', channel.imageUrl);
        $('#channel_import_detail [name="ch_image_updated"]').val('false');
        $.getJSON('/CMSAPI/channelCategory?channelId=' + channelId, function(category)
        {
          if (category != null) {
            $('#channel_import_detail [name="ch_category"]').val(category.key.id);
          }
        });
        $('#channel_import_detail [name="ch_savebutton"]').unbind().click(function()
        {
          var name = $('#channel_import_detail [name="ch_name"]').val();
          if (name == '') {
            alert($('#lang_warning_empty_name').text());
            return;
          }
          var category = $('#channel_import_detail [name="ch_category"]').val();
          if (category == 0) {
            alert($('#lang_warning_select_category').text());
            return;
          }
          var intro = $('#channel_import_detail [name="ch_intro"]').val();
          var imageUrl = $('#channel_import_detail [name="ch_image"]').attr('src');
          var imageUpdated = $('#channel_import_detail [name="ch_image_updated"]').val();
          var sourceUrl = $('#channel_import_detail [name="ch_import_url"]').val();
          var parameters =
          {
            'name':       name,
            'intro':      intro,
            //'imageUrl':   imageUrl,
            'categoryId': category,
            'tag':        '',
            'msoId':      $('#msoId').val(),
            'sourceUrl':  sourceUrl
          };
          if (imageUpdated == 'true') {
            parameters['imageUrl'] = imageUrl;
          }
          $.post('/CMSAPI/addChannelByUrl', parameters, function(response)
          {
            if (response != 'OK') {
              alert($('#lang_warning_error_occurs').text());
            } else {
              alert($('#lang_update_successfully').text());
              overallLayout.destroyRightSideContent(true);
              channelList.init();
            }
          }, 'text');
        });
      }, 'json');
    });
    $('#channel_import_detail [name="ch_savebutton"]').unbind().click(function()
    {
      alert($('#lang_warning_import_channel_source').text());
    });
    $('#channel_import_detail [name="ch_cancelbutton"]').unbind().click(function()
    {
      channelDetail.displayImportDetail();
    });
    $('#channel_import_detail').show();
  },
  init: function(channelId)
  {
    if (overallLayout.destroyRightSideContent(false) == false) return false;
    
    $.getJSON('/CMSAPI/channelInfo?channelId=' + channelId, function(channel)
    {
      $('#ch_id').val(channel.key.id);
      $('#ch_name').val('').val(channel.name);
      $('#ch_image').attr('src', channel.imageUrl);
      $('#ch_image_updated').val('false');
      $('#ch_intro').val('').val(channel.intro);
      $('#ch_tag').val(channel.tags);
      if (channelDetail.swfObject != null) {
        channelDetail.swfObject.destroy();
      }
      $('<span/>').attr('id', 'ch_upload_image').appendTo('#upload_button_place');
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
          var imageUrl = 'http://9x9tmp.s3.amazonaws.com/' + 'ch_logo_' + channel.key.id + '_' + file.size + file.type;
          $('#ch_image').attr('src', imageUrl);
          $('#ch_image_updated').val('true');
        },
        upload_error_handler: function(file, code, message)
        {
          $('#ch_uploading').hide();
          alert('error: ' + message);
        },
        file_queued_handler: function(file)
        {
          var post_params =
          {
            "AWSAccessKeyId": $('#s3_id').val(),
            "key":            'ch_logo_' + channel.key.id + '_' + file.size + file.type,
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
      var select_category = $('#lang_select_category').text();
      $('#channel_detail .sys_directory').html('<option value="0">' + select_category + '</option>');
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
        alert($('#lang_warning_empty_name').text());
        return;
      }
      if ($('#ch_category').val() == 0) {
        alert($('#lang_warning_select_category').text());
        return;
      }
      var parameters =
      {
        'channelId':  $('#ch_id').val(),
        //'imageUrl':   $('#ch_image').attr('src'),
        'name':       $('#ch_name').val(),
        'intro':      $('#ch_intro').val(),
        'tag':        $('#ch_tag').val(),
        'categoryId': $('#ch_category').val()
      };
      if ($('#ch_image_updated').val() == 'true') {
        parameters['imageUrl'] = $('#ch_image').attr('src');
      }
      $.post('/CMSAPI/saveChannel', parameters, function(response)
      {
        if (response != 'OK') {
          alert($('#lang_warning_error_occurs').text());
        }else {
          alert($('#lang_update_successfully').text());
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
        channelInfoBlock.find('.channel_info_removebutton').click({ 'channelId': channelId, 'channelName': channels[i].name, 'channelInfoBlock': channelInfoBlock }, function(event)
        {
          var confirmation = $('#lang_confirm_removing_channel').text().replace('{channelName}', event.data.channelName);
          if (confirm(confirmation) == false)
            return false;
          var parameters = {
            'channelId': event.data.channelId,
            'msoId': $('#msoId').val()
          };
          $.post('/CMSAPI/removeChannelFromList', parameters, function()
          {
            event.data.channelInfoBlock.parent().remove();
            if (overallLayout.destroyRightSideContent(false) == false) return false;
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
      if (overallLayout.destroyRightSideContent(false) == false) return false;
      $('#create_9x9_channel_button').unbind().click(skeletonCreation.create9x9Channel);
      $('.import_button').unbind().click(channelDetail.displayImportDetail);
      $('#choose_channel_type').show();
    });
  }
}

$(function()
{
  channelList.init();
  $(window).bind('beforeunload', function()
  {
    if ($('.program_create_detail_block_cloned').size() > 0) {
      return $('#lang_confirm_leaving_program_creation').text();
    }
    return;
  });
});
