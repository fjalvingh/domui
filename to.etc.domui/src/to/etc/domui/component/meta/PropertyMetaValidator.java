package to.etc.domui.component.meta;

import to.etc.domui.converter.*;

public interface PropertyMetaValidator {
	public Class< ? extends IValueValidator< ? >> getValidatorClass();
	public String[] getParameters();
}
