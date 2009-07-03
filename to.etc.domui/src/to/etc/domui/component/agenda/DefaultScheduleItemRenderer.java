package to.etc.domui.component.agenda;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * WARNING: This class needs a separate <i>instance</i> for every thing it renders for! It has
 * locals!!
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 26, 2008
 */
public class DefaultScheduleItemRenderer<T extends ScheduleItem> implements INodeContentRenderer<T> {
	private StringBuilder		m_sb = new StringBuilder();

	public synchronized void renderNodeContent(NodeBase component, NodeContainer root, T si, Object parameters) throws Exception {
		WeekAgendaComponent<T>	age = (WeekAgendaComponent<T>) component;

		if(si.getImageURL() != null) {
			Img	i = new Img();
			i.setBorder(0);
			i.setCssClass("ui-wa-img");
			i.setAlt(si.getImageURL());
			i.setSrc(si.getImageURL());
			root.add(i);
		}
		if(si.getType() != null) {
			//-- add a span describing the type
			Span sp = new Span();
			root.add(sp);
			sp.setCssClass("ui-wa-ity");
			sp.setButtonText(si.getType());
		}
		if(si.getName() != null) {
			Span sp = new Span();
			root.add(sp);
			sp.setCssClass("ui-wa-nm");
			sp.setButtonText(si.getName());
		}
		if(true) {
			Span sp = new Span();
			root.add(sp);
			sp.setCssClass("ui-wa-tm");
			m_sb.setLength(0);
			m_sb.append(age.getDateFormat().format(si.getStart()));
			long duration = si.getEnd().getTime() - si.getStart().getTime();
			age.appendDuration(m_sb, duration);
			sp.setButtonText(m_sb.toString());
		}
		if(si.getDetails() != null) {
			if(si.getName() != null) {					// If we have a name too go to the next line
				root.add(new BR());
			}
			Span sp = new Span();
			root.add(sp);
			sp.setCssClass("ui-wa-dt");
			sp.setButtonText(si.getDetails());
		}
	}
}
