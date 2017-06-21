package to.etc.domui.component.wizard3;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.themes.*;
import to.etc.webapp.*;
import to.etc.webapp.nls.*;

/**
 * @authors <a href="mailto:jal@etc.to">Frits Jalvingh</a, <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 21-6-17.
 */
public abstract class WizardDialog extends Window {

	private static final BundleRef BUNDLE = BundleRef.create(WizardDialog.class, "messages");

	private static final String DEFAULT_TITLE = BUNDLE.getString("wizarddialog.default.title");

	private List<WizardStep> m_stepList = new ArrayList<>();

	private WizardStep m_currentStep;

	private final Div m_stepDiv = new Div();

	private boolean m_isBuilt;

	private Map<String, WizardStorageItem<?>> m_storage = new HashMap<>();

	private WizardNavigatorFragment m_navigator;

	private Div m_bbContainer = new Div();

	private ButtonBar m_buttonBarLeft = new ButtonBar();

	private ButtonBar m_buttonBarRight = new ButtonBar();

	private boolean m_hasTwoButtonBars;

	private String m_buttonContainerHeight = "34px";

	public WizardDialog() {
		super(true, true, 1024, 768, DEFAULT_TITLE);
	}

	protected WizardDialog setButtonContainerHeight(int px) {
		m_buttonContainerHeight = px + "px";
		return this;
	}

	protected WizardDialog setButtonContainerCss(@Nonnull String className) {
		m_bbContainer.addCssClass(className);
		return this;
	}

	abstract protected void createPages() throws Exception;

	@Override
	final public void createContent() throws Exception {
		createPages();

		if(m_stepList.size() <= 0) {
			throw new ProgrammerErrorException("Please add wizard pages with addStep() inside the createPages() method");
		}

		m_navigator = refreshNavigator();
		add(m_navigator);

		m_stepDiv.removeAllChildren();
		add(m_stepDiv);
		m_stepDiv.setCssClass("ui-wzdl-page");

		m_bbContainer.removeAllChildren();
		m_bbContainer.setCssClass("ui-wzdl-bbcontainer");
		m_bbContainer.setHeight(m_buttonContainerHeight);
		m_buttonBarRight.setCssClass("ui-wzdl-bbright");
		m_bbContainer.add(m_buttonBarRight);

		if(m_hasTwoButtonBars) {
			m_buttonBarLeft.setCssClass("ui-wzdl-bbleft");
			m_bbContainer.add(m_buttonBarLeft);
		}
		add(m_bbContainer);

		setWizardStep(0);

		m_isBuilt = true;
	}

	protected void setTwoButtonBars() {
		m_hasTwoButtonBars = true;
	}

	private WizardNavigatorFragment refreshNavigator() throws Exception {
		if(null == m_navigator) {
			return m_navigator = new WizardNavigatorFragment(this);
		}
		m_navigator.removeAllChildren();
		m_navigator.createFragment();
		return m_navigator;
	}

	private void refreshButtonBar() throws Exception {
		m_buttonBarLeft.clearButtons();
		m_buttonBarRight.clearButtons();
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
	}

	private void addCancelButton() {
		DefaultButton cancelButton = new DefaultButton(BUNDLE.getString("wizardstep.default.cancelbutton"), Theme.BTN_CANCEL, click -> closePressed());
		if(m_hasTwoButtonBars) {
			m_buttonBarLeft.addButton(cancelButton);
		} else {
			m_buttonBarRight.addButton(cancelButton);
		}
	}

	private void addPrevButton() {
		DefaultButton prevButton = new DefaultButton(BUNDLE.getString("wizardstep.default.prevbutton"), Theme.BTN_MOVE_LEFT, click -> prevStep());
		prevButton.setDisabled(isFirstStep());
		m_buttonBarRight.addButton(prevButton);
	}

	private void addNextButton() throws Exception {
		DefaultButton nextButton = new DefaultButton(BUNDLE.getString("wizardstep.default.nextbutton"), Theme.BTN_MOVE_RIGHT, click -> {
			m_currentStep.onCompleted();
			nextStep();
		});
		if(isLastStep()) {
			nextButton.setDisabled(true);
		} else {
			nextButton.bind("disabled").to(m_currentStep, WizardStep.VALID);
		}
		m_buttonBarRight.addButton(nextButton);
	}

	private void addFinishButton() {
		DefaultButton finishButton = new DefaultButton(BUNDLE.getString("wizardstep.default.finishbutton"), Theme.BTN_CONFIRM, click -> {
			m_currentStep.onCompleted();
			closePressed();
		});
		m_buttonBarRight.addButton(finishButton);
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
		if(m_isBuilt) {
			refreshNavigator();
		}
		refreshButtonBar();
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

	WizardStep getCurrentStep() {
		return m_currentStep;
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
}
