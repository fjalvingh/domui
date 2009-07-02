package to.etc.domui.component.form;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;

/**
 * This is a fallback factory; it accepts anything and shows a String edit component for it. It
 * hopes that the Text<?> control can convert the string input value to the actual type using the
 * registered Converters. This is also the factory for regular Strings.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
@SuppressWarnings("unchecked")		// Hating Generics
public class ControlFactoryString implements ControlFactory {
	/**
	 * Accept any type using a string.
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel)
	 */
	public int accepts(final PropertyMetaModel pmm, final boolean editable) {
		return 1;
	}

	public Result createControl(final IReadOnlyModel< ? > model, final PropertyMetaModel pmm, final boolean editable) {
		Class<?>	iclz	= pmm.getActualType();

		//-- Treat everything else as a String using a converter.
		Text<?>	txt	= new Text(iclz);
		if(! editable)
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
				size++;						// Inc size to allow for decimal point or comma
				d -= pmm.getScale();		// Reduce integer part,
				if(d >= 4) {				// Can we get > 999? Then we can have thousand-separators
					int nd = (d-1) / 3;		// How many thousand separators could there be?
					size += nd;				// Increment input size with that
				}
			}
			txt.setSize(size);
		} else if(pmm.getLength() > 0) {
			txt.setSize(pmm.getLength() < 40 ? pmm.getLength() : 40);
		}

		if(pmm.getConverterClass() != null)
			txt.setConverterClass(pmm.getConverterClass());
		if(pmm.getLength() > 0)
			txt.setMaxLength(pmm.getLength());
		if(pmm.isRequired())
			txt.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			txt.setLiteralTitle(s);
		for(PropertyMetaValidator mpv: pmm.getValidators())
			txt.addValidator(mpv);
		return new Result(txt, model, pmm);
	}

}
