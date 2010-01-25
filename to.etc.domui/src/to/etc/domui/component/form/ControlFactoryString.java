package to.etc.domui.component.form;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;

/**
 * This is a fallback factory; it accepts anything and shows a String edit component for it. It
 * hopes that the Text<?> control can convert the string input value to the actual type using the
 * registered Converters. This is also the factory for regular Strings.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
@SuppressWarnings("unchecked")
// Hating Generics
public class ControlFactoryString implements ControlFactory {
	/**
	 * Accept any type using a string.
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel)
	 */
	public int accepts(final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass) {
		if(controlClass != null && !controlClass.isAssignableFrom(Text.class))
			return -1;

		return 1;
	}

	public Result createControl(final IReadOnlyModel< ? > model, final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass) {
		Class< ? > iclz = pmm.getActualType();
		Text< ? > txt = new Text(iclz);

		//-- Get simple things to do out of the way.
		if(!editable)
			txt.setReadOnly(true);
		if(pmm.getConverter() != null)
			txt.setConverter((IConverter) pmm.getConverter());
		if(pmm.isRequired())
			txt.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			txt.setTitle(s);
		for(PropertyMetaValidator mpv : pmm.getValidators())
			txt.addValidator(mpv);

		txt.setRegexpUserString(pmm.getRegexpUserString());
		txt.setValidationRegexp(pmm.getRegexpValidator());

		/*
		 * Start calculating maxlength and display length. Display length means the presented size on the
		 * UI (size= attribute); maxlength means just that - no data longer than maxlength can be entered.
		 * The calculation is complex and depends on the input type; the common types are handled here; other
		 * types should be handled by their own control factory.
		 *
		 * Length calculation is made fragile because the JPA @Column annotation's length attribute defaults
		 * to 255 (a decision made by some complete and utter idiot), so we take some special care with it
		 * if it has this value - it is not really used in the decision process anymore.
		 *
		 */
		//-- Precalculate some sizes for well-known types like numerics.
		int calcmaxsz = -1; // Calculated max input size
		int calcsz = -1; // Calculated display size,

		if(pmm.getPrecision() > 0) {
			// FIXME This should be localized somehow...
			//-- Calculate a size using scale and precision.
			int size = pmm.getPrecision();
			int d = size;
			String hint = pmm.getComponentTypeHint();
			if(hint != null) {
				hint = hint.toLowerCase();
			}
			if(hint == null || !hint.contains(MetaUtils.NO_MINUS)) {
				size++; // Allow minus
			}
			if(hint == null || !hint.contains(MetaUtils.NO_SEPARATOR)) {
				if(pmm.getScale() > 0) {
					size++; // Inc size to allow for decimal point or comma
					d -= pmm.getScale(); // Reduce integer part,
					if(d >= 4) { // Can we get > 999? Then we can have thousand-separators
						int nd = (d - 1) / 3; // How many thousand separators could there be?
						size += nd; // Increment input size with that
					}
				} else {
					if(d >= 4) { // Can we get > 999? Then we can have thousand-separators
						int nd = (d - 1) / 3; // How many thousand separators could there be?
						size += nd; // Increment input size with that
					}
				}
			}

			//-- If this is some form of money allow extra room for the currency indicator + space.
			if(NumericPresentation.isMonetary(pmm.getNumericPresentation())) {
				size += 2; // For now allow 2 extra characters
			}
			calcsz = size;
			calcmaxsz = size;
		} else if(NumericPresentation.isMonetary(pmm.getNumericPresentation())) {
			//-- Monetary amount with unclear precision- do a reasonable default. Allow for E 1.000.000.000,00 input size and way bigger max size
			calcsz = 18;
			calcmaxsz = 30;
		}

		//-- When a display length *is* present it *always* overrides any calculated value,
		if(pmm.getDisplayLength() > 0)
			calcsz = pmm.getDisplayLength();

		if(pmm.getLength() > 0 && pmm.getLength() != 255) { // Handle non-jpa-blundered lengths, if present
			//-- A length is present. It only defines the max. input size if no converter is present...
			if(pmm.getConverter() == null) {
				calcmaxsz = pmm.getLength(); // Defined max length always overrides anything else
				if(calcsz <= 0 && calcmaxsz < 40)
					calcsz = calcmaxsz; // Set the display size provided it is reasonable
			}
		}

		//-- Wrap it up...
		if(calcmaxsz > 0)
			txt.setMaxLength(calcmaxsz);
		if(calcsz <= 0) {
			if(calcmaxsz <= 0 || calcmaxsz > 40)
				calcsz = 40;
			else
				calcsz = calcmaxsz;
		}
		txt.setSize(calcsz);

		return new Result(txt, model, pmm);
	}

}
