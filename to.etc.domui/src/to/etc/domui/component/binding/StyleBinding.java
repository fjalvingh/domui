package to.etc.domui.component.binding;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

import javax.annotation.*;

/**
 * This is a binding between a single component and a single property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/10/14.
 */
final public class StyleBinding {
	@Nonnull
	private final StyleBinder m_styleBinder;

	@Nonnull
	private final NodeBase m_component;

	public StyleBinding(@Nonnull StyleBinder styleBinder, @Nonnull NodeBase component) {
		m_styleBinder = styleBinder;
		m_component = component;
	}

	@Nonnull
	public <T, P> StyleBinder to(@Nonnull T instance, @Nonnull IValueAccessor<P> property) {
		return m_styleBinder;
	}

	@Nonnull
	public <T> StyleBinder	to(@Nonnull T instance, @Nonnull String property) {
		return to(instance, MetaManager.getPropertyMeta(instance.getClass(), property));
	}
}
