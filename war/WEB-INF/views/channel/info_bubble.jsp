<%@ page isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  <span id="tail"></span>
  <ul class="info-list">
    <li class="ch-name">Channel Name</li>
    <li><span class="enhance">Channel info:</span><c:out value="${channel.intro}"/></li>
    <li><span class="enhance">Channel metadata:</span><c:out value="${channel.tag}"/></li>
    <li class="ch-control"><a href="/channel/edit/<c:out value="${channel.id}"/>" class="btn-editch">Edit</a></li>
  </ul>
