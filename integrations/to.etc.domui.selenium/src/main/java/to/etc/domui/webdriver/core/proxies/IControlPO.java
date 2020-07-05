package to.etc.domui.webdriver.core.proxies;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface IControlPO<T> {

	void setValue(T value) throws Exception;

	T getValue() throws Exception;

	boolean isReadonly() throws Exception;

	boolean isDisabled() throws Exception;
}
