package to.etc.domui.component.binding;

import to.etc.domui.util.*;

import javax.annotation.*;
import java.util.*;

/**
 * EXPERIMENTAL
 *
 * Defines the properties that the renderer has used to render it's content, so that users of the
 * renderer can determine whether it needs to re-render its content when data changes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/22/14.
 */
public interface IBindableContentRenderer<T> extends INodeContentRenderer<T> {
	@Nonnull
	public List<String> getBoundProperties();
}
