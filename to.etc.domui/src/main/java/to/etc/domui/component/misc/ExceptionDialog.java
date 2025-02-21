package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.dbpool.BetterSQLException;
import to.etc.domui.component.misc.MsgBox2.Type;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Pre;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;
import to.etc.util.MessageException;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;
import to.etc.webapp.nls.CodeException;
import to.etc.webapp.query.QConcurrentUpdateException;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Generic dialog to show exceptions. By default this just shows the
 * reason code passed and an exception stack trace. But you can register
 * extra handlers that decode messages from specific exception types.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-1-18.
 */
@NonNullByDefault
final public class ExceptionDialog {
	private static final Logger LOG = LoggerFactory.getLogger(ExceptionDialog.class);

	/**
	 * Registered ways to translate an exception to some comprehensible message.
	 */
	static private List<Function<Exception, ExceptionPresentation>> m_exceptionMessageTranslators = Collections.emptyList();

	private ExceptionDialog() {
	}

	static {
		register(ExceptionDialog::translateQConcurrent);
		register(ExceptionDialog::translateOptimisticLock);
		register(ExceptionDialog::translateUIException);
		register(ExceptionDialog::translateBetterSQLException);
		register(ExceptionDialog::translateSqlException);
		register(ExceptionDialog::translateMessageException);
	}

	@Nullable
	private static ExceptionPresentation translateMessageException(Exception e) {
		if(e instanceof MessageException)
			return new ExceptionPresentation(e.getMessage());
		return null;
	}

	@Nullable
	static private ExceptionPresentation translateQConcurrent(Exception x) {
		if(x instanceof QConcurrentUpdateException)
			return new ExceptionPresentation(Msgs.uiConcurrentUpdate.getString());
		return null;
	}

	@Nullable
	static private ExceptionPresentation translateOptimisticLock(Exception x) {
		if(x.getClass().getSimpleName().equals("OptimisticLockException"))
			return new ExceptionPresentation(Msgs.uiConcurrentUpdate.getString());
		return null;
	}

	@Nullable
	static private ExceptionPresentation translateUIException(Exception x) {
		if(x instanceof CodeException)
			return new ExceptionPresentation(x.getMessage());
		return null;
	}

	@Nullable
	static private ExceptionPresentation translateBetterSQLException(Exception x) {
		BetterSQLException bex = tryUnwrapBetterSQLException(x, 0);
		if(null != bex) {
			return new ExceptionPresentation(bex.getMessage());
		}
		return null;
	}

	@Nullable
	private static BetterSQLException tryUnwrapBetterSQLException(Throwable x, int level) {
		if (x instanceof BetterSQLException) {
			return (BetterSQLException) x;
		}
		Throwable cause = x.getCause();
		if(level > 5 || null == cause) {
			return null;
		}
		return tryUnwrapBetterSQLException(cause, level + 1);
	}

	@Nullable
	static private ExceptionPresentation translateSqlException(Exception x) {
		SQLException sqlx = extractSQLException(x);
		if(null == sqlx)
			return null;
		String sqlState = sqlx.getSQLState();
		if(sqlState == null)
			return null;

		if(sqlState.equals("23505"))
			return new ExceptionPresentation(Msgs.sqlErrNotUnique.format());
		return null;
	}

	/**
	 * Try to see if the exception contains, somehow, a SQLException.
	 */
	@Nullable
	private static SQLException extractSQLException(Throwable x) {
		if(x instanceof SQLException)
			return (SQLException) x;
		if(x instanceof WrappedException)
			return extractSQLException(((WrappedException) x).getCause());
		if(x instanceof BetterSQLException)
			return extractSQLException(((BetterSQLException) x).getCause());
		if(x instanceof InvocationTargetException)
			return extractSQLException(((InvocationTargetException) x).getCause());
		return null;
	}

	static public void createIgnore(@NonNull NodeContainer container, @NonNull String message, @NonNull Throwable xin) {
		try {
			create(container, message, xin);
		} catch(Exception x) {
			LOG.error("Failed to create exception dialog: " + x, x);
		}
	}

	/**
	 * Show an exception as an error dialog.
	 */
	static public void create(@NonNull NodeContainer container, @NonNull String message, @NonNull Throwable xin) {
		if(xin instanceof ValidationException)
			return;
		Exception x = WrappedException.unwrap(xin);
		ExceptionPresentation presentation = findExceptionMessage(x);
		if(null == presentation) {
			//-- A real unexpected exception.
			LOG.error("ExceptionDialog: " + x, x);
			StringBuilder sb = new StringBuilder();
			StringTool.strStacktrace(sb, x);
			Pre pre = new Pre();
			pre.add(message + "\n" + x.toString() + "\n\n" + sb);

			MsgBox2.on(container)
				.title("An error has occurred")
				.type(Type.ERROR)
				.content(pre)
				//.text(message + "\n" + x.toString() + "\n\n" + sb)
				.modal()
				//.size(700, 500)
			;
			return;
		}

		//-- Render the supplied presentation.
		MsgBox2.on(container)
			.title(message)
			.type(Type.ERROR)
			.content(presentation.getFragment())
			.text(presentation.getMessage())
			.modal()
			//.size(700, 500)
		;
	}

	private static synchronized List<Function<Exception, ExceptionPresentation>> getExceptionMessageTranslators() {
		return m_exceptionMessageTranslators;
	}

	@Nullable
	static private ExceptionPresentation findExceptionMessage(Exception x) {
		for(Function<Exception, ExceptionPresentation> mt : getExceptionMessageTranslators()) {
			ExceptionPresentation s = mt.apply(x);
			if(s != null)
				return s;
		}
		return null;
	}

	static public synchronized void register(Function<Exception, ExceptionPresentation> translator) {
		List<Function<Exception, ExceptionPresentation>> old = m_exceptionMessageTranslators;
		m_exceptionMessageTranslators = new ArrayList<>();
		m_exceptionMessageTranslators.add(translator);
		m_exceptionMessageTranslators.addAll(old);
	}

	static public final class ExceptionPresentation {
		@Nullable
		private final String m_message;

		@Nullable
		private final NodeContainer m_fragment;

		public ExceptionPresentation(@NonNull NodeContainer fragment) {
			m_fragment = fragment;
			m_message = null;
		}

		public ExceptionPresentation(@NonNull String message) {
			m_message = message;
			m_fragment = null;
		}

		@Nullable
		public String getMessage() {
			return m_message;
		}

		@Nullable
		public NodeContainer getFragment() {
			return m_fragment;
		}
	}
}
