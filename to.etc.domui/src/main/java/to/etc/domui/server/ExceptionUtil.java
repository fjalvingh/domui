package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.login.IUser;
import to.etc.domui.state.AppSession;
import to.etc.domui.state.UIContext;
import to.etc.domui.state.UserLogItem;
import to.etc.domui.util.DomUtil;
import to.etc.smtp.Address;
import to.etc.smtp.MailBuilder;
import to.etc.smtp.Message;
import to.etc.util.LineIterator;
import to.etc.util.SecurityUtils;
import to.etc.util.StringTool;
import to.etc.webapp.mailer.BulkMailer;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class ExceptionUtil {
	@NonNull
	final private RequestContextImpl m_ctx;

	public ExceptionUtil(@NonNull RequestContextImpl ri) {
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

	@NonNull
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

	public void renderEmail(@NonNull Throwable x) {
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

	/**
	 * Calculate a hash code for a stacktrace, so that duplicate occurrences can be found.
	 */
	@NonNull
	static public String getExceptionHash(@NonNull Throwable exception) {
		return getExceptionHash(StringTool.strStacktrace(exception));
	}

	/**
	 * Calculate a hash code for a stacktrace, so that duplicate occurrences can be found.
	 */
	@NonNull
	static public String getExceptionHash(@NonNull String exception) {
		StringBuilder sb = new StringBuilder();

		//-- 1. Add exception name: format is "java.lang.XxxException" optionally followed by ": message".
		int pos = exception.indexOf('\n');
		if(pos > 2) {
			String line = exception.substring(0, pos);
			pos = line.indexOf(':');
			if(pos == -1)
				sb.append(line);						// No colon: Exception name only.
			else
				sb.append(line, 0, pos);				// Only upto colon.
			sb.append("\n");
		}

		//-- Locate all "at" lines and only add the "classname + function" part.
		Matcher m = null;
		for(String line : new LineIterator(exception)) {
			if(null == m)
				m = LOCATION_PATTERN.matcher(line);
			else
				m.reset(line);
			if(m.matches()) {
				String frag = m.group(1);
				sb.append(frag).append("\n");
			}
		}

		String s = sb.toString();
//		System.out.println("Hash " + s);
		String md5Hash = SecurityUtils.getMD5Hash(s, "utf-8");
		return md5Hash;
	}

	@NonNull
	static private final Pattern LOCATION_PATTERN = Pattern.compile("\\s+[Aa]t\\s++([a-zA-Z0-9\\.\\$\\_]+)\\([^\\)]+\\).*");
}
