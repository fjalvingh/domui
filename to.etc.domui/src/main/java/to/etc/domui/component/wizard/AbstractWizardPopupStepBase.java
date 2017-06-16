package to.etc.domui.component.wizard;

import to.etc.domui.dom.html.*;

import javax.annotation.*;
import java.util.*;

/**
 * This class holds the base for every step for the {@link WizardPopupWindow}.
 * All steps should extend this class.
 *
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 6-6-17.
 */
@DefaultNonNull
public abstract class AbstractWizardPopupStepBase extends Div {

	static final String IS_VALID = "disabled";

	// This is needed for binding purposes
	private boolean m_disabled;

	private String m_stepTitle;

	private Map<String, WizardPopupStepStorageType<?>> m_storage = new HashMap<>();

	private WizardPopupWindow m_wizard;

	public void setWizard(@Nonnull WizardPopupWindow wizard) {
		m_wizard = wizard;
	}

	void setStepTitle(@Nonnull String title) {
		m_stepTitle = title;
	}

	String getStepTitle() {
		return m_stepTitle;
	}

	/**
	 * This abstract method is needed to determine whether the next button is clickable and, therefore,
	 * valid or not. If this method is true, the step is disabled and, thus, invalid.
	 * @return
	 */
	public abstract boolean isDisabled();

	/**
	 * With this, it can be set whether the step is valid or not.
	 * @param bool
	 */
	public void setDisabled(boolean bool) {
		m_disabled = bool;
	}

	/**
	 * This returns the step storage (e.g. all custom variables that this step may hold).
	 * @return
	 */
	public Map<String, WizardPopupStepStorageType<?>> getStorage() {
		return m_storage;
	}

	/**
	 * Returns a specific value from the storage. Returns null if key does not exist.
	 * @param key
	 * @return
	 */
	public WizardPopupStepStorageType<?> getStorageItem(@Nonnull String key)  {
		if(m_storage.containsKey(key)) {
			return m_storage.get(key);
		}
		throw new IllegalStateException("You are trying to get a storage item that does not exist!");
	}

	/**
	 * Adds something to the step storage. Needs a String as key. It could be handy to set a constant as
	 * String, so it can be used various times on different locations.
	 * @param key
	 * @param value
	 */
	protected void addToStorage(@Nonnull String key, @Nonnull WizardPopupStepStorageType<?> value) {
		if(m_storage.containsKey(key)) {
			throw new IllegalStateException("Key already exists in step storage!");
		}
		m_storage.put(key, value);
	}

	/**
	 * Deletes something from the step storage.
	 * @param key
	 */
	public void deleteFromStorage(@Nonnull String key) {
		if(!m_storage.containsKey(key)) {
			throw new IllegalStateException("Cannot delete a key that does not exist in step storage!");
		}
		m_storage.remove(key);
	}

	/**
	 * Returns the previous step. For instance, to get the previous
	 * @return
	 */
	public AbstractWizardPopupStepBase getPreviousStep() {
		return m_wizard.getPreviousStep();
	}

	public AbstractWizardPopupStepBase getNextStep() {
		return m_wizard.getNextStep();
	}
}
