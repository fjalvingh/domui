package to.etc.domui.util.vcs;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-8-17.
 */
final public class GitOptions {
	private static ConcurrentHashMap<String, GitOptions> m_map = new ConcurrentHashMap<>();

	private Properties m_properties;

	private GitOptions(Properties p) {
		m_properties = p;
	}

	@Nonnull
	static public GitOptions get(String name) {
		GitOptions g = m_map.get(name);
		if(null == g) {
			Properties p = new Properties();
			try(InputStream is = GitOptions.class.getResourceAsStream("/" + name)) {
				p.load(is);
			} catch(Exception x) {
			}
			g = new GitOptions(p);
			m_map.put(name, g);
		}
		return g;
	}

	public boolean hasProperties() {
		return m_properties.size() != 0;
	}

	public String getCommit() {
		return m_properties.getProperty("git.commit.id");
	}

	public String getBuildDate() {
		return m_properties.getProperty("git.build.time");
	}

	public String getCommitDate() {
		return m_properties.getProperty("git.commit.time");
	}

	public String getLastCommitter() {
		return m_properties.getProperty("git.commit.user.email");
	}
}
