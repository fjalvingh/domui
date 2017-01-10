package to.etc.domui.component.lookup.filter;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.webapp.query.*;
import to.etc.xml.*;

/**
 * Translates an Integer value to XML and vice versa
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/8/16.
 */
@DefaultNonNull
final class IntegerTranslator implements ITranslator<Integer> {

	@Nullable
	@Override
	public Integer deserialize(QDataContext dc, Node node) throws Exception {
		Node valueNode = DomTools.nodeFind(node, VALUE);
		String value = DomTools.textFrom(valueNode);
		return Integer.valueOf(value);
	}

	@Override
	public boolean serialize(XmlWriter writer, Object o) throws Exception {
		if(o instanceof Integer) {
			writer.tag(ITranslator.VALUE);
			writer.write(o.toString());
			writer.tagendnl();
			return true;
		}
		return false;
	}
}
