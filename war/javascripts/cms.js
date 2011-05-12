/**
 * 
 */
$(document).ready(function()
{
  //create a bubble popup for each DOM element with class attribute as "text", "button" or "link" and LI, P, IMG elements.
  $('li.ch_exsist, .ch_normal').CreateBubblePopup(
  {
    position:  'top',
    align:     'center',
    innerHtml: '<h2 class="popTitle">頻道標題</h2><br/>節目數量 : 10 <br/> 更新時間 ：2010/10/24 &nbsp;&nbsp; 16:34:30 ',
    innerHtmlStyle:
    {
      'color':      '#292929',
      'text-align': 'left',
      'font-size':  '0.8em'
    },
    themeName:   'all-black',
    themePath:   '/images/cms'
  });

  // -----------------------------

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
  }
});
