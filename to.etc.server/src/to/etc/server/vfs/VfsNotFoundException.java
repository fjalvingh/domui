package to.etc.server.vfs;

public class VfsNotFoundException extends Exception {
	private String	m_path;

	public VfsNotFoundException(String msg, String path) {
		super(msg);
		m_path = path;
	}

	/**
	 * @return the path
	 */
	public final String getPath() {
		return m_path;
	}
}
