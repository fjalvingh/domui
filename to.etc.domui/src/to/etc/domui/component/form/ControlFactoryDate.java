package to.etc.domui.component.form;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
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
	public int accepts(final PropertyMetaModel pmm, final boolean editable) {
		Class< ? > iclz = pmm.getActualType();
		if(Date.class.isAssignableFrom(iclz)) {
			return 2;
		}
		return 0;
	}

	public Result createControl(final IReadOnlyModel< ? > model, final PropertyMetaModel pmm, final boolean editable) {
		if(!editable) {
			Text<Date> txt = new Text<Date>(Date.class);
			txt.setReadOnly(true);
			return new Result(txt, model, pmm);
		}

		DateInput di = new DateInput();
		if(pmm.isRequired())
			di.setMandatory(true);
		if(pmm.getTemporal() == TemporalPresentationType.DATETIME)
			di.setWithTime(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			di.setLiteralTitle(s);
		return new Result(di, model, pmm);
	}
}
