$(function()
{

  $('#ui_tabs').tabs();
  $.jgrid.no_legacy_api = true;

  var channelContentType = {
    0: 'Unknwon',
    1: 'System',
    2: 'Podcast',
    3: 'YouTube Channel',
    4: 'YouTube Playlist'
  };

  var channelStatus = {
    0:    'Success',
    1:    'Error',
    2:    'Processing',
    3:    'Wait For Approval',
    51:   'Invalid format',
    53:   'URL Not Found',
    100:  'No Valid Episode',
    101:  'Bad Quality',
    1000: 'Tanscoding DB Error',
    1001: 'NNVMSO Json Error'
  }

  function callbackAfterSubmitForm(response, postData, formId)
  {
    if (response.responseText != 'OK') {
      if (response.responseText.length > 50)
        alert('Error Occurs');
      else
        alert(response.responseText);
    }
  }

  //////// Channel Management Tab ////////

  $('#chn_table').jqGrid(
  {
    colModel:
    [
      {
        label:    'Channel Logo',
        name:     'imageUrl',
        index:    'imageUrl',
        width:    150,
        align:    'center',
        sortable: true,
        hidden:   true,
        search:   false,
        editable: true,
        edittype: 'image',
        editoptions:
        {
          disabled: true
        },
        editrules:
        {
          edithidden: true
        }
      },
      {
        label:    'Channel ID',
        name:     'channel',
        index:    'channel',
        width:    80,
        align:    'center',
        search:   true,
        sortable: false,
        editable: true,
        editoptions:
        {
          disabled: true
        },
        searchoptions:
        {
          sopt: ['eq']
        }
      },
      {
        label:    'Channel Name',
        name:     'name',
        index:    'name',
        width:    200,
        align:    'center',
        search:   true,
        sortable: true,
        editable: true,
        editoptions:
        {
          maxlength: 100
        },
        searchoptions:
        {
          sopt: ['eq']
        }
      },
      {
        label:    'Updated Time',
        name:     'updateDate',
        index:    'updateDate',
        width:    140,
        align:    'center',
        search:   false,
        sortable: true,
        editable: true,
        editoptions:
        {
          disabled: true
        }
      },
      {
        label:    'Created Time',
        name:     'createDate',
        index:    'createDate',
        width:    140,
        align:    'center',
        search:   false,
        sortable: true,
        editable: true,
        hidden:   true,
        editoptions:
        {
          disabled: true
        },
        editrules:
        {
          edithidden: true
        }
      },
      {
        label:    'Source',
        name:     'sourceUrl',
        index:    'sourceUrl',
        width:    150,
        align:    'center',
        search:   true,
        sortable: true,
        hidden:   true,
        editable: true,
        editoptions:
        {
          disabled: true
        },
        editrules:
        {
          edithidden: true
        },
        searchoptions:
        {
          sopt: ['eq'],
          searchhidden: true
        }
      },
      {
        label:     'Status',
        name:      'status',
        index:     'status',
        width:     70,
        align:     'center',
        search:    true,
        sortable:  true,
        stype:     'select',
        formatter: 'select',
        editable:  true,
        edittype:  'select',
        editoptions:
        {
          value: channelStatus
        },
        searchoptions:
        {
          sopt:  ['eq'],
          value: channelStatus
        }
      },
      {
        label:     'Type',
        name:      'contentType',
        index:     'contentType',
        width:     90,
        align:     'center',
        search:    true,
        stype:     'select',
        sortable:  true,
        formatter: 'select',
        editable:  true,
        edittype:  'select',
        editoptions:
        {
          disabled: true,
          value:    channelContentType
        },
        searchoptions:
        {
          sopt:  ['eq'],
          value: channelContentType
        }
      },
      {
        label:     'Public',
        name:      'isPublic',
        index:     'isPublic',
        width:     70,
        align:     'center',
        search:    true,
        stype:     'select',
        sortable:  true,
        formatter: 'checkbox',
        editable:  true,
        edittype:  'checkbox',
        editoptions:
        {
          value: 'true:false'
        },
        searchoptions:
        {
          sopt:  ['eq'],
          value:
          {
            'true':  'true',
            'false': 'false'
          }
        }
      },
      {
        label:    'Ep. Count',
        name:     'programCount',
        index:    'programCount',
        width:    90,
        align:    'center',
        search:   false,
        sortable: true,
        hidden:   false,
        editable: true,
        editrules:
        {
          integer:    true,
          edithidden: true
        }
      },
      {
        label:    'Sub. Count',
        name:     'subscriptionCount',
        index:    'subscriptionCount',
        width:    80,
        align:    'center',
        search:   false,
        sortable: false,
        hidden:   false,
        editable: false,
        editrules:
        {
          integer:    true,
          edithidden: true
        }
      },
      {
        label:    'Introduction',
        name:     'intro',
        index:    'intro',
        width:    150,
        align:    'center',
        search:   false,
        sortable: true,
        hidden:   true,
        editable: true,
        edittype: 'textarea',
        editoptions:
        {
          rows: '3'
        },
        editrules:
        {
          edithidden: true
        }
      }
    ],
    url:         '/admin/channel/list',
    datatype:    'json',
    caption:     'Channel List',
    sortname:    'updateDate',
    sortorder:   'desc',
    rowNum:      10,
    rowList:     [10, 20, 30, 40, 50, 100],
    viewrecords: true,
    height:      'auto',
    hidegrid:    false,
    toppager:    true,
    gridComplete: function()
    {
      var totaltime = $(this).jqGrid('getGridParam', 'totaltime');
      $(this).jqGrid('setCaption', 'Channel List (' + totaltime + 'ms)');
    },
    onSelectRow: function(rowId, status)
    {
      $('#cc_table').attr('title', $(this).jqGrid('getCell', rowId, 'name'));
      $('#cc_table').attr('channel', rowId);
      $('#cc_table').jqGrid('setGridParam', { url: "/admin/channel/listCategories?channel=" + rowId }).trigger('reloadGrid');
    },
    ondblClickRow: function(rowId)
    {
      var imageUrl = $(this).jqGrid('getCell', rowId, 'imageUrl');
      $(this).jqGrid('setColProp', 'imageUrl', {editoptions: {src: imageUrl}});
      $(this).jqGrid('editGridRow', rowId,
      {
        url:               '/admin/channel/modify',
        caption:           'Edit Channel Meta',
        width:             'auto',
        top:               $(window).scrollTop() + 50, // $(this).offset().top - 50,
        left:              200, // $(this).offset().left + $(this).width() + 20,
        modal:             false,
        jqModal:           true,
        closeAfterEdit:    true,
        closeOnEscape:     true,
        reloadAfterSubmit: true,
        viewPagerButtons:  true,
        recreateForm:      true,
        beforeCheckValues: function(postData, formId, mode)
        {
          postData['imageUrl'] = $('#imageUrl', formId).attr('src');
          return postData;
        },
        beforeShowForm: function(formId)
        {
          $('#imageUrl', formId).click(function()
          {
            var imageUrl = prompt('Please enter new image URL', $(this).attr('src'));
            if (imageUrl != null) {
              $(this).attr('src', imageUrl);
            }
          });
        },
        afterclickPgButtons: function(whichButton, formId, rowId)
        {
          var imageUrl = $('#chn_table').jqGrid('getCell', rowId, 'imageUrl');
          $('#imageUrl', formId).attr('src', imageUrl);
        },
        afterComplete: callbackAfterSubmitForm
      });
    },
    subGrid: true,
    subGridRowExpanded: function(subgridId, rowId)
    {
      var subgridTableId = subgridId + '_t';
      $('#' + subgridId).html('<table id="' + subgridTableId + '" class="scroll"></table>');
      $('#' + subgridId).attr('style', 'padding: 5px 5px 5px 5px');
      $('#' + subgridTableId).jqGrid(
      {
        colModel:
        [
          {
            label:    'Thumbnail',
            name:     'imageUrl',
            index:    'imageUrl',
            width:    150,
            align:    'center',
            sortable: true,
            hidden:   true,
            editable: true,
            edittype: 'image',
            editoptions:
            {
              disabled: true
            },
            editrules:
            {
              edithidden: true
            }
          },
          {
            label:    'Program ID',
            name:     'id',
            index:    'id',
            width:    80,
            align:    'center',
            sortable: false,
            editable: true,
            editoptions:
            {
              disabled: true
            }
          },
          {
            label:    'Program Name',
            name:     'name',
            index:    'name',
            width:    200,
            align:    'center',
            sortable: true,
            editable: true,
            editoptions:
            {
              maxlength: 100
            }
          },
          {
            label:    'Publish Time',
            name:     'pubDate',
            index:    'pubDate',
            width:    120,
            align:    'center',
            sortable: true,
            editable: true,
            editoptions:
            {
              disabled: true
            }
          },
          {
            label:    'Updated Time',
            name:     'updateDate',
            index:    'updateDate',
            width:    120,
            align:    'center',
            sortable: true,
            editable: true,
            hidden:   true,
            editrules:
            {
              edithidden: true
            }
          },
          {
            label:    'Created Time',
            name:     'createDate',
            index:    'createDate',
            width:    120,
            align:    'center',
            sortable: true,
            editable: true,
            hidden:   true,
            editoptions:
            {
              disabled: true
            },
            editrules:
            {
              edithidden: true
            }
          },
          {
            label:    'MPEG4 URL',
            name:     'mpeg4FileUrl',
            index:    'mpeg4FileUrl',
            width:    150,
            align:    'center',
            sortable: true,
            hidden:   true,
            editable: true,
            editoptions:
            {
              disabled: true
            },
            editrules:
            {
              edithidden: true
            }
          },
          {
            label:    'WebM URL',
            name:     'webMFileUrl',
            index:    'webMFileUrl',
            width:    150,
            align:    'center',
            sortable: true,
            hidden:   true,
            editable: true,
            editoptions:
            {
              disabled: true
            },
            editrules:
            {
              edithidden: true
            }
          },
          {
            label:    'Other URL',
            name:     'otherFileUrl',
            index:    'otherFileUrl',
            width:    150,
            align:    'center',
            sortable: true,
            hidden:   true,
            editable: true,
            editoptions:
            {
              disabled: true
            },
            editrules:
            {
              edithidden: true
            }
          },
          {
            label:    'Audio URL',
            name:     'audioFileUrl',
            index:    'audioFileUrl',
            width:    150,
            align:    'center',
            sortable: true,
            hidden:   true,
            editable: true,
            editoptions:
            {
              disabled: true
            },
            editrules:
            {
              edithidden: true
            }
          },
          {
            label:     'Status',
            name:      'status',
            index:     'status',
            width:     70,
            align:     'center',
            sortable:  true,
            formatter: 'select',
            editable:  true,
            edittype:  'select',
            editoptions:
            {
              value: '0:Success;1:Error;101:Bad Quality'
            }
          },
          {
            label:     'Type',
            name:      'type',
            index:     'type',
            width:     70,
            align:     'center',
            sortable:  true,
            formatter: 'select',
            editable:  true,
            edittype:  'select',
            editoptions:
            {
              disabled: true,
              value:    '0:Unknown;1:Video;2:Audio'
            }
          },
          {
            label:     'Public',
            name:      'isPublic',
            index:     'isPublic',
            width:     70,
            align:     'center',
            sortable:  true,
            formatter: 'checkbox',
            editable:  true,
            edittype:  'checkbox',
            editoptions:
            {
              value: 'true:false'
            }
          },
          {
            label:    'Duration',
            name:     'duration',
            index:    'duration',
            width:    100,
            align:    'center',
            sortable: true,
            hidden:   true,
            editable: true,
            editoptions:
            {
              disabled: true
            },
            editrules:
            {
              edithidden: true
            }
          },
          {
            label:    'Introduction',
            name:     'intro',
            index:    'intro',
            width:    150,
            align:    'center',
            sortable: true,
            hidden:   true,
            editable: true,
            edittype: 'textarea',
            editoptions:
            {
              rows: '3'
            },
            editrules:
            {
              edithidden: true
            }
          }
        ],
        url:         '/admin/program/list?channel=' + rowId,
        datatype:    'json',
        caption:     'Program List',
        sortname:    'pubDate',
        sortorder:   'desc',
        rowNum:      10,
        rowList:     [10, 20, 30, 40, 50, 100],
        viewrecords: true,
        height:      'auto',
        hidegrid:    false,
        toppager:    true,
        gridComplete: function()
        {
          var totaltime = $(this).jqGrid('getGridParam', 'totaltime');
          $(this).jqGrid('setCaption', 'Program List (' + totaltime + 'ms)');
        },
        ondblClickRow: function(rowId)
        {
          var imageUrl = $(this).jqGrid('getCell', rowId, 'imageUrl');
          $(this).jqGrid('setColProp', 'imageUrl', {editoptions: {src: imageUrl}});
          $(this).jqGrid('editGridRow', rowId,
          {
            url:               '/admin/program/modify',
            caption:           'Edit Program Meta',
            width:             'auto',
            top:               $(window).scrollTop() + 50, // $(this).offset().top - 130,
            left:              200, // $(this).offset().left + $(this).width() + 15,
            modal:             false,
            jqModal:           true,
            closeAfterEdit:    true,
            closeOnEscape:     true,
            reloadAfterSubmit: true,
            viewPagerButtons:  true,
            recreateForm:      true,
            beforeCheckValues: function(postData, formId, mode)
            {
              postData['imageUrl'] = $('#imageUrl', formId).attr('src');
              return postData;
            },
            beforeShowForm: function(formId)
            {
              $('#imageUrl', formId).click(function()
              {
                var imageUrl = prompt('Please enter new image URL', $(this).attr('src'));
                if (imageUrl != null) {
                  $(this).attr('src', imageUrl);
                }
              });
            },
            afterclickPgButtons: function(whichButton, formId, rowId)
            {
              var imageUrl = $('#' + subgridTableId).jqGrid('getCell', rowId, 'imageUrl');
              $('#imageUrl', formId).attr('src', imageUrl);
            },
            afterComplete: callbackAfterSubmitForm
          });
        }
      });
      $('#' + subgridTableId).jqGrid('navGrid', '#' + subgridTableId + '_toppager',
      {
        edit:   false,
        add:    false,
        del:    false,
        search: false,
        view:   false
      });
    }
  });
  $('#chn_table').jqGrid('navGrid', '#chn_table_toppager',
  {
    edit:   false,
    add:    false,
    del:    false,
    search: true,
    view:   false
  }, {}, {}, {},
  {
    closeAfterSearch: true,
    closeAfterReset:  true,
    closeOnEscape:    true
  });

  $('#cc_table').jqGrid(
  {
    colModel:
    [
      {
        label:     'MSO ID',
        name:      'msoId',
        index:     'msoId',
        width:     80,
        align:     'center',
        sortable:  false,
        editable:  false
      },
      {
        label:    'Channel ID',
        name:     'channel',
        index:    'channelId',
        width:    100,
        align:    'center',
        sortable: true,
        hidden:   true,
        editable: true,
        editoptions:
        {
          disabled: true
        },
        editrules:
        {
          edithidden: true,
          required:   true,
          number:     true
        }
      },
      {
        label:    'Categoty ID',
        name:     'category',
        index:    'categoryId',
        width:    100,
        align:    'center',
        sortable: true,
        editable: true,
        edittype: 'select',
        editoptions:
        {
          dataUrl:  '/admin/category/categoriesHtmlSelectOptions',
          disabled: false
        },
        editrules:
        {
          required: true,
          number:   true
        }
      },
      {
        label:    'Category Name',
        name:     'name',
        index:    'name',
        width:    200,
        align:    'center',
        sortable: false,
        editable: false,
        editoptions:
        {
          maxlength: 100
        }
      },
      {
        label:    'Updated Time',
        name:     'updateDate',
        index:    'updateDate',
        width:    140,
        align:    'center',
        sortable: true,
        editable: false,
        editoptions:
        {
          disabled: true
        }
      },
      {
        label:    'Created Time',
        name:     'createDate',
        index:    'createDate',
        width:    140,
        align:    'center',
        sortable: true,
        editable: false,
        hidden:   true,
        editoptions:
        {
          disabled: true
        },
        editrules:
        {
          edithidden: true
        }
      },
      {
        label:     'Public',
        name:      'isPublic',
        index:     'isPublic',
        width:     50,
        align:     'center',
        sortable:  false,
        formatter: 'checkbox',
        editable:  false,
        edittype:  'checkbox',
        editoptions:
        {
          value:    'true:false'
        }
      },
      {
        label:     'Ch. Count',
        name:      'channelCount',
        index:     'channelCount',
        width:     70,
        align:     'center',
        sortable:  false,
        editable:  false,
        editoptions:
        {
          disabled: true
        },
        editrules:
        {
          integer: true
        }
      }
    ],
    url:         '/admin/channel/listCategories?channel=0',
    datatype:    'json',
    caption:     'Category List of Channel',
    sortname:    'updateDate',
    sortorder:   'desc',
    rowNum:      10,
    rowList:     [10, 20, 30, 40, 50, 100],
    viewrecords: true,
    height:      'auto',
    hidegrid:    false,
    toppager:    true,
    gridComplete: function()
    {
      var totaltime = $(this).jqGrid('getGridParam', 'totaltime');
      $(this).jqGrid('setCaption', "Category List of Channel - " + $(this).attr('title') + ' (' + totaltime + 'ms)');
    }
  });
  $('#cc_table').jqGrid('navGrid', '#cc_table_toppager',
  {
    edit:     false,
    add:      true,
    del:      true,
    search:   false,
    view:     false,
    addtitle: 'Add This Channel to A Category',
    deltitle: 'Remove This Channel from Category'
  },
  {
    // 'edit' proprtties
  },
  {
    // 'add' proprtties
    url:               '/admin/channel/addCategory',
    addCaption:        'Add This Channel to Category',
    width:             'auto',
    modal:             false,
    jqModal:           true,
    closeAfterAdd:     true,
    closeOnEscape:     true,
    reloadAfterSubmit: true,
    beforeShowForm: function(formId)
    {
      $('#channel', formId).val($('#cc_table').attr('channel'));
      if ($('#cc_table').attr('channel') == '0') {
        $('#category', formId).val('Select A Channel First !');
        $('#category', formId).attr('disabled', true);
      }
    },
    afterComplete: callbackAfterSubmitForm
  },
  {
    // 'delete' properties
    url:               '/admin/channel/deleteCategory',
    caption:           'Remove A Channel',
    msg:               'Do you want to remove a channel from Category ?',
    width:             'auto',
    modal:             false,
    jqModal:           true,
    closeOnEscape:     true,
    reloadAfterSubmit: true,
    afterComplete: callbackAfterSubmitForm
  });

  //////// Category Managment Tab ////////

  $('#cat_table').jqGrid(
  {
    colModel:
    [
      {
        label:     'MSO ID',
        name:      'msoId',
        index:     'msoId',
        width:     80,
        align:     'center',
        search:    true,
        sortable:  true,
        editable:  true,
        edittype:  'select',
        editoptions:
        {
          dataUrl:  '/admin/mso/msoHtmlSelectOptions',
          disabled: false
        },
        editrules:
        {
          required: true,
          number:   true
        },
        searchoptions:
        {
          sopt:    ['eq'],
          dataUrl: '/admin/mso/msoHtmlSelectOptions'
        }
      },
      {
        label:    'Category ID',
        name:     'categoryId',
        index:    'categoryId',
        width:    80,
        align:    'center',
        search:   false,
        sortable: false
      },
      {
        label:    'Category Name',
        name:     'name',
        index:    'name',
        width:    200,
        align:    'center',
        search:   false,
        sortable: true,
        editable: true,
        editoptions:
        {
          maxlength: 100
        },
        editrules:
        {
          required: true
        }
      },
      {
        label:    'Updated Time',
        name:     'updateDate',
        index:    'updateDate',
        width:    140,
        align:    'center',
        search:   false,
        sortable: true,
        editable: false,
        editoptions:
        {
          disabled: true
        }
      },
      {
        label:    'Created Time',
        name:     'createDate',
        index:    'createDate',
        width:    140,
        align:    'center',
        search:   false,
        sortable: true,
        editable: false,
        hidden:   true,
        editoptions:
        {
          disabled: true
        },
        editrules:
        {
          edithidden: true
        }
      },
      {
        label:     'Public',
        name:      'isPublic',
        index:     'isPublic',
        width:     70,
        align:     'center',
        search:    false,
        sortable:  true,
        formatter: 'checkbox',
        editable:  true,
        edittype:  'checkbox',
        editoptions:
        {
          value:    'true:false'
        }
      },
      {
        label:    'Ch. Count',
        name:     'channelCount',
        index:    'channelCount',
        width:    90,
        align:    'center',
        search:   false,
        sortable: true,
        hidden:   false,
        editable: true,
        editrules:
        {
          integer:    true,
          edithidden: true
        }
      }
    ],
    url:         '/admin/category/list',
    datatype:    'json',
    caption:     'Category List',
    sortname:    'updateDate',
    sortorder:   'desc',
    rowNum:      10,
    rowList:     [10, 20, 30, 40, 50, 100],
    viewrecords: true,
    height:      'auto',
    hidegrid:    false,
    toppager:    true,
    gridComplete: function()
    {
      var totaltime = $(this).jqGrid('getGridParam', 'totaltime');
      $(this).jqGrid('setCaption', 'Category List (' + totaltime + 'ms)');
    },
    ondblClickRow: function(rowId)
    {
      $(this).jqGrid('editGridRow', rowId,
      {
        url:               '/admin/category/modify',
        caption:           'Edit Category',
        width:             'auto',
        top:               $(window).scrollTop() + 100, // $(this).offset().top - 50,
        left:              200, // $(this).offset().left + $(this).width() + 20,
        modal:             false,
        jqModal:           true,
        closeAfterEdit:    true,
        closeOnEscape:     true,
        reloadAfterSubmit: true,
        viewPagerButtons:  false,
        recreateForm:      true,
        afterComplete: callbackAfterSubmitForm
      });
    },
    subGrid: true,
    subGridRowExpanded: function(subgridId, rowId)
    {
      var subgridTableId = subgridId + '_t';
      $('#' + subgridId).html('<table id="' + subgridTableId + '" category="' + rowId + '"></table>');
      $('#' + subgridId).attr('style', 'padding: 5px 5px 5px 5px');
      $('#' + subgridTableId).jqGrid(
      {
        colModel:
        [
          {
            label:    'Category ID',
            name:     'category',
            index:    'categoryId',
            width:    80,
            align:    'center',
            hidden:   true,
            sortable: true,
            editable: true,
            editoptions:
            {
              disabled: true
            },
            editrules:
            {
              edithidden: true,
              number:     true
            }
          },
          {
            label:    'Channel ID',
            name:     'channel',
            index:    'channelId',
            width:    80,
            align:    'center',
            sortable: true,
            editable: true,
            editoptions:
            {
              disabled: false
            },
            editrules:
            {
              number: true
            }
          },
          {
            label:    'Channel Name',
            name:     'name',
            index:    'name',
            width:    200,
            align:    'center',
            sortable: false,
            editable: false,
            editoptions:
            {
              maxlength: 100
            }
          },
          {
            label:    'Updated Time',
            name:     'updateDate',
            index:    'updateDate',
            width:    140,
            align:    'center',
            sortable: true,
            editable: false,
            editoptions:
            {
              disabled: true
            }
          },
          {
            label:    'Created Time',
            name:     'createDate',
            index:    'createDate',
            width:    140,
            align:    'center',
            sortable: true,
            editable: false,
            hidden:   true,
            editoptions:
            {
              disabled: true
            },
            editrules:
            {
              edithidden: true
            }
          },
          {
            label:     'Public',
            name:      'isPublic',
            index:     'isPublic',
            width:     50,
            align:     'center',
            sortable:  false,
            formatter: 'checkbox',
            editable:  false,
            edittype:  'checkbox',
            editoptions:
            {
              value:    'true:false'
            }
          },
          {
            label:     'Type',
            name:      'contentType',
            index:     'contentType',
            width:     90,
            align:     'center',
            sortable:  false,
            formatter: 'select',
            editable:  false,
            edittype:  'select',
            editoptions:
            {
              disabled: true,
              value:    channelContentType
            }
          },
          {
            label:    'Sub. Count',
            name:     'subscriberCount',
            index:    'subscriberCount',
            width:    90,
            align:    'center',
            sortable: false,
            hidden:   false,
            editable: false
          }
        ],
        url:         '/admin/category/channelList?category=' + rowId,
        datatype:    'json',
        caption:     'Category Channel List',
        sortname:    'updateDate',
        sortorder:   'desc',
        rowNum:      10,
        rowList:     [10, 20, 30, 40, 50, 100],
        viewrecords: true,
        height:      'auto',
        hidegrid:    false,
        toppager:    true,
        ondblClickRow: function()
        {
          // empty, but don't remove it
        },
        gridComplete: function()
        {
          var totaltime = $(this).jqGrid('getGridParam', 'totaltime');
          $(this).jqGrid('setCaption', 'Category Channel List (' + totaltime + 'ms)');
        }
      });
      $('#' + subgridTableId).jqGrid('navGrid', '#' + subgridTableId + '_toppager',
      {
        edit:     false,
        add:      true,
        del:      true,
        search:   false,
        view:     false,
        addtitle: 'Add A Channel to This Category',
        deltitle: 'Remove This Channel from Category'
      },
      {
        // 'edit' proprtties
      },
      {
        // 'add' proprtties
        url:               '/admin/channel/addCategory',
        addCaption:        'Add Channel to This Category',
        width:             'auto',
        modal:             false,
        jqModal:           true,
        closeAfterAdd:     true,
        closeOnEscape:     true,
        reloadAfterSubmit: true,
        beforeShowForm: function(formId)
        {
          $('#category', formId).val($('#' + subgridTableId).attr('category'));
        },
        afterComplete: callbackAfterSubmitForm
      },
      {
        // 'delete' properties
        url:               '/admin/channel/deleteCategory',
        caption:           'Remove A Channel',
        msg:               'Do you want to remove a channel from Category ?',
        width:             'auto',
        modal:             false,
        jqModal:           true,
        closeOnEscape:     true,
        reloadAfterSubmit: true,
        afterComplete: callbackAfterSubmitForm
      });
    }
  });
  $('#cat_table').jqGrid('navGrid', '#cat_table_toppager',
  {
    edit:   false,
    add:    true,
    del:    false,
    search: true,
    view:   false,
    addtitle: 'Create A New Category'
  },
  {
    // 'edit' properties
  },
  {
    // 'add' proprtties
    url:               '/admin/category/create',
    addCaption:        'Create A New Category',
    width:             'auto',
    modal:             false,
    jqModal:           true,
    closeAfterAdd:     true,
    closeOnEscape:     true,
    reloadAfterSubmit: true,
    recreateForm:      true,
    beforeShowForm: function(formId)
    {
      $('#tr_channelCount').hide();
      $('#isPublic', formId).attr('checked', true);
    },
    afterComplete: callbackAfterSubmitForm
  },
  {
    // 'search' properties
  },
  {
    closeAfterSearch: true,
    closeAfterReset:  true,
    closeOnEscape:    true
  });

  //////// MSO Managment Tab ////////

  $('#mso_table').jqGrid(
  {
    colModel:
    [
      {
        label:    'MSO Logo',
        name:     'logoUrl',
        index:    'logoUrl',
        width:    150,
        align:    'center',
        sortable: true,
        hidden:   true,
        editable: true,
        edittype: 'image',
        editoptions:
        {
          disabled: true
        },
        editrules:
        {
          edithidden: true
        }
      },
      {
        label:    'MSO ID',
        name:     'mso',
        index:    'mso',
        width:    80,
        align:    'center',
        stype:    'select',
        sortable: false,
        editable: true,
        editoptions:
        {
          disabled: true
        }
      },
      {
        label:    'MSO Name',
        name:     'name',
        index:    'name',
        width:    100,
        align:    'center',
        sortable: true,
        editable: true,
        editoptions:
        {
          maxlength: 100
        }
      },
      {
        label:    'MSO Title',
        name:     'title',
        index:    'title',
        width:    100,
        align:    'center',
        sortable: true,
        editable: true,
        editoptions:
        {
          maxlength: 100
        }
      },
      {
        label:    'Updated Time',
        name:     'updateDate',
        index:    'updateDate',
        width:    140,
        align:    'center',
        sortable: true,
        editable: true,
        editoptions:
        {
          disabled: true
        }
      },
      {
        label:    'Created Time',
        name:     'createDate',
        index:    'createDate',
        width:    140,
        align:    'center',
        sortable: true,
        editable: true,
        hidden:   true,
        editoptions:
        {
          disabled: true
        },
        editrules:
        {
          edithidden: true
        }
      },
      {
        label:    'Logo Click URL',
        name:     'logoClickUrl',
        index:    'logoClickUrl',
        width:    150,
        align:    'center',
        sortable: true,
        hidden:   true,
        editable: true,
        editrules:
        {
          edithidden: true
        }
      },
      {
        label:    'Jingle URL',
        name:     'jingleUrl',
        index:    'jingleUrl',
        width:    150,
        align:    'center',
        sortable: true,
        hidden:   true,
        editable: true,
        editrules:
        {
          edithidden: true
        }
      },
      {
        label:     'Type',
        name:      'type',
        index:     'type',
        width:     70,
        align:     'center',
        sortable:  true,
        formatter: 'select',
        editable:  true,
        edittype:  'select',
        editoptions:
        {
          disabled: true,
          value:    '0:Unknown;1:NN;2:MSO'
        }
      },
      {
        label:     'Language',
        name:      'preferredLangCode',
        index:     'preferredLangCode',
        width:     90,
        align:     'center',
        sortable:  true,
        formatter: 'select',
        editable:  true,
        edittype:  'select',
        editoptions:
        {
          value: 'en:en;zh:zh;zh-tw:zh-tw'
        }
      },
      {
        label:    'Contact Email',
        name:     'contactEmail',
        index:    'contactEmail',
        width:    70,
        align:    'center',
        hidden:   true,
        sortable: true,
        editable: true,
        editrules:
        {
          edithidden: true,
          email:      true
        }
      },
      {
        label:    'Introduction',
        name:     'intro',
        index:    'intro',
        width:    150,
        align:    'center',
        sortable: true,
        hidden:   true,
        editable: true,
        edittype: 'textarea',
        editoptions:
        {
          rows: '3'
        },
        editrules:
        {
          edithidden: true
        }
      },
      {
        label:    'Reg.',
        name:     'viewers',
        index:    'viewers',
        width:    60,
        align:    'center',
        sortable: false,
        hidden:   false,
        editable: false
      }
    ],
    url:         '/admin/mso/list',
    datatype:    'json',
    caption:     'MSO List',
    sortname:    'updateDate',
    sortorder:   'desc',
    rowNum:      10,
    rowList:     [10, 20, 30, 40, 50, 100],
    viewrecords: true,
    height:      'auto',
    hidegrid:    false,
    toppager:    true,
    gridComplete: function()
    {
      var totaltime = $(this).jqGrid('getGridParam', 'totaltime');
      $(this).jqGrid('setCaption', 'MSO List (' + totaltime + 'ms)');
    },
    ondblClickRow: function(rowId)
    {
      var logoUrl = $(this).jqGrid('getCell', rowId, 'logoUrl');
      $(this).jqGrid('setColProp', 'logoUrl', {editoptions: {src: logoUrl}});
      $(this).jqGrid('editGridRow', rowId,
      {
        url:               '/admin/mso/modify',
        caption:           'Edit MSO Meta',
        width:             'auto',
        top:               $(window).scrollTop() + 50, // $(this).offset().top - 50,
        left:              200, // $(this).offset().left + $(this).width() + 20,
        modal:             false,
        jqModal:           true,
        closeAfterEdit:    true,
        closeOnEscape:     true,
        reloadAfterSubmit: true,
        viewPagerButtons:  true,
        recreateForm:      true,
        beforeCheckValues: function(postData, formId, mode)
        {
          postData['logoUrl'] = $('#logoUrl', formId).attr('src');
          return postData;
        },
        beforeShowForm: function(formId)
        {
          $('#logoUrl', formId).click(function()
          {
            var logoUrl = prompt('Please enter new logo URL', $(this).attr('src'));
            if (logoUrl != null) {
              $(this).attr('src', logoUrl);
            }
          });
        },
        afterclickPgButtons: function(whichButton, formId, rowId)
        {
          var logoUrl = $('#mso_table').jqGrid('getCell', rowId, 'logoUrl');
          $('#logoUrl', formId).attr('src', logoUrl);
        },
        afterComplete: callbackAfterSubmitForm
      });
    },
    subGrid: true,
    subGridRowExpanded: function(subgridId, rowId)
    {
      var subgridTableId = subgridId + '_t';
      $('#' + subgridId).html('<table id="' + subgridTableId + '"></table>');
      $('#' + subgridId).attr('style', 'padding: 5px 5px 5px 5px');
      $('#' + subgridTableId).jqGrid(
      {
        colModel:
        [
          {
            label:    'Channel ID',
            name:     'channel',
            index:    'channelId',
            width:    100,
            align:    'center',
            sortable: true,
            editable: true,
            editoptions:
            {
              disabled: true
            },
            editrules:
            {
              required: true,
              number:   true
            }
          },
          {
            label:    'Channel Name',
            name:     'name',
            index:    'name',
            width:    180,
            align:    'center',
            sortable: false
          },
          {
            label:    'Updated Time',
            name:     'updateDate',
            index:    'updateDate',
            width:    140,
            align:    'center',
            sortable: true
          },
          {
            label:    'Created Time',
            name:     'createDate',
            index:    'createDate',
            width:    140,
            align:    'center',
            sortable: true,
            hidden:   true
          },
          {
            label:     'Seq.',
            name:      'seq',
            index:     'seq',
            width:     60,
            align:     'center',
            sortable:  true,
            editable:  true,
            editrules:
            {
              minValue: 1,
              maxValue: 81,
              required: true,
              integer:  true
            }
          },
          {
            label:     'Type',
            name:      'type',
            index:     'type',
            width:     70,
            align:     'center',
            sortable:  true,
            formatter: 'select',
            editable:  true,
            edittype:  'select',
            editoptions:
            {
              value: '1:General;2:Readonly;0:Unknown'
            }
          }
        ],
        url:         '/admin/msoIpg/list?mso=' + rowId,
        datatype:    'json',
        caption:     'MSO IPG List',
        sortname:    'updateDate',
        sortorder:   'desc',
        rowNum:      10,
        rowList:     [10, 20, 30, 40, 50, 100],
        viewrecords: true,
        height:      'auto',
        hidegrid:    false,
        toppager:    true,
        ondblClickRow: function(subRowId)
        {
          $(this).jqGrid('editGridRow', subRowId,
          {
            url:               '/admin/msoIpg/modify?mso=' + rowId,
            caption:           'Edit MSO IPG',
            width:             'auto',
            top:               $(window).scrollTop() + 100, // $(this).offset().top - 50,
            left:              200, // $(this).offset().left + $(this).width() + 20,
            modal:             false,
            jqModal:           true,
            closeAfterEdit:    true,
            closeOnEscape:     true,
            reloadAfterSubmit: true,
            viewPagerButtons:  false,
            recreateForm:      true,
            afterComplete: callbackAfterSubmitForm
          });
        },
        gridComplete: function()
        {
          var totaltime = $(this).jqGrid('getGridParam', 'totaltime');
          $(this).jqGrid('setCaption', 'MSO IPG List (' + totaltime + 'ms)');
        }
      });
      $('#' + subgridTableId).jqGrid('navGrid', '#' + subgridTableId + '_toppager',
      {
        edit:     false,
        add:      true,
        del:      true,
        search:   false,
        view:     false,
        addtitle: 'Add A New Channel to IPG',
        deltitle: 'Remove A Channel From IPG'
      },
      {
        // 'edit' properties
      },
      {
        // 'add' proprtties
        url:               '/admin/msoIpg/add?mso=' + rowId,
        addCaption:        'Add A Channel to IPG',
        width:             'auto',
        modal:             false,
        jqModal:           true,
        closeAfterAdd:     true,
        closeOnEscape:     true,
        reloadAfterSubmit: true,
        beforeShowForm: function(formId)
        {
          $('#channel', formId).attr('disabled', false);
        },
        afterComplete: callbackAfterSubmitForm
      },
      {
        // 'delete' properties
        url:               '/admin/msoIpg/delete?mso=' + rowId,
        caption:           'Remove A Channel',
        msg:               'Do you want to remove a channel from IPG ?',
        width:             'auto',
        modal:             false,
        jqModal:           true,
        closeOnEscape:     true,
        reloadAfterSubmit: true,
        afterComplete: callbackAfterSubmitForm
      });
    }
  });
  $('#mso_table').jqGrid('navGrid', '#mso_table_toppager',
  {
    edit:   false,
    add:    false,
    del:    false,
    search: false,
    view:   false
  });

  //////// User Managment Tab ////////

  $('#user_table').jqGrid(
  {
    colModel:
    [
      {
        label:    'Image',
        name:     'imageUrl',
        index:    'imageUrl',
        width:    150,
        align:    'center',
        sortable: true,
        hidden:   true,
        search:   false,
        editable: true,
        edittype: 'image',
        editoptions:
        {
          disabled: true
        },
        editrules:
        {
          edithidden: true
        }
      },
      {
        label:    'MSO ID',
        name:     'mso',
        index:    'msoId',
        width:    80,
        align:    'center',
        search:   true,
        stype:    'select',
        sortable: true,
        editable: true,
        editoptions:
        {
          disabled: true
        },
        searchoptions:
        {
          sopt:    ['eq'],
          dataUrl: '/admin/mso/msoHtmlSelectOptions'
        }
      },
      {
        label:    'User ID',
        name:     'user',
        index:    'userId',
        width:    80,
        align:    'center',
        search:   false,
        sortable: false,
        editable: true,
        editoptions:
        {
          disabled: true
        }
      },
      {
        label:    'Name',
        name:     'name',
        index:    'name',
        width:    100,
        align:    'center',
        search:   false,
        sortable: true,
        editable: true,
        editoptions:
        {
          maxlength: 100
        }
      },
      {
        label:    'Email',
        name:     'email',
        index:    'email',
        width:    130,
        align:    'center',
        search:   false,
        sortable: true,
        editable: true,
        editoptions:
        {
          maxlength: 100
        }
      },
      {
        label:    'Updated Time',
        name:     'updateDate',
        index:    'updateDate',
        width:    140,
        align:    'center',
        search:   false,
        sortable: true,
        editable: true,
        editoptions:
        {
          disabled: true
        }
      },
      {
        label:    'Created Time',
        name:     'createDate',
        index:    'createDate',
        width:    140,
        align:    'center',
        search:   false,
        sortable: true,
        editable: true,
        hidden:   true,
        editoptions:
        {
          disabled: true
        },
        editrules:
        {
          edithidden: true
        }
      },
      {
        label:     'Type',
        name:      'type',
        index:     'type',
        width:     70,
        align:     'center',
        search:    false,
        sortable:  true,
        formatter: 'select',
        editable:  true,
        edittype:  'select',
        editoptions:
        {
          disabled: true,
          value:    { 1: 'Admin', 2: 'TBC', 3: 'TCO', 4: 'User', 5: 'NN' }
        }
      },
      {
        label:     'Age',
        name:      'age',
        index:     'age',
        width:     70,
        align:     'center',
        hidden:    true,
        search:    false,
        sortable:  true,
        editable:  true,
        editrules:
        {
          edithidden: true
        }
      },
      {
        label:    'Introduction',
        name:     'intro',
        index:    'intro',
        width:    150,
        align:    'center',
        search:   false,
        sortable: true,
        hidden:   true,
        editable: true,
        edittype: 'textarea',
        editoptions:
        {
          rows: '3'
        },
        editrules:
        {
          edithidden: true
        }
      }
    ],
    url:         '/admin/nnuser/list',
    datatype:    'json',
    caption:     'User List',
    sortname:    'updateDate',
    sortorder:   'desc',
    rowNum:      10,
    rowList:     [10, 20, 30, 40, 50, 100],
    viewrecords: true,
    height:      'auto',
    hidegrid:    false,
    toppager:    true,
    gridComplete: function()
    {
      var totaltime = $(this).jqGrid('getGridParam', 'totaltime');
      $(this).jqGrid('setCaption', 'User List (' + totaltime + 'ms)');
    },
    ondblClickRow: function(rowId)
    {
      // empty, but don't remove it
    },
    subGrid: true,
    subGridRowExpanded: function(subgridId, rowId)
    {
      var subgridTableId = subgridId + '_t';
      $('#' + subgridId).html('<table id="' + subgridTableId + '"></table>');
      $('#' + subgridId).attr('style', 'padding: 5px 5px 5px 5px');
      $('#' + subgridTableId).jqGrid(
      {
        colModel:
        [
          {
            label:    'Channel ID',
            name:     'channel',
            index:    'channelId',
            width:    100,
            align:    'center',
            sortable: true,
            editable: true,
            editoptions:
            {
              disabled: true
            },
            editrules:
            {
              required: true,
              number:   true
            }
          },
          {
            label:    'Channel Name',
            name:     'name',
            index:    'name',
            width:    180,
            align:    'center',
            sortable: false
          },
          {
            label:    'Updated Time',
            name:     'updateDate',
            index:    'updateDate',
            width:    140,
            align:    'center',
            sortable: true
          },
          {
            label:    'Created Time',
            name:     'createDate',
            index:    'createDate',
            width:    140,
            align:    'center',
            sortable: true,
            hidden:   true
          },
          {
            label:     'Seq.',
            name:      'seq',
            index:     'seq',
            width:     60,
            align:     'center',
            sortable:  true,
            editable:  true,
            editrules:
            {
              minValue: 1,
              maxValue: 81,
              required: true,
              integer:  true
            }
          },
          {
            label:     'Type',
            name:      'type',
            index:     'type',
            width:     70,
            align:     'center',
            sortable:  true,
            formatter: 'select',
            editable:  true,
            edittype:  'select',
            editoptions:
            {
              value: '1:General;2:Readonly;0:Unknown'
            }
          }
        ],
        url:         '/admin/nnuser/subscription?id=' + rowId,
        datatype:    'json',
        caption:     'Subscription',
        sortname:    'updateDate',
        sortorder:   'desc',
        rowNum:      10,
        rowList:     [10, 20, 30, 40, 50, 100],
        viewrecords: true,
        height:      'auto',
        hidegrid:    false,
        toppager:    true,
        ondblClickRow: function(subRowId)
        {
          // empty, but don't remove it
        },
        gridComplete: function()
        {
          var totaltime = $(this).jqGrid('getGridParam', 'totaltime');
          $(this).jqGrid('setCaption', 'Subscription (' + totaltime + 'ms)');
        }
      });
      $('#' + subgridTableId).jqGrid('navGrid', '#' + subgridTableId + '_toppager',
      {
        edit:   false,
        add:    false,
        del:    false,
        search: false,
        view:   false
      });
    }
  });
  $('#user_table').jqGrid('navGrid', '#user_table_toppager',
  {
    edit:   false,
    add:    false,
    del:    false,
    search: true,
    view:   false
  }, {}, {}, {},
  {
    closeAfterSearch: true,
    closeAfterReset:  true,
    closeOnEscape:    true
  });
});
