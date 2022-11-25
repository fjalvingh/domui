package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public interface ICpControl<T> {
	void setValue(@Nullable T value) throws Exception;

	@Nullable
	T getValue() throws Exception;

	boolean isReadonly() throws Exception;

	boolean isDisabled() throws Exception;
}
