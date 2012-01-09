<%@page language="java" %>
<%@page import="to.etc.server.ajax.UnknownServiceMethodException"%>
<%
	UnknownServiceMethodException usx = (UnknownServiceMethodException) pageContext.getRequest().getAttribute("x");
%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.lang.reflect.Method"%>
<h1>Service method cannot be resolved on the service class</h1>
<p>The service you tried to access is not known because the class which handles 
this service, <span class="codename">${ x.serviceClass.name }</span>, does not
contain a valid method with the name <span class="codename">${ x.serviceMethod }</span>. Please check the
following:
<ul>
	<li>Did you make a spelling error in the call?</li>
	<li>Is the method you are trying to call public in the class?</li>
	<li>Is the method name unique in the class? If the method name occurs 2more than once (but with different
		parameters) the server cannot distinguish between them!</li>
</ul>

<p>Known methods that can be called in this class are:<br/>
<%
	Map<String, Integer>	cm = new HashMap<String, Integer>();
	Method[] mar = usx.getServiceClass().getMethods();
	for(Method m : mar)
	{
		Integer i = cm.get(m.getName());
		if(i == null)
			cm.put(m.getName(), Integer.valueOf(1));
		else
			cm.put(m.getName(), Integer.valueOf(i.intValue()+1));
	}
	StringBuilder sb = new StringBuilder();
	for(Method m : mar)
	{
		String name = m.getName();
		if(name.equalsIgnoreCase("getclass") || ! name.startsWith("get"))
			continue;
		Integer count = cm.get(name);
		if(count.intValue() == 1)
		{
			out.write("<i>");
			out.write(m.toString());
			out.write("</i><br/>");
		}
		else if(count.intValue() > 1)
		{
			if(sb.length() == 0)
				sb.append("The following method(s) cannot be called because their name occurs more than once:");
			else
				sb.append(", ");
			sb.append(m.toString());
		}
	}
	if(sb.length() > 0)
	{
		out.write("<br/>");
		out.write(sb.toString());
	}
%>

</p>
