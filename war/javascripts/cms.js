/**
 * 
 */
 
var bubblePopupProperties = 
{
  'position':  'top',
  'align':     'center',
  //'innerHtml': innerHtml,
  'innerHtmlStyle':
  {
    'color':      '#292929',
    'text-align': 'left',
    'font-size':  '0.8em'
  },
  'themeName': 'all-black',
  'themePath': '/images/cms'
};

var draggableProperties =
  {
    'start': function()
    {
      $('.ch_exist').RemoveBubblePopup();
      $('.ch_normal').RemoveBubblePopup();
    },
    'stop': function()
    {
      channelSetArea.initBubbles();
      channelPool.manageControls();
    },
    'appendTo': '#channel_set_area ul',
    'disabled': false,
    'opacity':  0.5,
    'helper':   "clone",
    'scroll':   false,
    'revert':   'invalid'
  };

var populateBubbleContent = function(channel)
{
  var span = $('<span></span>');
  var h2 = $('<h2 class="popTitle"></h2>').text(channel.name).textTruncate(20, '...').appendTo(span);
  var updateDate = new Date(channel.updateDate);
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
  
  var innerHtml = span.html();
  innerHtml += '<br/>節目數量 ：' + channel.programCount;
  innerHtml += '<br/>更新時間 ：' + year + '/' + month + '/' + date + '&nbsp;' + hour + ':' + minute + ':' + second;
  
  return innerHtml;
}

var channelPool =
{
  currentPosition: 0,
  slideWidth:      410,
  slides:          null,
  numberOfSlides:  null,
  populateSlides: function(channels)
  {
    for (var i = 0; i < channels.length; i = i + 8)
    {
      var slide = $('<div class="slide"><ul></ul></div>');
      for (var j = i; j < channels.length && j < i + 8; j++)
      {
        var item = $('<li class="ch_normal"></li>');
        var img = $('<img/>').attr('src', channels[j].imageUrl);
        var p = $('<p class="ch_name"></p>').text(channels[j].name).textTruncate(20, '...');
        var hidden = $('<input type="hidden" name="channelId"/>').val(channels[j].key.id);
        item.append(img).append(p).append(hidden).appendTo(slide);
        item.draggable(draggableProperties);
        
        var innerHtml = populateBubbleContent(channels[j]);
        $('<span></span>').html(innerHtml).hide().appendTo(item);
        
      }
      $('#slidesContainer').append(slide);
    }
    this.slides = $('.slide');
    this.numberOfSlides = this.slides.length;
  },
  init: function()
  {
    $.getJSON('/CMSAPI/listOwnedChannels?msoId=' + $('#msoId').val(), function(channels)
      {
        channelPool.populateSlides(channels);
        
        // Remove scrollbar in JS
        $('#slidesContainer').css('overflow', 'hidden');
        
        // Wrap all .slides with #slideInner div
        // Float left to display horizontally, readjust .slides width
        $('.slide')
          .wrapAll('<div id="slideInner"></div>')
          .css({ 'float': 'left', 'width': channelPool.slideWidth });
        
        // Set #slideInner width equal to total width of all slides
        $('#slideInner').css('width', channelPool.slideWidth * channelPool.numberOfSlides);
        
        // Insert controls in the DOM
        $('#slideshow')
          .prepend('<span class="control" id="leftControl">Clicking moves left</span>')
          .append('<span class="control" id="rightControl">Clicking moves right</span>');
        
        // Hide left arrow control on first load
        channelPool.manageControls();
        
        // Create event listeners for .controls clicks
        $('.control').click(function(event)
        {
          // Determine new position
          channelPool.currentPosition = ($(event.target).attr('id') == 'rightControl') ? channelPool.currentPosition + 1 : channelPool.currentPosition - 1;
          // Hide / show controls
          channelPool.manageControls();
          // Move slideInner using margin-left
          $('#slideInner').animate(
          {
            'marginLeft': channelPool.slideWidth * (-channelPool.currentPosition)
          });
        });
        
        $('#slidesContainer').droppable(
          {
            'accept': '.ch_exist',
            'drop': function(event, ui)
            {
              var seq = $(ui.draggable).index('#channel_set_area li.ch_none') + 1;
              var parameters = {
                'channelSetId': $('#cc_id').val(),
                'seq':          seq
              }
              $.post('/CMSAPI/removeChannelSetChannel', parameters, function() { channelSetArea.reload(); });
            }
          });
      });
  },
  // manageControls: Hides and Shows controls depending on currentPosition
  manageControls: function ()
  {
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
        bubblePopupProperties['innerHtml'] = $(this).find('span').html();
        var channelId = $(this).find('input[name=channelId]').val();
        if($.inArray(channelId, channelSetArea.channelIds) >= 0) {
          $(this).addClass('ch_disable');
          $(this).removeClass('ch_normal');
          $(this).draggable({ disabled: true });
          $(this).RemoveBubblePopup();
        } else{
          $(this).removeClass('ch_disable');
          $(this).addClass('ch_normal');
          $(this).draggable({ disabled: false });
          $(this).CreateBubblePopup(bubblePopupProperties);
        }
      });
  }
};

