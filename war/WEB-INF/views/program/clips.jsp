<%@ page isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

      <div id="onshow-list" class="show-list">
        <ul class="list-title">
          <li class="title-text">On-Air Shows</li>
          <li><a href="javascript:;" class="btn-arrange">Save arrangement</a></li>          
          <li class="disable action"><a href="javascript:;" class="btn-delete">Delete</a></li>
          <li class="disable action"><a href="javascript:;" class="btn-movedown">Move to Off-Air</a></li>
          <li class="disable action"><a href="javascript:;" class="btn-editshow">Edit</a></li>
          <li><a href="javascript:;" class="btn-all">Select all</a></li>
        </ul>
        <div class="constrain">
          <ul class="clip-list" id="sortable">
            <c:forEach items="${onair}" var="p">                    
            <li id="<c:out value="${p.id}"/>">
              <img src="<c:out value="${p.imageUrl}"/>" id="s01">
              <span class="status play"></span>
              <!-- <span class="time"><c:out value="${p.duration} }"/></span> -->
              <a href="javascript:;" class="check"></a>
              <p class="clip-title"><c:out value="${p.name}"/></p>
            </li>
            </c:forEach>                              
          </ul>
        </div>
      </div>      
      <div id="offshow-list" class="show-list">        
        <ul class="list-title">        
          <li class="title-text">Off-Air Shows</li>
          <li class="action disable"><a href="javascript:;" class="btn-delete">Delete</a></li>
          <li class="action disable"><a href="javascript:;" class="btn-moveup">Move to On-Air</a></li>
          <li class="action disable"><a href="javascript:;" class="btn-editshow">Edit</a></li>
          <li><a href="javascript:;" class="btn-all">Select all</a></li>
        </ul>
        <div class="constrain"> 
          <ul class="clip-list">
          <c:forEach items="${offair}" var="p">
            <c:if test="${p.status == 0}"><li id="<c:out value="${p.id}"/>" class="fail"></c:if>
            <c:if test="${p.status == 1}"><li id="<c:out value="${p.id}"/>" ></c:if>
            <c:if test="${p.status == 2}"><li id="<c:out value="${p.id}"/>" class="processing"></c:if>                          
              <c:if test="${p.status == 0}"><span class="status fail"></span></c:if>
	          <c:if test="${p.status == 1}"><img src="<c:out value="${p.imageUrl}"/>" id="s09" />
	                                        <span class="status play"></span></c:if>                            
              <c:if test="${p.status == 2}"><span class="status processing"></span></c:if>
              <!-- <span class="time">10:13</span> --> 
              <a href="javascript:;" class="check"></a>
              <p class="clip-title"><c:out value="${p.name}"/></p>
            </li>
          </c:forEach>  
          </ul>          
        </div>
      </div>
