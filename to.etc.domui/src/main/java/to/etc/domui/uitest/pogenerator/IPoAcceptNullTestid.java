package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.dom.html.NodeBase;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
@NonNullByDefault
public interface IPoAcceptNullTestid {
	String getProposedBaseName(PoGeneratorContext context, NodeBase node);
}
