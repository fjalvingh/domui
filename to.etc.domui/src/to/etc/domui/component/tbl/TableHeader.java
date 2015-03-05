package to.etc.domui.component.tbl;

import to.etc.domui.dom.html.*;

import javax.annotation.*;

/**
 * An extra table header that can be added to a {@link to.etc.domui.component.tbl.RowRenderer}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 1/26/15.
 */
@DefaultNonNull
abstract public class TableHeader extends TR {
	@Override abstract public void createContent() throws Exception;

	public TH addHeader() {
		TH th = new TH();
		add(th);
		return th;
	}

	public TH addHeader(@Nonnull NodeBase data) {
		TH th = addHeader();
		th.add(data);
		return th;
	}

	public TH addHeader(@Nonnull String css) {
		TH th = addHeader();
		th.setCssClass(css);
		return th;
	}
}
