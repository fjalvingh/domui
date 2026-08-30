package to.etc.domuidemo.pages.tutorial.component;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.binding.OldBindingHandler;
import to.etc.domui.component.input.AbstractDivControl;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;

/**
 * Tutorial, "writing a component": a control that holds a number of stars.
 * <p>
 * It is an ordinary Div that implements IControl&lt;Integer&gt; - which is all a
 * component is - and it gets most of that interface from {@link AbstractDivControl}.
 * What is left is what makes it a star rating: how it draws itself, what a click
 * on a star means, and what "mandatory" means for it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class StarRating extends AbstractDivControl<Integer> {
	private final int m_stars;

	public StarRating() {
		this(5);
	}

	public StarRating(int stars) {
		m_stars = stars;
	}

	@Override
	public void createContent() throws Exception {
		//-- setCssClass, not addCssClass: this runs again on every rebuild.
		setCssClass("dm-rating");
		if(isDisabled()) {
			addCssClass("dm-rating-disabled");
		} else if(isReadOnly()) {
			addCssClass("dm-rating-ro");
		}

		Integer value = internalGetValue();
		int rating = null == value ? 0 : value.intValue();
		for(int i = 1; i <= m_stars; i++) {
			int star = i;
			//-- Span(cssClass, text), so the star itself can be styled per state.
			Span span = new Span(star <= rating ? "dm-rating-on" : "dm-rating-off", star <= rating ? "★" : "☆");
			add(span);
			if(!isDisabled() && !isReadOnly()) {
				span.setTitle(star + " of " + m_stars);
				span.setClicked(a -> starClicked(star));
			}
		}
	}

	/**
	 * A click is this control's version of "the user typed something": it changes the
	 * value, moves it into whatever the control is bound to, and tells the page.
	 */
	private void starClicked(int star) throws Exception {
		Integer current = internalGetValue();
		Integer newValue = null != current && current.intValue() == star
			? null                                         // Clicking the current rating clears it
			: Integer.valueOf(star);

		setValue(newValue);                                // Rebuilds, but only on a real change
		OldBindingHandler.controlToModel(this);            // This request's binding pass already ran

		IValueChanged<StarRating> onValueChanged = (IValueChanged<StarRating>) getOnValueChanged();
		if(null != onValueChanged) {
			onValueChanged.onValueChanged(this);
		}
	}

	/**
	 * What "invalid" means for this control. It is called by both getValue() and
	 * getBindValue(); the difference between those two is who gets told about it.
	 */
	@Override
	protected void validateBindValue() {
		if(isMandatory() && null == internalGetValue()) {
			throw new ValidationException(Msgs.mandatory);
		}
	}

	/**
	 * getValue() reports: it puts the message on the control before throwing, so
	 * whoever asked for the value does not have to.
	 */
	@Override
	@Nullable
	public Integer getValue() {
		try {
			validateBindValue();
			setMessage(null);
			return internalGetValue();
		} catch(ValidationException vx) {
			setMessage(UIMessage.error(vx));
			throw vx;
		}
	}

	/**
	 * A label's "for" needs a real input to point at, and this control has none.
	 */
	@Nullable
	@Override
	public NodeBase getForTarget() {
		return null;
	}
}
