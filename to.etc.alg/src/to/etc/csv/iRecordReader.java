package to.etc.csv;

import java.io.*;

/**
 * Created on Oct 14, 2003
 * @author jal
 */
public interface iRecordReader {
	public abstract void open(Reader r, String name) throws Exception;

	public abstract void close() throws Exception;

	public abstract int getCurrentRecNr();

	/**
	 * Read the next (or first) record from the input and prepare it for
	 * processing.
	 * @return
	 */
	public abstract boolean nextRecord() throws IOException;

	/**
	 * Locates the specified field in the current record.
	 * @param name
	 * @return
	 */
	public abstract iInputField find(String name);

	public abstract iInputField getField(int ix);

	public abstract int size();
}
