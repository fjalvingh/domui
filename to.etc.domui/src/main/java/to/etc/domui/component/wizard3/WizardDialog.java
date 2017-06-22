package to.etc.domui.component.wizard3;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.themes.*;
import to.etc.webapp.*;
import to.etc.webapp.nls.*;

import javax.annotation.*;
import java.util.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a, <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 21-6-17.
 */
public abstract class WizardDialog extends Dialog {

	private static final BundleRef BUNDLE = BundleRef.create(WizardDialog.class, "messages");

	private static final String DEFAULT_TITLE = BUNDLE.getString("wizarddialog.default.title");

	public static final String CURRENT = "currentStep";

	private List<WizardStep> m_stepList = new ArrayList<>();

	private WizardStep m_currentStep;

	private final Div m_stepDiv = new Div();

	private boolean m_isBuilt;

	private Map<String, WizardStorageItem<?>> m_storage = new HashMap<>();

	private WizardNavigatorFragment m_navigator;

	private ButtonBar m_buttonBar = (ButtonBar) getButtonBar();

	private DefaultButton m_cancelButton;

	private DefaultButton m_prevButton;

	private DefaultButton m_nextButton;

	private DefaultButton m_finishButton;

	public WizardDialog() {
		super(true, true, 1024, 768, DEFAULT_TITLE);
	}

	abstract protected void createSteps() throws Exception;

	protected void addCssClassButtons() throws Exception {}

	@Override
	final public void createContent() throws Exception {
		createSteps();
		m_isBuilt = true;

		if(m_stepList.size() <= 0) {
			throw new ProgrammerErrorException("Please add wizard pages with addStep() inside the createSteps() method");
		}

		m_navigator = new WizardNavigatorFragment(this);
		add(m_navigator);

		m_stepDiv.removeAllChildren();
		add(m_stepDiv);
		m_stepDiv.setCssClass("ui-wzdl-step");

		setWizardStep(0);
		m_navigator.bind("currentStep").to(this, CURRENT);
	}

	private void setButtonBar() throws Exception {
		m_buttonBar.clearButtons();
		boolean hasDefaultButtonBar = true;
		if(m_currentStep.hasCancelButton()) {
			addCancelButton();
			hasDefaultButtonBar = false;
		}
		if(m_currentStep.hasPrevButton()) {
			addPrevButton();
			hasDefaultButtonBar = false;
		}
		if(m_currentStep.hasNextButton()) {
			addNextButton();
			hasDefaultButtonBar = false;
		}
		if(m_currentStep.hasFinishButton()) {
			addFinishButton();
			hasDefaultButtonBar = false;
		}
		if(hasDefaultButtonBar) {
			addCancelButton();
			addPrevButton();
			addNextButton();
			addFinishButton();
		}
		addCssClassButtons();
	}

	private void addCancelButton() {
		m_cancelButton = new DefaultButton(BUNDLE.getString("wizardstep.default.cancelbutton"), Theme.BTN_CANCEL, click -> closePressed());
		m_buttonBar.addButton(m_cancelButton);
	}

	private void addPrevButton() {
		m_prevButton = new DefaultButton(BUNDLE.getString("wizardstep.default.prevbutton"), Theme.BTN_MOVE_LEFT, click -> prevStep());
		m_prevButton.setDisabled(isFirstStep());
		m_buttonBar.addButton(m_prevButton);
	}

	private void addNextButton() throws Exception {
		m_nextButton = new DefaultButton(BUNDLE.getString("wizardstep.default.nextbutton"), Theme.BTN_MOVE_RIGHT, click -> {
			m_currentStep.onCompleted();
			nextStep();
		});
		if(isLastStep()) {
			m_nextButton.setDisabled(true);
		} else {
			m_nextButton.bind("disabled").to(m_currentStep, WizardStep.VALID);
		}
		m_buttonBar.addButton(m_nextButton);
	}

	private void addFinishButton() {
		m_finishButton = new DefaultButton(BUNDLE.getString("wizardstep.default.finishbutton"), Theme.BTN_CONFIRM, click -> {
			m_currentStep.onCompleted();
			closePressed();
		});
		m_buttonBar.addButton(m_finishButton);
	}

	private int getCurrentPageIndex() {
		return m_stepList.indexOf(m_currentStep);
	}

	private boolean isFirstStep() {
		return getCurrentPageIndex() <= 0;
	}

	private boolean isLastStep() {
		return getCurrentPageIndex() + 1 >= m_stepList.size();
	}

	private void nextStep() throws Exception {
		int index = getCurrentPageIndex();
		if(index + 1 >= m_stepList.size()) {
			return;
		}
		setWizardStep(index + 1);
	}

	private void prevStep() throws Exception {
		int index = getCurrentPageIndex();
		if(index <= 0) {
			return;
		}
		setWizardStep(index - 1);
	}

	private void setWizardStep(int index) throws Exception {
		if(index < 0 || index >= m_stepList.size()) {
			throw new IllegalStateException("Bad step index");
		}
		WizardStep currentStep = m_currentStep;
		if(null != currentStep) {
			currentStep.remove();
			m_currentStep = null;
		}
		currentStep = m_stepList.get(index);
		m_stepDiv.add(currentStep);
		m_currentStep = currentStep;
		setButtonBar();
	}

	protected void addStep(WizardStep wizardStep) {
		if(m_isBuilt) {
			throw new IllegalStateException("You cannot change the wizard's steps after it has been built");
		}
		m_stepList.add(wizardStep);
	}

	List<WizardStep> getStepList() {
		return m_stepList;
	}

	public WizardStep getCurrentStep() {
		return m_currentStep;
	}

	public void setCurrentStep(@Nonnull WizardStep wizardStep) {
		m_currentStep = wizardStep;
	}

	public Map<String, WizardStorageItem<?>> getStorage() {
		return m_storage;
	}

	protected void addStorageItem(@Nonnull String key, @Nonnull WizardStorageItem<?> value) {
		if(m_storage.containsKey(key)) {
			throw new ProgrammerErrorException("Key already exists in wizard's storage");
		}
		m_storage.put(key, value);
	}

	protected void replaceStorageItem(@Nonnull String key, @Nonnull WizardStorageItem<?> value) {
		if(!m_storage.containsKey(key)) {
			throw new ProgrammerErrorException("You are trying to replace a key that does not exist");
		}
		m_storage.replace(key, value);
	}

	protected WizardDialog addCssClassCancelButton(@Nonnull String cssClass) {
		m_cancelButton.addCssClass(cssClass);
		return this;
	}

	protected WizardDialog addCssClassNextButton(@Nonnull String cssClass) {
		m_nextButton.addCssClass(cssClass);
		return this;
	}

	protected WizardDialog addCssClassPrevButton(@Nonnull String cssClass) {
		m_prevButton.addCssClass(cssClass);
		return this;
	}

	protected WizardDialog addCssClassFinishButton(@Nonnull String cssClass) {
		m_finishButton.addCssClass(cssClass);
		return this;
	}
}
