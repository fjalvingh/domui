package to.etc.domui.component.lookup.filter;

import java.math.*;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.webapp.query.*;
import to.etc.xml.*;

/**
 *  Translates a BigInteger value to XML and vice versa
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/8/16.
 */
@DefaultNonNull
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
