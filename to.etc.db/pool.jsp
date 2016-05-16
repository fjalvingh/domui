<%@ page language="java" %>
<%@ page contentType="text/html" pageEncoding="utf-8" %>
<%@page import="to.etc.dbpool.info.JspPageHandler"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%
	/*
	 * This page is kept as a single page to make it easy to distribute. It also shows the horrible
	 * effects of having to do big JSP...
	 */
	JspPageHandler	jph = new JspPageHandler(out, request, "pool.jsp");
	jph.initialize();
%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<% jph.generateRefresh(); %>
<title>Database pool information (to.etc.db - newpool)</title>
<style type="text/css">
body        { font-family: "Verdana, Arial, Bitstream Vera Sans, sans"; font-size: 14px; background-color: #000000; color: #ffffff; }
.maintbl    { font-family: "Verdana, Arial, Bitstream Vera Sans, sans"; font-size: 14px; }
/*.maintbl td { border-left: 1px solid #cccccc; } */

.perftbl    { font-family: "Verdana, Arial, Bitstream Vera Sans, sans"; font-size: 12px; padding: 0px; margin: 0px; }
.perfhdr    { font-family: "Verdana, Arial, Bitstream Vera Sans, sans"; font-size: 8px; font-weight: bold; padding: 0px; margin: 0px; }
.perfodd    { background-color: #660000; } 
.perfeven   { background-color: #663300; } 
.hdr    { font-weight: bolder; font-size: 12px; }

.tbl { font-family: "Verdana, Arial, Bitstream Vera Sans, sans"; font-size: 12px; }

.conns		{ background-color: #009948;  }
.uconns      { background-color: #006633;  }
.errs       { background-color: #7a373d;  }
.stats      { background-color: #867373;  }
.info       {   }

.hanging   { background-color: #FF3A1C; font-size: 14pt; font-weight: bold; }
.trblyes   { background-color: #FF3A1C; font-size: 14pt; font-weight: bold; }

.oddpool    { background-color: #000033;  }
.evenpool   { background-color: #004466;    }
:link       { color: #fac559;   }
:VISITED    { color: #fac559; }
:hover      { color: #eeee00; }

/* Connection list */
.connrow.odd  	{ background-color: #005c56; }
.connrow.even  	{ background-color: #357571; }
.stmtstack		{ background-color: black; padding-left: 20px; margin: 5px; }
.stmtdiv		{ background-color: #1C1900; padding-left: 20px;}
.stmtsql		{ margin-left: 10px; padding: 5px; background-color: #382E00; }
.stmthdr		{ font-size: 14px; font-family: Arial, sans-serif; }

/* statistics */
.statrow.odd	{ background-color: #005c56; }
.statrow.even  	{ background-color: #357571; }
.statrequest	{ background-color: #A64B7A; font-style: italic; }

.statptbl		{ background-color: #00424F; }
.statpheader	{ background-color: #130069; }
.statprow		{ background-color: #483B82; }

</style>

<script type="text/javascript">
function toggleStacks(id) {
	var el = document.getElementById(id);
	if(el) {
		if(el.style.display == "none")
			el.style.display = "block";
		else		
			el.style.display = "none";
	}
}

</script>
</head>
<body>
<% jph.displayPage(); %>
</body>
</html>
