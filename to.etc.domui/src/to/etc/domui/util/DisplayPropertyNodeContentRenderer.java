package to.etc.domui.util;

import java.util.*;

import to.etc.domui.component.meta.impl.*;
import to.etc.domui.dom.html.*;

/**
 * This INodeRenderer implementation renders a content node by using a list of DisplayPropertyMetaModel data 
 * from the metamodel, rendering a single string formed by concatenating all display properties and getting
 * their string representation from the original source object (passed in as 'object').   
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 21, 2008
 */
public class DisplayPropertyNodeContentRenderer implements INodeContentRenderer<Object> {
	private List<DisplayPropertyMetaModel> m_list;

	public DisplayPropertyNodeContentRenderer(List<DisplayPropertyMetaModel> list) {
		m_list = list;
	}

	public void renderNodeContent(NodeBase component, NodeContainer node, Object object, Object parameters) throws Exception {
		StringBuilder sb = new StringBuilder();
		for(DisplayPropertyMetaModel dm : m_list) {
			if(sb.length() > 0)
				sb.append(' ');
			sb.append(dm.getAsString(object));
		}
		node.setButtonText(sb.toString());
	}
}
