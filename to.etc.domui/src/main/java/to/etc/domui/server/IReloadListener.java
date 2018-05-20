package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;

public interface IReloadListener {
	void reloaded(@NonNull ClassLoader newloader) throws Exception;
}
