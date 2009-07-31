package to.etc.domui.component.lookup;

import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

/**
 * Encapsulates a single created lookup "part" in the lookup form, and
 * defines the possible actions that we can define on it. This should
 * be able to return it's presentation, and it should be able to add
 * it's restrictions (caused by the user entering data in it's controls)
 * to a QCriteria.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 31, 2009
 */
public interface ILookupControlInstance {
	/**
	 * Return all of the nodes (input and otherwise) that together form the complete visual representation
	 * of this lookup line. This may NOT return null OR an empty list.
	 * @return
	 */
	public NodeBase[] getInputControls();

	/**
	 * Returns the control where the label should be attached to. Can return null, in that case the first
	 * IInput control or the first node in the list will be used.
	 * @return
	 */
	public NodeBase getLabelControl();

	/**
	 * When called this should clear all data input into the control instances, causing them to
	 * be empty (not adding to the restrictions set).
	 */
	public void clearInput();

	/**
	 * Evaluate the contents of the input for this lookup line; if the user has
	 * added data there then add the values to the query.
	 * @param crit
	 * @return
	 * @throws Exception
	 */
	public boolean appendCriteria(QCriteria< ? > crit) throws Exception;
}
