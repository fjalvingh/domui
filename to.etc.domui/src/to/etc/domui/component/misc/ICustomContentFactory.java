package to.etc.domui.component.misc;

import to.etc.domui.dom.html.*;

/**
 * Provides creation of custom content that is usually added to some standard rendering for specified object data.
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Feb 12, 2010
 */
public interface ICustomContentFactory<T> {
	NodeBase createNode(T object) throws Exception;
}
