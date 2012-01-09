<%@ page language="java" %>
<%@ page contentType="text/html" pageEncoding="iso-8859-1" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="to.etc.dbpool.ConnectionPool"%>
<%@page import="to.etc.dbpool.PoolManager"%>
<%
    String message = null;
    String action = request.getParameter("action");
    String id = request.getParameter("pool");
    boolean displaystacks = false;
    ConnectionPool  ipool = null;
    boolean showerrors = false;

    if(id != null && action != null) {
        ipool = PoolManager.getInstance().getPool(id);
        if(ipool == null)
            message = "The database pool "+id+" is not found.";
        else {
            if("traceon".equals(action)) {
                if(! ipool.dbgIsStackTraceEnabled()) {
                    message = "Use tracing for pool "+id+" enabled.";
                    ipool.dbgSetStacktrace(true);
                }
            } else if("traceoff".equals(action)) {
                if(ipool.dbgIsStackTraceEnabled()) {
                    message = "Use tracing for pool "+id+" disabled.";
                    ipool.dbgSetStacktrace(false);
                }
            } else if("errorson".equals(action)) {
                if(! ipool.isSavingErrors()) {
                    message = "Enabled error logging for pool "+id;
                    ipool.setSaveErrors(true);
                }
            } else if("errorsoff".equals(action)) {
                if(ipool.isSavingErrors()) {
                    message = "Disabled error logging for pool "+id;
                    ipool.setSaveErrors(false);
                }
            } else if("stacks".equals(action)) {
                displaystacks = true;
            } else if("showerrors".equals(action)) {
                showerrors = true;
            } else {
                message = "Unknown action "+action; 
            }
        }
    }
