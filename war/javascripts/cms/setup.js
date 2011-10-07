/**
 *
 */

var pageSetup = {
  
  sFacebookPermissions: 'email,read_stream,publish_stream,manage_pages,offline_access,user_birthday,friends_birthday,user_checkins,user_education_history,user_likes,friends_likes,user_location,user_photo_video_tags,friends_photo_video_tags,user_photos,friends_photos,user_videos,friends_videos,user_work_history,read_friendlists',
  initGenericOne: function() {
  },
  init: function() {
    $('#tabB').hide();
    $('.setup_context').css('background', 'url(' + $('#image_bg_setup').text() + ') no-repeat');
    
    $.post('/CMSAPI/listSnsAuth', { 'msoId': $('#msoId').val() }, function(snsList) {
      for (i in snsList) {
        var sns = snsList[i];
        if (sns.type == 1) {
          $('#facebook_connect').hide();
          $('#facebook_disconnect').show();
        }
      }
    });
    
    $('.syncInfo').unbind().hover(function() {
      $('#fb_hint').show();
    },function() {
      $('#fb_hint').hide();
    });
    
    $('.setupTabItem').unbind().click(function() {
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
    
    FB.init({
      appId  : '110847978946712',
      status : true, // check login status
      cookie : true, // enable cookies to allow the server to access the session
      xfbml  : true  // parse XFBML
    });
    
    $('#facebook_connect').unbind().click(function() {
      FB.login(function(response) {
        /**
         * response = {
         *   status: 'connected',
         *   session: {
         *     access_token: '...',
         *     expires:      '...',
         *     secret:       '...',
         *     session_key:  '...',
         *     sig:          '...',
         *     uid:          '...'
         *   },
         *   perms: 'read_stream'
         * }
         *
         * - OR -
         *
         * response = {
         *   status:  'notConnected',
         *   session: null,
         *   perms:   null
         * }
         */
        if (response.status == 'connected') {
          var parameters = {
            'msoId': $('#msoId').val(),
            'type':  1,
            'token': response.session.uid
          };
          $.post('/CMSAPI/createSnsAuth', parameters, function(response)
          {
            if (response != "OK") {
              alert($('#lang_warning_error_occurs').text());
              return;
            }
            $('#facebook_connect').hide();
            $('#facebook_disconnect').show();
          });
        }
      }, { perms: pageSetup.sFacebookPermissions });
    });
    
    $('#facebook_disconnect').unbind().click(function() {
      if (confirm($('#lang_confirm_disconnect_with_facebook').text())) {
        var parameters = {
          'msoId': $('#msoId').val(),
          'type': 1
        };
        $.post('/CMSAPI/removeSnsAuth', parameters, function(response) {
          if (response != "OK") {
            alert($('#lang_warning_error_occurs').text());
            return;
          }
          $('#facebook_connect').show();
          $('#facebook_disconnect').hide();
        });
      }
    });
    
    $('#password').unbind().change(function() {
      if ($('#password').val() == '')
        $('#save_password_button').attr('disabled', true);
      else
        $('#save_password_button').attr('disabled', false);
    });
    $('#save_password_button').unbind().click(function() {
      if ($('#password').val() == '') { return; }
      if ($('#password').val() != $('#retype').val()) {
        alert($('#lang_warning_retype_not_match').text());
        return;
      }
      var parameters = {
        'msoId': $('#msoId').val(),
        'newPassword': $('#password').val()
      };
      $.post('/CMSAPI/changePassword', parameters, function(response) {
        if (response != "OK") {
          alert($('#lang_warning_error_occurs').text());
        } else {
          alert($('#lang_update_successfully').text());
          $('[type="password"]').val('');
        }
      });
    });
    
    $('#cancel_password_button').unbind().click(function() {
      $('[type="password"]').val('');
    });
  }
};



