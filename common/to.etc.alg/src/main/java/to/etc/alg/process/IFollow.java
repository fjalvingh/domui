package to.etc.alg.process;

import org.eclipse.jdt.annotation.NonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 07-09-2023.
 */
public interface IFollow {
	void newData(boolean stderr, @NonNull char[] data, int length);
}
