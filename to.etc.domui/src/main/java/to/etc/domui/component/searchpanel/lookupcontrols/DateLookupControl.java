package to.etc.domui.component.searchpanel.lookupcontrols;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.input.DateInput2;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;
import to.etc.util.DateUtil;

import java.util.Date;
import java.util.Objects;

/**
 * Date lookup control: this shows two date input boxes, and returns a date period from them
 * that can be used to search some date.
 *
 * @since 2.0
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-12-17.
 */
public class DateLookupControl extends Div implements IControl<DatePeriod> {
	private boolean m_withTime;

	private String m_hint;

	private boolean m_readOnly;

	private boolean m_disabled;

	private boolean m_mandatory;

	private DateInput2 m_dateFrom = new DateInput2();

	private DateInput2 m_dateTo = new DateInput2();

	@Override public void createContent() throws Exception {
		add(m_dateFrom);
		m_dateFrom.setWithTime(m_withTime);
		Span sp = new Span(" " + Msgs.uiLookupDateTill.getString() + " ");
		add(sp);
		sp.setCssClass("ui-lfd-datetill");
		add(m_dateTo);
		m_dateTo.setWithTime(m_withTime);

		String hint = m_hint;
		if(hint != null) {
			m_dateFrom.setTitle(hint);
			m_dateTo.setTitle(hint);
		}
	}

	@Override
	public DatePeriod getValue() {
		Date from = m_dateFrom.getValue();
		Date to = m_dateTo.getValue();
		// Validate display dates before adding the inclusive +1 day to "until". Swapping after that
		// adjustment produces a wrong exclusive upper bound (INSD-155).
		if(from != null && to != null && from.getTime() > to.getTime()) {
			ValidationException vx = new ValidationException(Msgs.uiLookupDateOrder);
			m_dateTo.setMessage(UIMessage.error(vx));
			throw vx;
		}
		UIMessage toMessage = m_dateTo.getMessage();
		if(toMessage != null && toMessage.getCode() == Msgs.uiLookupDateOrder) {
			m_dateTo.setMessage(null);
		}
		if(null != to && !m_withTime) {
			//in case of date only search add 1 day and truncate time, since date only search is inclusive for dateTo
			to = DateUtil.addDays(to, 1);
		}
		return new DatePeriod(from, to);
	}

	@Override public void setValue(@Nullable DatePeriod v) {
		if(null == v) {
			m_dateFrom.setValue(null);
			m_dateTo.setValue(null);
		} else {
			m_dateFrom.setValue(v.getFrom());
			m_dateTo.setValue(v.getTo());
		}
	}

	@Override public DatePeriod getValueSafe() {
		return getValue();
	}

	@Override public boolean isReadOnly() {
		return m_readOnly;
	}

	@Override public void setReadOnly(boolean ro) {
		if(m_readOnly == ro)
			return;
		m_readOnly = ro;
		m_dateTo.setReadOnly(ro);
		m_dateFrom.setReadOnly(ro);
	}

	@Override public boolean isDisabled() {
		return m_disabled;
	}

	@Override public boolean isMandatory() {
		return m_mandatory;
	}

	@Override public void setMandatory(boolean ro) {
		m_mandatory = ro;
	}

	@Override public void setDisabled(boolean d) {
		if(m_disabled == d)
			return;
		m_disabled = d;
		m_dateFrom.setDisabled(d);
		m_dateTo.setDisabled(d);
	}

	@Nullable @Override public NodeBase getForTarget() {
		return m_dateFrom.getForTarget();
	}

	@Override public IValueChanged<?> getOnValueChanged() {
		return null;
	}

	@Override public void setOnValueChanged(IValueChanged<?> onValueChanged) {

	}

	public boolean isWithTime() {
		return m_withTime;
	}

	public void setWithTime(boolean withTime) {
		if(m_withTime == withTime)
			return;
		m_withTime = withTime;
		forceRebuild();
	}

	public String getHint() {
		return m_hint;
	}

	@Override
	public void setHint(String hint) {
		if(Objects.equals(m_hint, hint))
			return;
		m_hint = hint;
		forceRebuild();
	}
}
