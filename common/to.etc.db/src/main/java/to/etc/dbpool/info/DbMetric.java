package to.etc.dbpool.info;

/**
 * This contains some metric obtained from database performance statistics (v$client_stats in Oracle).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2/2/16.
 */
final public class DbMetric {
	private final MetricsDefinition m_definition;

	private double m_value;

	public DbMetric(MetricsDefinition md, double value) {
		m_definition = md;
		m_value = value;
	}

	public DbMetric(DbMetric src) {
		m_definition = src.m_definition;
		m_value = src.m_value;
	}

	public MetricsDefinition getDefinition() {
		return m_definition;
	}

	public double getValue() {
		return m_value;
	}

	public void setValue(double val) {
		m_value = val;
	}


	public String getKey() {
		return m_definition.getKey();
	}

	public String getLabel() {
		return m_definition.getLabel();
	}

	public String getDescription() {
		return m_definition.getDescription();
	}

	public String getFormattedValue() {
		return m_definition.getTranslator().translate(getValue());
	}

	@Override
	public String toString() {
		return m_definition.toString() + " = " + m_value;
	}
}
