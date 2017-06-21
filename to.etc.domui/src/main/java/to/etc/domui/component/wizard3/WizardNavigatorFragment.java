package to.etc.domui.component.wizard3;

import java.util.*;

import to.etc.domui.dom.html.*;

/**
 * @authors <a href="mailto:jal@etc.to">Frits Jalvingh</a, <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 21-6-17.
 */
public class WizardNavigatorFragment extends Div {

	private final WizardDialog m_wizard;

	/** The step currently shown as "current" */
	private WizardStep m_currentStep;

	WizardNavigatorFragment(WizardDialog wizard) {
		m_wizard = wizard;
	}

	@Override
	public void createContent() throws Exception {
		createFragment();
	}

	void createFragment() throws Exception {
		List<WizardStep> stepList = m_wizard.getStepList();
		WizardStep current = m_currentStep = m_wizard.getCurrentStep();
		int currentPageIndex = stepList.indexOf(current);

		for(int stepNumber = 0; stepNumber < stepList.size(); stepNumber++) {
			if(stepNumber != 0) {
				add(new Div("ui-wznf-sep"));
			}
			WizardStep step = stepList.get(stepNumber);
			if(stepNumber < currentPageIndex) {
				renderAsPast(step);
			} else if(stepNumber == currentPageIndex) {
				renderAsCurrent(step);
			} else {
				renderAsFuture(step);
			}
		}
	}

	private void renderAsFuture(WizardStep step) {
		Div cont = new Div("ui-wznf-next");
		add(cont);
		cont.add(new Span(step.getStepLabel()));
	}

	private void renderAsCurrent(WizardStep step) {
		Div cont = new Div("ui-wznf-curr");
		add(cont);
		cont.add(new Span(step.getStepLabel()));
	}

	private void renderAsPast(WizardStep step) {
		Div cont = new Div("ui-wznf-past");
		add(cont);
		cont.add(new Span(step.getStepLabel()));
	}
}
