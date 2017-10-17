package to.etc.domui.dom.html;

import javax.annotation.Nullable;

/**
 * This should be implemented by nodes that can be used in
 * "for" for a label.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-10-17.
 */
public interface IForTarget {
	/**
	 * Return the node to actually use inside the label's "for" attribute. If no such node
	 * exists return null, in which the for will be absent. The node returned can also
	 * implement IForTarget in which case the for calculation will call its getForTarget()
	 * until it reaches a node which returns <b>itself</b> as the target. That is the
	 * sign that the node can be used in the for.
	 *
	 * The property in this interface is being bound to inside {@link Label}.
	 */
	@Nullable
	NodeBase getForTarget();
}
