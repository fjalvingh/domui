package to.etc.domui.component.searchpanel;

import to.etc.domui.dom.html.NodeContainer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-12-17.
 */
public interface ISearchFormBuilder {
	/** Defines the target node for the form to be built. */
	void setTarget(NodeContainer target) throws Exception;

	void append(SearchControlLine<?> it) throws Exception;

	void finish() throws Exception;
}
