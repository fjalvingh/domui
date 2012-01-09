<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page language="java" %>
<%@page import="to.etc.server.ajax.ServiceException, java.util.*, to.etc.util.StringTool"%>
<%@page contentType="text/html; charset=UTF-8" %>
<%
	Throwable t = (Throwable) pageContext.getRequest().getAttribute("x");
%>
<html>
<head>
	<base href="${ base }" />
	<title>Exception: ${x}</title>
	<link rel="stylesheet" href="__root/exceptions.css" />
	<script type="text/javascript">
<!--
function toggle(id,sp)
{
	var element = document.getElementById(id);
	if(element.style.display == "none")
	{
		element.style.display="block";
		sp.innerHTML = '<img src="__root/minus.png" border="0">';
	}
	else
	{
		element.style.display="none";
		sp.innerHTML = '<img src="__root/plus.png" border="0">';
	}
}
-->
	</script>
</head>
<body>

	<jsp:include flush="false" page="${include}"></jsp:include>
	<%
		if(t instanceof ServiceException && ((ServiceException)t).hasContext())
		{
			ServiceException sx = (ServiceException) t;
	%>	
		<div class="xsectionheader">
			Servlet request context <span class="toggler" onclick="toggle('context', this)"><img src="__root/plus.png" /></span>
		</div>
		<div class="xsection" id="context" style="display:none">
			<table>
				<tr>
					<td class="xitemname" nowrap="nowrap" width="10%">Input URL</td>
					<td class="xitemvalue"><%= sx.getUrl() %></td>
				</tr>
				<tr>
					<td class="xitemname" nowrap="nowrap" width="10%">HTTP Method</td>
					<td class="xitemvalue"><%= sx.getMethod() %></td>
				</tr>
				<tr>
					<td class="xitemname" nowrap="nowrap" width="10%">Servlet path</td>
					<td class="xitemvalue"><%= sx.getServletPath() %></td>
				</tr>
				<tr>
					<td class="xitemname" nowrap="nowrap" width="10%">Query String</td>
					<td class="xitemvalue"><%= sx.getQueryString() %></td>
				</tr>
				<tr>
					<td class="xitemname" nowrap="nowrap" width="10%">Remote address</td>
					<td class="xitemvalue"><%= sx.getRemoteAddress() %></td>
				</tr>
				<tr>
					<td class="xitemname" nowrap="nowrap" width="10%">Remote USER</td>
					<td class="xitemvalue"><%= sx.getRemoteUser() %></td>
				</tr>
			</table>

			<div class="xsectionheader">
				Request parameters <span class="toggler" onclick="toggle('reqparam', this)"><img src="__root/plus.png" /></span>
			</div>
			<div class="xparams" id="reqparam" style="display: none">
				<table border="1" width="100%">
				<%
					for(Map.Entry<String, String[]> e : sx.getParameters().entrySet())
					{
						String name = (String) e.getKey();
						String[] val = (String[]) e.getValue();
				%>
					<tr>
						<td class="xparamname" nowrap="nowrap" width="10%">
							<%= name %>
						</td>
						<td class="xitemvalue"><% 
							if(val == null || val.length == 0)
								out.write("(No values)");
							else if(val.length == 1)
								out.write(StringTool.htmlStringize(val[0]));
							else
							{
								for(int i = 0; i < val.length; i++)
								{
									out.write("<i>"+i+":</i>&nbsp;");
									out.write(StringTool.htmlStringize(val[i]));
									out.write("<br/>\n");
								}
							}
						%></td>
					</tr>
				<%
					}
				%>
				</table>
			</div>


			<div class="xsectionheader">
				Request headers <span class="toggler" onclick="toggle('reqhdr', this)"><img src="__root/plus.png" /></span>
			</div>
			<div class="xparams" id="reqhdr" style="display: none">
				<table border="1" width="100%">
				<%
					for(Map.Entry<String, String[]> e : sx.getHeaders().entrySet())
					{
						String name = (String) e.getKey();
						String[] val = (String[]) e.getValue();
				%>
					<tr>
						<td class="xparamname" nowrap="nowrap" width="10%">
							<%= name %>
						</td>
						<td class="xitemvalue"><% 
							if(val == null || val.length == 0)
								out.write("(No values)");
							else if(val.length == 1)
								out.write(StringTool.htmlStringize(val[0]));
							else
							{
								for(int i = 0; i < val.length; i++)
								{
									out.write("<i>"+i+":</i>&nbsp;");
									out.write(StringTool.htmlStringize(val[i]));
									out.write("<br/>\n");
								}
							}
						%></td>
					</tr>
				<%
					}
				%>
				</table>
			</div>

		</div>
		<div>&nbsp</div>
	<%
		}
		int idnr = 0;
		Set doneset = new HashSet();
		String ext = "";
		String nested = "";
	%>

	<%-- Stack trace code. --%>
	<div class="xsectionheader">
		Stack dump for this (and nested) exceptions. <span class="toggler" onclick="toggle('stacktraces', this)"><img src="__root/plus.png" /></span>
	</div>
	<div id="stacktraces" class="xsection" style="display: none;">
		<%
			Throwable current = t;
			while(true)
			{
				doneset.add(current);
				
		%>
		<div class="xexceptionname <%= nested %>">
			<%= ext+current.toString() %> <span class="toggler" onclick="toggle('stack<%= idnr %>', this)"><img src="__root/plus.png" /></span>
		<div class="xstacktrace" id="stack<%= idnr %>" style="display: none">
			<pre><%
				/*
				 * Dump a stacktrace for the current frame only.
				 */
				StackTraceElement[] ar = current.getStackTrace();
				if(ar == null || ar.length == 0)
					out.write("? No stack trace data available!?");
				else
				{
					for(int i = 0; i < ar.length; i++)
					{
						out.write(ar[i].toString());
						out.write("\n");
					}
				}
			%></pre>
		</div>

		<%
				Throwable next = null;
				if(current instanceof ServletException)
				{
					next = ((ServletException) current).getRootCause();
					if(next == null || doneset.contains(next))
						break;
				}
				else
				{
					next = current.getCause();
					if(next == null || doneset.contains(next))
						break;
				}
				current = next;
				ext = "Caused by: ";
				idnr++;
				nested = " nested";
			}
			
			while(idnr-- >= 0)
				out.write("</div>");
		%>
	</div>

</body>
</html>
