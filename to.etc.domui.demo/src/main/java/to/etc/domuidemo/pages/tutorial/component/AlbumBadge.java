package to.etc.domuidemo.pages.tutorial.component;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.input.AbstractDivControl;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;

/**
 * Tutorial, "writing a component": a control whose value is an <i>object</i> rather
 * than a number - which is where value change detection gets interesting.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class AlbumBadge extends AbstractDivControl<Album> {
	@Override
	public void createContent() throws Exception {
		setCssClass("dm-badge");

		Album album = internalGetValue();
		if(null == album) {
			add(new Span("dm-badge-empty", "(no album)"));
			return;
		}
		add(new Span("dm-badge-t", album.getTitle()));
		add(new Span("dm-badge-i", " #" + album.getId()));
	}

	/**
	 * A label's "for" needs a real input to point at, and this control has none.
	 */
	@Nullable
	@Override
	public NodeBase getForTarget() {
		return null;
	}
}
