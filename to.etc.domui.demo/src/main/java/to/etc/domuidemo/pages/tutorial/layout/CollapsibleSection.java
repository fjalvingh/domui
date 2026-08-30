package to.etc.domuidemo.pages.tutorial.layout;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.event.INotify;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.html.Div;

/**
 * Tutorial, "layout", step 5: a section with a title line that can be folded shut.
 * <p>
 * It is a fragment like any other - a Div that builds itself - but it is written to be
 * used rather than to be read: the caller says what it is called and fills its content,
 * and the opening and closing is this class's own business.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class CollapsibleSection extends Div {
	private final String m_title;

	/** The state this fragment rebuilds itself from. */
	private boolean m_expanded;

	/**
	 * The content, made once and kept: it holds what the <i>caller</i> put in it, so it
	 * has to survive this fragment being built again.
	 */
	private final Div m_content = new Div("dm-cs-c");

	@Nullable
	private INotify<CollapsibleSection> m_onToggle;

	public CollapsibleSection(String title) {
		this(title, true);
	}

	public CollapsibleSection(String title, boolean expanded) {
		m_title = title;
		m_expanded = expanded;
		setCssClass("dm-cs");
	}

	@Override
	public void createContent() throws Exception {
		Div header = new Div("dm-cs-h");
		add(header);
		header.add(new LinkButton(m_title, m_expanded ? Icon.faAngleDown : Icon.faAngleRight, a -> toggle()));

		if(m_expanded) {
			add(m_content);
		}
	}

	/**
	 * Where the caller puts what the section contains.
	 */
	public Div getContent() {
		return m_content;
	}

	public boolean isExpanded() {
		return m_expanded;
	}

	public void toggle() throws Exception {
		setExpanded(!m_expanded);
		INotify<CollapsibleSection> onToggle = m_onToggle;
		if(null != onToggle) {
			onToggle.onNotify(this);
		}
	}

	public void setExpanded(boolean expanded) {
		if(expanded == m_expanded) {
			return;
		}
		m_expanded = expanded;
		forceRebuild();                                    // Only this fragment is redrawn
	}

	@Nullable
	public INotify<CollapsibleSection> getOnToggle() {
		return m_onToggle;
	}

	/**
	 * Called after the section was opened or closed, for a page that wants to know.
	 */
	public void setOnToggle(@Nullable INotify<CollapsibleSection> onToggle) {
		m_onToggle = onToggle;
	}
}
