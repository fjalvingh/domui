package to.etc.domui.util;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.webapp.nls.*;

/**
 * Renders the content for a node by looking up a property value of the specified class and rendering that one.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 16, 2010
 */
public class PropertyNodeContentRenderer<T> implements INodeContentRenderer<T> {
	@Nonnull
	final private PropertyValueConverter<T> m_converter;

	public PropertyNodeContentRenderer(@Nonnull PropertyValueConverter<T> converter) {
		m_converter = converter;
	}

	public PropertyNodeContentRenderer(String... properties) {
		m_converter = new PropertyValueConverter<T>(properties);
	}

	public void renderNodeContent(NodeBase component, NodeContainer node, T object, Object parameters) throws Exception {
		String val = m_converter.convertObjectToString(NlsContext.getLocale(), object);
		node.add(val);
	}
}
