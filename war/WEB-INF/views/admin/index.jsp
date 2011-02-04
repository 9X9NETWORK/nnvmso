<ul>
  <li>initialize (works only for small set of data)
    <ul>
      <li><a href="/admin/init/initDevel?debug=1">turn on debug</a></li>          	   
      <li><a href="/admin/init/initDevel?debug=0">turn off debug</a></li>          	   
      <li><a href="/admin/init/initProTask?devel=0&trans=1&debug=1">turn on production data, but debug=1</a></li>
      <li><a href="/admin/init/deleteAll">deleteAll</a></li>
    </ul>
  </li> 
</ul>

<ul>
  <li>Cache
    <ul>
      <li><a href="/admin/cache/deleteAll">deleteAll</a></li>      
      <li>cache category, one mso at a time. <br/>
		  add, http://host/admin/cache/category?id=xx  <br/>
		  delete, http://host/admin/cache/category?id=123&delete=1  <br/>
		  list, <a href="/admin/cache/category?list=1"> list </a> 
	  </li>
	  <li>cache current mso [1. the host url is what current mso is 2. check config, current mso] <br/>
	  	  add, http://host/admin/cache/mso <br/>
	  	  delete, http://host/admin/cache/mso?delete=1 <br/>	  	  
	  	  list, <a href="/admin/cache/mso?list=1"> list </a>
    </ul>
  </li>
</ul>

<ul>
  <li>Config
    <ul>
      <li><a href="/admin/config/transcodingServer">transcodingServer</a></li>
      <li><a href="/admin/config/mso">current mso</a></li>
      <li>change config, examples<br/> 
      	  http://host/admin/config/changeConfig?msoName=9x9&key=cdn&value=akamai <br/> 
      	  http://host/admin/config/changeConfig?msoName=5f&key=cdn&value=amazon <br/> 
      	  http://host/admin/config/changeConfig?msoName=5f&key=debug&value=1 <br/> 
      	  http://host/admin/config/changeConfig?msoName=5f&key=debug&value=0 <br/>         	  
      </li>      	   
    </ul>
  </li>
</ul>

<ul>
  <li>Branding
    <ul>
      <li><a href="/admin/mso/list">mso listing</a></li>
      <li>change data <br/>
		  http://host/admin/mso/modify?id=123&logoUrl=xx&logoClickUrl=xx&jingleUrl=xx                       	   
    </ul>
  </li>
</ul>

<ul>
  <li>Category Listing
    <ul>
      <li><a href="/admin/category/list">category listing</a></li>
      <li>category listing by brand <br/>
          http://host/admin/category/list?mso=xx
      </li>
      <li>change data <br/>
		  http://host/admin/category/modify?id=123&name=xxx&&isPublic=1&channelCount=123
	  </li>
	  <li>list every channel under a category <br/>
	      http://host/admin/category/channelList?id=123 
	  </li>
	      	                          	   
    </ul>
  </li>
</ul>

<ul>
  <li>MsoIpg
    <ul>
      <li>msoIpg listing by brand <br/>
          http://host/admin/msoIpg/list?mso=123
      </li>
    </ul>
  </li>
</ul>

<ul>
  <li>Channel listing
    <ul>
      <li><a href="/admin/channel/list">channel listing</a></li>
      <li>channel modify <br/>
          http://host/admin/channel/modify?id=123&name=xx&status=1&programCount=123
      </li>                	   
    </ul>
  </li>
</ul>

