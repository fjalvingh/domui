package to.etc.domui.component.lookup.filter;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.webapp.query.*;
import to.etc.xml.*;

/**
 * Translates a String value to XML and vice versa
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/8/16.
 */
@DefaultNonNull
final class StringTranslator implements ITranslator<String> {

	@Nullable
	@Override
	public String deserialize(QDataContext dc, Node node) throws Exception {
		Node valueNode = DomTools.nodeFind(node, VALUE);
		if(null == valueNode)
			return null;
		return DomTools.textFrom(valueNode);
	}

	@Override
	public boolean serialize(XmlWriter writer, Object o) throws Exception {
		if(o instanceof String) {
			writer.tag(ITranslator.VALUE);
			writer.write(o.toString());
			writer.tagendnl();
			return true;
		}
		return false;
	}
}
