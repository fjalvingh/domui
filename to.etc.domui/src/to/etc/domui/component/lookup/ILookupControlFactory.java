package to.etc.domui.component.lookup;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;

/**
 * Creates the stuff needed to generate a single property lookup control, plus
 * the stuff to handle the control's input and converting it to part of a
 * QCriteria restriction.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 23, 2008
 */
public interface ILookupControlFactory {
	/**
	 * Returns >=0 if this can create a lookup instance for a property.
	 * @param pmm
	 * @return
	 */
	public <X extends IInputNode< ? >> int accepts(SearchPropertyMetaModel pmm, X control);

	public <X extends IInputNode< ? >> ILookupControlInstance createControl(SearchPropertyMetaModel spm, X control);


}
