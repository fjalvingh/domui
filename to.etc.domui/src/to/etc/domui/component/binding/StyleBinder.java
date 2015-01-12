package to.etc.domui.component.binding;

import to.etc.domui.dom.html.*;

import javax.annotation.*;
import java.util.*;

/**
 * EXPERIMENTAL Alter the style of a DomUI node depending on the value of a property bound to.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/10/14.
 */
final public class StyleBinder {
	@Nonnull
	final private Map<Object, String> m_styleMap = new HashMap<>();

	@Nonnull
	public StyleBinder define(@Nullable Object value, @Nonnull String style) {
		m_styleMap.put(value, style);
		return this;
	}

	@Nonnull
	public StyleBinding bind(@Nonnull NodeBase component) {
		return new StyleBinding(this, component);
	}

	@Nullable
	public String getStyleFor(@Nullable Object value) {
		return m_styleMap.get(value);
	}
}
