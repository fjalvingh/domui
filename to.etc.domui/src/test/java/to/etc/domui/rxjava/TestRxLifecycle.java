package to.etc.domui.rxjava;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.junit.Test;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-11-20.
 */
public class TestRxLifecycle {
	@Test
	public void testLifeCycle1() throws Exception {
		PublishSubject<String> ps = PublishSubject.<String>create();

		@NonNull Observable<String> replay = ps.replay(1000).autoConnect();

		Observable<String> s10 = replay
			.doOnSubscribe(a -> {
				System.out.println("onSubscribe called " + a + ", " + a.getClass());
				a.dispose();
			});

		Observable<String> s20 = s10.observeOn(Schedulers.io());

		Observable<String> s30 = s20.doOnDispose(() -> {
			System.out.println("Dispose called");
		});

		Disposable subscribe = s30.subscribe(a -> {
			System.out.println(">> result: " + a);
		});

		ps.onNext("Hello");
		Thread.sleep(2000);
		subscribe.dispose();
	}
}
