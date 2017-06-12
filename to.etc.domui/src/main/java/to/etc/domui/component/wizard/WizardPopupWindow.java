package to.etc.domui.component.wizard;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.menu.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.nls.*;

import javax.annotation.*;
import java.util.*;

/**
 * This class holds the WizardPopupWindow for creating a dynamic wizard window.
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 2-6-17.
 */
@DefaultNonNull
public class WizardPopupWindow extends Window {

	private static final BundleRef BUNDLE = BundleRef.create(WizardPopupWindow.class, "messages");

	private static final String DEFAULT_NEXT_BUTTON_LABEL = BUNDLE.getString("wizardstep.default.nextbutton");

	private static final String DEFAULT_CANCEL_BUTTON_LABEL = BUNDLE.getString("wizardstep.default.cancelbutton");

	private static final String DEFAULT_END_BUTTON_LABEL = BUNDLE.getString("wizardstep.default.endbutton");

	private static final String DEFAULT_BACK_BUTTON_LABEL = BUNDLE.getString("wizardstep.default.backbutton");

	private static final String STEP_LABEL_NAME = "name";

	private static final String STEP_LABEL_CANCEL = "cancel";

	private static final String STEP_LABEL_BACK = "back";

	private static final String STEP_LABEL_NEXT = "next";

	private static final String STEP_LABEL_END = "end";

	private final ArrayList<WizardPopupStep> m_steps = new ArrayList<>();

	private WizardPopupStep m_currentStep;

	private Div m_currentStepBody;

	public WizardPopupStep getCurrentStep() {
		return m_currentStep;
	}

	public void setCurrentStep(@Nonnull WizardPopupStep step) {
		m_currentStep = step;
	}

	public ArrayList<WizardPopupStep> getSteps() {
		return m_steps;
	}

	/**
	 * WizardPopupWindow's (i.e. 'wizard's') constructor. Setup the wizard is easy. First, provide a valid String title. Next,
	 * provide the steps that the wizard has to display by adding a Map with step labels (to create those labels, one can use
	 * the static method createWizardStepLabels in this class), and by adding an implementation of {@link IWizardPopupStep}.
	 * @param wizardTitle
	 * @param steps
	 */
	public WizardPopupWindow(@Nonnull String wizardTitle, @Nonnull Map<Map<String, String>, IWizardPopupStep> steps) {
		super(true, true, 1280, 800, wizardTitle);
		setupWizard(steps);
	}

	/**
	 * This setups the wizard with the values provided in the constructor.
	 * @param steps
	 */
	private void setupWizard(@Nonnull Map<Map<String, String>, IWizardPopupStep> steps) {
		if(steps.size() <= 0) {
			throw new IllegalStateException("At least one phase must be defined!");
		}
		for(Map<String, String> step : steps.keySet()) {
			m_steps.add(new WizardPopupStep(step.get(STEP_LABEL_NAME), steps.get(step), step.get(STEP_LABEL_BACK), step.get(STEP_LABEL_NEXT), step.get(STEP_LABEL_CANCEL), step.get(STEP_LABEL_END)));
		}
		m_currentStep = m_steps.get(0);
	}

	@Override
	public void createContent() throws Exception {
		refreshNavbarContainer();
		refreshBodyContainer();
	}

	/**
	 * This refreshes the div that holds the wizard's navbar.
	 */
	public void refreshNavbarContainer() {
		Div navbar = buildNavbar();
		Div stepsContainer = buildStepsContainer();
		navbar.add(stepsContainer);
		add(navbar);
	}

	/**
	 * This refreshes the div that holds the body of the current step.
	 * @throws Exception
	 */
	public void refreshBodyContainer() throws Exception {
		if(null == m_currentStepBody) {
			m_currentStepBody = new Div();
		}
		m_currentStepBody.removeAllChildren();
		m_currentStepBody.add(new WizardPopupStepBodyFragment(m_currentStep, getActions(), isFirstStep(), isLastStep()));
		add(m_currentStepBody);
	}

	/**
	 * Returns whether the current step is the first one.
	 * @return
	 */
	private boolean isFirstStep() {
		return m_steps.indexOf(m_currentStep) == 0;
	}

	/**
	 * Returns whether the current step is the last one.
	 * @return
	 */
	private boolean isLastStep() {
		return m_steps.indexOf(m_currentStep) + 1 == m_steps.size();
	}

	/**
	 * Build the wizard's navbar that holds all available steps.
	 * @return
	 */
	private Div buildNavbar() {
		Div navbar = new Div();
		navbar.setCssClass("wizard-navbar");
		return navbar;
	}

	/**
	 * This creates a container that holds all available wizard steps
	 * in the navbar.
	 * @return
	 */
	private Div buildStepsContainer() {
		Div phasesContainer = new Div();
		phasesContainer.setCssClass("steps-list-container");

		Iterator<WizardPopupStep> itr = m_steps.iterator();
		while(itr.hasNext()) {
			WizardPopupStep phase = itr.next();
			if(phase.equals(m_currentStep)) {
				phasesContainer.add(new WizardPopupStepNavbarFragment(phase, true));
			} else {
				phasesContainer.add(new WizardPopupStepNavbarFragment(phase, false));
			}
			if(itr.hasNext()) {
				phasesContainer.add(">");
			}
		}
		return phasesContainer;
	}

	/**
	 * Create list of available wizard actions (e.g. next button, end button).
	 * @return
	 */
	private Map<WizardPopupStepActions, IUIAction<Void>> getActions() {
		Map<WizardPopupStepActions, IUIAction<Void>> actions = new HashMap<>();
		for(WizardPopupStepActions action : WizardPopupStepActions.values()) {
			actions.put(action, action.getAction(this));
		}
		return actions;
	}

	/**
	 * With this, one can create wizard step labels. Only the step title label is mandatory.
	 * If other labels are not provided, the default labels will be used.
	 * @param titleLabel
	 * @param cancelButtonLabel
	 * @param backButtonLabel
	 * @param nextButtonLabel
	 * @param endButtonLabel
	 * @return
	 */
	public static Map<String, String> createWizardStepLabels(@Nonnull String titleLabel, @Nullable String cancelButtonLabel, @Nullable String backButtonLabel, @Nullable String nextButtonLabel, @Nullable String endButtonLabel) {
		Map<String, String> buttonLabels = new HashMap<>();
		buttonLabels.put(STEP_LABEL_NAME, titleLabel);
		buttonLabels.put(STEP_LABEL_CANCEL, null == cancelButtonLabel ? DEFAULT_CANCEL_BUTTON_LABEL : cancelButtonLabel);
		buttonLabels.put(STEP_LABEL_BACK, null == backButtonLabel ? DEFAULT_BACK_BUTTON_LABEL : backButtonLabel);
		buttonLabels.put(STEP_LABEL_NEXT, null == nextButtonLabel ? DEFAULT_NEXT_BUTTON_LABEL : nextButtonLabel);
		buttonLabels.put(STEP_LABEL_END, null == endButtonLabel ? DEFAULT_END_BUTTON_LABEL : endButtonLabel);
		return buttonLabels;
	}
}
