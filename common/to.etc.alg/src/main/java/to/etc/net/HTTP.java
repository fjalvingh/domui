package to.etc.net;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-07-19.
 */
final public class HTTP {
	private static HttpClient m_client;

	private HTTP() {}

	static public synchronized HttpClient client() {
		HttpClient client = m_client;
		if(null == client) {
			ExecutorService ex = Executors.newCachedThreadPool();

			m_client = client = HttpClient.newBuilder()
				.executor(ex)
				.followRedirects(Redirect.NORMAL)
				.build();
		}
		return client;
	}

	static public synchronized void close() {
		HttpClient client = m_client;
		if(null != client) {
			m_client = null;
			System.out.println("destroy: closing http client resources");
			Optional<Executor> executorStupidity = client.executor();
			if(executorStupidity.isPresent()) {					// Sure. This is of course better than NULL because no one would forget this. Idiots. And now the compiler cannot check.
				System.out.println("destroy: preparing to close HTTP executor");
				Executor executor = executorStupidity.get();
				if(executor instanceof ExecutorService) {
					((ExecutorService) executor).shutdownNow();
					System.out.println("destroy: closed HTTP executor");
				}
			}
		}
	}
}
