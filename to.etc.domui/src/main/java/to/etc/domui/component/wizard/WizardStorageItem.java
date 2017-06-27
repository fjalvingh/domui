package to.etc.domui.component.wizard;

import javax.annotation.*;

/**
 * This class holds everything related to the WizardStorageItem,
 * an item that can be stored inside the wizard during it's life.
 * 
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs</a>
 * Created on 21-6-17.
 */
public class WizardStorageItem<T> {

	@Nullable
	private T m_storageItem;

	@Nullable
	public T get() {
		return m_storageItem;
	}

	public void set(@Nonnull T storageItem) {
		m_storageItem = storageItem;
	}
}
