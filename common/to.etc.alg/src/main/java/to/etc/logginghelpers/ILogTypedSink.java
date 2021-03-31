package to.etc.logginghelpers;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.ConsoleUtil;
import to.etc.util.StringTool;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 14, 2013
 */
public interface ILogTypedSink {
	/**
	 * This is the NULL sink which drops all messages into the void
	 */
	@NonNull
	ILogTypedSink NULL = new ILogTypedSink() {
		@Override
		public void logRecord(@NonNull LogType t, @Nullable Throwable exception, @NonNull String line) {
		}
	};

	@NonNull
	ILogTypedSink SYSLOG = new ILogTypedSink() {
		@Override
		public void logRecord(@NonNull LogType t, @Nullable Throwable exception, @NonNull String line) {
			ConsoleUtil.consoleLog(t.name(), line);
			if(null != exception)
				ConsoleUtil.consoleError(StringTool.strStacktrace(exception));
		}
	};

	@NonNull
	ILogTypedSink SYSOUT = new ILogTypedSink() {
		@Override
		public void logRecord(@NonNull LogType t, @Nullable Throwable exception, @NonNull String line) {
			System.out.println(t + " " + line);
			if(null != exception)
				System.out.println(StringTool.strStacktrace(exception));
		}
	};

	void logRecord(@NonNull LogType t, @Nullable Throwable exception, @NonNull String line);

	default void logRecord(@NonNull LogType t, @NonNull String line) {
		logRecord(t, null, line);
	}

	default void log(@NonNull String line) {
		logRecord(LogType.LOG, line);
	}

	default void exception(@NonNull Throwable exception, @NonNull String where) {
		logRecord(LogType.ERR, exception, where);
	}
}
