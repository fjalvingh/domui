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
package to.etc.csv;

import java.io.*;

/**
 * Wraps around a iRecordReader to provide some extra functions.
 *
 * @author jal
 */
public class InputWrapper {
	private iRecordReader	m_ir;

	public InputWrapper(iRecordReader r) {
		m_ir = r;
	}

	/**
	 * @param name
	 * @return
	 */
	public iInputField find(String name) {
		return m_ir.find(name);
	}

	/**
	 * @return
	 */
	public int getCurrentRecNr() {
		return m_ir.getCurrentRecNr();
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public boolean nextRecord() throws IOException {
		return m_ir.nextRecord();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return m_ir.toString();
	}


	/**
	 * Defines new names for the field. The list of names
	 * is written to each field, depending on the position. Names
	 * are separated by semicolon.
	 * The names are rewritten every time this function is called.
	 *
	 * TODO: This does not seem to belong here....
	 */
	public void setNames(String namelist) {
		int ix = 0;
		int len = namelist.length();
		int nix = 0; // Current name index,
		while(ix < len) // Names left?
		{
			//-- Collect the next name
			int pos = namelist.indexOf(';', ix); // Find next ;
			if(pos == -1)
				pos = len; // If not found use rest of string
			String aname = namelist.substring(ix, pos);
			ix = pos; // Set new position at ;
			aname = aname.trim(); // Do not allow surrounding spaces.

			//-- Set this name,
			if(aname.length() > 0) {
				iInputField f = m_ir.getField(nix); // To this field,
				f.setName(aname);
			}
			nix++;
			if(ix >= len)
				break;
			ix++; // Past semicolon.
		}
	}

	public iInputField field(String name) throws Exception {
		iInputField f = find(name);
		if(f == null)
			throw new FieldNotFoundException("Input field name `" + name + "' is undefined in the input record.");
		return f;
	}

	public String get(String name) throws Exception {
		return field(name).getValue();
	}
}
