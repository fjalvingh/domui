package to.etc.domui.component.dynaima;

import java.text.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.util.*;

/**
 * ChartField defines value of the field that will be displayed on the chart as well as it's label.
 *
 * @author <a href="mailto:nmaksimovic@execom.eu">Nemanja Maksimovic</a>
 * Created on 11 Jul 2011
 */
public final class ChartField {

	public static final Comparator<ChartField> COMPARATOR_BY_VALUE = new Comparator<ChartField>() {
		@Override
		public int compare(ChartField o1, ChartField o2) {
			return Double.compare(o1.getValue(), o2.getValue());
		}
	};

	public static final Comparator<ChartField> COMPARATOR_BY_LABEL = new Comparator<ChartField>() {
		@Override
		public int compare(ChartField o1, ChartField o2) {
			return o1.getLabel().compareTo(o2.getLabel());
		}
	};

	private static final int VALUE_EXTRA_CHARACTERS = 3;

	private static final int LABEL_LIMIT = 30 - VALUE_EXTRA_CHARACTERS;

	private final double m_value;

	private final String m_label;
	
	public ChartField(double value, @Nonnull String label) {
		super();
		if(label == null) {
			throw new IllegalArgumentException("ChartField.label cannot be null");
		}
		m_value = value;
		m_label = label;
	}

	public double getValue() {
		return m_value;
	}

	@Nonnull
	public String getLabel() {
		return m_label;
	}

	public String getShortLabel() {
		final String value = "[" + new DecimalFormat("#####,##0.##").format(m_value) + "] ";
		final String label = DomUtil.isBlank(m_label) || m_label.length() < LABEL_LIMIT - value.length() ? m_label : m_label.substring(0, LABEL_LIMIT - 3 - value.length()) + "...";
		return value + label;
	}

	@Override
	public String toString() {
		return "Label: " + m_label + " Value: " + m_value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_label == null) ? 0 : m_label.hashCode());
		long temp;
		temp = Double.doubleToLongBits(m_value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		ChartField other = (ChartField) obj;
		if(m_label == null) {
			if(other.m_label != null)
				return false;
		} else if(!m_label.equals(other.m_label))
			return false;
		return Double.doubleToLongBits(m_value) == Double.doubleToLongBits(other.m_value);
	}

}
