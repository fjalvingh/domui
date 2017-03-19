package to.etc.domui.component.binding;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

import javax.annotation.*;

/**
 * This is a binding that "translates" a model property into a css style on the component, using
 * a value map.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/10/14.
 */
final public class StyleBinding implements IBinding {
	@Nonnull
	private final StyleBinder m_styleBinder;

	@Nonnull
	private final NodeBase m_component;

	/** The instance bound to */
	@Nullable
	private Object m_instance;

	/** If this contains whatever property-related binding this contains the property's meta model, needed to use it's value accessor. */
	@Nullable
	private IValueAccessor< ? > m_instanceProperty;

	@Nullable
	private String m_previousStyle;

	StyleBinding(@Nonnull StyleBinder styleBinder, @Nonnull NodeBase component) {
		m_styleBinder = styleBinder;
		m_component = component;
	}

	@Nonnull
	public <T, P> StyleBinder to(@Nonnull T instance, @Nonnull IValueAccessor<P> property) throws Exception {
		m_instance = instance;
		m_instanceProperty = property;
		m_component.addBinding(this);
		moveModelToControl();
		return m_styleBinder;
	}

	@Nonnull
	public <T> StyleBinder	to(@Nonnull T instance, @Nonnull String property) throws Exception {
		return to(instance, MetaManager.getPropertyMeta(instance.getClass(), property));
	}


	/**
	 * Update the style according to the model's value.
	 * @throws Exception
	 */
	@Override public void moveModelToControl() throws Exception {
		Object instance = m_instance;
		if(null == instance)
			throw new IllegalStateException("Instance bound to is null??");
		IValueAccessor<?> pmm = m_instanceProperty;
		if(null == pmm)
			throw new IllegalStateException("Instance property is null?");
		try {
			Object value = pmm.getValue(instance);
			String style = m_styleBinder.getStyleFor(value);
			updateStyle(style);

		} catch(Exception x) {
			System.err.println("Style binding error: "+x);
		}
	}

	/**
	 * Remove any previous style and add the new one, if applicable.
	 * @param style
	 */
	private void updateStyle(@Nullable String style) {
		if(DomUtil.isEqual(m_previousStyle, style))
			return;

		//-- If a previous style was set: remove it,
		String prev = m_previousStyle;
		if(null != prev) {
			m_component.removeCssClass(prev);
			m_previousStyle = null;
		}
		if(style != null) {
			m_previousStyle = style;
			m_component.addCssClass(style);
		}
	}

	/**
	 * A style binding never moves anything back to the model.
	 * @throws Exception
	 */
	@Nullable
	@Override public BindingValuePair<?, ?> getBindingDifference() throws Exception {
		return null;
	}

	@Override public <T> void setModelValue(T value) {
		throw new IllegalStateException("A style binding cannot move data to the model!");
	}

	/**
	 * A style binding never has errors.
	 * @return
	 */
	@Nullable @Override public UIMessage getBindError() {
		return null;
	}
}
