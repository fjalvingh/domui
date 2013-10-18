package to.etc.domui.autotest;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * Collection of all that is known for the page, by scanning all it's nodes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 10, 2013
 */
public class PageData {
	static private final Set<String> INPUTTAGS = new HashSet<String>(Arrays.asList("input", "select", "textarea"));

	@Nonnull
	private final List<NodeBase> m_clickTargets = new ArrayList<NodeBase>();

	@Nonnull
	private final List<NodeBase> m_baseInputs = new ArrayList<NodeBase>();

	@Nonnull
	final private Page m_page;

	public PageData(@Nonnull Page page) {
		m_page = page;
	}

	@Nonnull
	public Page getPage() {
		return m_page;
	}

	public void checkNode(@Nonnull NodeBase node) {
		if(node.getClicked() != null)
			m_clickTargets.add(node);

		//-- Handle "normal" html input nodes.
		String name = node.getTag();
		if(INPUTTAGS.contains(name)) {
			m_baseInputs.add(node);
		}
	}

	@Nonnull
	public List<NodeBase> getClickTargets() {
		return m_clickTargets;
	}

	@Nonnull
	public List<NodeBase> getBaseInputs() {
		return m_baseInputs;
	}
}
