package to.etc.domuidemo.pages.rxjava;

import to.etc.domui.dom.css.Overflow;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.rxjava.PageScheduler;
import to.etc.domuidemo.logic.RxTimer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-11-20.
 */
public class RxTimePage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new HTag(1, "RxJava timer lifecycle test"));

		Div d = new Div();
		add(d);
		d.setHeight("400px");
		d.setOverflow(Overflow.AUTO);

		RxTimer.getTicker()
			.observeOn(PageScheduler.on(this))
			.doOnSubscribe(a -> {
				append(d, "Subscribed");
			})
			.doOnDispose(() -> {
				append(d, "Disposed");
			})
			.subscribe(next -> {
				append(d, "next: " + next);
			}, error -> {
				append(d, "ERROR: " + error);
			}, () -> {
				append(d, "completed");
			});
	}

	private void append(Div d, String subscribed) {
		d.add(new Div("", subscribed));
		System.out.println("rx>>>> " + subscribed);
	}
}
