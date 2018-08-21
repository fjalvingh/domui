package to.etc.domui.util.bugs.reporters.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
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
			if(! updateIssueByHash(client, item, hash)) {
				//postBug(client, item, hash);
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	/**
	 * Create a new issue in Jira.
	 */
	private void postBug(JiraRestClient client, BugItem item, String hash) {
		IssueInputBuilder builder = new IssueInputBuilder(getProjectKey(item), getIssueTypeId(item), createSummary(item))
			.setFieldValue("description", calculateDescription(item))
			;
		String hashField = getHashCodeFieldname();
		if(hashField != null) {
			builder.setFieldValue("customfield_" + hashField, hash);
		}
		IssueInput ii = builder.build();

		BasicIssue issue = client.getIssueClient().createIssue(ii).claim();
		System.out.println("Issue created as " + issue.getKey());
	}

	/**
	 * Calculate the value for the description field, which will contain all of the information
	 * collected from the issue.
	 */
	private String calculateDescription(BugItem item) {
		StringBuilder sb = new StringBuilder();
		sb.append("Automatic bug report");

		try {
			InetAddress host = InetAddress.getLocalHost();
			String name = host.getCanonicalHostName();
			sb.append(" from server ").append(name);
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

	private boolean updateIssueByHash(JiraRestClient client, BugItem item, String hash) {
		String hashField = getHashCodeFieldname();
		if(null == hashField)
			return false;

		// see https://community.atlassian.com/t5/Jira-questions/Jira-Text-Search-in-Custom-Fields/qaq-p/26757
		SearchRestClient searchClient = client.getSearchClient();

		StringBuilder sb = new StringBuilder();
		sb.append("project=\"").append(getProjectKey(null)).append("\"");

		sb.append(" and ").append("cf[").append(hashField).append("]").append("~\"").append(hash).append("\"");
		SearchResult result = searchClient.searchJql(sb.toString()).claim();
		int total = result.getTotal();
		if(0 == total) {
			System.out.println("jira: no result for hash=" + hash);
			return false;
		}

		for(Issue issue : result.getIssues()) {
			String key = issue.getKey();
			System.out.println("jira: key=" + key);

			Comment comment = Comment.valueOf(calculateDescription(item));
			client.getIssueClient().addComment(issue.getCommentsUri(), comment).claim();
			System.out.println("jira: issue " + key + " updated");
			return false;
		}
		return false;
	}

	/**
	 * Create the bug's summary line, using the bug message and a part of the exception string.
	 */
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

	/**
	 * If a hash code field has been added to the Jira instance this should return its
	 * code as a string, i.e. "10024". If no field has been added all bugs will just add
	 * a new issue.
	 */
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
