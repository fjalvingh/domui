package to.etc.domui.component.wizard;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;

import javax.annotation.*;

/**
 * Holds the fragment for the navbar of the {@link WizardPopupWindow}
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 2-6-17.
 */
@DefaultNonNull
public class WizardPopupStepNavbarFragment extends Div {

	private String m_name;

	private boolean m_current;

	WizardPopupStepNavbarFragment(@Nonnull WizardPopupStep step, boolean isCurrentStep) {
		m_name = step.getStepName();
		m_current = isCurrentStep;
	}

	@Override
	public void createContent() throws Exception {
		Div stepName = new Div();
		stepName.setCssClass("steps-list-item");

		if(m_current) {
			LinkButton lb = new LinkButton(m_name);
			stepName.add(lb);
		} else {
			stepName.add(m_name);
		}

		add(stepName);
	}
}
