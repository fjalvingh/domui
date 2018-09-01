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

		//-- 1. Add exception name: format is "java.lang.XxxException". Do not add the message.
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
		System.out.println("Hash " + s);
		String md5Hash = SecurityUtils.getMD5Hash(s, "utf-8");
		return md5Hash;
	}

	@NonNull
	static private final Pattern LOCATION_PATTERN = Pattern.compile("\\s+[Aa]t\\s+([a-zA-Z0-9\\.\\$\\_]+)\\([^\\)]+\\).*");


	static private final String S = "org.hibernate.exception.ConstraintViolationException: could not insert: [nl.skarp.portal.core.db.MonServerComponent]\n"
		+ "        at org.hibernate.exception.SQLStateConverter.convert(SQLStateConverter.java:96)\n"
		+ "        at org.hibernate.exception.JDBCExceptionHelper.convert(JDBCExceptionHelper.java:66)\n"
		+ "        at org.hibernate.persister.entity.AbstractEntityPersister.insert(AbstractEntityPersister.java:2455)\n"
		+ "        at org.hibernate.persister.entity.AbstractEntityPersister.insert(AbstractEntityPersister.java:2875)\n"
		+ "        at org.hibernate.action.EntityInsertAction.execute(EntityInsertAction.java:79)\n"
		+ "        at org.hibernate.engine.ActionQueue.execute(ActionQueue.java:273)\n"
		+ "        at org.hibernate.engine.ActionQueue.executeActions(ActionQueue.java:265)\n"
		+ "        at org.hibernate.engine.ActionQueue.executeActions(ActionQueue.java:184)\n"
		+ "        at org.hibernate.event.def.AbstractFlushingEventListener.performExecutions(AbstractFlushingEventListener.java:321)\n"
		+ "        at org.hibernate.event.def.DefaultFlushEventListener.onFlush(DefaultFlushEventListener.java:51)\n"
		+ "        at org.hibernate.impl.SessionImpl.flush(SessionImpl.java:1216)\n"
		+ "        at to.etc.domui.hibernate.generic.HibernateLongSessionContext.commit(HibernateLongSessionContext.java:120)\n"
		+ "        at nl.skarp.monitoring.collectors.CachedServer.getComponent(CachedServer.java:124)\n"
		+ "        at nl.skarp.monitoring.collectors.CachedComponent.getChild(CachedComponent.java:95)\n"
		+ "        at nl.skarp.monitoring.collectors.DbSizeReader.getDatabaseSizes(DbSizeReader.java:146)\n"
		+ "        at nl.skarp.monitoring.collectors.DbSizeReader.run(DbSizeReader.java:76)\n"
		+ "        at nl.skarp.monitoring.collectors.MonitorThread.runServerDatabaseSizeCheck(MonitorThread.java:167)\n"
		+ "        at nl.skarp.monitoring.collectors.MonitorThread.runSizeChecks(MonitorThread.java:101)\n"
		+ "        at nl.skarp.monitoring.collectors.MonitorThread.checkSizes(MonitorThread.java:72)\n"
		+ "        at nl.skarp.monitoring.collectors.MonitorThread.run(MonitorThread.java:44)\n"
		+ "Caused by: to.etc.dbpool.BetterSQLException: org.postgresql.util.PSQLException: ERROR: duplicate key value violates unique constraint \"msc_pk\"\n"
		+ "  Detail: Key (msc_id)=(5975) already exists.\n"
		+ "\n"
		+ "SQL: insert into source_mapping.mon_server_component (msc_database_password, msc_database_port, msc_database_type, msc_database_userid, msc_name, parent_msc_id, msr_id, msc_class, msc_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?)\n"
		+ "Parameters:\n"
		+ "#1  : [null]           : msc_database_password   = [null]\n"
		+ "#2  : [null]           : msc_database_port       = [null]\n"
		+ "#3  : [null]           : msc_database_type       = [null]\n"
		+ "#4  : [null]           : msc_database_userid     = [null]\n"
		+ "#5  : java.lang.String : msc_name                = staging_912_prd\n"
		+ "#6  : java.lang.Long   : parent_msc_id           = 5050\n"
		+ "#7  : java.lang.Long   : msr_id                  = 1\n"
		+ "#8  : java.lang.String : msc_class               = DATABASE\n"
		+ "#9  : java.lang.Long   : msc_id                  = 5975\n"
		+ "        at to.etc.dbpool.PreparedStatementProxy.wrap(PreparedStatementProxy.java:108)\n"
		+ "        at to.etc.dbpool.PreparedStatementProxy.executeUpdate(PreparedStatementProxy.java:149)\n"
		+ "        at org.hibernate.jdbc.NonBatchingBatcher.addToBatch(NonBatchingBatcher.java:46)\n"
		+ "        at org.hibernate.persister.entity.AbstractEntityPersister.insert(AbstractEntityPersister.java:2435)\n"
		+ "        ... 17 more\n"
		+ "Caused by: org.postgresql.util.PSQLException: ERROR: duplicate key value violates unique constraint \"msc_pk\"\n"
		+ "  Detail: Key (msc_id)=(5975) already exists.\n"
		+ "        at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2476)\n"
		+ "        at org.postgresql.core.v3.QueryExecutorImpl.processResults(QueryExecutorImpl.java:2189)\n"
		+ "        at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2476)\n"
		+ "        at org.postgresql.core.v3.QueryExecutorImpl.processResults(QueryExecutorImpl.java:2189)\n"
		+ "        at org.postgresql.core.v3.QueryExecutorImpl.execute(QueryExecutorImpl.java:300)\n"
		+ "        at org.postgresql.jdbc.PgStatement.executeInternal(PgStatement.java:428)\n"
		+ "        at org.postgresql.jdbc.PgStatement.execute(PgStatement.java:354)\n"
		+ "        at org.postgresql.jdbc.PgPreparedStatement.executeWithFlags(PgPreparedStatement.java:169)\n"
		+ "        at org.postgresql.jdbc.PgPreparedStatement.executeUpdate(PgPreparedStatement.java:136)\n"
		+ "        at to.etc.dbpool.PreparedStatementProxy.executeUpdate(PreparedStatementProxy.java:146)\n"
		+ "        ... 19 more\n";

	static public void main(String[] args) {
		String h = getExceptionHash(S);
		System.out.println(h);
	}
}
