package to.etc.domui.state;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.eclipse.jdt.annotation.NonNull;

import java.util.List;

/**
 * The content of a previously-destroyed {@link WindowSession}, in a format suitable for a
 * reload. Instances get stored inside HttpSession and can be retrieved by window ID after
 * a development time reload has saved them.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 27, 2013
 */
@Immutable
final public class SavedWindow {
	@NonNull
	private final String m_id;

	@NonNull
	final private List<SavedPage> m_pageList;

	SavedWindow(@NonNull String id, @NonNull List<SavedPage> pageList) {
		m_id = id;
		m_pageList = pageList;
	}

	@NonNull
	public String getId() {
		return m_id;
	}

	@NonNull
	public List<SavedPage> getPageList() {
		return m_pageList;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(m_id).append(": ");
		int ix = 0;
		for(SavedPage sp : m_pageList) {
			if(ix++ > 0)
				sb.append(", ");
			sb.append(sp.toString());
		}

		return sb.toString();
	}
}
