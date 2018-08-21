package to.etc.domui.util.bugs.reporters.jira;

import to.etc.domui.util.bugs.Bug;
import to.etc.domui.util.bugs.BugItem;

import java.net.URI;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-8-18.
 */
public class JiraReporterImpl extends JiraReporter {
	private final String m_projectKey;

	public JiraReporterImpl(URI jiraURL, String userName, String password, String projectKey) {
		super(jiraURL, userName, password);
		m_projectKey = projectKey;
	}

	@Override protected String getProjectKey(BugItem item) {
		return m_projectKey;
	}

	@Override protected long getIssueTypeId(BugItem item) {
		return 10004;							// Bug
	}


	public static void main(String[] args) throws Exception {
		URI uri = new URI("https://skarpsectorintelligence.atlassian.net/");

		JiraReporterImpl ri = new JiraReporterImpl(uri, "fritsjalvingh@skarp.nl", "723B7rhVdT", "PTL");
		Bug.addGlobalListener(ri);

		try {
			throw new IllegalStateException("Testing the reporter");
		} catch(Exception x) {
			Bug.panic(x, "Test reporter framework");
		}
	}

	@Override protected String getHashCodeFieldname() {
		return "customfield_10024";
	}
}
