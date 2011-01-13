package to.etc.domui.component.lookup;

import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

/**
 * ILookupControlInstance which uses a generic input control to create an equals criterion
 * on the input value, provided it is not null.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 19, 2009
 */
final public class EqLookupControlImpl extends AbstractLookupControlImpl {
	final private IInputNode< ? > m_control;

	final private String m_property;

	public EqLookupControlImpl(String property, IInputNode< ? > n) {
		super((NodeBase) n);
		m_control = n;
		m_property = property;
	}

	@Override
	public AppendCriteriaResult appendCriteria(QCriteria< ? > crit) throws Exception {
		Object value = m_control.getValue();
		if(value != null) {
			crit.eq(m_property, value);
			return AppendCriteriaResult.VALID;
		}
		return AppendCriteriaResult.EMPTY; // Okay but no data
	}
}
