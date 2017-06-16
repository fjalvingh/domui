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

	private Map<Map<String, String>, AbstractWizardPopupStepBase> m_steps = new LinkedHashMap<>();

	private AbstractWizardPopupStepBase m_currentStep;

	private Div m_currentStepBody;

	private Div m_currentStepsContainer;

	private Div m_currentNavbar;

	private final boolean m_horizontal;

	private final Map<String, String> m_icons;

	private final String m_layout;

	public Map<Map<String, String>, AbstractWizardPopupStepBase> getSteps() {
		return m_steps;
	}

	public AbstractWizardPopupStepBase getCurrentStep() {
		return m_currentStep;
	}

	public void setCurrentStep(@Nonnull AbstractWizardPopupStepBase step) {
		m_currentStep = step;
	}

	/**
	 * Setup the wizard is easy. First, provide a valid String title. Next, provide the steps that the wizard has to display by adding
	 * a Map with step labels (to create those labels, one can use the static method createWizardStepLabels in this class), and by adding
	 * an implementation of {@link AbstractWizardPopupStepBase}. Finally, set horizontal to true if you want a wizard with a horizontal
	 * navbar.
	 * @param wizardTitle
	 * @param steps
	 * @param horizontal
	 */
	public WizardPopupWindow(@Nonnull String wizardTitle, @Nonnull Map<Map<String, String>, AbstractWizardPopupStepBase> steps, @Nullable Map<String, String> icons, boolean horizontal) {
		super(true, true, 1024, 768, wizardTitle);
		m_icons = icons;
		m_horizontal = horizontal;
		m_layout = m_horizontal ? BUNDLE.getString("layout.style.horizontal") : BUNDLE.getString("layout.style.vertical");
		setupWizard(steps);
	}

	/**
	 * This setups the wizard with the values provided in the constructor.
	 * @param steps
	 */
	private void setupWizard(@Nonnull Map<Map<String, String>, AbstractWizardPopupStepBase> steps) {
		if(steps.size() <= 0) {
			throw new IllegalStateException("At least one step must be defined!");
		}
		m_steps = steps;
		m_currentStep = getStepsList().get(0);
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
		m_currentStepBody.add(new WizardPopupStepBodyFragment(m_currentStep, getCurrentStepLabels(), getActions(), isFirstStep(), isLastStep(), m_layout, m_icons));
		add(m_currentStepBody);
	}

	/**
	 * Returns the map of labels for the current step. Throws an exception if it is unable to do that.
	 * @return
	 */
	private Map<String, String> getCurrentStepLabels() {
		for(Map.Entry<Map<String, String>, AbstractWizardPopupStepBase> step : getSteps().entrySet()) {
			if(step.getValue().equals(m_currentStep)) {
				return step.getKey();
			}
		}
		throw new IllegalStateException("Cannot get current step labels. Do not use an AbstractWizardPopupStepBase instance more than once.");
	}

	/**
	 * Returns whether the current step is the first one.
	 * @return
	 */
	private boolean isFirstStep() {
		List<AbstractWizardPopupStepBase> steps = getStepsList();
		return steps.indexOf(m_currentStep) == 0;
	}

	/**
	 * Returns whether the current step is the last one.
	 * @return
	 */
	private boolean isLastStep() {
		List<AbstractWizardPopupStepBase> steps = getStepsList();
		return steps.indexOf(m_currentStep) + 1 == steps.size();
	}

	/**
	 * Build the wizard's navbar that holds all available steps.
	 * @return
	 */
	private Div buildNavbar() {
		Div navbar = m_currentNavbar;
		if(null == navbar) {
			navbar = new Div();
			navbar.setCssClass("ui-gwiz-steps" + m_layout);
		}
		return m_currentNavbar = navbar;
	}

	/**
	 * This creates a container that holds all available wizard steps
	 * in the navbar.
	 * @return
	 */
	private Div buildStepsContainer() {
		Div stepsContainer = m_currentStepsContainer;
		if(null == stepsContainer) {
			stepsContainer = new Div();
			stepsContainer.setCssClass("ui-gwiz-steps-list-container" + m_layout);
		} else {
			stepsContainer.removeAllChildren();
		}

		int counter = 0;
		for(AbstractWizardPopupStepBase step : m_steps.values()) {
			int stepNumber = counter + 1;
			int currentStepNumber = getStepsList().indexOf(m_currentStep) + 1;
			stepsContainer.add(new WizardPopupStepNavbarFragment(step.getStepTitle(), step.equals(m_currentStep), m_layout, m_icons, currentStepNumber, stepNumber, m_steps.values().size() + 1));
			counter++;
		}
		return m_currentStepsContainer = stepsContainer;
	}

	/**
	 * Create list of available wizard actions (e.g. next button, end button).
	 * @return
	 */
	private Map<String, IUIAction<Void>> getActions() {
		Map<String, IUIAction<Void>> actions = new HashMap<>();
		actions.put(BUNDLE.getString("action.indicator.cancel"), cancelStep());
		actions.put(BUNDLE.getString("action.indicator.next"), toNextStep());
		actions.put(BUNDLE.getString("action.indicator.back"), toPreviousStep());
		return actions;
	}

	/**
	 * Action for cancel button.
	 * @return
	 */
	private IUIAction<Void> cancelStep() {
		return new IUIAction<Void>() {
			@Nullable
			@Override
			public String getDisableReason(@Nullable Void instance) throws Exception {
				return null;
			}

			@Nonnull
			@Override
			public String getName(@Nullable Void instance) throws Exception {
				return "cancelStep";
			}

			@Nullable
			@Override
			public String getTitle(@Nullable Void instance) throws Exception {
				return null;
			}

			@Nullable
			@Override
			public String getIcon(@Nullable Void instance) throws Exception {
				return null;
			}

			@Override
			public void execute(@Nonnull NodeBase component, @Nullable Void instance) throws Exception {
				closePressed();
			}
		};
	}

	/**
	 * Action for next button.
	 * @return
	 */
	private IUIAction<Void> toNextStep() {
		return new IUIAction<Void>() {
			@Nullable
			@Override
			public String getDisableReason(@Nullable Void instance) throws Exception {
				return null;
			}

			@Nonnull
			@Override
			public String getName(@Nullable Void instance) throws Exception {
				return "nextStep";
			}

			@Nullable
			@Override
			public String getTitle(@Nullable Void instance) throws Exception {
				return null;
			}

			@Nullable
			@Override
			public String getIcon(@Nullable Void instance) throws Exception {
				return null;
			}

			@Override
			public void execute(@Nonnull NodeBase component, @Nullable Void instance) throws Exception {
				List<AbstractWizardPopupStepBase> steps = getStepsList();
				if(!m_currentStep.isDisabled()) {
					int nextStep = steps.indexOf(m_currentStep) + 1;
					if(nextStep > 0 && nextStep < steps.size()) {
						setCurrentStep(steps.get(nextStep));
						refreshNavbarContainer();
						refreshBodyContainer();
					}
				}
			}
		};
	}

	/**
	 * Action for previous button.
	 * @return
	 */
	private IUIAction<Void> toPreviousStep() {
		return new IUIAction<Void>() {
			@Nullable
			@Override
			public String getDisableReason(@Nullable Void instance) throws Exception {
				return null;
			}

			@Nonnull
			@Override
			public String getName(@Nullable Void instance) throws Exception {
				return "previousStep";
			}

			@Nullable
			@Override
			public String getTitle(@Nullable Void instance) throws Exception {
				return null;
			}

			@Nullable
			@Override
			public String getIcon(@Nullable Void instance) throws Exception {
				return null;
			}

			@Override
			public void execute(@Nonnull NodeBase component, @Nullable Void instance) throws Exception {
				List<AbstractWizardPopupStepBase> steps = getStepsList();
				int previousStepIndex = getStepsList().indexOf(m_currentStep) - 1;
				if(previousStepIndex >= 0 && previousStepIndex < steps.size()) {
					AbstractWizardPopupStepBase previousStep = steps.get(previousStepIndex);
					setCurrentStep(previousStep);
					refreshNavbarContainer();
					refreshBodyContainer();
				}
			}
		};
	}

	/**
	 * Returns a list of all {@link AbstractWizardPopupStepBase}.
	 * @return
	 */
	private List<AbstractWizardPopupStepBase> getStepsList() {
		List<AbstractWizardPopupStepBase> steps = new ArrayList<>();
		steps.addAll(getSteps().values());
		return steps;
	}

	/**
	 * Returns the previous step.
	 * @return
	 */
	public AbstractWizardPopupStepBase getPreviousStep() {
		return switchStep(true);
	}

	/**
	 * Returns the next step.
	 * @return
	 */
	public AbstractWizardPopupStepBase getNextStep() {
		return switchStep(false);
	}

	/**
	 * Returns the previous or the next step. Throws an exception when step is not found.
	 * @param back
	 * @return
	 */
	AbstractWizardPopupStepBase switchStep(boolean back) {
		List<AbstractWizardPopupStepBase> steps = getStepsList();
		int currentIndex = steps.indexOf(getCurrentStep());
		if(currentIndex >= 0 && currentIndex < steps.size()) {
			return back ? steps.get(currentIndex - 1) : steps.get(currentIndex + 1);
		}
		throw new IllegalStateException("You are trying to get a step that does not exist!");
	}
}
