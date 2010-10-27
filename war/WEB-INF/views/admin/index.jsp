<%@ include file="/WEB-INF/views/layout/header.jsp" %>
<%@ page isELIgnored="false"%>

<div class="container">
<div id="signin-content" class="content">

<b>mso account:</b> a@a.com, foobie <br/>
<b>user account:</b> u@u.com <br/>
<b>channel create:</b><a href="/channel/create">channel create</a><br/>
<b>channel edit: </b><c:out value="${hostname}"/>/channel/edit/{channelId}<br/>
<b>program create: </b><c:out value="${hostname}"/>/show/create/{channelId}<br/>
<b>program edit: </b><c:out value="${hostname}"/>/show/edit/{programId}<br/>
<b>player edit:</b><a href="/admin/playerEdit?id=<c:out value="${msoKey}"/>"/>edit</a><br/>
<b>player Container:</b><a href="/admin/playerContainer?id=<c:out value="${msoKey}"/>"> container </a><br/>
<br/>
<b>channelLineup:</b>
  <a href="<c:out value="${hostname}"/>/player/channelLineup?user=<c:out value="${userKey}"/>"><c:out value="${hostname}"/>/player/channelLineup?user=<c:out value="${userKey}"></c:out></a>
<br/><br/>
<b>programInfo by * and user id :</b>
  <a href="<c:out value="${hostname}"/>/player/programInfo?channel=*&user=<c:out value="${userKey}"/>"><c:out value="${hostname}"/>/player/programInfo?channel=*&user=<c:out value="${userKey}"></c:out></a>
<br/>
<b>programInfo single channel:</b><c:out value="${hostname}"/>/player/programInfo?channel={channelId},{channelId}<br/>
<b>programInfo single channel:</b><c:out value="${hostname}"/>/player/programInfo?channel={channelId}<br/>
<br/>

<br/>
<b>login:&nbsp;</b><a href="/mso/login">login</a> &nbsp;aws@9x9.com; foobie<br/>
<b>aws api:</b> <c:out value="${hostname}"/>/aws/contentUpdate<br/>
<b>program create:</b><a href="/show/create/<c:out value="${awsChannel}"/>">show create via upload</a><br/>
<b>program list:</b><a href="/show/list/<c:out value="${awsChannel}"/>">show listing</a><br/>
</div>
</div>
</div>