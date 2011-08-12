/**
 *
 */

$('.setup_context').css('background', 'url(' + $('#image_bg_setup').text() + ') no-repeat;');

$('.syncInfo').unbind().hover(function() {
  $('#fb_hint').show();
},function() {
  $('#fb_hint').hide();
});

$('.setupTabItem').unbind().click(function()
{
  $('.setupTabItem').removeClass('link_Act').addClass('link_Normal');
  $('.setupContent').hide();
  $(this).removeClass('link_Normal').addClass('link_Act');

  var tabId = $(this).attr('id');
  if (tabId == 'tabA') {
    $('#syncAcc').show();
  } else if (tabId == 'tabB') {
    $('#addAcc').show();
  } else if (tabId == 'tabC') {
    $('#setPs').show();
  }
});
