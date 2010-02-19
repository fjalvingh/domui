package to.etc.domui.state;

/**
 * This interface can, implemented on a page, be used to keep a
 * page active when it's URL parameters change. Normally if an
 * existing page's parameters on the URL change the existing page
 * is discarded and a new page is created. Pages implementing this
 * will get a pageParametersChanged() event instead.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 21, 2008
 */
public interface IParameterChangeListener {
	public void pageParametersChanged(PageParameters papa) throws Exception;
}
