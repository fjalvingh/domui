package to.etc.domui.component.lookup.filter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.w3c.dom.Node;
import to.etc.domui.component.lookup.LookupForm;
import to.etc.webapp.query.QDataContext;
import to.etc.xml.DomTools;
import to.etc.xml.XmlWriter;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Translates the controls on a {@link LookupForm} to a readable format
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 1/27/16.
 */
@NonNullByDefault
public final class LookupFilterTranslator {

	private static final String FILTER = "Filter";

	private LookupFilterTranslator() {
	}

	public static String serialize(Map<String, ?> filterValues) throws Exception {
		StringWriter serializedFilter = new StringWriter();
		XmlWriter w = new XmlWriter(serializedFilter);
		w.tag(FILTER);
		for(Entry<String, ?> entry : filterValues.entrySet()) {
			Object filterValue = entry.getValue();
			if(filterValue != null) {
				w.tag(ITranslator.ITEM);
				LookupFilterTranslatorRegistry.serialize(w, filterValue, entry.getKey());
				w.tagendnl();
			}
		}
		w.tagendnl();
		w.close();
		return serializedFilter.toString();
	}

	public static Map<String, Object> deserialize(QDataContext dc, String filterQuery) throws Exception {
		Map<String, Object> map = new HashMap<>();
		Node documentRoot = DomTools.getDocumentRoot(filterQuery, "", false);
		List<Node> filterValueNodes = DomTools.nodesFind(documentRoot, ITranslator.ITEM);
		for(int i = 0; i < filterValueNodes.size(); i++) {
			Node filterValueNode = filterValueNodes.get(i);
			Node metaDataNode = DomTools.nodeFind(filterValueNode, ITranslator.METADATA);
			if(metaDataNode == null) { // FIXME Is this possible? "This can happen when a filter value is not serialized"
				continue;
			}
			String key = DomTools.strAttr(metaDataNode, ITranslator.KEY);
			String type = DomTools.strAttr(metaDataNode, ITranslator.TYPE);

			map.put(key, LookupFilterTranslatorRegistry.deserialize(dc, type, filterValueNode));
		}
		return map;
	}
}
