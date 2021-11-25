package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TH;
import to.etc.domui.dom.html.TR;

/**
 * An extra table header that can be added to a {@link to.etc.domui.component.tbl.RowRenderer}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 1/26/15.
 */
@NonNullByDefault
public class TableHeader extends TR {
	@Override
	public void createContent() throws Exception {
	}

	public TH addHeader() {
		TH th = new TH();
		add(th);
		return th;
	}

	public TH addHeader(@NonNull NodeBase data) {
		TH th = addHeader();
		th.add(data);
		return th;
	}

	public TH addHeader(@NonNull String css) {
		TH th = addHeader();
		th.setCssClass(css);
		return th;
	}
}
