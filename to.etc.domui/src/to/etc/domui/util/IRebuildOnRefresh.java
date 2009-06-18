package to.etc.domui.util;

/**
 * This marker interface, when present on an UrlPage class, forces
 * the rebuild of a page every time it is refreshed. The refresh
 * is done by:
 * <ul>
 *	<li>issuing a forceRebuild() just before the page is rendered anew</li>
 *	<li>Clearing any pending QDataContext in the page</li>
 * </ul>
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2009
 */
public interface IRebuildOnRefresh {
}
