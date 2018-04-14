package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2/22/16.
 */
public interface IRowRenderHelper<T> {
	void setRow(@NonNull T row) throws Exception;
}
