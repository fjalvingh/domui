package to.etc.domuidemo.pages.test.rxjava;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.css.Overflow;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.rxjava.PageScheduler;
import to.etc.domuidemo.logic.RxTimer;

/**
 * Fixture for the rxjava integration: a timer observable, observed on this
 * page's {@link PageScheduler}, appends a line per tick. Leaving the page must
 * dispose the subscription - which shows as a "Disposed" line, and as the ticks
 * stopping.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-11-20.
 */
public class RxTimePage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		cp.add(new HTag(1, "RxJava timer lifecycle test"));

		Div d = new Div();
		cp.add(d);
		d.setHeight("400px");
		d.setOverflow(Overflow.AUTO);
		d.setTestID("ticks");

		RxTimer.getTicker()
			.observeOn(PageScheduler.on(this))
			.doOnSubscribe(a -> append(d, "Subscribed"))
			.doOnDispose(() -> append(d, "Disposed"))
			.subscribe(next -> append(d, "next: " + next)
				, error -> append(d, "ERROR: " + error)
				, () -> append(d, "completed"));
	}

	private void append(Div d, String what) {
		d.add(new Div("", what));
	}
}