var channelSetArea =
{
  channelIds: [],
  reload: function()
  {
    $('.ch_exist').each(function()
      {
        $(this).RemoveBubblePopup();
      });
    $('.ch_exist').html('').removeClass('ch_exist').draggable({ 'disabled': true });
    this.channelIds = [];
    this.init();
  },
  initBubbles: function()
  {
    $('.ch_exist').each(function()
      {
        bubblePopupProperties['innerHtml'] = $(this).find('span').html();
        $(this).CreateBubblePopup(bubblePopupProperties);
      });
  },
  init: function()
  {
    $.getJSON('/CMSAPI/defaultChannelSetChannels?msoId=' + $('#msoId').val(), function(channels)
    {
      for (var i = 0; i < channels.length; i++)
      {
        var seq = channels[i].seq;
        var img = $('<img/>').attr('src', channels[i].imageUrl);
        var dom = $('#channel_set_area li').get(seq - 1);
        $(dom).addClass('ch_exist').append(img);
        $('<span></span>').hide().html(populateBubbleContent(channels[i])).appendTo(dom);
        channelSetArea.initBubbles();
        $(dom).draggable(draggableProperties);
        channelSetArea.channelIds.push(channels[i].key.id.toString());
      }
      channelPool.manageControls();
      $('.ch_none').droppable(
        {
          accept: 'li',
          drop: function(event, ui)
          {
            var from = $(ui.draggable).index('#channel_set_area li.ch_none') + 1;
            var to = $(event.target).index('#channel_set_area li.ch_none') + 1;
            if (from > 0) {
              var parameters = {
                'channelSetId': $('#cc_id').val(),
                'from':         from,
                'to':           to
              };
              $.post('/CMSAPI/changeChannelSetChannel', parameters, function() { channelSetArea.reload(); });
            } else {
              var channelId = $(ui.draggable).find('input[name="channelId"]').val();
              var parameters = {
                'channelSetId': $('#cc_id').val(),
                'channelId':    channelId,
                'seq':          to
              }
              $.post('/CMSAPI/addChannelSetChannel', parameters, function() { channelSetArea.reload(); });
            }
          }
        });
    });
  }
};

var initChannelSetInfo = function()
{
  
  $.getJSON('/CMSAPI/systemCategories', function(categories)
    {
      for (var i = 0; i < categories.length; i++)
      {
        $('<option></option>')
          .attr('value', categories[i].key.id)
          .text(categories[i].name)
          .appendTo('#sys_directory');
      }
      $.getJSON('/CMSAPI/defaultChannelSetCategory?msoId=' + $('#msoId').val(), function(category)
        {
          if (category != null)
          {
            $('#sys_directory').val(category.key.id);
          }
        });
      $.getJSON('/CMSAPI/defaultChannelSetInfo?msoId=' + $('#msoId').val(), function(channelSet)
        {
          if (channelSet != null)
          {
            addthis_config['pubid'] = $('#msoId').val();
            var url = 'http://' + ((location.host == 'www.9x9.tv') ? '9x9.tv' : location.host) + '/';
            var addthis_share = 
              {
                'title': channelSet.name,
                'description': channelSet.intro
              }
            url += ((channelSet.beautifulUrl != null) ? channelSet.beautifulUrl : channelSet.defaultUrl);
            if (channelSet.beautifulUrl != null || channelSet.defaultUrl != null)
            {
              $('#channel_set_promote_url').text(url).attr('href', url);
              addthis_share['url'] = url;
              addthis.button('#addthis_button', null, addthis_share);
            }
            $('#cc_name').val(channelSet.name);
            $('#cc_tag').val(channelSet.tag);
            $('#cc_intro').text(channelSet.intro);
            if (channelSet.imageUrl != null) {
              $('#cc_image').attr('src', channelSet.imageUrl);
            }
            
            $('#cc_id').val(channelSet.key.id);
          }
          
        });
    });
}

var uploadImage = function()
{
  var imageUrl = prompt('上傳功能將在稍後開通，請暫以輸入圖片網址取代', $('#cc_image').attr('src'));
  if (imageUrl != null)
    $('#cc_image').attr('src', imageUrl);
}

var publishChannelSet = function()
{
  var categoryId = $('#sys_directory').val();
  if (categoryId == 0) {
    alert('你必需選一個系統分類');
    return;
  }
  var intro = $('#cc_intro').text();
  if (intro.length > 200) {
    alert('"介紹"超過字數限制');
    return;
  }
  var name = $('#cc_name').val();
  if (name.length > 40) {
    alert('"名稱"超過字數限制');
    return;
  } else if (name.length == 0) {
    alert('"名稱"不可以為空');
    return;
  }
  var tag = $('#cc_tag').val();
  if (tag.length > 200) {
    alert('"標籤“超過長度');
    return;
  }
  var imageUrl = $('#cc_image').attr('src');
  if (imageUrl.length == 0 || imageUrl == '/images/cms/upload_img.jpg')
    return alert('"圖示“不可以為空');
  
  var parameters = {
    'channelSetId': $('#cc_id').val(),
    'imageUrl':     imageUrl,
    'name':         name,
    'intro':        intro,
    'tag':          tag,
    'categoryId':   categoryId
  };
  
  $.post('/CMSAPI/saveChannelSet', parameters, function(response)
    {
      if (response != 'OK')
        alert('儲存資料時發生錯誤');
      else
        alert('發佈成功');
    }, 'text');
  
}

$(document).ready(function()
{
  
  initChannelSetInfo();
  channelPool.init();
  channelSetArea.init();
  
  $('#upload_image').click(uploadImage);
  $('#publish_channel_set').click(publishChannelSet);
  
});
