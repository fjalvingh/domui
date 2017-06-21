package to.etc.domui.component.wizard3;

import javax.annotation.*;

/**
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
