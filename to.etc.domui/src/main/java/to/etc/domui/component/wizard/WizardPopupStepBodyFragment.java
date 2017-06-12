package to.etc.domui.component.wizard;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.menu.*;
import to.etc.domui.dom.html.*;

import javax.annotation.*;
import java.util.*;

/**
 * Holds the fragment for the body of a {@link WizardPopupWindow} step {@link WizardPopupStep}
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 2-6-17.
 */
public class WizardPopupStepBodyFragment extends Div {

	private String m_previousButtonName;

	private String m_nextButtonName;

	private String m_cancelButtonName;

	private String m_endButtonName;

	private HashMap<WizardPopupStepActions, IUIAction<Void>> m_actions;

	private boolean m_isFirst;

	private boolean m_isLast;

	private IWizardPopupStep m_body;

	WizardPopupStepBodyFragment(@Nonnull WizardPopupStep step, @Nonnull HashMap<WizardPopupStepActions, IUIAction<Void>> actions, boolean isFirstStep, boolean isLastStep) {
		m_previousButtonName = step.getPreviousButtonName();
		m_cancelButtonName = step.getCancelButtonName();
		m_nextButtonName = step.getNextButtonName();
		m_endButtonName = step.getEndButtonName();
		m_body = step.getStepBody();
		m_actions = actions;
		m_isFirst = isFirstStep;
		m_isLast = isLastStep;
	}

	public IWizardPopupStep getBody() {
		return m_body;
	}

	@Override
	public void createContent() throws Exception {
		Div stepBody = m_body.getWizardPopupStepDiv();
		stepBody.setCssClass("wizard-body");
		add(stepBody);

		Div buttonArea = new Div();
		buttonArea.setCssClass("button-container");

		ButtonBar bb = new ButtonBar();

		DefaultButton previousButton = new DefaultButton(m_actions.get(WizardPopupStepActions.PREVIOUS));
		previousButton.setText(m_previousButtonName);
		bb.addButton(previousButton);

		if(m_isFirst) {
			previousButton.setDisabled(true);
		}

		DefaultButton nextEndButton;
		if(m_isLast) {
			nextEndButton = new DefaultButton(m_actions.get(WizardPopupStepActions.FINISH));
			nextEndButton.setText(m_endButtonName);
		} else {
			nextEndButton = new DefaultButton(m_actions.get(WizardPopupStepActions.NEXT));
			nextEndButton.setText(m_nextButtonName);
		}
		nextEndButton.bind("disabled").to(m_body, IWizardPopupStep.IS_DISABLED);
		bb.addButton(nextEndButton);

		DefaultButton cancelButton = new DefaultButton(m_actions.get(WizardPopupStepActions.CANCEL));
		cancelButton.setText(m_cancelButtonName);
		bb.addButton(cancelButton);

		buttonArea.add(bb);
		add(buttonArea);
	}
}
