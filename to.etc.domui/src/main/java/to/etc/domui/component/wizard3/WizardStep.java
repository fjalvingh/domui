package to.etc.domui.component.wizard3;

import to.etc.domui.dom.html.*;

import javax.annotation.*;

/**
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 21-6-17.
 */
@DefaultNonNull
public abstract class WizardStep extends Div {

	static final String VALID = "disabled";

	// This is needed for binding purposes
	private boolean m_disabled;

	private final String m_stepLabel;

	private boolean m_cancelButton;

	private boolean m_prevButton;

	private boolean m_nextButton;

	private boolean m_finishButton;

	public WizardStep(@Nonnull String stepLabel) {
		m_stepLabel = stepLabel;
	}

	String getStepLabel() {
		return m_stepLabel;
	}

	@Override
	public abstract void createContent() throws Exception;

	protected void onCompleted() throws Exception {}

	public boolean isDisabled() {
		return m_disabled;
	}

	protected void setDisabled(boolean disabled) {
		m_disabled = disabled;
	}

	boolean hasCancelButton() {
		return m_cancelButton;
	}

	protected WizardStep setCancelButton() {
		m_cancelButton = true;
		return this;
	}

	boolean hasPrevButton() {
		return m_prevButton;
	}

	protected WizardStep setPrevButton() {
		m_prevButton = true;
		return this;
	}

	boolean hasNextButton() {
		return m_nextButton;
	}

	protected WizardStep setNextButton() {
		m_nextButton = true;
		return this;
	}

	boolean hasFinishButton() {
		return m_finishButton;
	}

	protected WizardStep setFinishButton() {
		m_finishButton = true;
		return this;
	}
}
