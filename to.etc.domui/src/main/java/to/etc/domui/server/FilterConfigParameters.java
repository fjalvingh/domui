package to.etc.domui.server;

import java.io.*;

import javax.annotation.*;
import javax.servlet.*;

public class FilterConfigParameters implements ConfigParameters {
	@Nonnull
	private FilterConfig m_fc;

	@Nonnull
	private File m_webFileRoot;

	public FilterConfigParameters(@Nonnull FilterConfig fc, @Nonnull File webFileRoot) {
		m_fc = fc;
		m_webFileRoot = webFileRoot;
	}

	@Override
	public String getString(@Nonnull String name) {
		return m_fc.getInitParameter(name);
	}

	@Nonnull
	@Override
	public File getWebFileRoot() {
		return m_webFileRoot;
	}
}