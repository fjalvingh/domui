package to.etc.domui.component.form;

import java.math.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;

/**
 * Factory which creates a Text input specialized for entering monetary amounts. This
 * accepts properties with type=Double/double or BigDecimal, and with one of the monetary
 * numeric presentations.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 4, 2009
 */
public class ControlFactoryMoney implements ControlFactory {
	/**
	 * Accept any type using a string.
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel)
	 */
	public int accepts(final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass) {
		if(controlClass != null && !controlClass.isAssignableFrom(Text.class)) // This will create a Text class,
			return -1;
		Class<?> clz = pmm.getActualType();
		if(clz != Double.class && clz != double.class && clz != BigDecimal.class) // Must be proper type
			return -1;
		if(!NumericPresentation.isMonetary(pmm.getNumericPresentation()))
			return -1;
		return 2;
	}

	/**
	 * Create a Text control with the basic monetary converter, or the proper converter for the specified type.
	 * @see to.etc.domui.component.form.ControlFactory#createControl(to.etc.domui.util.IReadOnlyModel, to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@SuppressWarnings("unchecked")
	public Result createControl(final IReadOnlyModel< ? > model, final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass) {
		Class< ? > iclz = pmm.getActualType();

		//		if(!editable) {
		//			//-- Return a span containing the value.
		//
		//
		//		}

		//-- Treat everything else as a String using a converter.
		Text< ? > txt = new Text(iclz);
		if(!editable)
			txt.setReadOnly(true);

		/*
		 * Length calculation using the metadata. This uses the "length" field as LAST, because it is often 255 because the
		 * JPA's column annotation defaults length to 255 to make sure it's usability is bloody reduced. Idiots.
		 */
		if(pmm.getDisplayLength() > 0)
			txt.setSize(pmm.getDisplayLength());
		else if(pmm.getPrecision() > 0) {
			// FIXME This should be localized somehow...
			//-- Calculate a size using scale and precision.
			int size = pmm.getPrecision();
			int d = size;
			if(pmm.getScale() > 0) {
				size++; // Inc size to allow for decimal point or comma
				d -= pmm.getScale(); // Reduce integer part,
				if(d >= 4) { // Can we get > 999? Then we can have thousand-separators
					int nd = (d - 1) / 3; // How many thousand separators could there be?
					size += nd; // Increment input size with that
				}
			}
			txt.setSize(size);
		} else if(pmm.getLength() > 0) {
			txt.setSize(pmm.getLength() < 40 ? pmm.getLength() : 40);
		}

		if(pmm.getConverter() != null)
			txt.setConverter((IConverter) pmm.getConverter());
		else {
			if(pmm.getActualType() == Double.class || pmm.getActualType() == double.class)
				txt.setConverter((IConverter) ConverterRegistry.getConverterInstance(MoneyDoubleNumeric.class));
			else if(pmm.getActualType() == BigDecimal.class)
				txt.setConverter((IConverter) ConverterRegistry.getConverterInstance(MoneyBigDecimalNumeric.class));
			else
				throw new IllegalStateException("Cannot handle type=" + pmm.getActualType() + " in monetary control factory");
		}
		if(pmm.getLength() > 0)
			txt.setMaxLength(pmm.getLength());
		if(pmm.isRequired())
			txt.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			txt.setTitle(s);
		for(PropertyMetaValidator mpv : pmm.getValidators())
			txt.addValidator(mpv);
		return new Result(txt, model, pmm);
	}
}
