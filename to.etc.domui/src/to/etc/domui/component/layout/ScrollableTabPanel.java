package to.etc.domui.component.layout;

/**
 * Customization of {@link TabPanel} that render tabs in single line, provide scroller buttons if needed.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Sep 2, 2010
 */
public class ScrollableTabPanel extends TabPanel {
	public ScrollableTabPanel() {
		super();
		setScrollable(true);
	}

	public ScrollableTabPanel(final boolean markErrorTabs) {
		super(markErrorTabs);
		setScrollable(true);
	}

}
