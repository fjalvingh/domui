package to.etc.domui.util.exporters;

/**
 * Denotes the supported Excel formats.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public enum ExcelFormat {
	XLSX("xlsx", "Microsoft Office Excel (xlsx)")
	, XLS("xls", "Microsoft Office Excel (xls)")
	;

	private final String m_description;
	private final String m_suffix;

	ExcelFormat(String suffix, String description) {
		m_description = description;
		m_suffix = suffix;
	}

	public String getDescription() {
		return m_description;
	}

	public String getSuffix() {
		return m_suffix;
	}
}
