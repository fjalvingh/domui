package to.etc.domui.component2.form4;

import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 6-3-18.
 */
public class ResponsiveFormLayouter implements IFormLayouter {
	@Nonnull
	final private FormBuilder.IAppender m_appender;

	private boolean m_horizontal;

	@Nullable
	private Div m_formContainer;

	@Nullable
	private Div m_lastPair;

	@Nullable
	private Div m_lastControlContainer;

	@Nullable
	private Div m_lastLabelContainer;

	public ResponsiveFormLayouter(@Nonnull FormBuilder.IAppender appender) {
		m_appender = appender;
	}

	@Override public void setHorizontal(boolean horizontal) {
		m_horizontal = horizontal;
	}

	@Override public void addControl(@Nonnull NodeBase control, @Nullable NodeContainer lbl, String controlCss, String labelCss, boolean append) {
		if(append) {
			Div controlContainer = m_lastControlContainer;
			Div labelContainer = m_lastLabelContainer;
			if(controlContainer != null && labelContainer != null) {
				if(null != lbl)
					labelContainer.add(lbl);
				controlContainer.add(control);
				return;
			}

			//-- No last pair added, just add a new set.
		}

		String fix = m_horizontal ? "-h" : "-v";

		Div form = m_formContainer;
		if(null == form) {
			form = m_formContainer = new Div("ui-f5 ui-f5" + fix);
			m_appender.add(form);
		}

		Div pair = m_lastPair = new Div("ui-f5-pair ui-f5-pair" + fix);
		form.add(pair);

		Div lc = m_lastLabelContainer = new Div("ui-f5-lbl ui-f5-lbl" + fix);
		pair.add(lc);
		if(null != lbl) {
			lc.add(lbl);
		}

		Div cc = m_lastControlContainer = new Div("ui-f5-ctl ui-f5-ctl" + fix);
		pair.add(cc);
		cc.add(control);
	}

	@Override public void clear() {
		m_formContainer = null;
		m_lastLabelContainer = null;
		m_lastControlContainer = null;
		m_lastPair = null;
	}
}
