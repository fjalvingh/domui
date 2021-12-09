package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ICpControl<T> {
	void setValue(T value) throws Exception;

	T getValue() throws Exception;

	boolean isReadonly() throws Exception;

	boolean isDisabled() throws Exception;
}
