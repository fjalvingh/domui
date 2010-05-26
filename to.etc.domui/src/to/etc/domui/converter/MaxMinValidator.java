package to.etc.domui.converter;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class MaxMinValidator implements IValueValidator<Number> {
	private Number m_max, m_min;

	/**
	 * Create a validator comparing to these INCLUSIVE bounds.
	 * @param max
	 * @param min
	 */
	public MaxMinValidator(Number min, Number max) {
		m_max = max;
		m_min = min;
	}

	/**
	 * Sigh. Of course Number does not implement Comparable, because that would
	 * be useful.
	 * @see to.etc.domui.converter.IValueValidator#validate(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void validate(Number input) throws Exception {
		Class< ? > ac = input.getClass();
		if(m_max.getClass() == ac && m_min.getClass() == ac && input instanceof Comparable< ? >) {
			int r = ((Comparable) input).compareTo(m_min);
			if(r < 0) {
				throw new ValidationException(Msgs.V_TOOSMALL, m_min.toString());
			}
			r = ((Comparable) input).compareTo(m_max);
			if(r > 0) {
				throw new ValidationException(Msgs.V_TOOLARGE, m_max.toString());
			}
		} else {
			if(input.doubleValue() > m_max.doubleValue())
				throw new ValidationException(Msgs.V_TOOLARGE, m_max.toString());
			if(input.doubleValue() < m_min.doubleValue())
				throw new ValidationException(Msgs.V_TOOSMALL, m_min.toString());
		}
	}
}
