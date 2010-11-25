package to.etc.net;

import java.io.*;

/**
 * Used to provide file data for a file parameter to send to a HTTP server
 * using MultipartPoster. This class has three methods to open, to get data
 * and to close something which is to be send as a file. These methods are
 * called as follows:
 *	<ul><li>open() is called before the data is to be sent</li>
 * 		<li>repeated calls to getBytes() are done until it returns -1; each
 *      	call must copy whatever data fits into the byte array passed</li>
 *      <li>close() is called after this process</li>
 * 	</ul>
 * </p>
 * <p>For an example implementation reading a file see aFileToSend in the
 * MultipartPoster source.</p>
 *
 */
public abstract class MultipartFile {
	public MultipartFile() {
	}


	/**
	 * This must provide data for the file to sent. It gets called with a buffer
	 * that should be filled with as much data as will fit. It will be called
	 * repeatedly until it returns -1.
	 * @param buf		the buffer to copy data to,
	 * @return			the #bytes put into the buffer, or -1 when done.
	 * @throws IOException	on any error.
	 */
	public abstract int getBytes(byte[] buf) throws IOException;


	public void open() throws IOException {
	}

	public int getSize() {
		return 0;
	}

	public void close() {
	}
}
