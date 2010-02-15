package to.etc.domui.component.misc;

import to.etc.domui.component.input.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * This is a special control which can be used to display all kinds of values. It behaves as a "span" containing some
 * value that can be converted, translated and whatnot. It is meant for not too complex values that are usually
 * represented as a span.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 15, 2010
 */
public class ValueDisplay<T> extends Span implements IDisplayControl<T>, IBindable {
	private Class<T> m_valueClass;

	private T m_value;

	/**If the value is to be converted use this converter for it. */
	private IConverter<T> m_converter;

	public ValueDisplay(Class<T> valueClass) {
		m_valueClass = valueClass;
	}

	public Class<T> getValueClass() {
		return m_valueClass;
	}

	/**
	 * Returns the class of the converter for this control. This returns null if no converter has been set. It also
	 * returns null if a default converter is used.
	 *
	 * @return
	 */
	public IConverter<T> getConverter() {
		return m_converter;
	}

	/**
	 * Sets the Converter to use to convert the value to a string.
	 * @param converter
	 */
	public void setConverter(IConverter<T> converter) {
		m_converter = converter;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface (EXPERIMENTAL)					*/
	/*--------------------------------------------------------------*/
	/** When this is bound this contains the binder instance handling the binding. */
	private DisplayOnlyBinder m_binder;

	/**
	 * Return the binder for this control.
	 * @see to.etc.domui.component.input.IBindable#bind()
	 */
	public IBinder bind() {
		if(m_binder == null)
			m_binder = new DisplayOnlyBinder(this);
		return m_binder;
	}

	/**
	 * Returns T if this control is bound to some data value.
	 * @see to.etc.domui.component.input.IBindable#isBound()
	 */
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IDisplayControl interface.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.dom.html.IDisplayControl#getValue()
	 */
	@Override
	public T getValue() {
		return m_value;
	}

	@Override
	public void setValue(T v) {
		if(DomUtil.isEqual(m_value, v))
			return;
		m_value = v;
		forceRebuild();
	}
}
