package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-10-19.
 */
@NonNullByDefault
public interface IBasicParameterContainer {
	@Nullable
	Object getObject(String name);

	int size();

	Set<String> getParameterNames();

	@Nullable
	String getUrlContextString();

	int getDataLength();
}
