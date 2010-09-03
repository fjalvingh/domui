package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.trouble.*;

/**
 * Parameterizable converter for numbers.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 19, 2010
 */
public class NumberConverter<T extends Number> implements IConverter<T> {
	private NumericPresentation m_presentation;

	private int m_scale;

	private Class<T> m_actualType;

	public NumberConverter(Class<T> actualType, NumericPresentation presentation, int scale) {
		m_actualType = actualType;
		m_presentation = presentation;
		m_scale = scale;
	}

	@Override
	public String convertObjectToString(Locale loc, T in) throws UIException {
		return NumericUtil.renderNumber(in, m_presentation, m_scale);
	}

	@Override
	public T convertStringToObject(Locale loc, String in) throws UIException {
		return NumericUtil.parseNumber(m_actualType, in);
	}
}
