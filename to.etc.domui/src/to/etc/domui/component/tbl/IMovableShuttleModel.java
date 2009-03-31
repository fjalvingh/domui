package to.etc.domui.component.tbl;

/**
 * This extension of the Shuttle model is required if you want to be able to order target items.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 6, 2008
 */
public interface IMovableShuttleModel<S, T> extends IShuttleModel<S, T> {
	public void		moveTargetItem(int from, int to) throws Exception;
}
