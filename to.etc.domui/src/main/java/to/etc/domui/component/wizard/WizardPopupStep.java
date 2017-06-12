package to.etc.domui.component.wizard;

import to.etc.webapp.nls.*;

import javax.annotation.*;

/**
 * Defines a step for a {@link WizardPopupWindow}. A WizardPopupWindow can have unlimited steps.
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 2-6-17.
 */
@DefaultNonNull
public class WizardPopupStep {

	private static final BundleRef BUNDLE = BundleRef.create(WizardPopupStep.class, "messages");

	private static final String DEFAULT_NEXT_BUTTON_LABEL = BUNDLE.getString("wizardstep.default.nextbutton");

	private static final String DEFAULT_CANCEL_BUTTON_LABEL = BUNDLE.getString("wizardstep.default.cancelbutton");

	private static final String DEFAULT_BACK_BUTTON_LABEL = BUNDLE.getString("wizardstep.default.backbutton");

	private static final String DEFAULT_END_BUTTON_LABEL = BUNDLE.getString("wizardstep.default.endbutton");

	private String m_stepName;

	private IWizardPopupStep m_stepBody;

	private String m_previousButtonName;

	private String m_nextButtonName;

	private String m_cancelButtonName;

	private String m_endButtonName;

	public WizardPopupStep(@Nonnull String stepName, @Nonnull IWizardPopupStep stepBody) {
		this(stepName, stepBody, DEFAULT_BACK_BUTTON_LABEL, DEFAULT_NEXT_BUTTON_LABEL, DEFAULT_CANCEL_BUTTON_LABEL, DEFAULT_END_BUTTON_LABEL);
	}

	public WizardPopupStep(@Nonnull String stepName, @Nonnull IWizardPopupStep stepBody, @Nullable String previousButtonName, @Nullable String nextButtonName, @Nullable String cancelButtonName, @Nullable String endButtonName) {
		m_stepName = stepName;
		m_stepBody = stepBody;
		m_previousButtonName = null == previousButtonName ? DEFAULT_BACK_BUTTON_LABEL : previousButtonName;
		m_nextButtonName = null == nextButtonName ? DEFAULT_NEXT_BUTTON_LABEL : nextButtonName;
		m_cancelButtonName = null == cancelButtonName ? DEFAULT_CANCEL_BUTTON_LABEL : cancelButtonName;
		m_endButtonName = null == endButtonName ? DEFAULT_END_BUTTON_LABEL : endButtonName;
	}

	public IWizardPopupStep getStepBody() {
		return m_stepBody;
	}

	public String getStepName() {
		return m_stepName;
	}

	public String getPreviousButtonName() {
		return m_previousButtonName;
	}

	public String getNextButtonName() {
		return m_nextButtonName;
	}

	public String getEndButtonName() {
		return m_endButtonName;
	}

	public String getCancelButtonName() {
		return m_cancelButtonName;
	}

}
