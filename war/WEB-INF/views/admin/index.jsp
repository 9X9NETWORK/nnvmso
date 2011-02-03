<ul>
  <li>initialize (works only for small set of data)
    <ul>
      <li><a href="/admin/init/initAll?devel=0&debug=1">turn on debug</a></li>          	   
      <li><a href="/admin/init/initAll?devel=0&debug=0">turn off debug</a></li>          	   
      <li><a href="/admin/init/deleteAll">deleteAll</a></li>
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
		  http://host/admin/mso/modify?id=xx&logoUrl=xx&logoClickUrl=xx&jingleUrl                        	   
    </ul>
  </li>
</ul>

<ul>
  <li>Category Listing
    <ul>
      <li><a href="/admin/category/list">category listing</a></li>
      <li>category listing by brand <br/>
          http://host/admin/category/list?id=2417
      </li>
      <li>change data <br/>
		  http://host/admin/mso/modify?id=xx&logoUrl=xx&logoClickUrl=xx&jingleUrl                        	   
    </ul>
  </li>
</ul>

<ul>
  <li>Channel listing
    <ul>
      <li><a href="/admin/channel/list">channel listing</a></li>
      <li>channel modify <br/>
          http://host/admin/channel/modify?id=xx&name=xx&status=xx&programCount=xx            	   
    </ul>
  </li>
</ul>

