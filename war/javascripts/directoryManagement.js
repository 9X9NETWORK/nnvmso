$(function()
{
  $('.ch_normal').CreateBubblePopup(
  {
    position:  'top',
    align:     'center',
    innerHtml: '<h2 class="popTitle">頻道標題</h2><br/> 節目數量 : 10 <br/> 更新時間 ：2010/10/24 &nbsp;&nbsp; 16:34:30 ',
    innerHtmlStyle:
    {
      'color':      '#292929',
      'text-align': 'left',
      'font-size':  '0.8em'
    },
    themeName: 'all-black',
    themePath: '/images/cms'
  });
  $('#treeview').jstree({});
});
