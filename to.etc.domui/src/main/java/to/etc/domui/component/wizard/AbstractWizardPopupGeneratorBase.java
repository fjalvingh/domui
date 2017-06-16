package to.etc.domui.component.wizard;

import to.etc.webapp.nls.*;

import javax.annotation.*;
import java.util.*;

/**
 * This class is the abstract base for a WizardPopupWindow generator (i.e. the class that instantiates the {@link WizardPopupWindow}).
 * With a generator, one can instantiate a wizard, set the title, and add steps easily. Read README.md for more information.
 *
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 12-6-17.
 */
@DefaultNonNull
public abstract class AbstractWizardPopupGeneratorBase {

	private static final BundleRef BUNDLE = BundleRef.create(WizardPopupWindow.class, "messages");

	private static final String DEFAULT_NEXT_BUTTON_LABEL = BUNDLE.getString("wizardstep.default.nextbutton");

	private static final String DEFAULT_CANCEL_BUTTON_LABEL = BUNDLE.getString("wizardstep.default.cancelbutton");

	private static final String DEFAULT_END_BUTTON_LABEL = BUNDLE.getString("wizardstep.default.endbutton");

	private static final String DEFAULT_BACK_BUTTON_LABEL = BUNDLE.getString("wizardstep.default.backbutton");

	private static final String STEP_LABEL_CANCEL = BUNDLE.getString("label.indicator.cancel");

	private static final String STEP_LABEL_BACK = BUNDLE.getString("label.indicator.back");

	private static final String STEP_LABEL_NEXT = BUNDLE.getString("label.indicator.next");

	private static final String STEP_LABEL_END = BUNDLE.getString("label.indicator.end");

	private static final String STEP_LABEL_TITLE = BUNDLE.getString("label.indicator.title");

	private static final String ICON_VALID_KEY = BUNDLE.getString("icons.indicator.valid");

	private static final String ICON_BACK_KEY = BUNDLE.getString("icons.indicator.back");

	private static final String ICON_NEXT_KEY = BUNDLE.getString("icons.indicator.next");

	private static final String ICON_CANCEL_KEY = BUNDLE.getString("icons.indicator.cancel");

	private static final String ICON_END_KEY = BUNDLE.getString("icons.indicator.end");

	private Map<Map<String, String>, AbstractWizardPopupStepBase> m_steps = new LinkedHashMap<>();

	private WizardPopupWindow m_wizard;

	private AbstractWizardPopupStepBase m_step;

	private String m_wizardTitle;

	private String m_stepTitle;

	@Nullable
	private String m_nextButtonLabel;

	@Nullable
	private String m_cancelButtonLabel;

	@Nullable
	private String m_endButtonLabel;

	@Nullable
	private String m_backButtonLabel;

	@Nullable
	public String m_validIcon;

	@Nullable
	private String m_cancelIcon;

	@Nullable
	private String m_nextIcon;

	@Nullable
	private String m_backIcon;

	@Nullable
	private String m_endIcon;

	abstract protected void setupWizard();

	public WizardPopupWindow getWizard() {
		return m_wizard;
	}

	/**
	 * This initializes the wizard. Should be run at the end of the implemented
	 * setupWizard method. When set to true, the wizard with the horizontal navbar
	 * will be shown. Otherwise, the vertical bar is displayed.
	 * @param horizontal
	 */
	protected void initWizard(boolean horizontal) {
		if(null == m_wizardTitle) {
			throw new IllegalStateException("Cannot initialize a wizard without title! Set a title first.");
		}
		if(m_steps.size() <= 0) {
			throw new IllegalStateException("Cannot initialize a wizard without steps! Add a step first.");
		}

		Map<String, String> icons = new HashMap<>();
		icons.put(ICON_VALID_KEY, m_validIcon);
		icons.put(ICON_CANCEL_KEY, m_cancelIcon);
		icons.put(ICON_NEXT_KEY, m_nextIcon);
		icons.put(ICON_BACK_KEY, m_backIcon);
		icons.put(ICON_END_KEY, m_endIcon);

		m_wizard = new WizardPopupWindow(m_wizardTitle, m_steps, icons, horizontal);

		List<AbstractWizardPopupStepBase> steps = new ArrayList<>(m_steps.values());
		for(AbstractWizardPopupStepBase step : steps) {
			step.setWizard(m_wizard);
		}
	}

	/**
	 * Sets the icon for the 'valid' sign for completed steps.
	 * @param icon
	 * @return
	 */
	public AbstractWizardPopupGeneratorBase setValidIcon(@Nonnull String icon) {
		m_validIcon = icon;
		return this;
	}

	/**
	 * Sets the icon for the cancel button.
	 * @param icon
	 * @return
	 */
	public AbstractWizardPopupGeneratorBase setCancelIcon(@Nonnull String icon) {
		m_cancelIcon = icon;
		return this;
	}

