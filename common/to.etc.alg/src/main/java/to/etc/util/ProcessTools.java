/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.util;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.alg.process.IFollow;
import to.etc.alg.process.StreamCopyThread;
import to.etc.alg.process.StreamReaderThread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Helper code to spawn processes and capture their output.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
final public class ProcessTools {
	@NonNull
	final private ProcessBuilder m_builder;

	private Writer m_stdout;

	private Writer m_stderr;

	private IFollow m_follow;

	private boolean m_flush;

	public ProcessTools() {
		m_builder = new ProcessBuilder();
	}

	public ProcessTools(@NonNull ProcessBuilder pb) {
		m_builder = pb;
	}

	@NonNull
	public ProcessTools stdout(@NonNull Writer stdout) {
		m_stdout = stdout;
		return this;
	}

	@NonNull
	public ProcessTools stderr(@NonNull Writer stderr) {
		m_stderr = stderr;
		return this;
	}

	@NonNull
	public ProcessTools add(@NonNull String cmd) {
		m_builder.command().add(cmd);
		return this;
	}

	@NonNull
	public ProcessTools add(@NonNull List<String> cmd) {
		m_builder.command().addAll(cmd);
		return this;
	}

	@NonNull
	public ProcessTools add(String... args) {
		for(String s: args)
			m_builder.command().add(s);
		return this;
	}

	@NonNull
	public ProcessTools setCommand(@NonNull List<String> cmd) {
		m_builder.command(cmd);
		return this;
	}

	@NonNull
	public ProcessTools directory(@NonNull File cmd) {
		m_builder.directory(cmd);
		return this;
	}

	@NonNull
	public ProcessTools follow(@NonNull IFollow cmd) {
		m_follow = cmd;
		return this;
	}

	@NonNull
	public ProcessTools flush() {
		m_flush = true;
		return this;
	}

	@NonNull
	public Map<String, String> env() {
		return m_builder.environment();
	}

	@NonNull
	public ProcessTools envStrip(@NonNull String... names) {
		for(String name : names) {
			env().remove(name);
		}
		return this;
	}

	public int run() throws Exception {
		Writer stdout = m_stdout;
		Writer stderr = m_stderr;
		if(null == stdout)
			throw new IllegalStateException("Stdout not redirected");
		if(null == stderr) {
			m_builder.redirectErrorStream(true);
		}
		Process pr = m_builder.start();

		StreamReaderThread outr = new StreamReaderThread(stdout, "stdout", pr.getInputStream(), null, m_follow, m_flush);
		outr.start();
		StreamReaderThread errr = null;
		if(stderr != null) {
			errr = new StreamReaderThread(stderr, "stderr", pr.getErrorStream(), null, m_follow, m_flush);
			errr.start();
		}
		int rc = pr.waitFor();
		outr.join();
		if(null != errr)
			errr.join();
		return rc;
	}

	/**
	 * Waits for completion of the command and collect data into the streams.
	 */
	static public int dumpStreams(Process pr, Appendable iosb) throws Exception {
		//-- Create two reader classes,
		StreamReaderThread outr = new StreamReaderThread(iosb, "stdout", pr.getInputStream());
		StreamReaderThread errr = new StreamReaderThread(iosb, "stderr", pr.getErrorStream());

		//-- Start both of 'm
		outr.start();
		errr.start();
		int rc = pr.waitFor();
		outr.join();
		errr.join();
		return rc;
	}

	/**
	 * Runs the process whose data is in the ProcessBuilder and captures the result with stdout and stderr merged.
	 */
	static public int runProcess(ProcessBuilder pb, Appendable sb) throws Exception {
		pb.redirectErrorStream(true); // Merge stdout and stderr
		Process pr = pb.start();
		StreamReaderThread outr = new StreamReaderThread(sb, "stdout", pr.getInputStream());
		outr.start();
		int rc = pr.waitFor();
		outr.join();
		return rc;
	}

	/**
	 * Runs the process whose data is in the ProcessBuilder and captures the result.
	 */
	static public int runProcess(ProcessBuilder pb, OutputStream stdout, Appendable stderrsb) throws Exception {
		Process pr = pb.start();
		StreamReaderThread errr = new StreamReaderThread(stderrsb, "stderr", pr.getErrorStream());
		StreamCopyThread outr = new StreamCopyThread(stdout, "stdout", pr.getInputStream());
		outr.start();
		errr.start();
		int rc = pr.waitFor();
		outr.join();
		errr.join();
		return rc;
	}

	/**
	 * Runs the process whose data is in the ProcessBuilder (with adding 'input' after process is started) and captures the result.
	 */
	static public int runProcessWithInput(ProcessBuilder pb, OutputStream stdout, Appendable stderrsb, String input) throws Exception {
		Process pr = pb.start();
		StreamReaderThread errr = new StreamReaderThread(stderrsb, "stderr", pr.getErrorStream());
		StreamCopyThread outr = new StreamCopyThread(stdout, "stdout", pr.getInputStream());
		outr.start();
		errr.start();

		BufferedWriter writer = new BufferedWriter(
			new OutputStreamWriter(pr.getOutputStream())
		);
		writer.write(input, 0, input.length());
		writer.newLine();
		writer.close();

		int rc = pr.waitFor();
		outr.join();
		errr.join();
		return rc;
	}

	/**
	 * Runs the process whose data is in the ProcessBuilder and captures the result with stdout and stderr merged.
	 */
	static public int runProcess(ProcessBuilder pb, Appendable outsb, Appendable errsb) throws Exception {
		Process pr = pb.start();
		StreamReaderThread outr = new StreamReaderThread(outsb, "stdout", pr.getInputStream());
		StreamReaderThread errr = new StreamReaderThread(errsb, "stderr", pr.getErrorStream());
		outr.start();
		errr.start();
		int rc = pr.waitFor();
		outr.join();
		errr.join();
		return rc;
	}

	/**
	 * Runs the process whose data is in the ProcessBuilder and captures the
	 * result with stdout and stderr merged into a writer.
	 */
	static public int runProcess(ProcessBuilder pb, Writer out) throws Exception {
		pb.redirectErrorStream(true); // Merge stdout and stderr
		Process pr = pb.start();
		StreamReaderThread outr = new StreamReaderThread(out, "stdout", pr.getInputStream());
		outr.start();
		int rc = pr.waitFor();
		outr.join();
		return rc;
	}
}
