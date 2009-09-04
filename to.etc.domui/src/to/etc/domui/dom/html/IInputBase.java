package to.etc.domui.dom.html;

import to.etc.domui.dom.errors.*;

/**
 * Changed to extend INodeErrorDelegate since message related interface is defined there. 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 4 Sep 2009
 */
public interface IInputBase extends INodeErrorDelegate {

	public IValueChanged< ? , ? > getOnValueChanged();

	public void setOnValueChanged(IValueChanged< ? , ? > onValueChanged);
}
