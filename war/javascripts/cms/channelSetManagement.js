/**
 * 
 */
 
var page$ = {
  objChannelSetInfo: {
    init: function() {
      $.post('/CMSAPI/systemCategories', function(categories) {
        for (var i = 0; i < categories.length; i++) {
          $('<option></option>')
            .attr('value', categories[i].key.id)
            .text(categories[i].name)
            .appendTo('#sys_directory');
        }
        $.post('/CMSAPI/defaultChannelSetCategory', { 'msoId': $('#msoId').val() }, function(category) {
          if (category != null) {
            $('#sys_directory').val(category.key.id);
          }
        }, 'json');
        $.post('/CMSAPI/defaultChannelSetInfo', { 'msoId': $('#msoId').val() }, function(channelSet) {
          if (channelSet != null) {
            var url = 'http://' + ((location.host == 'www.9x9.tv') ? '9x9.tv' : location.host) + '/';
            url += ((channelSet.beautifulUrl != null) ? channelSet.beautifulUrl : channelSet.defaultUrl);
            if (channelSet.beautifulUrl != null || channelSet.defaultUrl != null) {
              $('#channel_set_promote_url').text(url).attr('href', url);
              $('.addthis_button_expanded').attr('addthis:url', url);
              cms.initAddthis();
            }
            $('#cc_name').val(channelSet.name);
            $('#cc_tag').val(channelSet.tag);
            $('#cc_intro').text(channelSet.intro);
            if (channelSet.imageUrl != null) {
              $('#cc_image').attr('src', channelSet.imageUrl);
            }
            $('#cc_image_updated').val('false');
            $('#cc_id').val(channelSet.key.id);
            $('#upload_image').click(function() {
              $('#upload_image_form').submit(function(event) {
                alert(event);
              });
            });
            var swfupload_settings = {
              flash_url:          '/javascripts/swfupload/swfupload.swf',
              upload_url:         'http://9x9tmp.s3.amazonaws.com/',
              file_size_limit:    '10240',
              file_types:         '*.jpg;*.png',
              file_types_description: 'Image Files',
              file_post_name:     'file',
              button_placeholder: $('#upload_image').get(0),
              button_action:       SWFUpload.BUTTON_ACTION.SELECT_FILE,
              button_image_url:   $('#image_btn_upload').text(),
              button_width:       '95',
              button_height:      '32',
              button_cursor:      SWFUpload.CURSOR.HAND,
              button_window_mode : SWFUpload.WINDOW_MODE.TRANSPARENT,
              debug:              false,
              http_success :      [201],
              upload_success_handler: function(file, serverData, recievedResponse) {
                $('#uploading').hide();
                $('#cc_image').attr('src', 'http://9x9tmp.s3.amazonaws.com/' + 'ch_set_logo_' + channelSet.key.id + '_'+ file.size + file.type);
                $('#cc_image_updated').val('true');
              },
              upload_error_handler: function(file, code, message) {
                $('#uploading').hide();
                alert('error: ' + message);
              },
              file_queued_handler: function(file) {
                var post_params =
                  {
                    "AWSAccessKeyId": $('#s3_id').val(),
                    "key":            'ch_set_logo_' + channelSet.key.id + '_'+ file.size + file.type,
                    "acl":            "public-read",
                    "policy":         $('#s3_policy').val(),
                    "signature":      $('#s3_signature').val(),
                    "content-type":   (file.type == '.jpg') ? "image/jpeg" : "image/png",
                    "success_action_status": "201"
                  };
                this.setPostParams(post_params);
                this.startUpload(file.id);
                $('#uploading').show();
              }
            };
            var swfu = new SWFUpload(swfupload_settings);
          }
        }, 'json');
      }, 'json');
    }
  },
  channelSetArea: {
    channelIds: [],
    reload: function() {
      $('.ch_exist').each(function() {
        $(this).RemoveBubblePopup();
      });
      $('.ch_exist').html('').removeClass('ch_exist').draggable({ 'disabled': true });
      page$.channelSetArea.channelIds = [];
      page$.channelSetArea.init();
    },
    initBubbles: function() {
      $('.ch_exist').each(function() {
        cms.bubblePopupProperties['innerHtml'] = $(this).find('span').html();
        $(this).CreateBubblePopup(cms.bubblePopupProperties);
      });
    },
    init: function() {
      $.post('/CMSAPI/defaultChannelSetChannels', { 'msoId': $('#msoId').val() }, function(channels) {
        for (var i = 0; i < channels.length; i++) {
          var seq = channels[i].seq;
          var img = $('<img/>').attr('src', channels[i].imageUrl);
          var dom = $('#channel_set_area li').get(seq - 1);
          $(dom).addClass('ch_exist').append(img);
          $('<input/>').attr('type', 'hidden').attr('name', 'channelId').val(channels[i].key.id).appendTo(dom);
          $('<span></span>').hide().html(page$.populateBubbleContent(channels[i])).appendTo(dom);
          page$.channelSetArea.initBubbles();
          $(dom).draggable(page$.draggableProperties);
          page$.channelSetArea.channelIds.push(channels[i].key.id.toString());
        }
        page$.channelPool.manageControls();
        $('.ch_none').droppable(page$.droppableProperties);
      }, 'json');
    }
  },
  channelPool: {
    currentPosition: 0,
    slideWidth:      410,
    slides:          null,
    numberOfSlides:  null,
    populateSlides: function(channels) {
      for (var i = 0; i < channels.length; i = i + 8) {
        var slide = $('<div class="slide"><ul></ul></div>');
        for (var j = i; j < channels.length && j < i + 8; j++) {
          var item = $('<li class="ch_normal"></li>');
          var img = $('<img/>').attr('src', channels[j].imageUrl);
          var p = $('<p class="ch_name"></p>').text(channels[j].name).textTruncate(20, '...');
          var hidden = $('<input type="hidden" name="channelId"/>').val(channels[j].key.id);
          item.append(img).append(p).append(hidden).appendTo(slide);
          item.draggable(page$.draggableProperties);
          
          var innerHtml = page$.populateBubbleContent(channels[j]);
          $('<span></span>').html(innerHtml).hide().appendTo(item);
          
        }
        $('#slidesContainer').append(slide);
      }
      this.slides = $('.slide');
      this.numberOfSlides = this.slides.length;
    },
    init: function() {
      $.post('/CMSAPI/listOwnedChannels', { 'msoId': $('#msoId').val() }, function(channels) {
        page$.channelPool.populateSlides(channels);
        
        // Remove scrollbar in JS
        $('#slidesContainer').css('overflow', 'hidden');
        
        // Wrap all .slides with #slideInner div
        // Float left to display horizontally, readjust .slides width
        $('.slide')
          .wrapAll('<div id="slideInner"></div>')
          .css({ 'float': 'left', 'width': page$.channelPool.slideWidth });
        
        // Set #slideInner width equal to total width of all slides
        $('#slideInner').css('width', page$.channelPool.slideWidth * page$.channelPool.numberOfSlides);
        
        // Insert controls in the DOM
        $('#slideshow')
          .prepend('<span class="control" id="leftControl">Clicking moves left</span>')
          .append('<span class="control" id="rightControl">Clicking moves right</span>');
        
        // Hide left arrow control on first load
        page$.channelPool.manageControls();
        
        // Create event listeners for .controls clicks
        $('.control').click(function(event)
        {
          // Determine new position
          page$.channelPool.currentPosition = ($(event.target).attr('id') == 'rightControl') ? page$.channelPool.currentPosition + 1 : page$.channelPool.currentPosition - 1;
          // Hide / show controls
          page$.channelPool.manageControls();
          // Move slideInner using margin-left
          $('#slideInner').animate(
          {
            'marginLeft': page$.channelPool.slideWidth * (-page$.channelPool.currentPosition)
          });
        });
        
        $('#slidesContainer').droppable({
          'accept': '.ch_exist',
          'hoverClass': 'ch_drag',
          'drop': function(event, ui) {
            var dragObj = $(ui.draggable);
            var seq = dragObj.index('#channel_set_area li.ch_none') + 1;
            var parameters = {
              'channelSetId': $('#cc_id').val(),
              'seq':          seq
            }
            $.post('/CMSAPI/removeChannelSetChannel', parameters, function(response) {
              if (response != 'OK') {
                alert($('#lang_warning_error_occurs').text());
                page$.channelSetArea.reload();
                return;
              }
              var channelId = dragObj.find('input[name="channelId"]').val();
              var channelPoolItem = $('li.ch_disable input[name="channelId"][value="'+channelId+'"]').parent();
              cms.bubblePopupProperties['innerHtml'] = channelPoolItem.find('span').html();
              channelPoolItem.removeClass('ch_disable')
                             .addClass('ch_normal')
                             .draggable({ disabled: false })
                             .CreateBubblePopup(cms.bubblePopupProperties);
              page$.channelSetArea.channelIds = $.grep(page$.channelSetArea.channelIds, function(value) { return value != channelId });
              dragObj.draggable('disable').html('').removeClass('ch_exist').RemoveBubblePopup();
            });
          }
        });
      }, 'json');
    },
    // manageControls: Hides and Shows controls depending on currentPosition
    manageControls: function () {
      // Hide left arrow if position is first slide
      if(this.currentPosition == 0) {
        $('#leftControl').hide();
      } else {
        $('#leftControl').show();
      }
      // Hide right arrow if position is last slide
      if(this.currentPosition == this.numberOfSlides - 1) {
        $('#rightControl').hide();
      } else {
        $('#rightControl').show();
      }
      $('.ch_normal').RemoveBubblePopup();
      var slideDom = $('#slideInner .slide').get(this.currentPosition);
      $(slideDom).find('li').each(function(index, element)
      {
        cms.bubblePopupProperties['innerHtml'] = $(this).find('span').html();
        var channelId = $(this).find('input[name=channelId]').val();
        if($.inArray(channelId, page$.channelSetArea.channelIds) >= 0) {
          $(this).addClass('ch_disable');
          $(this).removeClass('ch_normal');
          $(this).draggable({ disabled: true });
          $(this).RemoveBubblePopup();
        } else{
          $(this).removeClass('ch_disable');
          $(this).addClass('ch_normal');
          $(this).draggable({ disabled: false });
          $(this).CreateBubblePopup(cms.bubblePopupProperties);
        }
      });
    }
  },
  droppableProperties: {
    accept: 'li',
    hoverClass: 'ch_drag',
    drop: function(event, ui) {
      var from = $(ui.draggable).index('#channel_set_area li.ch_none') + 1;
      var to = $(event.target).index('#channel_set_area li.ch_none') + 1;
      if (from > 0) {
        var parameters = {
          'channelSetId': $('#cc_id').val(),
          'from':         from,
          'to':           to
        };
        $.post('/CMSAPI/changeChannelSetChannel', parameters, function(response) {
          if (response != 'OK') {
            alert($('#lang_warning_error_occurs').text());
            page$.channelSetArea.reload();
            return;
          }
          var dragObj = $(ui.draggable);
          var dropObj = $(event.target);
          var dragCloned = $(ui.draggable).clone();
          var dropCloned = $(event.target).clone();
          dragObj.draggable('disable').html('').removeClass('ch_exist').RemoveBubblePopup();
          
          dropObj.draggable(page$.draggableProperties).html(dragCloned.html()).addClass('ch_exist');
          cms.bubblePopupProperties['innerHtml'] = $(dropObj).find('span').html();
          $(dropObj).CreateBubblePopup(cms.bubblePopupProperties);
          
          if (dropCloned.hasClass('ch_exist')) {
            dragObj.draggable(page$.draggableProperties).html(dropCloned.html()).addClass('ch_exist');
            cms.bubblePopupProperties['innerHtml'] = $(dragObj).find('span').html();
            $(dragObj).CreateBubblePopup(cms.bubblePopupProperties);
          }
        });
      } else {
        var channelId = $(ui.draggable).find('input[name="channelId"]').val();
        var parameters = {
          'channelSetId': $('#cc_id').val(),
          'channelId':    channelId,
          'seq':          to
        }
        $.post('/CMSAPI/addChannelSetChannel', parameters, function(response) {
          if (response != 'OK') {
            alert($('#lang_warning_error_occurs').text());
            page$.channelSetArea.reload();
            return;
          }
          var dragObj = $(ui.draggable);
          var dropObj = $(event.target);
          if (dropObj.hasClass('ch_exist')) {
            var channelId = dropObj.find('input[name="channelId"]').val();
            var channelPoolItem = $('li.ch_disable input[name="channelId"][value="'+channelId+'"]').parent();
            cms.bubblePopupProperties['innerHtml'] = channelPoolItem.find('span').html();
            channelPoolItem.removeClass('ch_disable');
            channelPoolItem.addClass('ch_normal');
            channelPoolItem.draggable({ disabled: false });
            channelPoolItem.CreateBubblePopup(cms.bubblePopupProperties);
            page$.channelSetArea.channelIds = $.grep(page$.channelSetArea.channelIds, function(value) { return value != channelId });
            dropObj.html('');
          }
          var channelId = dragObj.find('input[name="channelId"]').val();
          cms.bubblePopupProperties['innerHtml'] = dragObj.find('span').html();
          dropObj.addClass('ch_exist')
            .append(dragObj.find('img').clone())
            .append(dragObj.find('input[name="channelId"]').clone())
            .append(dragObj.find('span').clone())
            .draggable(page$.draggableProperties)
            .CreateBubblePopup(cms.bubblePopupProperties);
          page$.channelSetArea.channelIds.push(channelId);
          $(dragObj).addClass('ch_disable')
            .removeClass('ch_normal')
            .draggable({ disabled: true })
            .RemoveBubblePopup();
        });
      }
    }
  },
  populateBubbleContent: function(channel) {
    var span = $('<span></span>');
    var h2 = $('<h2 class="popTitle"></h2>').text(channel.name).textTruncate(20, '...').appendTo(span);
    var updateDate = new Date(channel.updateDate);
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
    
    var innerHtml = span.html();
    var label_program_count = $('#lang_label_program_count').text();
    var label_update_time = $('#lang_label_update_time').text();
    innerHtml += '<br/>' + label_program_count + ' : ' + channel.programCount;
    innerHtml += '<br/>' + label_update_time + ' : ' + year + '/' + month + '/' + date + '&nbsp;' + hour + ':' + minute + ':' + second;
    
    return innerHtml;
  },
  draggableProperties: {
    'start': function() {
      $('.ch_exist').RemoveBubblePopup();
      $('.ch_normal').RemoveBubblePopup();
    },
    'stop': function() {
      page$.channelSetArea.initBubbles();
      page$.channelPool.manageControls();
    },
    'appendTo': '#channel_set_area ul',
    'disabled': false,
    'opacity':  0.6,
    'helper':   "clone",
    'scroll':   false,
    'revert':   'invalid',
    'zIndex':   2700
  },
  funcPublishChannelSet: function() {
    var categoryId = $('#sys_directory').val();
    if (categoryId == 0) {
      alert($('#lang_warning_select_category').text());
      return;
    }
    var intro = $('#cc_intro').val();
    if (intro.length > 200) {
      alert($('#lang_warning_intro_over_limitation').text());
      return;
    }
    var name = $('#cc_name').val();
    if (name.length > 40) {
      alert($('#lang_warning_name_over_limitation').text());
      return;
    } else if (name.length == 0) {
      alert($('#lang_warning_empty_name').text());
      return;
    }
    var tag = $('#cc_tag').val();
    if (tag.length > 200) {
      alert($('#lang_warning_tag_over_limitation').text());
      return;
    }
    var imageUrl = $('#cc_image').attr('src');
    var imageUpdated = $('#cc_image_updated').val();
    if (imageUrl.length == 0 || imageUrl == '/images/cms/upload_img.jpg') {
      alert($('#lang_warning_empty_logo').text());
      return;
    }
    var parameters = {
      'channelSetId': $('#cc_id').val(),
      'name':         name,
      'intro':        intro,
      'tag':          tag,
      'categoryId':   categoryId
    };
    if (imageUpdated == 'true') {
      parameters['imageUrl'] = imageUrl;
    }
    $.post('/CMSAPI/saveChannelSet', parameters, function(response) {
      if (response != 'OK')
        alert($('#lang_warning_error_occurs').text());
      else
        alert($('#lang_update_successfully').text());
    }, 'text');
  },
  init: function() {
    $('.ch_bg').css('background', 'url(' + $('image_bg_album').text() + ') no-repeat;');
    
    page$.objChannelSetInfo.init();
    page$.channelPool.init();
    page$.channelSetArea.init();
    
    $('#publish_channel_set').click(page$.funcPublishChannelSet);
    
  }
};

