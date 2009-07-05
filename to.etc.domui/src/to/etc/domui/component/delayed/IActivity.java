package to.etc.domui.component.delayed;

import to.etc.domui.dom.html.*;

/**
 * The worker code for a delayed activity, i.e. a thingy registered using an
 * AsyncContainer.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 7, 2008
 */
public interface IActivity {
	public Div run(IProgress p) throws Exception;
}
