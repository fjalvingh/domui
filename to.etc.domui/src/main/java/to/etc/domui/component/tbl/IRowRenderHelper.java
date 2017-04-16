package to.etc.domui.component.tbl;

import javax.annotation.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2/22/16.
 */
public interface IRowRenderHelper<T> {
	void setRow(@Nonnull T row) throws Exception;
}
