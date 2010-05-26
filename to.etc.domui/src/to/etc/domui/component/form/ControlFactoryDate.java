package to.etc.domui.component.form;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;

/**
 * Accepts the "java.util.Date" type only and creates a DateInput component for it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
public class ControlFactoryDate implements ControlFactory {
	/**
	 * Accept java.util.Date class <i>only</i>.
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	public int accepts(final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass, Object context) {
		if(controlClass != null && !controlClass.isAssignableFrom(DateInput.class))
			return -1;

		Class< ? > iclz = pmm.getActualType();
		if(Date.class.isAssignableFrom(iclz)) {
			return 2;
		}
		return 0;
	}

	public ControlFactoryResult createControl(final IReadOnlyModel< ? > model, final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass, Object context) {
		if(!editable && (controlClass == null || controlClass.isAssignableFrom(Text.class))) {
			//			Text<Date> txt = new Text<Date>(Date.class);
			//			txt.setReadOnly(true);
			// FIXME EXPERIMENTAL Replace the TEXT control with a DisplayValue control.
			DisplayValue<Date> txt = new DisplayValue<Date>(Date.class);

			//20100208 vmijic - fixed readonly presentation for date fields.
			Class< ? extends IConverter<Date>> cc;
			if(pmm == null)
				cc = DateTimeConverter.class;
			else {
				switch(pmm.getTemporal()){
					default:
						throw new IllegalStateException("Unsupported temporal metadata type: " + pmm.getTemporal());
					case UNKNOWN:
						/*$FALL_THROUGH$*/
					case DATETIME:
						cc = DateTimeConverter.class;
						break;
					case DATE:
						cc = DateConverter.class;
						break;
					case TIME:
						cc = TimeOnlyConverter.class;
						break;
				}
			}

			txt.setConverter(ConverterRegistry.getConverterInstance(cc));
			return new ControlFactoryResult(txt, model, pmm);
		}

		DateInput di = new DateInput();
		if(pmm.isRequired())
			di.setMandatory(true);
		if(!editable)
			di.setDisabled(true);
		if(pmm.getTemporal() == TemporalPresentationType.DATETIME)
			di.setWithTime(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			di.setTitle(s);
		return new ControlFactoryResult(di, model, pmm);
	}
}
