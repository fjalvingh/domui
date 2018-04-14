package to.etc.domui.autotest;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Collection of all that is known for the page, by scanning all it's nodes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 10, 2013
 */
public class PageData {
	static private final Set<String> INPUTTAGS = new HashSet<String>(Arrays.asList("input", "select", "textarea"));

	@NonNull
	private final List<NodeBase> m_clickTargets = new ArrayList<NodeBase>();

	@NonNull
	private final List<NodeBase> m_baseInputs = new ArrayList<NodeBase>();

	@NonNull
	final private Page m_page;

	public PageData(@NonNull Page page) {
		m_page = page;
	}

	@NonNull
	public Page getPage() {
		return m_page;
	}

	public void checkNode(@NonNull NodeBase node) {
		if(node.getClicked() != null)
			m_clickTargets.add(node);

		//-- Handle "normal" html input nodes.
		String name = node.getTag();
		if(INPUTTAGS.contains(name)) {
			m_baseInputs.add(node);
		}
	}

	@NonNull
	public List<NodeBase> getClickTargets() {
		return m_clickTargets;
	}

	@NonNull
	public List<NodeBase> getBaseInputs() {
		return m_baseInputs;
	}
}
