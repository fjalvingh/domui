package to.etc.domui.component.agenda;

import java.util.*;

/**
 * Listener interface for when a day is clicked on a MonthPanel.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 6, 2008
 */
public interface IDayClicked {
	public void		dayClicked(MonthPanel p, Date d) throws Exception;
}
