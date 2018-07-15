/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.binding;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IDisplayControl;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.IValueAccessor;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.CodeException;

/**
 * This binds the control's VALUE property to some model property (bidirectional binding).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2009
 */
@NonNullByDefault
final public class ComponentPropertyBindingBidi<C extends NodeBase, CV, M, MV> extends AbstractComponentPropertyBinding<C, CV, M, MV> implements IBinding {
	@Nullable
	final private IBidiBindingConverter<CV, MV> m_converter;

	public ComponentPropertyBindingBidi(C control, PropertyMetaModel<CV> controlProperty, M modelInstance, IValueAccessor<MV> accessor, @Nullable IBidiBindingConverter<CV, MV> converter) {
		super(control, controlProperty, modelInstance, accessor);
		m_converter = converter;
	}

	/**
	 * If this binding is in error: return the message describing that error.
	 */
	@Override
	@Nullable
	public UIMessage getBindError() {
		return m_bindError;
	}

	/**
	 * Calculate the list of changes made to controls, as part one of the controlToModel
	 * process. Each control whose value changed will be registered in a list of
	 * {@link BindingValuePair} instances which will also contain any error message.
	 *
	 * This is the *hard* part of binding: it needs to handle control errors caused by bindValue() throwing
	 * an exception.
	 */
	@Override
	@Nullable
	public BindingValuePair<MV> getBindingDifference() throws Exception {
		NodeBase control = m_control;
		if(control instanceof IDisplayControl)
			return null;

		/*
		 * jal 20150414 Readonly (display) and disabled controls should not bind their value
		 * back to the model. This solves the following problem (at least for these kind of
		 * fields): take a model that has two Text<Integer> controls: one editable bound to
		 * property a, and one readonly bound to b.
		 * The setter for 'a' calculates a new value for b somehow (like b = a + 12).
		 *
		 * When the screen renders a will be 0 and b will be 12, so the Text controls represent
		 * that. Now when Text for a changes to 10 the following happens:
		 * - 10 gets moved to setA(), and this calls setB(22). So property b is now 22.
		 * - 12 gets moved to setB() from the _unchanged_ Text<> from b, so overwriting the new value.
		 * This cause of events is clearly wrong for readonly/disabled fields, so we disable
		 * them from updating the model.
		 *
		 * The general case, where both controls are editable, amounts to whom should
		 * win in an update: if Text<b> changed due to a user and Text<A> also changed
		 * and caused an update to b - which update is "the most important"? This is
		 * not yet solved (but might be by letting either model or UI win in case of a
		 * conflicting model update).
		 */
		if(control instanceof IControl) {
			IControl<CV> ict = (IControl<CV>) control;
			if(ict.isDisabled() || ict.isReadOnly()) {
				return null;
			}
		}

		/*
		 * Get the control's value. If the control is in error (validation/conversion) then
		 * add the problem inside the Error collector, signaling a problem to any logic
		 * that would run after.
		 */
		MV controlModelValue;
		UIMessage newError = null;
		try {
			CV controlValue = m_controlProperty.getValue(m_control);
			IBidiBindingConverter<CV, MV> converter = m_converter;
			if(converter == null) {
				controlModelValue = (MV) controlValue;
			} else {
				controlModelValue = converter.controlToModel(controlValue);
			}
			m_lastValueFromControlAsModelValue = controlModelValue;
			//System.out.println(this + ": diff - control value = " + controlValue);
			m_bindError = null;
		} catch(CodeException cx) {
			controlModelValue = null;
			m_lastValueFromControlAsModelValue = null;
			newError = UIMessage.error(cx);
			newError.setErrorNode(control);
			newError.setErrorLocation(control.getErrorLocation());
			if(!newError.equals(control.getMessage())) {
				m_bindError = newError;
			}
			//System.out.println(this + ": diff - exception " + cx.toString());

			//System.out.println("~~ " + control + " to " + instanceProperty + ": " + cx);
		}

		//-- When in error we cannot set anything anyway, so exit.
		if(null != newError && !newError.getCode().equals(Msgs.MANDATORY) ) {
			/*
			 * jal 20171018 When a mandatory LookupInput gets cleared its value becomes null, and this
			 * value should be propagated to the model. It seems likely that in ALL cases of error
			 * we need to move a null there!
			 */

			return null;
		}

		MV currentModelValue = getValueFromModel();
		if(MetaManager.areObjectsEqual(currentModelValue, controlModelValue))
			return null;

		return new BindingValuePair<>(this, controlModelValue);
	}

	@Nullable
	@Override protected CV convertModelToControl(@Nullable MV modelValue) throws Exception {
		IBidiBindingConverter<CV, MV> converter = m_converter;
		if(null != converter) {
			return converter.modelToControl(modelValue);
		} else {
			return (CV) modelValue;
		}
	}
}
