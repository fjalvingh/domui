package to.etc.domui.server;

import java.net.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.login.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.smtp.*;
import to.etc.util.*;
import to.etc.webapp.mailer.*;

public class ExceptionUtil {
	@Nonnull
	final private RequestContextImpl m_ctx;

	public ExceptionUtil(@Nonnull RequestContextImpl ri) {
		m_ctx = ri;
	}

	public String renderParameters() {
		StringBuilder sb = new StringBuilder();
		sb.append("<table class='listtbl'>\n");
		sb.append("<thead><tr>\n");
		sb.append("<th>name</th><th>Value</th>");
		sb.append("</tr></thead>\n");
		String[] names = m_ctx.getParameterNames();
		if(names != null) {
			for(String name: names) {
				boolean first = true;
				String[] values = m_ctx.getParameters(name);
				if(values == null || values.length == 0) {
					sb.append("<tr><td>").append(StringTool.htmlStringize(name)).append("</td><td>No value</td></tr>");
				} else {
					for(String value : values) {
						sb.append("<tr><td>");
						if(first)
							sb.append(StringTool.htmlStringize(name));
						else
							sb.append("\u00a0");
						first = false;
						sb.append("</td><td>");

						sb.append(StringTool.htmlStringize(value));
						sb.append("</td></tr>");
					}
				}
			}
		}
		sb.append("</table>");
		return sb.toString();
	}

	@Nonnull
	public List<UserLogItem> getUserLog() {
		AppSession session = m_ctx.getSession();
		return session.getLogItems();
	}

	@Nullable
	public String lastName(@Nullable String name) {
		if(null == name)
			return null;
		return name.substring(name.lastIndexOf('.') + 1);
	}

	public void renderEmail(@Nonnull Throwable x) {
		String addr = DomApplication.get().getProblemMailAddress();
		if(null == addr)
			return;
		String subj = DomApplication.get().getProblemMailSubject();
		if(null == subj)
			return;
		String from = DomApplication.get().getProblemFromAddress();
		if(null == from)
			return;

		try {
			InetAddress host = InetAddress.getLocalHost();
			subj += " (" + host.getCanonicalHostName() + ")";
		} catch(Exception xxx) {}

		//-- Print a text version of all information and cause it to be sent.
		MailBuilder mb = new MailBuilder();
		mb.initialize(subj);
		mb.append("A problem occurred in this DomUI application: ").append(x.toString()).nl();
		mb.nl();
		try {
			InetAddress host = InetAddress.getLocalHost();
			mb.append("Server: ").append(host.getCanonicalHostName()).append(", ").append(host.getHostAddress()).nl();
		} catch(Exception xxx) {}

		try {
			IUser user = UIContext.getCurrentUser();
			if(null != user) {
				mb.append("User name: ").append(user.getDisplayName()).append(", login id ").append(user.getLoginID()).nl();
			}
		} catch(Exception xxx) {}


		mb.ttl("Exception stack trace");
		StringBuilder sb = new StringBuilder();
		DomUtil.dumpException(sb, x);
		for(String s : new LineIterator(sb.toString())) {
			mb.append(s).nl();
		}

		mb.nl();
		mb.ttl("Page input parameters");

		String[] names = m_ctx.getParameterNames();
		if(names != null) {
			for(String name : names) {
				boolean first = true;
				String[] values = m_ctx.getParameters(name);
				if(values == null || values.length == 0) {
					mb.b(name).append(": ");
					mb.append("No value").nl();
				} else {
					for(String value : values) {
						if(first)
							mb.b(name).append(": ");
						else
							mb.append(StringTool.strToFixedLength("", name.length())).append(": ");
						first = false;
						mb.append(value).nl();
					}
				}
			}
		}

		mb.nl();
		mb.ttl("Click/event stream (new to old)");
		AppSession session = m_ctx.getSession();
		//                                     012345678901 012345678901234567890123456789 0123456789012345678901234
		//                                     -13s 999ms   0MC0ZakN00016ddnzHC00FYG.c3    LocalEnvironmentsPage
		mb.append(StringTool.strToFixedLength("Time", 12));
		mb.append(StringTool.strToFixedLength("CID", 30));
		mb.append(StringTool.strToFixedLength("Page", 24));
		mb.append("Message").nl();
		List<UserLogItem> logItems = new ArrayList<>(session.getLogItems());
		Collections.reverse(logItems);
		for(UserLogItem li : logItems) {
			mb.append(StringTool.strToFixedLength(li.time(), 12));
			mb.append(StringTool.strToFixedLength(li.getCid(), 30));
			mb.append(StringTool.strToFixedLength(lastName(li.getPage()), 24));
			mb.append(li.getText()).nl();
		}

		Message m = mb.createMessage();
		m.addTo(new Address(addr));
		m.setFrom(new Address(from, from));
		try {
			BulkMailer.getInstance().store(m);
		} catch(Exception xxx) {
			xxx.printStackTrace();
		}

	}
}
