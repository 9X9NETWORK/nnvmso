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

var channelDetail =
{
  swfuploadObject: null,
  init: function(channelId)
  {
    $.getJSON('/CMSAPI/channelInfo?channelId=' + channelId, function(channel)
    {
      $('#ch_id').val(channel.key.id);
      $('#ch_name').val('').val(channel.name);
      $('#ch_image').attr('src', channel.imageUrl);
      $('#ch_intro').val('').val(channel.intro);
      $('#ch_tag').val('');
      if (channelDetail.swfuploadObject != null)
        channelDetail.swfuploadObject.destroy();
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
      channelDetail.swfuploadObject = new SWFUpload(swfupload_settings);
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
        if (response != 'OK')
          alert('發生錯誤');
        else
          alert('儲存成功');
      }, 'text');
    });
  }
}

var channelList =
{
  init: function()
  {
    // load channels
    $.getJSON('/CMSAPI/listOwnedChannels?msoId=' + $('#msoId').val(), function(channels)
    {
      if (channels.length == 0) {
        $('#channel_list_empty').show();
        return;
      }
      for (i in channels)
      {
        var channelInfoBlock = $('#channel_info_block').clone(true).removeAttr('id');
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
        });
        channelInfoBlock.find('.channel_info_detailbutton').click({ 'channelId': channelId }, function(event)
        {
          alert('program list');
          //channelInfo.init(event.data.channelId);
        });
        channelInfoBlock.find('.channel_info_promoteurl').text(promoteUrl).attr('href', promoteUrl);
        $('<li></li>').append(channelInfoBlock).appendTo('#channel_list_ul');
        // channel info block click event
        channelInfoBlock.click({ 'channelId': channelId }, function(event)
        {
          $('.channel_info_block').removeClass('chFocus').addClass('chUnFocus');
          $('.channel_info_title').removeClass('chFocusTitle').addClass('chUnFocusTitle');
          $('.channel_info_image').removeClass('chFocusImg').addClass('chUnFocusImg');
          $(this).removeClass('chUnFocus').addClass('chFocus');
          $('.channel_info_title', this).removeClass('chUnFocusTitle').addClass('chFocusTitle');
          $('.channel_info_image', this).removeClass('chUnFocusImg').addClass('chFocusImg');
          channelDetail.init(event.data.channelId);
        });
      }
      $('#channel_list').show();
    });
    
  }
}

$(function()
{
  channelList.init();
});
