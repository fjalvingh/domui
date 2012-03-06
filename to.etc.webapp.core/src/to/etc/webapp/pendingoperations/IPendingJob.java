package to.etc.webapp.pendingoperations;

import java.io.*;

import to.etc.util.*;

public interface IPendingJob extends Serializable {
	void execute(ILogSink log, PendingOperation po, Progress progress) throws Exception;
}
