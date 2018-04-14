package to.etc.domui.component.lookup.filter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Node;
import to.etc.webapp.query.QDataContext;
import to.etc.xml.DomTools;
import to.etc.xml.XmlWriter;

import java.math.BigInteger;

/**
 *  Translates a BigInteger value to XML and vice versa
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/8/16.
 */
@NonNullByDefault
final class BigIntegerTranslator implements ITranslator<BigInteger> {

	@Nullable
	@Override
	public BigInteger deserialize(QDataContext dc, Node node) throws Exception {
		Node valueNode = DomTools.nodeFind(node, VALUE);
		String value = DomTools.textFrom(valueNode);
		return new BigInteger(value);
	}

	@Override
	public boolean serialize(XmlWriter writer, Object o) throws Exception {
		if(o instanceof BigInteger) {
			writer.tag(ITranslator.VALUE);
			writer.write(o.toString());
			writer.tagendnl();
			return true;
		}
		return false;
	}
}
