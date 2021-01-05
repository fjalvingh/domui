package to.etc.domuidemo.logic;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

/**
 * Test class that contains a hot stream of timer events.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-11-20.
 */
final public class RxTimer {
	static private Observable<Long> m_source = Observable.interval(1000, 1000, TimeUnit.MILLISECONDS)
		.subscribeOn(Schedulers.io())
		;

	static public Observable<Long> getTicker() {
		return m_source;
	}



}