%>
<%@page import="java.util.List"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Database pool information (to.etc.dbpool)</title>
<meta http-equiv="refresh" content="30">
<style type="text/css">
body        { font-family: "Verdana, Arial, Bitstream Vera Sans, sans"; font-size: 12pt; background-color: #000000; color: #ffffff; }
.maintbl    { font-family: "Verdana, Arial, Bitstream Vera Sans, sans"; font-size: 10pt; }
.maintbl td { border-left: 1px solid #cccccc; }

.perftbl    { font-family: "Verdana, Arial, Bitstream Vera Sans, sans"; font-size: 6pt; padding: 0px; margin: 0px; }
.perfhdr    { font-family: "Verdana, Arial, Bitstream Vera Sans, sans"; font-size: 6pt; font-weight: bold; padding: 0px; margin: 0px; }
.perfodd    { background-color: #660000; } 
.perfeven   { background-color: #663300; } 
.hdr    { font-weight: bolder; font-size: 8pt; }

.conns      { background-color: #006633;  }
.errs       { background-color: #fa755a;  }
.stats      { background-color: #867373;  }
.info       {   }

.oddpool    { background-color: #000033;  }
.evenpool   { background-color: #004466;    }
:link       { color: #fac559;   }
:VISITED    { color: #fac559; }
:hover      { color: #eeee00; }

</style>
</head>
<body>
    <% if(message != null) { %>
        <h2><%= message %></h2>

    <% } %>
    <h2>Defined database pools:</h2>
    <table class="maintbl">
        <tr class="hdr">
            <td rowspan="2">ID</td>
            <td class="conns" colspan="6" align="center" valign="middle">#conns</td>
            <td class="errs" colspan="3" align="center">Conn errors</td>
            <td class="stats" colspan="3" align="center">#statements</td>
            <td class="info" rowspan="2" align="center" valign="middle">Debug actions</td>
            <td class="info" rowspan="2" align="center">Connection usage time histogram</td>
        </tr>
        <tr class="hdr">
            <td class="conns">Unpooled</td>
            <td class="conns">In use</td>
            <td class="conns">peak use</td>
            <td class="conns" title="The #of connections that is currently allocated from the database">allocated</td>
            <td class="conns" title="The max. #of connections that the pool is allowed to allocate from the database">max alloc</td>
            <td class="conns" title="The total #of times a connection was allocated FROM the pool">#allocations</td>

            <td class="errs" title="The #of times a program had to wait for a pooled connection to become available">waits</td>
            <td class="errs" title="The #of times the pool was exhausted (exception was thrown)">fails</td>
            <td class="errs" title="The #of times a hanging connection was forcefully released">dangles</td>

            <td class="stats">curr</td>
            <td class="stats">peak open</td>
            <td class="stats">total</td>
        </tr>
<%
    ConnectionPool[] par = PoolManager.getInstance().getPoolList();
    boolean odd = false; 
    for(ConnectionPool pool : par) {
%>
        <tr class="<%= odd ? "oddpool" : "evenpool" %>">
            <td title="url: <%= pool.getURL() %>"><b><%= pool.getID() %></b><br/><%= pool.isPooledMode()?"(pooled)":"" %></td>

            <td class="conns"><%= pool.getUnpooledInUse() %></td>
            <td class="conns"><%= pool.getUsed() %></td>
            <td class="conns"><%= pool.getMaxUsed() %></td>
            <td class="conns"><%= pool.getAllocatedConnections() %></td>
            <td class="conns"><%= pool.getMaxConns() %></td>
            <td class="conns"><%= pool.getConnectionAllocationCount() %></td>

            <td class="errs"><%= pool.getConnectionWaits() %></td>
            <td class="errs"><%= pool.getConnectionFails() %></td>
            <td class="errs"><%= pool.getHangDisconnects() %></td>

            <td class="stats"><%= pool.getCurrOpenStmt() %></td>
            <td class="stats"><%= pool.getPeakOpenStmt() %></td>
            <td class="stats"><%= pool.getTotalStmt() %></td>

            <td class="info" nowrap="nowrap"><% if(pool.dbgIsStackTraceEnabled()) { %>
                Trace Enabled <a href="pool.jsp?pool=<%= pool.getID()%>&action=traceoff">(disable)</a>
                <br/>
                <% if(displaystacks) { %>
                    <a href="pool.jsp">Hide Stacks</a>
                <% } else { %>
                    <a href="pool.jsp?pool=<%= pool.getID()%>&action=stacks">Display Stacks</a>
                <% } } else { %>
                Trace Disabled <a href="pool.jsp?pool=<%= pool.getID()%>&action=traceon">(enable)</a>
                <% } %>
                <br/>
                <% 
                    List errors = pool.getSavedErrorList();
                    if(pool.isSavingErrors() && errors != null) { 
                %>
                    <a href="pool.jsp?pool=<%= pool.getID()%>&action=errorsoff">Disable error logging</a>
                    <% if(errors.size() > 0) { %>
                        <% if(showerrors) { %>
                            <br/><a href="pool.jsp">Hide Errors</a>
                        <% }else{ %>
                            <br/><a href="pool.jsp?pool=<%= pool.getID()%>&action=showerrors">Show <%= errors.size() %> error(s)</a>
                        <% } %>
                    <% } else { %>
                        <br/>No errors logged                        
                    <% } %>

                <% } else { %>
                    <a href="pool.jsp?pool=<%= pool.getID()%>&action=errorson">Enable error log</a>
                <% } %>
            </td>

            <td><%= pool.getUseTimeTableStr() %>
            </td>
        </tr>
<%
        odd = ! odd;
    }
%>    
    </table>


<% if(displaystacks) { %>
    <h2>In-use connections for pool <%= ipool.getID() %></h2>
    <pre><%= ipool.dbgDumpUsedStacks() %></pre>
<% } %>

<% if(showerrors) { %>
    <h2>Saved errors in the error log for pool <%= ipool.getID() %></h2>
        <%
            List errors = ipool.getSavedErrorList();
            if(errors == null || errors.size() == 0) {
        %>
            <p>There are currently no more errors to display</p>
        <% } else {
            for(int i = 0; i < errors.size(); i++) {
                ConnectionPool.ErrorEntry ee = (ConnectionPool.ErrorEntry)errors.get(i);
        %>
                <h3>On <%= ee.getTs() %>: <%= ee.getSubject() %></h3>
                <pre>
                    <%= ee.getMsg() %>
                </pre>
                <br/>
        <%  }
        }
        %>
<% } %>

</body>
</html>
