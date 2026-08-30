package to.etc.domuidemo.pages.tutorial.messages;

/**
 * Tutorial, "telling something to a user": an exception that means something to the
 * application, and which the application therefore teaches {@link to.etc.domui.component.misc.ExceptionDialog}
 * to present as a sentence instead of as a stack trace. The translator doing that is
 * registered in {@link to.etc.domuidemo.Application}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class OutOfStockException extends RuntimeException {
	private final String m_album;

	public OutOfStockException(String album) {
		super("Out of stock: " + album);
		m_album = album;
	}

	public String getAlbum() {
		return m_album;
	}
}
