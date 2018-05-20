package to.etc.domui.component.binding;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeBase;

import java.util.HashMap;
import java.util.Map;

/**
 * EXPERIMENTAL Alter the style of a DomUI node depending on the value of a property bound to.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/10/14.
 */
final public class StyleBinder {
	@NonNull
	final private Map<Object, String> m_styleMap = new HashMap<>();

	@NonNull
	public StyleBinder define(@Nullable Object value, @NonNull String style) {
		m_styleMap.put(value, style);
		return this;
	}

	@NonNull
	public StyleBinding bind(@NonNull NodeBase component) {
		return new StyleBinding(this, component);
	}

	@Nullable
	public String getStyleFor(@Nullable Object value) {
		return m_styleMap.get(value);
	}
}
