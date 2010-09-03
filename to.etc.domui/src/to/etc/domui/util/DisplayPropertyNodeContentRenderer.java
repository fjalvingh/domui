package to.etc.domui.util;

import java.util.*;

import to.etc.domui.component.meta.*;
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
	//	private ClassMetaModel m_targetClassModel;

	private List<ExpandedDisplayProperty> m_list;

	private List<ExpandedDisplayProperty> m_flat;

	public DisplayPropertyNodeContentRenderer(ClassMetaModel cmm, List<ExpandedDisplayProperty> list) {
		//		m_targetClassModel = cmm;
		m_list = list;
	}

	private void prepare() {
		if(m_flat != null)
			return;
		m_flat = ExpandedDisplayProperty.flatten(m_list);
	}

	@Override
	public void renderNodeContent(NodeBase component, NodeContainer node, Object object, Object parameters) throws Exception {
		prepare();
		StringBuilder sb = new StringBuilder();

		for(ExpandedDisplayProperty xdp : m_flat) {
			if(sb.length() > 0)
				sb.append(' ');
			String s = xdp.getPresentationString(object);
			sb.append(s);
		}
		node.setText(sb.toString());
	}
}
