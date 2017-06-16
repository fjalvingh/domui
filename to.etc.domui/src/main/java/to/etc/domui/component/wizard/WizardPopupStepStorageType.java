package to.etc.domui.component.wizard;

import javax.annotation.*;

/**
 * This represents an item that can be saved to a step storage.
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 15-6-17.
 */
@DefaultNonNull
public class WizardPopupStepStorageType<T> {

	private T m_storageItem;

	public T get(){
		return m_storageItem;
	}

	public void set(T storageItem){
		m_storageItem = storageItem;
	}

}
