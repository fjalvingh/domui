package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-10-18.
 */
public interface IApplicationInitializer {
	void onStartInitialization(@NonNull DomApplication da);

	void onEndInitialization(@NonNull DomApplication da);

	void onAfterDestroy(@NonNull DomApplication da);
}
