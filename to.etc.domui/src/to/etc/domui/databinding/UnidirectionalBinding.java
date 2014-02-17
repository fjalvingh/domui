package to.etc.domui.databinding;

import javax.annotation.*;

import to.etc.domui.databinding.value.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

/**
 * Unidirectional bind from a -&gt; b.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 30, 2013
 */
final public class UnidirectionalBinding extends Binding {
	private IReadWriteModel< ? > m_model;

	public UnidirectionalBinding(@Nonnull BindingContext context, @Nonnull IObservableValue< ? > sourceo, @Nonnull IReadWriteModel< ? > mdl) throws Exception {
		super(context, sourceo);
		m_model = mdl;
		addSourceListener();
		moveSourceToTarget();
	}

	@Override
	protected void moveSourceToTarget() throws Exception {
		Object val;
		try {
			val = ((IObservableValue<Object>) m_from).getValue();
			IUniConverter<Object, Object> uc = (IUniConverter<Object, Object>) m_converter;
			if(null != uc) {
				val = uc.convertSourceToTarget(val);
			}
			((IReadWriteModel<Object>) m_model).setValue(val);
		} catch(ValidationException vx) {
			bindingError(vx);
			return;
		}
	}
}