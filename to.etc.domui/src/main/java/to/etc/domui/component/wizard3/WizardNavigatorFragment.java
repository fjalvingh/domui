package to.etc.domui.component.wizard3;

import to.etc.domui.dom.html.*;

import javax.annotation.*;
import java.util.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a, <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 21-6-17.
 */
class WizardNavigatorFragment extends Div {

	static final String CURRENT = "currentStep";

	/** The step currently shown as "current" */
	@Nullable
	private AbstractWizardStep m_currentStep;

	@Nonnull
	private final AbstractWizardDialog m_wizard;

	WizardNavigatorFragment(AbstractWizardDialog wizard) {
		m_wizard = wizard;
	}

	@Override
	public void createContent() throws Exception {
		Div area = new Div();
		area.setCssClass("ui-wznf-area");
		add(area);

		AbstractWizardStep current = m_currentStep = m_wizard.getCurrentStep();
		List<AbstractWizardStep> stepList = m_wizard.getStepList();
		int currentPageIndex = stepList.indexOf(current);
		for(int stepNumber = 0; stepNumber < stepList.size(); stepNumber++) {
			if(stepNumber != 0) {
				Div seperator = new Div();
				seperator.setCssClass("ui-wznf-sep");
				area.add(seperator);
			}
			AbstractWizardStep step = stepList.get(stepNumber);
			if(stepNumber < currentPageIndex) {
				renderAsPast(area, step);
			} else if(stepNumber == currentPageIndex) {
				renderAsCurrent(area, step);
			} else {
				renderAsFuture(area, step);
			}
		}
	}

	private void renderAsFuture(@Nonnull Div area, @Nonnull AbstractWizardStep step) {
		Div cont = new Div();
		cont.setCssClass("ui-wznf-next");
		area.add(cont);
		cont.add(new Span(step.getStepLabel()));
	}

	private void renderAsCurrent(@Nonnull Div area, @Nonnull AbstractWizardStep step) {
		Div cont = new Div();
		cont.setCssClass("ui-wznf-curr");
		area.add(cont);
		cont.add(new Span(step.getStepLabel()));
	}

	private void renderAsPast(@Nonnull Div area, @Nonnull AbstractWizardStep step) {
		Div cont = new Div();
		cont.setCssClass("ui-wznf-prev");
		area.add(cont);
		cont.add(new Span(step.getStepLabel()));
	}

	@Nullable
	public AbstractWizardStep getCurrentStep() throws Exception {
		return m_currentStep;
	}

	public void setCurrentStep(@Nonnull AbstractWizardStep currentStep) {
		m_currentStep = currentStep;
		forceRebuild();
	}
}
