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
package to.etc.alg.process;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Helper code to spawn processes and capture their output.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
final public class ProcessTools2 {
	@NonNull
	final private ProcessBuilder m_builder;

	@Nullable
	private Writer m_stdoutWriter;

	@Nullable
	private Writer m_stderrWriter;

	@Nullable
	private OutputStream m_stdoutStream;

	@Nullable
	private OutputStream m_stderrStream;

	@Nullable
	private InputStream m_stdinStream;

	private IFollow m_follow;

	private boolean m_flush;

	public ProcessTools2() {
		m_builder = new ProcessBuilder();
	}

	public ProcessTools2(String... args) {
		m_builder = new ProcessBuilder();
		add(args);
	}

	public ProcessTools2(@NonNull ProcessBuilder pb) {
		m_builder = pb;
	}

	@NonNull
	public ProcessTools2 stdin(InputStream is) {
		m_stdinStream = is;
		return this;
	}

	@NonNull
	public ProcessTools2 stdout(@NonNull OutputStream stdout) {
		m_stdoutStream = stdout;
		return this;
	}

	@NonNull
	public ProcessTools2 stderr(@NonNull OutputStream stderr) {
		m_stderrStream = stderr;
		return this;
	}

	@NonNull
	public ProcessTools2 stdout(@NonNull Writer stdout) {
		m_stdoutWriter = stdout;
		return this;
	}

	@NonNull
	public ProcessTools2 stderr(@NonNull Writer stderr) {
		m_stderrWriter = stderr;
		return this;
	}

	@NonNull
	public ProcessTools2 add(@NonNull String cmd) {
		m_builder.command().add(cmd);
		return this;
	}

	@NonNull
	public ProcessTools2 add(@NonNull List<String> cmd) {
		m_builder.command().addAll(cmd);
		return this;
	}

	@NonNull
	public ProcessTools2 add(String... args) {
		for(String s: args)
			m_builder.command().add(s);
		return this;
	}

	@NonNull
	public ProcessTools2 setCommand(@NonNull List<String> cmd) {
		m_builder.command(cmd);
		return this;
	}

	@NonNull
	public ProcessTools2 directory(@NonNull File cmd) {
		m_builder.directory(cmd);
		return this;
	}

	@NonNull
	public ProcessTools2 follow(@NonNull IFollow cmd) {
		m_follow = cmd;
		return this;
	}

	@NonNull
	public ProcessTools2 flush() {
		m_flush = true;
		return this;
	}

	@NonNull
	public Map<String, String> env() {
		return m_builder.environment();
	}

	@NonNull
	public ProcessTools2 envStrip(@NonNull String... names) {
		for(String name : names) {
			env().remove(name);
		}
		return this;
	}

	public int run() throws Exception {
		Writer stdoutWriter = m_stdoutWriter;
		OutputStream stdoutStream = m_stdoutStream;
		if(null == stdoutStream && null == stdoutWriter)
			throw new IllegalStateException("Stdout not redirected");

		Process pr = m_builder.start();

		Thread outr;
		Thread errr = null;
		Thread inr = null;
		if(null != stdoutWriter) {
			outr = new StreamReaderThread(stdoutWriter, "stdout", pr.getInputStream(), null, m_follow, m_flush);
		} else if(stdoutStream != null) {
			outr = new StreamCopyThread(stdoutStream, "stdout", pr.getInputStream());
		} else
			throw new IllegalStateException();

		Writer stderrWriter = m_stderrWriter;
		OutputStream stderrStream = m_stderrStream;
		if(null != stderrWriter) {
			errr = new StreamReaderThread(stderrWriter, "stderr", pr.getErrorStream(), null, m_follow, m_flush);
		} else if(null != stderrStream) {
			errr = new StreamCopyThread(stderrStream, "stderr", pr.getErrorStream());
		} else {
			m_builder.redirectErrorStream(true);
		}

		InputStream stdinStream = m_stdinStream;
		if(null != stdinStream) {
			inr = new StreamCopyThread(pr.getOutputStream(), "stdin", stdinStream, true);
		}

		outr.start();
		if(null != errr)
			errr.start();
		if(null != inr)
			inr.start();
		int rc = pr.waitFor();
		outr.join();
		if(null != errr)
			errr.join();
		if(null != inr)
			inr.join();
		return rc;
	}

}
