<%@page language="java" %>
<%@page import="to.etc.server.ajax.ServiceException"%>
<%
	ServiceException usx = (ServiceException) pageContext.getRequest().getAttribute("x");
%>
<h1>Service exception</h1>
<p>${x}</p>
