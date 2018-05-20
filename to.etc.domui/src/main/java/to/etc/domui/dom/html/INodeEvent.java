package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;

public interface INodeEvent<T> {
	void handle(@NonNull T event) throws Exception;
}
