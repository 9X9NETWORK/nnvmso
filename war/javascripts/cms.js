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

var initChannelPool = function()
{
  $.getJSON('/CMSAPI/listOwnedChannels?msoId=' + $('#msoId').val(), function(channels)
    {
      for (var i = 0; i < channels.length; i = i + 8)
      {
        var slide = $('<div class="slide"><ul></ul></div>');
        for (var j = i; j < channels.length && j < i + 8; j++)
        {
          var item = $('<li class="ch_normal"></li>');
          var img = $('<img/>').attr('src', channels[j].imageUrl);
          var p = $('<p class="ch_name"></p>').text(channels[j].name).textTruncate(20, '...');
          item.append(img).append(p).appendTo(slide);
          
          var innerHtml = populateBubbleContent(channels[j]);
          $('<span></span>').html(innerHtml).hide().appendTo(item);
          
        }
        $('#slidesContainer').append(slide);
      }
      
      var currentPosition = 0;
      var slideWidth = 410;
      var slides = $('.slide');
      var numberOfSlides = slides.length;
      
      // Remove scrollbar in JS
      $('#slidesContainer').css('overflow', 'hidden');
      
      // Wrap all .slides with #slideInner div
      // Float left to display horizontally, readjust .slides width
      slides
        .wrapAll('<div id="slideInner"></div>')
        .css({ 'float': 'left', 'width': slideWidth });
      
      // Set #slideInner width equal to total width of all slides
      $('#slideInner').css('width', slideWidth * numberOfSlides);
      
      // Insert controls in the DOM
      $('#slideshow')
        .prepend('<span class="control" id="leftControl">Clicking moves left</span>')
        .append('<span class="control" id="rightControl">Clicking moves right</span>');
      
      // Hide left arrow control on first load
      manageControls(currentPosition);
      
      // Create event listeners for .controls clicks
      $('.control').bind('click', function()
      {
        // Determine new position
        currentPosition = ($(this).attr('id') == 'rightControl') ? currentPosition + 1 : currentPosition - 1;
        // Hide / show controls
        manageControls(currentPosition);
        // Move slideInner using margin-left
        $('#slideInner').animate(
        {
          'marginLeft': slideWidth * (-currentPosition)
        });
      });
      
      // manageControls: Hides and Shows controls depending on currentPosition
      function manageControls(position)
      {
        // Hide left arrow if position is first slide
        if(position == 0) {
          $('#leftControl').hide();
        } else {
          $('#leftControl').show();
        }
        // Hide right arrow if position is last slide
        if(position == numberOfSlides - 1) {
          $('#rightControl').hide();
        } else {
          $('#rightControl').show();
        }
        $('.ch_normal').RemoveBubblePopup();
        var slideDom = $('#slideInner .slide').get(position);
        $(slideDom).find('li').each(function(index, element)
          {
            bubblePopupProperties['innerHtml'] = $(this).find('span').html();
            $(this).CreateBubblePopup(bubblePopupProperties);
          });
      }
    });
}

var initChannelSetArea = function()
{
  $.getJSON('/CMSAPI/defaultChannelSetChannels?msoId=' + $('#msoId').val(), function(channels)
    {
      //alert(channels.length);
      for (var i = 0; i < channels.length; i++)
      {
        var seq = channels[i].seq;
        var img = $('<img/>').attr('src', channels[i].imageUrl);
        var dom = $('#channel_set_area li').get(seq);
        $(dom).addClass('ch_exist').append(img);
        bubblePopupProperties['innerHtml'] = populateBubbleContent(channels[i]);
        $(dom).CreateBubblePopup(bubblePopupProperties);
      }
    });
}

$(document).ready(function()
{
  
  initChannelPool();
  initChannelSetArea();
  
});
