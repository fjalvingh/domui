package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface IWithStoredState<T> {
	T storeState();

	void loadState(T state) throws Exception;
}
