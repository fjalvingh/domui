package to.etc.webapp.query;

/**
 * Represents the selection of some operation on a property, or of the property value itself.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
final public class QPropertySelection extends QSelectionItem {
	private String		m_property;

	/**
	 * Create a selection using the specified function applied to the specified property.
	 * @param function
	 * @param property
	 */
	protected QPropertySelection(QSelectionFunction function, String property) {
		super(function);
		m_property = property;
	}

	public String getProperty() {
		return m_property;
	}
}
