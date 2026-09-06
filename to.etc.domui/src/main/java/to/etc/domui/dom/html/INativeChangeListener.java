package to.etc.domui.dom.html;

import to.etc.function.IExecute;

/**
 * This interface is allowed on HTML nodes <b>only</b>, and controls whether a node renders an "onchange" handler.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 27, 2014
 */
public interface INativeChangeListener {
	IExecute getOnValueChanged();
}
