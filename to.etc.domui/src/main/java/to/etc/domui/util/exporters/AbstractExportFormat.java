package to.etc.domui.util.exporters;

import java.io.File;

/**
 * Base class for {@link IExportFormat} factories.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
abstract public class AbstractExportFormat implements IExportFormat {
	private final String m_extension;

	private final String m_name;

	public AbstractExportFormat(String extension, String name) {
		m_extension = extension;
		m_name = name;

	}

	@Override public String name() {
		return m_name;
	}

	@Override public String extension() {
		return m_extension;
	}

	@Override abstract public IExportWriter<?> createWriter(File output) throws Exception;
}
