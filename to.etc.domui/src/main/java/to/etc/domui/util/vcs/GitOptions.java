package to.etc.domui.util.vcs;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-8-17.
 */
final public class GitOptions {
	private static Properties m_properties;

	private GitOptions() {
	}

	static synchronized private Properties getProperties() {
		Properties properties = m_properties;
		if(null == properties) {
			m_properties = properties = new Properties();
			try(InputStream is = GitOptions.class.getResourceAsStream("/git.properties")) {
				properties.load(is);
			} catch(Exception x) {
			}
		}
		return properties;
	}

	static public boolean hasProperties() {
		return getProperties().size() != 0;
	}

	static public String getCommit() {
		return getProperties().getProperty("git.commit.id");
	}

	static public String getBuildDate() {
		return getProperties().getProperty("git.build.time");
	}

	static public String getCommitDate() {
		return getProperties().getProperty("git.commit.time");
	}

	static public String getLastCommitter() {
		return getProperties().getProperty("git.commit.user.email");
	}
}
