package to.etc.domui.component.wizard;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.themes.*;
import to.etc.domui.util.*;
import to.etc.webapp.*;
import to.etc.webapp.nls.*;

import javax.annotation.*;
import java.util.*;

/**
 * The wizard dialog component. Simply, extend this dialog and implement all necessary methods.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a, <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 21-6-17.
 */
@DefaultNonNull
public abstract class AbstractWizardDialog extends Dialog {

	private static final BundleRef BUNDLE = BundleRef.create(AbstractWizardDialog.class, "messages");

	private static final String DEFAULT_TITLE = BUNDLE.getString("wizarddialog.default.title");

	public static final String CURRENT = "currentStep";

	private final List<AbstractWizardStep> m_stepList = new ArrayList<>();

	@Nullable
	private AbstractWizardStep m_currentStep;

	private final Div m_stepDiv = new Div();

	private boolean m_isBuilt;

	private Map<String, WizardStorageItem<?>> m_storage = new HashMap<>();

	@Nullable
	private WizardNavigatorFragment m_navigator;

	@Nullable
	private DefaultButton m_cancelButton;

	@Nullable
	private DefaultButton m_prevButton;

	@Nullable
	private DefaultButton m_nextButton;

	@Nullable
	private DefaultButton m_finishButton;

	public AbstractWizardDialog() {
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

		getNavigator().bind(WizardNavigatorFragment.CURRENT).to(this, CURRENT);
	}

	/**
	 * Creates the button bar. Will setup a default button bar
	 * if wizard steps do not have explicit buttons.
	 * @throws Exception
	 */
	private void setButtonBar() throws Exception {
		ButtonBar bb = (ButtonBar) getButtonBar();
		if(bb.getChildCount() > 0) {
			bb.clearButtons();
		}
		boolean hasDefaultButtonBar = true;
		if(getCurrentStep().hasCancelButton()) {
			addCancelButton();
			hasDefaultButtonBar = false;
		}
		if(getCurrentStep().hasPrevButton()) {
			addPrevButton();
			hasDefaultButtonBar = false;
		}
		if(getCurrentStep().hasNextButton()) {
			addNextButton();
			hasDefaultButtonBar = false;
		}
		if(getCurrentStep().hasFinishButton()) {
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

	private void addCancelButton() throws Exception {
		m_cancelButton = getButtonBar().addButton(BUNDLE.getString("wizardstep.default.cancelbutton"), Theme.BTN_CANCEL, click -> closePressed());
	}

	private void addPrevButton() throws Exception {
		DefaultButton prevButton = m_prevButton = getButtonBar().addButton(BUNDLE.getString("wizardstep.default.prevbutton"), "THEME/btnBack.png", click -> prevStep());
		prevButton.setDisabled(isFirstStep());
	}

	private void addNextButton() throws Exception {
		if(!isLastStep()) {
			DefaultButton nextButton = m_nextButton = getButtonBar().addButton(BUNDLE.getString("wizardstep.default.nextbutton"), "THEME/btnNext.png", click -> {
				if(getCurrentStep().onCompleted()) {
					nextStep();
				}
			});
			nextButton.bind("disabled").to(this, "currentStep.disabled");
		}
	}

	private void addFinishButton() throws Exception {
		if(isLastStep()) {
			m_finishButton = getButtonBar().addButton(BUNDLE.getString("wizardstep.default.finishbutton"), Theme.BTN_CONFIRM, click -> {
				if(getCurrentStep().onCompleted()) {
					closePressed();
				}
			});
		}
	}

	private int getCurrentPageIndex() {
		return m_stepList.indexOf(getCurrentStep());
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
		AbstractWizardStep currentStep = m_currentStep;
		if(null != currentStep) {
			currentStep.remove();
			m_currentStep = null;
		}
		currentStep = m_stepList.get(index);
		currentStep.onReturn();
		m_stepDiv.add(currentStep);
		m_currentStep = currentStep;
		setButtonBar();
	}

	protected void addStep(AbstractWizardStep abstractWizardStep) {
		if(m_isBuilt) {
			throw new IllegalStateException("You cannot change the wizard's steps after it has been built");
		}
		m_stepList.add(abstractWizardStep);
	}

	List<AbstractWizardStep> getStepList() {
		return m_stepList;
	}

	@Nonnull
	public AbstractWizardStep getCurrentStep() {
		AbstractWizardStep currentStep = m_currentStep;
		if(currentStep == null) {
			throw new ProgrammerErrorException("The currentStep should be available");
		}
		return currentStep;
	}

	public void setCurrentStep(@Nullable AbstractWizardStep abstractWizardStep) {
		m_currentStep = abstractWizardStep;
	}

	@Nonnull
	private WizardNavigatorFragment getNavigator() {
		return DomUtil.nullChecked(m_navigator);
	}


	public Map<String, WizardStorageItem<?>> getStorage() {
		return m_storage;
	}

	public void addStorageItem(@Nonnull String key, @Nonnull WizardStorageItem<?> value) {
		if(m_storage.containsKey(key)) {
			throw new ProgrammerErrorException("Key already exists in wizard's storage");
		}
		m_storage.put(key, value);
	}

	public void replaceStorageItem(@Nonnull String key, @Nonnull WizardStorageItem<?> value) {
		if(!m_storage.containsKey(key)) {
			throw new ProgrammerErrorException("You are trying to replace a key that does not exist");
		}
		m_storage.replace(key, value);
	}

	protected AbstractWizardDialog addCssClassCancelButton(@Nonnull String cssClass) {
		DefaultButton cancelButton = m_cancelButton;
		if(cancelButton == null) {
			return this;
		}
		cancelButton.addCssClass(cssClass);
		return this;
	}

	protected AbstractWizardDialog addCssClassNextButton(@Nonnull String cssClass) {
		DefaultButton nextButton = m_nextButton;
		if(nextButton == null) {
			return this;
		}
		nextButton.addCssClass(cssClass);
		return this;
	}

	protected AbstractWizardDialog addCssClassPrevButton(@Nonnull String cssClass) {
		DefaultButton prevButton = m_prevButton;
		if(prevButton == null) {
			return this;
		}
		prevButton.addCssClass(cssClass);
		return this;
	}

	protected AbstractWizardDialog addCssClassFinishButton(@Nonnull String cssClass) {
		DefaultButton finishButton = m_finishButton;
		if(finishButton == null) {
			return this;
		}
		finishButton.addCssClass(cssClass);
		return this;
	}

	@Nullable
	private DefaultButton getFinishButton() {
		return m_finishButton;
	}
}
