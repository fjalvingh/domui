package to.etc.domui.util.bugs.reporters.jira;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.util.bugs.Bug;
import to.etc.domui.util.bugs.BugItem;
import to.etc.util.DeveloperOptions;

import java.net.URI;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-8-18.
 */
public class JiraReporterImpl extends JiraReporter {
	private final String m_projectKey;

	private long m_issueType = 10004;						// Should be "Bug"

	@Nullable
	private String m_hashFieldName;

	public JiraReporterImpl(URI jiraURL, String userName, String password, String projectKey) {
		super(jiraURL, userName, password);
		m_projectKey = projectKey;
	}

	public JiraReporterImpl issueType(long issueType) {
		m_issueType = issueType;
		return this;
	}
	public JiraReporterImpl hashFieldName(String name) {
		m_hashFieldName = name;
		return this;
	}

	@Override protected String getProjectKey(BugItem item) {
		return m_projectKey;
	}

	@Override protected long getIssueTypeId(BugItem item) {
		return m_issueType;
	}

	public static void main(String[] args) throws Exception {
		URI uri = new URI("https://skarpsectorintelligence.atlassian.net/");

		JiraReporterImpl ri = new JiraReporterImpl(uri, DeveloperOptions.getString("test.jira.userid"), DeveloperOptions.getString("test.jira.password"), "PTL");
		Bug.addGlobalListener(ri);

		try {
			throw new IllegalStateException("Testing the reporter");
		} catch(Exception x) {
			Bug.panic(x, "Test reporter framework");
		}
	}

	@Nullable
	@Override protected String getHashCodeFieldname() {
		return m_hashFieldName;
	}
}

