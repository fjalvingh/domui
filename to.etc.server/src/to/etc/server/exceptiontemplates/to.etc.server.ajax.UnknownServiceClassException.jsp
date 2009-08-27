<%@page language="java" %>
<%@page import="to.etc.server.ajax.UnknownServiceClassException"%>
<%
	UnknownServiceClassException usx = (UnknownServiceClassException) pageContext.getRequest().getAttribute("x");
%>
<h1>Service handler class cannot be located</h1>
<p>The service you tried to access is not known because the class which handles 
this service, <span class="codename">${ x.requestedClass }</span>, cannot be
located by the server. Check your call to make sure you use the correct service path or change the "default package path"
for the server.</p>