	/**
	 * Sets the icon for the next button.
	 * @param icon
	 * @return
	 */
	public AbstractWizardPopupGeneratorBase setNextIcon(@Nonnull String icon) {
		m_nextIcon = icon;
		return this;
	}

	/**
	 * Sets the icon for the back button.
	 * @param icon
	 * @return
	 */
	public AbstractWizardPopupGeneratorBase setBackIcon(@Nonnull String icon) {
		m_backIcon = icon;
		return this;
	}

	/**
	 * Sets the icon for the end button.
	 * @param icon
	 * @return
	 */
	public AbstractWizardPopupGeneratorBase setEndIcon(@Nonnull String icon) {
		m_endIcon = icon;
		return this;
	}

	/**
	 * Set the wizard's title.
	 * @param title
	 */
	public void setWizardTitle(@Nonnull String title) {
		m_wizardTitle = title;
	}

	/**
	 * With this a step can be added to the wizard. Default labels will be used.
	 * @param stepTitle
	 * @param step
	 */
	public AbstractWizardPopupGeneratorBase addStep(@Nonnull String stepTitle, @Nonnull AbstractWizardPopupStepBase step) {
		isStepTitleInStepsMap(stepTitle);
		step.setStepTitle(stepTitle);
		m_stepTitle = stepTitle;
		m_step = step;
		return this;
	}

	/**
	 * Returns true if step title already exists in m_steps. Step title must always be unique.
	 * @param stepTitle
	 * @return
	 */
	private void isStepTitleInStepsMap(@Nonnull String stepTitle) {
		for(Map<String, String> labels : m_steps.keySet()) {
			for(String label : labels.values()) {
				if(label.equalsIgnoreCase(stepTitle)) {
					throw new IllegalStateException("There is a step with this step title provided already! Set a unique step title.");
				}
			}
		}
	}

	/**
	 * With this, a step can removed from the wizard. For example, this could be handy for
	 * dynamic wizard, that should change when a certain step is selected.
	 * @param stepLabels
	 * @param step
	 */
	public void removeStep(@Nonnull Map<String, String> stepLabels, @Nonnull AbstractWizardPopupStepBase step) {
		if(m_steps.containsKey(stepLabels) && m_steps.containsValue(step)) {
			if(!m_steps.remove(stepLabels, step)) {
				throw new IllegalStateException("Cannot delete step from step map!");
			}
		}
	}

	/**
	 * Returns the 'storage' of this step (e.g. custom variables that are saved in this object).
	 * @param step
	 * @return
	 */
	public Map<String, WizardPopupStepStorageType<?>> getStepStorage(@Nonnull AbstractWizardPopupStepBase step) {
		return step.getStorage();
	}

	/**
	 * Sets the back button label.
	 * @param backButtonLabel
	 * @return
	 */
	public AbstractWizardPopupGeneratorBase backButton(@Nonnull String backButtonLabel) {
		m_backButtonLabel = backButtonLabel;
		return this;
	}

	/**
	 * Sets the next button label.
	 * @param nextButtonLabel
	 * @return
	 */
	public AbstractWizardPopupGeneratorBase nextButton(@Nonnull String nextButtonLabel) {
		m_nextButtonLabel = nextButtonLabel;
		return this;
	}

	/**
	 * Sets the cancel button label.
	 * @param cancelButtonLabel
	 * @return
	 */
	public AbstractWizardPopupGeneratorBase cancelButton(@Nonnull String cancelButtonLabel) {
		m_cancelButtonLabel = cancelButtonLabel;
		return this;
	}

	/**
	 * Sets the end button label.
	 * @param endButtonLabel
	 * @return
	 */
	public AbstractWizardPopupGeneratorBase endButton(@Nonnull String endButtonLabel) {
		m_endButtonLabel = endButtonLabel;
		return this;
	}

	/**
	 * This adds the current step labels and the current step to the steps map.
	 */
	public void set() {
		m_steps.put(createStepLabels(), m_step);
	}

	/**
	 * With this, wizard steps labels are made and returned in a map.
	 * @return
	 */
	private Map<String, String> createStepLabels() {
		Map<String, String> buttonLabels = new HashMap<>();
		buttonLabels.put(STEP_LABEL_TITLE, m_stepTitle);
		buttonLabels.put(STEP_LABEL_CANCEL, null == m_cancelButtonLabel ? DEFAULT_CANCEL_BUTTON_LABEL : m_cancelButtonLabel);
		buttonLabels.put(STEP_LABEL_BACK, null == m_backButtonLabel ? DEFAULT_BACK_BUTTON_LABEL : m_backButtonLabel);
		buttonLabels.put(STEP_LABEL_NEXT, null == m_nextButtonLabel ? DEFAULT_NEXT_BUTTON_LABEL : m_nextButtonLabel);
		buttonLabels.put(STEP_LABEL_END, null == m_endButtonLabel ? DEFAULT_END_BUTTON_LABEL : m_endButtonLabel);
		return buttonLabels;
	}
}
