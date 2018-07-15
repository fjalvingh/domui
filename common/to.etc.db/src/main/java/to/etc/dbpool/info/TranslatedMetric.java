package to.etc.dbpool.info;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2/2/16.
 */
final public class TranslatedMetric {
	private final String m_key;

	private final String m_label;

	private final String m_description;

	private final String m_translate;

	public TranslatedMetric(String key, String label, String description, String translate) {

		m_key = key;
		m_label = label;
		m_description = description;
		m_translate = translate;
	}

	public String getKey() {
		return m_key;
	}

	public String getLabel() {
		return m_label;
	}

	public String getDescription() {
		return m_description;
	}

	public String getValue() {
		return m_translate;
	}
}
