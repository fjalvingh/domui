package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;

import javax.servlet.FilterConfig;
import java.io.File;

public class FilterConfigParameters implements ConfigParameters {
	@NonNull
	private FilterConfig m_fc;

	@NonNull
	private File m_webFileRoot;

	public FilterConfigParameters(@NonNull FilterConfig fc, @NonNull File webFileRoot) {
		m_fc = fc;
		m_webFileRoot = webFileRoot;
	}

	@Override
	public String getString(@NonNull String name) {
		return m_fc.getInitParameter(name);
	}

	@NonNull
	@Override
	public File getWebFileRoot() {
		return m_webFileRoot;
	}
}
