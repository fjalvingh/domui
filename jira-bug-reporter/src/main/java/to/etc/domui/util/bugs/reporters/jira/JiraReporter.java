package to.etc.domui.util.bugs.reporters.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.util.bugs.BugItem;
import to.etc.domui.util.bugs.BugSeverity;
import to.etc.domui.util.bugs.IBugContribution;
import to.etc.domui.util.bugs.IBugListener;
import to.etc.util.StringTool;

import java.net.InetAddress;
import java.net.URI;

/**
 * This reporter is used to report bugs reported by the Bug framework to a Jira
 * instance. The data for the bug is collected, a hash for the bug is calculated
 * and a search is initiated for a user field that would contain hashes for bugs.
 *
 * If a bug with the same hash is found the message is added to the existing bug,
 * else we create a new one.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-8-18.
 */
abstract public class JiraReporter implements IBugListener {
	private final URI m_jiraURL;

	private final String m_userName;

	private final String m_password;

	public JiraReporter(URI jiraURL, String userName, String password) {
		m_jiraURL = jiraURL;
		m_userName = userName;
		m_password = password;
	}

	@Override public void bugSignaled(BugItem item) {
		if(! accept(item))
			return;

		try(JiraRestClient client = new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(m_jiraURL, m_userName, m_password)) {
			String hash = item.getHash();
			//findBugByHash(client, hash);

			postBug(client, item, hash);



		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	private void postBug(JiraRestClient client, BugItem item, String hash) {
		IssueInputBuilder builder = new IssueInputBuilder(getProjectKey(item), getIssueTypeId(item), createSummary(item))
			.setFieldValue("description", calculateDescription(item))
			;
		String hashField = getHashCodeFieldname();
		if(hashField != null) {
			builder.setFieldValue(hashField, hash);
		}
		IssueInput ii = builder.build();

		BasicIssue issue = client.getIssueClient().createIssue(ii).claim();
		System.out.println("Issue created as " + issue.getKey());
	}

	private String calculateDescription(BugItem item) {
		StringBuilder sb = new StringBuilder();
		sb.append("Automatic bug report");

		try {
			InetAddress host = InetAddress.getLocalHost();
			String name = host.getCanonicalHostName();
			sb.append(" from server").append(name);
		} catch(Exception xxx) {}

		sb.append(": ").append(item.getMessage()).append("\n");

		Throwable exception = item.getException();
		if(null != exception) {
			sb.append("Exception was: ");
			StringTool.strStacktrace(sb, exception);
			sb.append("\n");
		}

		//-- Append all contributions, where available
		for(IBugContribution contribution : item.getContributions()) {
			try {
				contribution.appendTo(sb);
				sb.append("\n");
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
		return sb.toString();
	}

	private void findBugByHash(JiraRestClient client, String hash) {


	}

	protected String createSummary(BugItem bug) {
		String message = bug.getMessage();
		Throwable exception = bug.getException();
		if(null != exception) {
			if(! message.contains(exception.toString())) {
				message += " (" + exception.toString() + ")";
			}
		}

		String prefix = getPrefix();
		if(null != prefix) {
			message = prefix + message;
		}

		if(message.length() > 128)
			message = message.substring(0, 125) + "...";

		return message;
	}

	abstract protected String getProjectKey(BugItem item);

	abstract protected long getIssueTypeId(BugItem item);

	@Nullable
	abstract protected String getHashCodeFieldname();

	/**
	 * By default only report PANIC items.
	 */
	protected boolean accept(BugItem item) {
		return item.getSeverity() == BugSeverity.PANIC;
	}

	protected String getPrefix() {
		return "[auto] ";
	}
}
