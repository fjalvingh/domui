package to.etc.domui.component.wizard;

import to.etc.domui.dom.html.*;
import to.etc.webapp.nls.*;

import javax.annotation.*;
import java.util.*;

/**
 * Holds the fragment for the navbar of the {@link WizardPopupWindow}
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 2-6-17.
 */
@DefaultNonNull
class WizardPopupStepNavbarFragment extends Div {

	private static final BundleRef BUNDLE = BundleRef.create(WizardPopupStepNavbarFragment.class, "messages");

	@Nullable
	private String m_icon;

	private final String m_name;

	private final boolean m_current;

	private final String m_layout;

	private final int m_currentNumber;

	private final int m_number;

	private final int m_total;

	WizardPopupStepNavbarFragment(@Nonnull String stepTitle, boolean isCurrentStep, @Nonnull String layout, @Nullable Map<String, String> icons, int currentStepNumber, int stepNumber, int totalNumber) {
		m_name = stepTitle;
		m_current = isCurrentStep;
		m_layout = layout;
		m_number = stepNumber;
		m_total = totalNumber;
		m_currentNumber = currentStepNumber;

		if(null != icons) {
			m_icon = icons.get(BUNDLE.getString("icons.indicator.valid"));
		}
	}

	@Override
	public void createContent() throws Exception {
		Div stepNumber = new Div();
		if(m_current) {
			stepNumber.setCssClass("ui-gwiz-steps-list-item-number-active");
			stepNumber.add("" + m_number);
		} else {
			if(m_number < m_currentNumber) {
				String icon = m_icon;
				if(null == icon) {
					stepNumber.setCssClass("ui-gwiz-steps-list-item-number-completed");
					stepNumber.add("" + m_number);
				} else {
					stepNumber.setCssClass("ui-gwiz-steps-list-item-icon-completed");
					stepNumber.add(new Img(icon));
				}
			} else if (m_number > m_currentNumber && m_number <= m_total) {
				stepNumber.setCssClass("ui-gwiz-steps-list-item-number-inactive");
				stepNumber.add("" + m_number);
			}
		}
		add(stepNumber);

		Div stepName = new Div();
		stepName.setCssClass("ui-gwiz-steps-list-item" + m_layout);
		if(m_current) {
			stepName.addCssClass("ui-gwiz-steps-list-item-active");
		} else {
			stepName.addCssClass("ui-gwiz-steps-list-item-inactive");
		}
		stepName.add(m_name);
		add(stepName);
	}
}
