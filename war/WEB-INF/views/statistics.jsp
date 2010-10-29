<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<script type="text/javascript">
$(document).ready(function() {
	$("#statistics").addClass("on");
})
</script>

<form id="statistics-form">
  <div class="container">
    <div id="statistics-content" class="content">
      <p class="titlebar">Statistics</p>
      <div class="formholder">
        
      </div>
    </div>
    <div id="footer">
      <p id="copyright">&copy; 2010 9x9CloudTV. All rights reserved.</p>
    </div>
  </div>
</form>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>