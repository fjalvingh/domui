package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface IWithStoredState {
	Object storeState();

	void loadState(Object state) throws Exception;
}
