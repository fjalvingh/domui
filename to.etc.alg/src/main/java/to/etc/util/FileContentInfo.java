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

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Helper class to determine a file's type (text, binary) and encoding (for text files). A file
 * is marked as "binary" if it contains nulls and is not recognized as a text file. This will
 * also determine a tab size either by scanning for defaults or by markers in the text.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 21, 2010
 */
final public class FileContentInfo {
	private File m_source;

	private String m_name;

	private String m_encoding;

	private boolean m_text;

	private String m_extension;

	private int m_tabSize = 4;

	private int m_maxLineSize = 0;

	private int m_numNulls = 0;

	private int m_numUtf8 = 0;

	private int m_numNonISO = 0;

	private int m_numHigh = 0;

	private int m_numLines = 0;

	private boolean m_encodingValid;

	private FileContentInfo(File src, String name) {
		m_source = src;
		m_name = name;
	}

	@Nonnull
	static public FileContentInfo createType(@Nonnull File src, @Nonnull String name) throws Exception {
		FileContentInfo fc = new FileContentInfo(src, name);
		fc.load();
		return fc;
	}

	protected void load() throws Exception {
		InputStream is = new FileInputStream(m_source);

		byte utfleadin = 0;
		byte[] buf = new byte[8192];
		try {
			int uix = 0;
			int curll = 0;
			int sz;
			while(0 < (sz = is.read(buf))) {
				for(int i = 0; i < sz; i++) {
					byte v = buf[i];
					if(v == 0)
						m_numNulls++;
					if(uix > 0) {
						//-- Must be an UTF-8 sequence. Char must start with 10xx xxxx.
						if((v & 0xc0) != 0x80) {
							//-- Invalid UTF-8 sequence. Mark as done and count as ISO
							uix = 0;
							if(utfleadin >= 0x80 && utfleadin <= 0xa0) // Invalid iso-8859-1 sequence -> must be win1252
								m_numNonISO++;
							else
								m_numHigh++;
							if(v >= 0x80 && v <= 0xa0) // Invalid iso-8859-1 sequence -> must be win1252
								m_numNonISO++;
							else
								m_numHigh++;
							curll += 2; // whatever.
						} else {
							//-- Valid UTF-8 sequence. Decrement count
							if(--uix == 0) {
								m_numUtf8++;
								curll++;
							}
						}
					} else {
						//-- Not in an UTF sequence.
						if(v == '\n') {
							if(curll > m_maxLineSize)
								m_maxLineSize = curll;
							curll = 0;
							m_numLines++;
						} else if((v & 0xc0) >= 0xc0) {
							//-- Valid UTF-8 1st character. Determine expected count.
							if((v & 0xf8) == 0xf0)
								uix = 3;
							else if((v & 0xf0) == 0xe0)
								uix = 2;
							else if((v & 0xe0) == 0xc0)
								uix = 1;
							else {
								//-- Invalid UTF8 -
								if(v >= 0x80 && v <= 0xa0) // Invalid iso-8859-1 sequence -> must be win1252
									m_numNonISO++;
								else
									m_numHigh++;
							}
						} else if(v > 0x80) {
							if(v >= 0x80 && v <= 0xa0) // Invalid iso-8859-1 sequence -> must be win1252
								m_numNonISO++;
							else
								m_numHigh++;
						}
					}
				}
			}

			//-- Ok; determine type...
			m_extension = FileTool.getFileExtension(m_source.getName());
			m_text = true;

			m_encodingValid = true;
			if(m_numNulls > 0) {
				m_text = false;
				m_encodingValid = false;
			}
			if(m_numUtf8 > 0 || (m_numNonISO == 0 && m_numHigh == 0)) {
				m_encoding = "utf-8";
			} else if(m_numNonISO > 0) {
				m_encoding = "win1252"; // Contains 0x80..0x9f chars so cannot be iso-8859-15
				if(m_numUtf8 > 0)
					m_encodingValid = false;
			} else {
				m_encoding = "iso-8859-15";
				if(m_numUtf8 > 0)
					m_encodingValid = false;
			}
		} finally {
			FileTool.closeAll(is);
		}
	}

	public File getSource() {
		return m_source;
	}

	public String getName() {
		return m_name;
	}

	public String getEncoding() {
		return m_encoding;
	}

	public boolean isText() {
		return m_text;
	}

	public String getExtension() {
		return m_extension;
	}

	public int getTabSize() {
		return m_tabSize;
	}

	public int getMaxLineSize() {
		return m_maxLineSize;
	}

	public int getNumNulls() {
		return m_numNulls;
	}

	public int getNumUtf8() {
		return m_numUtf8;
	}

	public int getNumNonISO() {
		return m_numNonISO;
	}

	public int getNumHigh() {
		return m_numHigh;
	}

	public int getNumLines() {
		return m_numLines;
	}

	public boolean isEncodingValid() {
		return m_encodingValid;
	}
}
