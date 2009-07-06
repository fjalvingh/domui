package to.etc.domui.component.misc;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * This is a component which allows it's content to be literal XHTML.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 9, 2008
 */
public class LiteralXhtml extends NodeBase {
	private String m_xml;

	public LiteralXhtml() {
		super("div");
		setCssClass("ui-lxh");
	}

	@Override
	public void visit(final INodeVisitor v) throws Exception {
		v.visitLiteralXhtml(this);
	}

	public String getXml() {
		return m_xml;
	}

	public void setXml(final String xml) {
		if(DomUtil.isEqual(xml, m_xml))
			return;
		StringBuilder sb = new StringBuilder(xml.length());
		try {
			StringTool.entitiesToUnicode(sb, xml, true);
		} catch(Exception x) {
			throw new WrappedException(x);
		}
		m_xml = sb.toString();
		changed();
	}
}
