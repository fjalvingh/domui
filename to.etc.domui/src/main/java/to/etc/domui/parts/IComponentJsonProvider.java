package to.etc.domui.parts;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.state.IPageParameters;

public interface IComponentJsonProvider {
	@NonNull Object provideJsonData(@NonNull IPageParameters parameterSource) throws Exception;
}
