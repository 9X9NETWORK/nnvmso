<html> 
<head>
<title>S3 POST Form</title> 
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <body>          
    <form action="http://com-nnaws.s3.amazonaws.com/" id="s3form" method="post" enctype="multipart/form-data">
      <input type="hidden" name="key" value="<%= request.getAttribute("key") %>" >
      <input type="hidden" name="AWSAccessKeyId" value="<%= request.getAttribute("AWSAccessKeyId") %>" > 
      <input type="hidden" name="acl" value="<%= request.getAttribute("acl") %>" >
      <input type="hidden" name="success_action_redirect" value="<%= request.getAttribute("success_action_redirect") %>" >
      <input type="hidden" name="policy" value="<%= request.getAttribute("policy") %>">
      <input type="hidden" name="signature" value="<%= request.getAttribute("signature") %>">
      <input type="hidden" name="Content-Type" id="contentType" value="" />
      <input type="hidden" name="x-amz-meta-filename" value="<%= request.getAttribute("x-amz-meta-filename") %>" >
      <input type="hidden" name="x-amz-meta-type" value="<%= request.getAttribute("x-amz-meta-type") %>" >
      <input type="hidden" name="x-amz-meta-token" value="<%= request.getAttribute("x-amz-meta-token") %>" >
      <input type="hidden" name="x-amz-meta-creatDate" value="<%= request.getAttribute("x-amz-meta-creatDate") %>" >      
      File to upload to S3:
      <input type="file" name="file" id="file" />  
      <br> 
      <input type="submit" value="Upload File to S3"> 
    </form> 
  </body>
</html>
