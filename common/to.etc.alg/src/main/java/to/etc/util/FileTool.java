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
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import to.etc.xml.DomTools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Contains some often-used file subroutines.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class FileTool {
	private static final Logger LOG = LoggerFactory.getLogger(FileTool.class);

	static public final long KB = 1024L;

	static public final long MB = 1024L * KB;

	static public final long GB = 1024L * MB;

	static public final int DEFAULT_MAX_TEXTFILE_LENGTH = (int) (5 * MB);

	private FileTool() {
	}

	/**
	 * The seed TS to use as base of names.
	 */
	static private long m_seed_ts;

	/**
	 * The sequence number.
	 */
	static private long m_index;

	/**
	 * This returns the File location of a directory that should contain application-generated
	 * log files. This should be used instead of /tmp to allocate log files where needed. It
	 * checks for several default locations.
	 */
	@NonNull
	synchronized static public File getLogRoot(@NonNull String appVar) {
		String name = appVar.toUpperCase().replace(".", "").replace("-", "");

		//-- Is there a valid "vp.logroot" thing?
		String s = System.getProperty(appVar);
		File logRoot = checkLogDir(s, "The java system property '" + appVar + "'");

		if(null == logRoot) {
			//-- Is there a valid logroot environment variable?
			s = System.getenv(name);
			checkLogDir(s, "The environment variable '" + name + "'");
		}

		if(null == logRoot) {
			//-- No defined log output. Try java.io.tmpdir
			s = System.getProperty("java.io.tmpdir");
			logRoot = checkLogDir(s, "Java's tmpdir location");
		}
		if(null == logRoot) {
			//-- Is there a valid TMP environment variable?
			s = System.getenv("TMP");
			logRoot = checkLogDir(s, "The environment variable 'TMP'");
		}
		if(null == logRoot) {
			//-- Is there a valid TEMP environment variable?
			s = System.getenv("TEMP");
			logRoot = checkLogDir(s, "The environment variable 'TEMP'");
		}
		if(null == logRoot) {
			logRoot = checkLogDir("/tmp", "The system /tmp directory");
		}
		if(null == logRoot)
			throw new RuntimeException("None of the log directories can be found!! Please specify a proper one using " + appVar + " property, " + name + " envvar or normal UNIX conventions");
		return logRoot.getAbsoluteFile();
	}

	@Nullable
	private static File checkLogDir(@NonNull String s, @NonNull String source) {
		if(null != s && !s.trim().isEmpty()) {
			File f = new File(s);
			if(!f.exists()) {
				if(!f.mkdirs())
					System.out.println("init: " + source + " is set to '" + s + "', but that directory does not exist and I cannot create it. Ignoring the parameter...");
				else {
					System.out.println("init: " + source + " is set to '" + s + "', but the directory was not present. I created it, please check it's permissions.");
					return f;
				}
			} else {
				if(!f.isDirectory())
					System.out.println("init: " + source + " is set to '" + s + "', but that is not a directory. Ignoring the parameter...");
				else
					return f;
			}
		}
		return null;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Directory maintenance and bulk code.				*/
	/*--------------------------------------------------------------*/

	/**
	 * Returns the java.io.tmpdir directory. Throws an exception if it does not exist or
	 * is inaccessible.
	 */
	static public File getTmpDir() {
		String v = System.getProperty("java.io.tmpdir");
		if(v == null)
			v = "/tmp";
		File tmp = new File(v);
		if(!tmp.exists() || !tmp.isDirectory())
			throw new IllegalStateException("The 'java.io.tmpdir' variable does not point to an existing directory (" + tmp + ")");
		return tmp;
	}

	static {
		m_seed_ts = System.currentTimeMillis();
	}

	/**
	 * Create a temp directory within the root directory.
	 */
	static public synchronized File newDir(final File root) {
		for(; ; ) {
			String fn = makeName("td");
			File of = new File(root, fn);
			if(!of.exists()) {
				ignore(of.mkdirs());
				return of;
			}
		}
	}

	/**
	 * Create a temp file within the specified root directory.
	 */
	static public synchronized File makeTempFile(final File root) {
		for(; ; ) {
			String fn = makeName("tf");
			File of = new File(root, fn);
			if(!of.exists()) {
				try {
					ignore(of.createNewFile());
				} catch(Exception x) {
					if(x instanceof RuntimeException)
						throw (RuntimeException) x;
					throw new WrappedException(x);
				}
				return of;
			}
		}
	}

	static private String makeName(final String type) {
		StringBuffer sb = new StringBuffer(32);
		sb.append(type);
		add36(sb, m_seed_ts);
		sb.append('-');
		add36(sb, m_index++);
		return sb.toString();
	}

	static private void add36(final StringBuffer sb, long v) {
		if(v > 36)
			add36(sb, v / 36);
		v = v % 36;
		if(v < 10)
			sb.append((char) (v + '0'));
		else
			sb.append((char) (v - 10 + 'a'));
	}

	/**
	 * Deletes all files in the directory. It skips errors and tries to delete
	 * as much as possible. If elogb is not null then all errors are written
	 * there.
	 */
	static public boolean dirEmpty(@NonNull File dirf) {
		return dirEmpty(dirf, null);
	}

	/**
	 * Delete the directory <i>and</i> all it's contents.
	 */
	static public void deleteDir(@NonNull File f) {
		dirEmpty(f);
		delete(f);
	}

	/**
	 * prepare a directory in this way: if it does not exist, create it.
	 * if it does exist then delete all files from the dir.
	 *
	 * @param dir the directory that must be made existent
	 * @throws Exception when creation fails or when removing old contents
	 *                   fails.
	 */
	public static void prepareDir(@NonNull File dir) throws Exception {
		if(!dir.exists()) {
			if(dir.mkdirs()) // make sure all parent dirs exist, then create this one
				return; // success

			throw new Exception("unable to create directory: " + dir.getPath());
		} else {
			if(!dirEmpty(dir))
				throw new Exception("unable to empty the directory: " + dir.getPath());
		}
	}

	/**
	 * Deletes all files in the directory. It skips errors and tries to delete
	 * as much as possible. If elogb is not null then all errors are written
	 * there.
	 */
	@Deprecated
	static public boolean dirEmpty(@NonNull File dirf, final Vector<Object> elogb) {
		boolean hase = false;

		File[] ar = dirf.listFiles();
		if(ar == null)
			return true;

		for(int i = 0; i < ar.length; i++) {
			String name = ar[i].getName();
			if(!name.equals(".") && !name.equals("..")) {
				try {
					if(ar[i].isDirectory())
						dirEmpty(ar[i], elogb);
					if(!ar[i].delete())
						throw new IOException("Delete failed?");
				} catch(IOException x) {
					if(elogb != null) {
						elogb.add(dirf);
						elogb.add(x);
					}
					hase = true;

				}
			}
		}

		return !hase;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	File name manipulation.								*/
	/*--------------------------------------------------------------*/

	/**
	 * Returns the extension of a file. The extension DOES NOT INCLUDE the . If no
	 * extension is present then the empty string is returned ("").
	 */
	@NonNull
	static public String getFileExtension(@NonNull String fn) {
		int s1 = fn.lastIndexOf('/');
		int s2 = fn.lastIndexOf('\\');
		if(s2 > s1)
			s1 = s2;
		if(s1 == -1)
			s1 = 0;

		int p = fn.lastIndexOf('.');
		if(p < s1)
			return "";
		return fn.substring(p + 1);
	}

	/**
	 * Returns the start position of the filename extension in the string. If
	 * the string has no extension then this returns -1.
	 */
	static public int findFilenameExtension(@NonNull  String fn) {
		int slp = fn.lastIndexOf('/');
		int t = fn.lastIndexOf('\\');
		if(t > slp)
			slp = t; // Find last directory separator,

		//-- Now find last dot,
		int dp = fn.lastIndexOf('.');
		if(dp < t) // Before dir separator: dot is in directory part,
			return -1;
		return dp;
	}

	/**
	 * Returns the file name excluding the suffix of the name. So test.java
	 * returns test.
	 */
	@NonNull
	static public String fileNameSansExtension(@NonNull String fn) {
		int slp = fn.lastIndexOf('/');
		int t = fn.lastIndexOf('\\');
		int start = slp == -1
			? t == -1 ? 0 : t
			: slp;

		//-- Now find last dot,
		int dp = fn.lastIndexOf('.');
		if(dp == -1 || dp < start)
			return fn;
		return fn.substring(0, dp);
	}

	/**
	 * Copies a file of max. 1GB.
	 */
	static public void copyFile(@NonNull File destf, @NonNull File srcf) throws IOException {
		copyFile(destf, srcf, 1 * GB);
	}

	static public void copyFile(@NonNull File destf, @NonNull File srcf, long maxSize) throws IOException {
		try(InputStream is = new FileInputStream(srcf); OutputStream os = new FileOutputStream(destf)) {
			copyFile(os, is, maxSize);
			ignore(destf.setLastModified(srcf.lastModified()));
		}
	}

	/**
	 * Copies the inputstream to the output stream, limited to 1GB of data(!).
	 */
	static public void copyFile(@NonNull OutputStream os, @NonNull InputStream is) throws IOException {
		copyFile(os, is, 1 * GB);
	}

	static public void copyFile(@NonNull OutputStream os, @NonNull InputStream is, long maxSize) throws IOException {
		byte[] buf = new byte[8192];
		int sz;
		long size = 0L;
		while(0 < (sz = is.read(buf))) {
			size += sz;
			if(size > maxSize)
				throw new IOException("Copied data exceeds the configured maximum (" + maxSize + " bytes)");
			os.write(buf, 0, sz);
		}
	}

	/**
	 * Copy the input reader to the output reader.
	 */
	static public void copyFile(@NonNull Writer w, @NonNull Reader r) throws IOException {
		char[] buf = new char[8192];
		int sz;
		while(0 < (sz = r.read(buf)))
			w.write(buf, 0, sz);
	}

	/**
	 * Copies an entire directory structure from src to dest. This copies the
	 * files from src into destd; it does not remove files in destd that are
	 * not in srcd. Use synchronizeDir() for that.
	 */
	static public void copyDir(@NonNull File destd, @NonNull File srcd) throws IOException {
		if(!srcd.exists())
			return;
		if(srcd.isFile()) {
			copyFile(destd, srcd);
			return;
		}
		if(destd.exists() && destd.isFile())
			destd.delete();
		destd.mkdirs();

		//-- Right: on with the copy then
		File[] ar = srcd.listFiles();
		for(File sf : ar) {
			File df = new File(destd, sf.getName());
			if(sf.isFile()) {
				if(df.isDirectory())
					deleteDir(df);
				copyFile(df, sf); // Then copy the file.
			} else if(sf.isDirectory()) {
				if(df.isFile()) // ... but target is a file now?
					delete(df); // then delete it...
				copyDir(df, sf); // ..before copying
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Copy using hard links.								*/
	/*--------------------------------------------------------------*/

	/**
	 * Copy a file or directory using hard links.
	 */
	static public void copyHardlinked(@NonNull File targetDir, @NonNull File sourceDir, String... ignorePaths) throws IOException {
		if(!sourceDir.exists())
			return;

		if(targetDir.exists()) {
			if(targetDir.isFile())
				delete(targetDir);
			else
				FileTool.dirEmpty(targetDir);
		}
		if(sourceDir.isFile()) {
			hardlinkFile(new File(targetDir, sourceDir.getName()), sourceDir);
			return;
		}

		Set<String> ignoreSet = new HashSet<>();
		Collections.addAll(ignoreSet, ignorePaths);

		ignore(targetDir.mkdirs());
		StringBuilder sb = new StringBuilder();
		internalCopyHardDir(targetDir, sourceDir, sb, ignoreSet);
	}

	static private void internalCopyHardDir(@NonNull File targetDir, @NonNull File sourceDir, @NonNull StringBuilder sb, @NonNull Set<String> ignoreSet) throws IOException {
		File[] ar = sourceDir.listFiles();
		if(null == ar)
			return;
		int len = sb.length();
		for(File f : ar) {
			File destf = new File(targetDir, f.getName());
			sb.setLength(len);
			if(len > 0)
				sb.append("/");
			sb.append(f.getName());
			if(ignoreSet.contains(sb.toString()))
				continue;

			if(f.isFile()) {
				hardlinkFile(destf, f);
			} else {
				destf.mkdirs();
				internalCopyHardDir(destf, f, sb, ignoreSet);
			}
		}
	}

	private static void hardlinkFile(@NonNull File destf, @NonNull File srcf) throws IOException {
		Files.createLink(destf.toPath(), srcf.toPath());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Fully reading some data stream/file into a string.	*/
	/*--------------------------------------------------------------*/

	/**
	 * Read a file's contents in a string using the default encoding of the platform.
	 */
	static public String readFileAsString(final File f) throws Exception {
		StringBuilder sb = new StringBuilder((int) f.length() + 20);
		readFileAsString(sb, f);
		return sb.toString();
	}

	/**
	 * Read a file's contents as byte[].
	 */
	public static byte[] readFileAsByteArray(@NonNull File file) throws IOException {
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			int intSize = FileTool.getIntSizeOfFile(file);
			byte[] data = new byte[intSize];
			int read = in.read(data);
			if(read != intSize)
				throw new IOException("Tried to read " + intSize + " bytes but only got " + read);
			return data;
		} finally {
			FileTool.closeAll(in);
		}
	}

	@NonNull
	public static byte[] readByteArray(@NonNull InputStream is) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileTool.copyFile(baos, is);
		baos.close();
		return baos.toByteArray();
	}

	/**
	 * Load a class resource as a byte array. If the resource is not found this returns null.
	 */
	@Nullable
	public static byte[] readResourceAsByteArray(@NonNull Class<?> clz, @NonNull String name) throws IOException {
		InputStream is = clz.getResourceAsStream(name);
		if(null == is)
			return null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			copyFile(baos, is);
			baos.close();
			return baos.toByteArray();
		} finally {
			closeAll(is);
		}
	}

	static public void readFileAsString(final Appendable o, final File f) throws Exception {
		try(LineNumberReader lr = new LineNumberReader(new FileReader(f))) {
			String line;
			while(null != (line = lr.readLine())) {
				o.append(line);
				o.append("\n");
			}
		}
	}

	/**
	 * Read a file into a string using the specified encoding.
	 */
	static public String readFileAsString(final File f, Charset encoding) throws Exception {
		try(InputStream is = new FileInputStream(f)) {
			return readStreamAsString(is, encoding);
		}
	}

	/**
	 * Use the charset variant.
	 * Read a file into a string using the specified encoding.
	 */
	static public String readFileAsString(final File f, final String encoding) throws Exception {
		try(InputStream is = new FileInputStream(f)) {
			return readStreamAsString(is, encoding);
		}
	}

	/**
	 * mbp, moved here from old DaemonBase with some adaptions. Reads the head
	 * and tail lines of a text file into the stringbuffer.
	 * The number of lines in the head is at most "headsize". If this
	 * count is exceeded the read lines will be placed in a circular string buffer
	 * of size "tailsize", and appended to the stringbuffer when the whole file
	 * has been processed.
	 * Note that if headsize plus tailsize exceeds the actual number of lines,
	 * this means that the whole file will be placed in the stringbuffer.
	 * <p>
	 * Intended use is to mail (excerpts from) logfiles from Daemon processes.
	 */
	static public void readHeadAndTail(final StringBuffer sb, final File f, final int headsize, final int tailsize) throws Exception {
		String[] ring = null; // ring buffer for lines if more than N
		int ringix = 0; // index into ringbuffer
		int linecount = 0; // lines processed so far.
		try(LineNumberReader lr = new LineNumberReader(new FileReader(f))) {
			String line;

			while(null != (line = lr.readLine())) {
				linecount++;
				if(ring != null) {
					// Yes. Save this line into the ringbuffer
					if(ringix == tailsize)
						ringix = 0; // rollover back to zero if needed
					ring[ringix++] = line; // put line in ringbuffer
				} else {
					// Copy line to output stringbuffer
					sb.append(line); // Add the line,
					sb.append('\n'); // And a newline,
					if(linecount > headsize) // Reached requested nr of lines in head ?
						ring = new String[tailsize]; // Create the ringbuffer
				}
			}

			// Do we need to output the tail or did everything fit in the head ?
			if(ring != null) {
				// There is a tail
				if(linecount > (headsize + tailsize)) {
					sb.append("\n ...\n ...\n ...");
					sb.append(linecount - headsize - tailsize);
					sb.append(" lines were skipped ...\n ...\n ...\n");
				}

				// index in ringbuffer was already advanced in read loop, but not checked for roll-over
				if(ringix == tailsize)
					ringix = 0;
				// if there is nothing at this point in the ringbuffer, start outputting
				// from zero up to here otherwise output from here on for tailsize counts

				if(ring[ringix] == null) {
					for(int ix = 0; ix < ringix - 1; ix++) {
						sb.append(ring[ix]);
						sb.append('\n');
					}
				} else {
					for(int ct = 0; ct < tailsize; ct++) {
						sb.append(ring[ringix++]);
						sb.append('\n');
						if(ringix == tailsize)
							ringix = 0;
					}
				}
			}
		}
	}

	static public File copyStreamToTmpFile(final InputStream is, String name) throws Exception {
		File file = new File(getTmpDir(), name);
		return copyStreamToFile(is, file);
	}

	static public File copyStreamToTmpFile(final InputStream is) throws Exception {
		File file = makeTempFile(getTmpDir());
		return copyStreamToFile(is, file);
	}

	public static File copyStreamToFile(final InputStream is, File file) throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			copyFile(os, is);
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {
			}
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {
			}
		}
		return file;
	}

	/**
	 * Use the Charset variant.
	 */
	static public String readStreamAsString(final InputStream is, final String enc) throws Exception {
		StringBuilder sb = new StringBuilder(128);
		readStreamAsString(sb, is, enc);
		return sb.toString();
	}

	/**
	 * Use the Charset variant.
	 */
	static public void readStreamAsString(final Appendable o, final InputStream f, final String enc) throws Exception {
		Reader r = new InputStreamReader(f, enc);
		readStreamAsString(o, r);
	}

	static public String readStreamAsString(final InputStream is, final Charset enc) throws Exception {
		return readStreamAsString(is, enc, DEFAULT_MAX_TEXTFILE_LENGTH);
	}

	static public String readStreamAsString(final InputStream is, final Charset enc, int maxLength) throws Exception {
		StringBuilder sb = new StringBuilder(128);
		readStreamAsString(sb, is, enc, maxLength);
		return sb.toString();
	}

	static public void readStreamAsString(final Appendable o, final InputStream f, Charset enc) throws Exception {
		readStreamAsString(o, f, enc, DEFAULT_MAX_TEXTFILE_LENGTH);
	}

	static public void readStreamAsString(final Appendable o, final InputStream f, Charset enc, int maxLength) throws Exception {
		Reader r = new InputStreamReader(f, enc);
		readStreamAsString(o, r, maxLength);
	}

	static public void readStreamAsString(final Appendable o, final Reader r) throws Exception {
		readStreamAsString(o, r, DEFAULT_MAX_TEXTFILE_LENGTH);
	}

	static public void readStreamAsString(final Appendable o, final Reader r, int maxLength) throws Exception {
		char[] buf = new char[4096];
		int len = 0;
		for(; ; ) {
			int ct = r.read(buf);
			len += ct;
			if(ct < 0 || len > maxLength)
				break;
			o.append(new String(buf, 0, ct));
		}
	}

	static public String readStreamAsString(final Reader r) throws Exception {
		return readStreamAsString(r, DEFAULT_MAX_TEXTFILE_LENGTH);
	}

	static public String readStreamAsString(final Reader r, int maxLength) throws Exception {
		StringBuilder sb = new StringBuilder(128);
		readStreamAsString(sb, r, maxLength);
		return sb.toString();
	}

	static public void appendFileFromString(File f, String v, Charset enc) throws Exception {
		try(OutputStream os = new FileOutputStream(f, true)) {
			writeFileFromString(os, v, enc);
		}
	}

	static public void writeFileFromString(final File f, final String v, final String enc) throws Exception {
		try(OutputStream os = new FileOutputStream(f)) {
			writeFileFromString(os, v, enc);
		}
	}

	static public void writeFileFromString(final OutputStream os, final String v, final String enc) throws Exception {
		try(Writer w = new OutputStreamWriter(os, enc == null ? "UTF8" : enc)) {
			w.write(v);
		}
	}

	static public void writeFileFromString(final File f, final String v, Charset enc) throws Exception {
		try(OutputStream os = new FileOutputStream(f)) {
			writeFileFromString(os, v, enc);
		}
	}

	static public void writeFileFromString(final OutputStream os, final String v, Charset enc) throws Exception {
		try(Writer w = new OutputStreamWriter(os, enc == null ? StandardCharsets.UTF_8 : enc)) {
			w.write(v);
		}
	}

	@NonNull
	static public String readResourceAsString(Class<?> base, String name, String encoding) throws Exception {
		InputStream is = base.getResourceAsStream(name);
		try(is) {
			if(null == is)
				throw new IllegalStateException(base + ":" + name + " resource not found");
			return readStreamAsString(is, encoding);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	File hash stuff..									*/
	/*--------------------------------------------------------------*/

	/**
	 * Create an MD5 hash for a file's contents.
	 */
	@NonNull
	static public byte[] hashFile(final File f) throws IOException {
		try(InputStream is = new FileInputStream(f)) {
			return hashFile(is);
		}
	}

	/**
	 * Create an MD5 hash for a buffer set.
	 */
	@NonNull
	static public byte[] hashBuffers(final byte[][] data) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException x) {
			throw new RuntimeException("MISSING MANDATORY SECURITY DIGEST PROVIDER MD5: " + x.getMessage());
		}

		//-- Ok, calculate.
		for(byte[] b : data)
			md.update(b);
		return md.digest();
	}

	/**
	 * Create a HEX MD5 hash for a buffer set.
	 */
	@NonNull
	static public String hashBuffersHex(final byte[][] data) {
		return StringTool.toHex(hashBuffers(data));
	}

	/**
	 * Hashes all data from an input stream and returns an MD5 hash.
	 *
	 * @param is The stream to read and hash.
	 * @return A hash (16 bytes MD5)
	 */
	@NonNull
	static public byte[] hashFile(final InputStream is) throws IOException {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException x) {
			throw new RuntimeException("MISSING MANDATORY SECURITY DIGEST PROVIDER MD5: " + x.getMessage());
		}

		//-- Ok, read and calculate.
		byte[] buf = new byte[8192];
		int szrd;
		while(0 <= (szrd = is.read(buf)))
			md.update(buf, 0, szrd);
		return md.digest();
	}

	/**
	 * Hash a file and return it's hex MD5hash.
	 */
	@NonNull
	static public String hashFileHex(final File f) throws IOException {
		return StringTool.toHex(hashFile(f));
	}

	/**
	 * Hash an InputStream and return it's hex MD5hash.
	 */
	@NonNull
	static public String hashFileHex(final InputStream is) throws IOException {
		return StringTool.toHex(hashFile(is));
	}

	/**
	 * Hashes all data from an input stream and returns an MD5 hash in hex. This hashes the file but replaces
	 * all cr or crlf or lfcr with a single lf.
	 */
	@NonNull
	static public String hashTextFile(@NonNull final File f) throws IOException {
		try(InputStream is = new FileInputStream(f)) {
			return StringTool.toHex(hashTextFile(is));
		}
	}

	/**
	 * Hashes all data from an input stream and returns an MD5 hash. This hashes the file but replaces
	 * all cr or crlf or lfcr with a single lf.
	 *
	 * @param is The stream to read and hash.
	 * @return A hash (16 bytes MD5)
	 */
	@NonNull
	static public byte[] hashTextFile(@NonNull final InputStream is) throws IOException {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException x) {
			throw new RuntimeException("MISSING MANDATORY SECURITY DIGEST PROVIDER MD5: " + x.getMessage());
		}

		//-- Ok, read and calculate. Ignore all.
		byte[] buf = new byte[8192];
		int szrd;
		while(0 <= (szrd = is.read(buf))) {
			//-- Got a buffer. Scan for cr and remove;
			int six = 0;
			int oix = 0;
			while(six < szrd) {
				byte c = buf[six++];
				if(c != (byte) 13) {
					buf[oix++] = c;
				}
			}
			md.update(buf, 0, oix);
		}
		return md.digest();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Loading properties.									*/
	/*--------------------------------------------------------------*/

	/**
	 * Load a file as a Properties file.
	 */
	@NonNull
	static public Properties loadProperties(final File f) throws Exception {
		try(InputStream is = new FileInputStream(f)) {
			Properties p = new Properties();
			p.load(is);
			return p;
		}
	}

	/**
	 * Save a properties file.
	 */
	static public void saveProperties(final File f, final Properties p) throws Exception {
		try(OutputStream os = new FileOutputStream(f)) {
			p.store(os, "# No comment");
		}
	}

	/**
	 * Opens the jar file and tries to load the plugin.properties file from it.
	 */
	static public Properties loadPropertiesFromZip(final File f, final String name) throws Exception {
		//-- Try to locate a zipentry containing coma.jar
		try(InputStream is = new FileInputStream(f)) {
			return loadPropertiesFromZip(is, name);
		}
	}

	/**
	 * Opens the jar file and tries to load the plugin.properties file from it.
	 */
	static public Properties loadPropertiesFromZip(final InputStream is, final String name) throws Exception {
		try(ZipInputStream zis = new ZipInputStream(is)) {
			for(; ; ) {
				ZipEntry ze = zis.getNextEntry();
				if(ze == null)
					break;
				String n = ze.getName();
				if(n.equalsIgnoreCase(name)) {
					//-- Gotcha! Create parameters and load 'm
					byte[] data;
					try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
						copyFile(baos, zis, 1024L * 1024L);
						data = baos.toByteArray();
					}

					Properties p = new Properties();
					p.load(new ByteArrayInputStream(data));    // Load properties
					return p;
				}
				zis.closeEntry();
			}
		}
		return null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Quickies to load a single file from a ZIP.			*/
	/*--------------------------------------------------------------*/

	/**
	 * Opens the jar file and tries to load the plugin.properties file from it.
	 */
	static public Document loadXmlFromZip(final File f, final String name, final boolean nsaware) throws Exception {
		//-- Try to locate a zipentry containing coma.jar
		try(InputStream is = new FileInputStream(f)) {
			return loadXmlFromZip(is, f + "!" + name, name, nsaware);
		}
	}

	/**
	 * Opens the jar file and tries to load the plugin.properties file from it.
	 */
	static public Document loadXmlFromZip(final InputStream is, final String ident, final String name, final boolean nsaware) throws Exception {
		try(ZipInputStream zis = new ZipInputStream(is)) {
			for(; ; ) {
				ZipEntry ze = zis.getNextEntry();
				if(ze == null)
					break;
				String n = ze.getName();
				if(n.equalsIgnoreCase(name)) {
					//-- Gotcha! Create parameters and load 'm
					return DomTools.getDocument(new SizeCountingInputStream(zis, 512 * MB), ident, nsaware);
				}
				zis.closeEntry();
			}
		}
		return null;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Classloader and class related stuff.				*/
	/*--------------------------------------------------------------*/

	/**
	 * Creates a classloader to load data from the given jar file.
	 */
	static public ClassLoader makeJarLoader(final File f) throws MalformedURLException {
		URL u = f.toURI().toURL();
		URLClassLoader uc = URLClassLoader.newInstance(new URL[]{u});
		return uc;
	}

	static public ClassLoader makeJarLoader(final File f, final ClassLoader parent) throws MalformedURLException {
		URL u = f.toURI().toURL();
		URLClassLoader uc = URLClassLoader.newInstance(new URL[]{u}, parent);
		return uc;
	}

	static public void copyResource(final Writer w, final Class<?> cl, final String rid) throws Exception {
		Reader r = null;
		try {
			InputStream is = cl.getResourceAsStream(rid);
			if(is == null)
				throw new IllegalStateException("Missing resource '" + rid + "' at class=" + cl.getName());
			r = new InputStreamReader(is, "utf-8");
			copyFile(w, r);
		} finally {
			if(r != null)
				r.close();
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Zip and unzip.										*/
	/*--------------------------------------------------------------*/

	/**
	 * Zip the contents of dir or file to the zipfile. The zipfile
	 * is deleted before the new contents are added to it.
	 */
	public static void zip(final File zipfile, final File dir) throws Exception {
		if(zipfile.exists())
			if(!zipfile.delete())
				throw new IOException("Unable to delete zipfile: " + zipfile);

		ZipOutputStream zos = null;
		InputStream is = null;
		byte[] buf = new byte[8192]; // Copy buffer
		try {
			//-- Create the output stream
			zos = new ZipOutputStream(new FileOutputStream(zipfile));
			if(dir.isFile())
				zipFile(zos, "", dir, buf);
			else
				zipDir(zos, "", dir, buf); // Zip the entry passed
		} finally {
			if(zos != null)
				try {
					zos.close();
				} catch(Exception x) {
				}
			if(is != null)
				try {
					is.close();
				} catch(Exception x) {
				}
		}
	}

	static private void zipDir(final ZipOutputStream zos, final String base, File f, final byte[] buf) throws IOException {
		if(!f.isDirectory())
			throw new IllegalStateException("Must be a directory");

		//-- Write a directory entry if there's something
		if(!base.isEmpty()) {
			ZipEntry ze = new ZipEntry(base); // The dir ending in /
			zos.putNextEntry(ze);
			ze.setTime(f.lastModified());
		}

		//-- Now recursively copy the rest.
		File[] far = f.listFiles(); // All files in the dir
		for(File file : far) {
			f = file;
			if(f.isFile())
				zipFile(zos, base, f, buf); // just zip the file,
			else
				zipDir(zos, base + f.getName() + "/", f, buf);
		}
	}

	/**
	 * Recursive workhorse for zipping the entry passed, be it file or directory.
	 */
	static private void zipFile(final ZipOutputStream zos, final String base, final File f, final byte[] buf) throws IOException {
		//-- Create a relative name for this entry
		if(!f.isFile())
			throw new IllegalStateException(f + ": must be file");
		try(InputStream is = new FileInputStream(f)) {
			//-- Write this file.
			ZipEntry ze = new ZipEntry(base + f.getName());
			ze.setTime(f.lastModified());
			zos.putNextEntry(ze);

			//-- Copy
			int sz;
			while(0 <= (sz = is.read(buf)))
				zos.write(buf, 0, sz);
		}
	}

	static public void zipAppend(ZipOutputStream zos, String fileName, File f) throws IOException {
		try(InputStream is = new FileInputStream(f)) {
			//-- Write this file.
			ZipEntry ze = new ZipEntry(fileName);
			ze.setTime(f.lastModified());
			zos.putNextEntry(ze);

			//-- Copy
			FileTool.copyFile(zos, is);
		}
	}

	/**
	 * Unzips an archive. Max file size (and total expanded size) is 1GB.
	 */
	public static void unzip(File dest, InputStream is) throws Exception {
		unzip(dest, is, 1 * GB, 1 * GB);
	}

	public static void unzip(File dest, InputStream is, long maxFileSize, long maxTotalSize) throws Exception {
		dest.mkdirs();
		long totalSize = 0L;
		byte[] buf = new byte[8192];
		try(ZipInputStream zis = new ZipInputStream(is)) {
			ZipEntry ze;
			while(null != (ze = zis.getNextEntry())) {
				String name = ze.getName();
				checkValidRelativePath(name);
				File of = new File(dest, name);                    // Create a full path
				if(ze.isDirectory())
					of.mkdirs();
				else {
					//-- Copy.
					File dir = of.getParentFile();
					dir.mkdirs();
					try(OutputStream os = new FileOutputStream(of)) {
						int sz;
						long fileSize = 0L;
						while(0 < (sz = zis.read(buf))) {
							fileSize += sz;
							if(fileSize > maxFileSize)
								throw new IOException("Zip file " + name + " is larger than the maximum size (" + maxFileSize + " bytes)");
							totalSize += sz;
							if(totalSize > maxTotalSize) {
								throw new IOException("Zip file expands to a larger set of files than the allowed maximum size (" + maxTotalSize + " bytes)");
							}
							os.write(buf, 0, sz);
						}
					}
				}
				zis.closeEntry();
			}
		}
	}

	public static void unzipSingleFile(File dest, InputStream is) throws Exception {
		unzipSingleFile(dest, is, 1 * GB);
	}

	public static void unzipSingleFile(File dest, InputStream is, long maxFileSize) throws Exception {
		dest.getParentFile().mkdirs();
		byte[] buf = new byte[8192];
		try(ZipInputStream zis = new ZipInputStream(is)) {
			ZipEntry ze = zis.getNextEntry();
			if(null == ze) {
				throw new IllegalArgumentException("Expected single file in zip but empty zip located!");
			}
			if(ze.isDirectory()) {
				throw new IllegalArgumentException("Expected single file in zip but located directory! " + ze);
			}
			try(OutputStream os = new FileOutputStream(dest)) {
				copyFile(os, zis, maxFileSize);
			}
			zis.closeEntry();
			if(null != zis.getNextEntry()) {
				throw new IllegalArgumentException("Expected single file in zip but multiple zip entries located!");
			}
		}
	}

	/**
	 * Unzip the contents of the zipfile to the directory. The directory is
	 * created if it does not yet exist.
	 */
	public static void unzip(final File dest, final File zipfile) throws Exception {
		unzip(dest, zipfile, 1 * GB, 1 * GB);
	}

	public static void unzip(final File dest, final File zipfile, long maxFileSize, long maxTotalSize) throws Exception {
		if(zipfile.length() < 1)
			return;

		try(InputStream is = new FileInputStream(zipfile)) {
			unzip(dest, is, maxFileSize, maxTotalSize);
		}
	}

	@NonNull
	static public List<String> getZipDirectory(@NonNull File in) throws Exception {
		List<String> res = new ArrayList<>();
		try(ZipInputStream zis = new ZipInputStream(new FileInputStream(in))) {
			ZipEntry ze;
			while(null != (ze = zis.getNextEntry())) {
				String name = ze.getName();
				checkValidRelativePath(name);
				res.add(name);
				zis.closeEntry();
			}
			return res;
		}
	}

	@Nullable
	static public InputStream getZipContent(final File src, final String name) throws IOException {
		boolean ok = false;
		InputStream is = new FileInputStream(src);
		try {
			InputStream ris = getZipContent(is, name);
			ok = true;
			return ris;
		} finally {
			if(!ok)
				FileTool.closeAll(is);
		}
	}

	/**
	 * Returns a stream which is the uncompressed data stream for a zip file
	 * component.
	 */
	@Nullable
	static public InputStream getZipContent(@NonNull final InputStream zipis, @NonNull final String name) throws IOException {
		ZipInputStream zis = null;

		//-- Try to locate a zipentry for the spec'd name
		try {
			zis = new ZipInputStream(zipis);
			for(; ; ) {
				ZipEntry ze = zis.getNextEntry();
				if(ze == null)
					break;
				String n = ze.getName();
				if(n.equalsIgnoreCase(name)) {
					final ZipInputStream z = zis;
					zis = null;
					return new InputStream() {
						@Override
						public long skip(final long size) throws IOException {
							return z.skip(size);
						}

						@Override
						public synchronized void reset() throws IOException {
							z.reset();
						}

						@Override
						public int read(final byte[] b) throws IOException {
							return z.read(b);
						}

						@Override
						public int read(final byte[] b, final int off, final int len) throws IOException {
							return z.read(b, off, len);
						}

						@Override
						public int read() throws IOException {
							return z.read();
						}

						@Override
						public void close() throws IOException {
							z.close();
							zipis.close();
							super.close();
						}

						@Override
						public int available() throws IOException {
							return z.available();
						}
					};
				}
				zis.closeEntry();
			}
			return null;
		} finally {
			try {
				if(zis != null)
					zis.close();
			} catch(Exception x) {
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	ByteBufferSet utilities.							*/
	/*--------------------------------------------------------------*/

	/**
	 * Loads a byte[][] from an input stream until exhaustion.
	 */
	@NonNull
	static public byte[][] loadByteBuffers(@NonNull final InputStream is) throws IOException {
		ArrayList<byte[]> al = new ArrayList<byte[]>();
		byte[] buf = new byte[8192];
		int off = 0;
		for(; ; ) {
			//-- Fill the (next part of the) buffer
			int max = buf.length - off;
			int sz = is.read(buf, off, max);
			if(sz == -1) {
				//-- EOF - data complete.
				if(off <= 0)
					break;
				byte[] newbuf = new byte[off];
				System.arraycopy(buf, 0, newbuf, 0, off);
				al.add(newbuf);
				break;
			}
			off += sz;
			if(off >= buf.length) {
				al.add(buf);
				off = 0;
				buf = new byte[8192];
			}
		}
		return al.toArray(new byte[al.size()][]);
	}

	/**
	 * Load an entire file in a byte buffer set.
	 */
	static public byte[][] loadByteBuffers(final File in) throws IOException {
		InputStream is = new FileInputStream(in);
		try {
			return loadByteBuffers(is);
		} finally {
			try {
				is.close();
			} catch(Exception x) {
			}
		}
	}

	/**
	 * Save the data in byte buffers to a file.
	 */
	static public void save(final File of, final byte[][] data) throws IOException {
		OutputStream os = new FileOutputStream(of);
		try {
			save(os, data);
		} finally {
			try {
				os.close();
			} catch(Exception x) {
			}
		}
	}

	/**
	 * Save the data in byte buffers to an output stream.
	 */
	static public void save(final OutputStream os, final byte[][] data) throws IOException {
		for(byte[] b : data)
			os.write(b);
	}

	@NonNull
	static public byte[] loadArray(@NonNull File src) throws IOException {
		long sz = src.length();
		if(sz > Integer.MAX_VALUE)
			throw new IOException("File too large (> 2GB)");
		byte[] data = new byte[(int) sz];
		InputStream is = new FileInputStream(src);
		try {
			int len = is.read(data);
			if(len != data.length)
				throw new IOException("Unexpected end of file after reading " + len + " of " + data.length + " bytes");
			return data;
		} finally {
			closeAll(is);
		}
	}

	/**
	 * Save the data in byte array to a file.
	 */
	static public void save(final File of, final byte[] data) throws IOException {
		OutputStream os = new FileOutputStream(of);
		try {
			os.write(data);
		} finally {
			try {
				os.close();
			} catch(Exception x) {
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Data marshalling and unmarshalling					*/
	/*--------------------------------------------------------------*/

	/**
	 * Sends an int fragment
	 */
	static public void writeInt(final OutputStream os, final int val) throws IOException {
		os.write((val >> 24) & 0xff);
		os.write((val >> 16) & 0xff);
		os.write((val >> 8) & 0xff);
		os.write(val & 0xff);
	}

	/**
	 * Sends a long
	 */
	static public void writeLong(final OutputStream os, final long val) throws IOException {
		writeInt(os, (int) (val >> 32));
		writeInt(os, (int) (val & 0xffffffff));
	}

	static public void writeString(final OutputStream os, String s) throws IOException {
		if(s == null)
			s = "";
		byte[] data = s.getBytes("UTF-8");
		//        msg("hex of '"+s+"' is "+StringTool.toHex(data)+" ("+data.length+" bytes)");
		writeInt(os, data.length);
		os.write(data);
	}

	/**
	 * Reads a 4-byte bigendian int off the connection.
	 */
	static public int readInt(final InputStream is) throws IOException {
		int v1 = is.read();
		int v2 = is.read();
		int v3 = is.read();
		int v4 = is.read();
		return (v1 << 24) | (v2 << 16) | (v3 << 8) | v4;
	}

	static public long readLong(final InputStream is) throws IOException {
		int v1 = readInt(is);
		int v2 = readInt(is);
		return (long) v1 << 32 | ((long) v2 & 0xffffffff);
	}

	static public String readString(final InputStream is) throws IOException {
		int sl = readInt(is);
		if(sl < 0)
			return null;
		byte[] data = new byte[sl];
		if(is.read(data) != sl)
			throw new IOException("String could not be fully read");
		return new String(data, "UTF-8");
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Logging helpers.									*/
	/*--------------------------------------------------------------*/

	static public InputStream wrapInputStream(final InputStream rawStream, final ILogSink s, final int maxinmemory) throws Exception {
		//-- Read the input stream and copy to memory or file.
		File tempfile = null;
		byte[] buf = new byte[maxinmemory];
		InputStream is = rawStream;
		int off = 0;

		//-- Initial read of a buffer. Exit if the buffer is full AND more data is available(!)
		for(; ; ) {
			int szleft = buf.length - off; // #bytes left in buffert
			if(szleft == 0) // Buffer overflow: need big file.
				break;

			int szread = is.read(buf, off, szleft); // Read fully,
			if(szread == -1) {
				//-- Got EOF within single buffer: no need to read further, use memory stream. Off now contains the length read totally. Start to dump the data,
				StringBuilder sb = new StringBuilder(off * 4);
				sb.append("Raw INPUT dump of the input stream:\n");
				for(int doff = 0; doff < off; doff += 32) {
					StringTool.arrayToDumpLine(sb, buf, doff, 32, buf.length);
					sb.append("\n");
				}
				sb.append("Total size of the input stream is " + off + " bytes\n");
				s.log(sb.toString());
				return new ByteArrayInputStream(buf, 0, off); // Return the memory based copy.
			}

			//-- Read completed, but room to spare: try again,
			off += szread;
		}

		//-- Ok: the buffer overflowed. We allocate a tempfile and dump the data in there.
		tempfile = File.createTempFile("soapin", ".bin");
		try {
			StringBuilder sb = new StringBuilder(off * 4);
			try(OutputStream os = new FileOutputStream(tempfile)) {
				os.write(buf); // Copy what's already read.

				//-- Dump what's already read into a string thing
				sb.append("Raw INPUT dump of the input stream:\n");
				int doff = 0;
				for(; doff < buf.length; doff += 32) {
					StringTool.arrayToDumpLine(sb, buf, doff, 32, buf.length);
					sb.append("\n");
				}

				//-- Now continue reading buffers, dumping 'm and adding them to the file.
				for(; ; ) {
					int szread = is.read(buf);
					if(szread <= 0)
						break;

					//-- Push data read to the overflow file
					os.write(buf, 0, szread);

					//-- Log whatever's read,
					for(int rlen = 0; rlen < szread; rlen += 32) {
						StringTool.arrayToDumpLine(sb, buf, rlen, 32, buf.length);
						sb.append("\n");
						doff += 32;
					}
					off += szread;
				}
				os.close();
			}

			//-- Log the data,
			sb.append("Total size of the input stream is ").append(off).append(" bytes\n");
			s.log(sb.toString());
			final InputStream tis = new FileInputStream(tempfile);
			final File del = tempfile;
			tempfile = null;

			return new InputStream() {
				@Override
				public int read() throws IOException {
					return tis.read();
				}

				@Override
				public int read(final byte[] b, final int xoff, final int len) throws IOException {
					return tis.read(b, xoff, len);
				}

				@Override
				public void close() throws IOException {
					tis.close();
					delete(del);
				}
			};
		} finally {
			try {
				if(tempfile != null)
					delete(tempfile);
			} catch(Exception x) {
				// Ignore
			}
		}
	}

	static public InputStream copyAndDumpStream(StringBuilder tgt, InputStream in, String encoding) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
		copyFile(bos, in);
		bos.close();

		byte[] data = bos.toByteArray(); // Data read from stream;
		tgt.append(new String(data, encoding));
		return new ByteArrayInputStream(data);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Serialization helpers.								*/
	/*--------------------------------------------------------------*/

	/**
	 * Save a serializable object to a datastream.
	 */
	static public void saveSerialized(OutputStream os, Serializable obj) throws IOException {
		try(ObjectOutputStream oos = new ObjectOutputStream(os)) {
			oos.writeObject(obj);
		} finally {
			closeAll(os);
		}
	}

	static public void saveSerialized(File f, Serializable obj) throws IOException {
		try(OutputStream os = new FileOutputStream(f)) {
			saveSerialized(os, obj);
		}
	}

	/**
	 * Load a single serialized object from a datastream.
	 */
	@Nullable
	static public Object loadSerialized(InputStream is) throws IOException, ClassNotFoundException {
		try(ObjectInputStream iis = new ObjectInputStream(is)) {
			return iis.readObject();
		}
	}

	/**
	 * Load a single serialized object from a file.
	 */
	@Nullable
	static public Object loadSerialized(File f) throws IOException, ClassNotFoundException {
		try(InputStream is = new FileInputStream(f)) {
			return loadSerialized(is);
		}
	}

	/**
	 * Load a serialized object, and return null on any load exception.
	 */
	@Nullable
	static public Object loadSerializedNullOnError(InputStream is) {
		try(ObjectInputStream iis = new ObjectInputStream(is)) {
			return iis.readObject();
		} catch(Exception x) {
			return null;
		}
	}

	@Nullable
	static public Object loadSerializedNullOnError(File f) throws IOException, ClassNotFoundException {
		try(InputStream is = new FileInputStream(f)) {
			return loadSerialized(is);
		} catch(Exception x) {
			return null;
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Miscellaneous.										*/
	/*--------------------------------------------------------------*/

	/**
	 * This attempts to close all of the resources passed to it, without throwing exceptions. It
	 * is meant to be used from finally clauses. Please take care: objects that require a succesful
	 * close (like writers or outputstreams) should NOT be closed by this method! They must be
	 * closed using a normal close within the exception handler.
	 * This list can also contain File objects; these files/directories will be deleted.
	 */
	static public void closeAll(Object... list) {
		StringBuilder sb = new StringBuilder();
		for(Object o : list) {
			try {
				tryClose(o);
			} catch(Exception x) {
				sb.append("Failed to close ").append(o).append(": ").append(x.toString()).append("\n");
				StringTool.strStacktrace(sb, x);
			}
		}
		if(sb.length() > 0)
			throw new RuntimeException("Some resources failed to close:\n" + sb);
	}

	static private void tryClose(Object v) throws Exception {
		if(v instanceof AutoCloseable) {
			((AutoCloseable) v).close();
		} else if(v instanceof File) {
			File f = (File) v;
			if(f.isFile())
				delete(f);
			else
				FileTool.deleteDir(f);
		} else if(v != null) {
			Method m = ClassUtil.findMethod(v.getClass(), "close");
			if(m == null) {
				m = ClassUtil.findMethod(v.getClass(), "release");
			}
			if(m != null) {
				m.invoke(v);
			} else {
				StringTool.dumpErrorLocation(LOG, "UNKNOWN RESOURCE OF TYPE " + v.getClass() + " TO CLOSE PASSED TO FileTool.closeAll()!!!!\nFIX THIS IMMEDIATELY!!!!!!");
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Comparing file system structures.					*/
	/*--------------------------------------------------------------*/

	/**
	 * Compare the content of two directories, and callback methods on changes. The
	 * callbacks define what needs to be done to change "a" (old) into "b" (new).
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Nov 29, 2010
	 */
	static public void compareDirectories(IDirectoryDelta delta, File a, File b) throws Exception {
		StringBuilder sb = new StringBuilder();
		compare(delta, sb, a, b);
	}

	static private void compare(IDirectoryDelta delta, StringBuilder sb, File a, File b) throws Exception {
		int clen = sb.length();
		try {

			//-- Create the relative path.
			if(clen != 0)
				sb.append(File.separator);
			sb.append(a.getName());
			String relpath = sb.toString();

			//-- Compare the two thingies.
			if(!a.exists()) {
				if(!b.exists())
					return; // Silly, cannot happen.

				//-- b exists but A does not: file/directory added
				if(b.isFile())
					delta.fileAdded(b, a, relpath); // File a
				else {
					//-- Directory added: mark the directory b as added, then add it's content fully.
					if(delta.directoryAdded(b, a, relpath)) // File a dir delta
						addDirectoryContents(delta, sb, a, b); // Walk the entire "b" structure, and add everything..
				}
			} else if(!b.exists()) {
				//-- Path exists in a but not in b -> it was deleted.
				if(a.isFile())
					delta.fileDeleted(b, a, relpath);
				else {
					//-- Directory deleted: mark b as deleted, then delete it's contents if that is needed.
					if(delta.directoryDeleted(b, a, relpath))
						deleteDirectoryContents(delta, sb, a, b);
				}
			} else {
				//-- Both a and b exist. Compare types 1st.
				if(a.isFile()) {
					if(b.isDirectory()) {
						//-- Disjoint: file changed to directory in B. This is a "deleted" file in b, then an added directory in b,
						delta.fileDeleted(b, a, relpath); // b as a FILE was deleted
						if(delta.directoryAdded(b, a, relpath)) // b as DIRECTORY was added
							addDirectoryContents(delta, sb, a, b); // Walk the entire "b" structure, and add everything..
					} else {
						//-- Both a and b are files. Ask the delta thing to decide whether they are equal and dont bother with the result.
						delta.compareFiles(b, a, relpath);
					}
				} else {
					//-- a is a directory.
					if(b.isFile()) {
						//-- Disjoint: directory a changed to file b. This is a "delete directory" in b followed by an "add file".
						if(delta.directoryDeleted(b, a, relpath)) // b as a DIR was deleted
							deleteDirectoryContents(delta, sb, a, b);
						delta.fileAdded(b, a, relpath);
					} else {
						//-- Both A and B are existing directories. We need to compare their contents.
						compareDirectories(delta, sb, a, b);
					}
				}
			}
		} finally {
			sb.setLength(clen);
		}
	}

	/**
	 * Compare the contents of both existing directories.
	 */
	static private void compareDirectories(IDirectoryDelta delta, StringBuilder sb, File a, File b) throws Exception {
		File[] aar = a.listFiles();
		File[] bar = b.listFiles();
		Set<String> bset = new HashSet<String>(); // Set containing all 'b' files.
		for(File bf : bar)
			bset.add(bf.getName());

		for(File af : aar) {
			File bf = new File(b, af.getName()); // Get how the name would be in "a"
			bset.remove(af.getName());
			compare(delta, sb, af, bf);
		}

		//-- Everything left in bset was added in b....
		for(String name : bset) {
			File bf = new File(b, name);
			File af = new File(a, name);
			compare(delta, sb, af, bf);
		}
	}

	/**
	 * Called when directory "b" does not exist while "a" does. It means the
	 * directory was deleted from "b". Send delete events for all items below
	 * "a".
	 */
	static private void deleteDirectoryContents(IDirectoryDelta delta, StringBuilder sb, File a, File b) throws Exception {
		File[] aar = a.listFiles(); // Everything in a
		int clen = sb.length();
		for(File af : aar) {
			File bf = new File(b, af.getName()); // Get how the name would be in "a"
			if(clen == 0)
				sb.append(File.separator);
			sb.append(af.getName());
			String relpath = sb.toString();
			if(bf.isFile())
				delta.fileDeleted(bf, af, relpath);
			else {
				//-- Directory added: mark the directory b as added, then add it's content fully.
				if(delta.directoryDeleted(bf, af, relpath)) // File a dir delta
					deleteDirectoryContents(delta, sb, af, bf); // Walk the entire "a" structure, and delete everything from "b"
			}
			sb.setLength(clen);
		}
	}

	/**
	 * Called when a new directory "b" is discovered that was not present as "a". This walks
	 * the content of "b", and calls add events for files/directories in "a".
	 */
	static private void addDirectoryContents(IDirectoryDelta delta, StringBuilder sb, File a, File b) throws Exception {
		File[] bar = b.listFiles(); // Everything in b
		int clen = sb.length();
		for(File bf : bar) {
			File af = new File(a, bf.getName()); // Get how the name would be in "a"
			if(clen == 0)
				sb.append(File.separator);
			sb.append(bf.getName());
			String relpath = sb.toString();
			if(bf.isFile())
				delta.fileAdded(bf, af, relpath);
			else {
				//-- Directory added: mark the directory b as added, then add it's content fully.
				if(delta.directoryAdded(bf, af, relpath)) // File a dir delta
					addDirectoryContents(delta, sb, af, bf); // Walk the entire "b" structure, and add everything..
			}
			sb.setLength(clen);
		}
	}

	/**
	 * Saves blob into specified file.
	 */
	static public void saveBlob(@NonNull File out, @NonNull Blob in) throws Exception {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = in.getBinaryStream();
			os = new FileOutputStream(out);
			copyFile(os, is);
			os.close();
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {
			}
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {
			}
		}
	}

	/**
	 * Returns size of a file as int type. In case of Integer range overflow {@link Integer#MAX_VALUE}, throws {@link IllegalStateException}.
	 * This can be used on files with expected size less then 2GB.
	 */
	public static int getIntSizeOfFile(@NonNull File file) {
		long size = file.length();
		if(size > Integer.MAX_VALUE) {
			throw new IllegalStateException("We do not allow file sizes > " + StringTool.strSize(Integer.MAX_VALUE) + ", found file size:" + StringTool.strSize(size));
		}
		return (int) size;
	}

	@NonNull
	public static File createTmpDir() throws IOException {
		File f = File.createTempFile("work", ".dir");
		delete(f);
		return f;
	}

	/**
	 * Returns number of lines in a specified file.
	 */
	public static int getNumberOfLines(@NonNull File file) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			int lines = 0;
			while(reader.readLine() != null)
				lines++;
			return lines;
		} finally {
			FileTool.closeAll(reader);
		}
	}

	/**
	 * Calculate the relative path of file in the root passed.
	 */
	@Nullable
	static public String getRelativePath(@NonNull File root, @NonNull File other) {
		try {
			String modroot = root.toString().replace('\\', '/');
			String inroot = other.toString().replace('\\', '/');
			if(modroot.equals(inroot))
				return "";

			if(!inroot.startsWith(modroot + "/"))
				return null;
			return inroot.substring(modroot.length() + 1);
		} catch(Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	@NonNull
	static public Reader getResourceReader(@NonNull Class<?> root, @Nullable String name) {
		InputStream is = root.getResourceAsStream(name);
		if(null == is)
			throw new IllegalStateException("JUnit test: missing test resource with base=" + root + " and name " + name);
		try {
			return new InputStreamReader(is, "utf-8");
		} catch(UnsupportedEncodingException x) {
			throw WrappedException.wrap(x);
		}
	}

	/**
	 * Returns the string from specified Clob.
	 */
	@NonNull
	public static String readAsString(@NonNull Clob data) throws IOException, SQLException {
		try(Reader reader = data.getCharacterStream()) {
			char[] buf = new char[8192];
			int len;
			final StringBuilder sb = new StringBuilder();
			while((len = reader.read(buf)) > 0)
				sb.append(buf, 0, len);
			return sb.toString();
		}
	}

	/**
	 * Does not allow .., / or \ at the root, not a colon in the path.
	 */
	public static void checkValidRelativePath(String name) {
		if(name.contains("..") || name.startsWith("/") || name.startsWith("\\") || name.contains(":"))
			throw new IllegalArgumentException("Invalid relative path name: " + name);
	}

	/**
	 * Used to prevent idiotic errors from Sonar for file.delete.
	 */
	public static void ignore(boolean delete) {
		//-- And we need a nested comment too 8-(

	}

	/**
	 * Deletes the file or (empty) directory, and reports an error in the log if that fails.
	 */
	public static void delete(File file) {
		try {
			Files.delete(file.toPath());
		} catch(Exception x) {
			LOG.debug("Failed to delete " + file + ": " + x, x);
		}
	}
}
