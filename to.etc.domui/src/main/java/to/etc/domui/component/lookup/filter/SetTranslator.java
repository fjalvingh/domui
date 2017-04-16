package to.etc.domui.component.lookup.filter;

import java.util.*;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.webapp.query.*;
import to.etc.xml.*;

/**
 * Translates a Set of values to XML and vice versa.
 * This class writes the Set characteristics and then uses the Set's values
 * to (de)serialize them with the appropriate translator.
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/8/16.
 */
@DefaultNonNull
final class SetTranslator implements ITranslator<Set<?>> {

	@Nullable
	@Override
	public Set<?> deserialize(QDataContext dc, Node node) throws Exception {
		Set<Object> set = new HashSet<>();

		Node val = DomTools.nodeFind(node, VALUE);
		List<Node> items = DomTools.nodesFind(val, ITEM);
		for(Node item: items) {
			Node metaData = DomTools.nodeFind(item, METADATA);
			String serializerType = DomTools.strAttr(metaData, TYPE);
			Object o = LookupFilterTranslatorRegistry.deserialize(dc, serializerType, item);
			set.add(o);
		}

		return set;
	}

	@Override
	public boolean serialize(XmlWriter writer, Object o) throws Exception {
		if(o instanceof Set<?>) {
			Iterator<?> iter = ((Set<?>) o).iterator();
			writer.tag(VALUE);
			while(iter.hasNext()) {
				writer.tag(ITEM);
				Object o1 =	iter.next();
				o1.getClass().getCanonicalName();
				LookupFilterTranslatorRegistry.serialize(writer, o1, null);
				writer.tagendnl();
			}
			writer.tagendnl();
			return true;
		}
		return false;
	}
}
