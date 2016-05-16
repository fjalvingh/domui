package to.etc.domui.component.lookup.filter;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.domui.component.lookup.*;
import to.etc.webapp.query.*;
import to.etc.xml.*;

/**
 * Translates the controls on a {@link LookupForm} to a readable format
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 1/27/16.
 */
@DefaultNonNull
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
