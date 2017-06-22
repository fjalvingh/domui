package to.etc.domui.component.wizard3;

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

	private final ButtonBar m_buttonBar = (ButtonBar) getButtonBar();

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

	private void setButtonBar() throws Exception {
		m_buttonBar.clearButtons();
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

	private void addCancelButton() {
		m_cancelButton = new DefaultButton(BUNDLE.getString("wizardstep.default.cancelbutton"), Theme.BTN_CANCEL, click -> closePressed());
		m_buttonBar.addButton(m_cancelButton);
	}

	private void addPrevButton() {
		DefaultButton prevButton = m_prevButton = new DefaultButton(BUNDLE.getString("wizardstep.default.prevbutton"), Theme.BTN_MOVE_LEFT, click -> prevStep());
		prevButton.setDisabled(isFirstStep());
		m_buttonBar.addButton(prevButton);
	}

	private void addNextButton() throws Exception {
		DefaultButton nextButton = m_nextButton = new DefaultButton(BUNDLE.getString("wizardstep.default.nextbutton"), Theme.BTN_MOVE_RIGHT, click -> {
			getCurrentStep().onCompleted();
			nextStep();
		});
		if(isLastStep()) {
			nextButton.setDisabled(true);
		} else {
			nextButton.bind("disabled").to(getCurrentStep(), AbstractWizardStep.VALID);
		}
		m_buttonBar.addButton(nextButton);
	}

	private void addFinishButton() throws Exception {
		m_finishButton = new DefaultButton(BUNDLE.getString("wizardstep.default.finishbutton"), Theme.BTN_CONFIRM, click -> {
			getCurrentStep().onCompleted();
			closePressed();
		});
		if(isLastStep()) {
			DomUtil.nullChecked(m_finishButton).bind("disabled").to(getCurrentStep(), AbstractWizardStep.VALID);
			m_buttonBar.addButton(DomUtil.nullChecked(getFinishButton()));
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

	public void setCurrentStep(AbstractWizardStep abstractWizardStep) {
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
