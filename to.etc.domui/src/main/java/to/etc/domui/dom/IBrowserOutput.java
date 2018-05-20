/*
 * DomUI Java User Interface library
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
package to.etc.domui.dom;

import java.io.*;

public interface IBrowserOutput {
	void writeRaw(CharSequence s) throws IOException;

	/**
	 * Writes string data. This escapes XML control characters to their entity
	 * equivalent. This does NOT indent data with newlines, because string data
	 * in a content block may not change.
	 */
	void text(String s) throws IOException;

	void nl() throws IOException;

	void inc();

	void dec();

	void setIndentEnabled(boolean ind);

	boolean isIndentEnabled();

	/**
	 * Writes a tag start. It can be followed by attr() calls.
	 */
	void tag(final String tagname) throws IOException;

	/**
	 * Ends a tag by adding a > only.
	 */
	void endtag() throws IOException;

	/**
	 * Ends a tag by adding />.
	 */
	void endAndCloseXmltag() throws IOException;

	/**
	 * Write the closing tag (&lt;/name&gt;).
	 */
	void closetag(String name) throws IOException;

	/**
	 * Appends an attribute to the last tag. The value's characters that are invalid are quoted into
	 * entities.
	 */
	void attr(String name, String value) throws IOException;

	void rawAttr(String name, String value) throws IOException;

	/**
	 * Write a simple numeric attribute thingy.
	 */
	void attr(String name, long value) throws IOException;

	void attr(String name, int value) throws IOException;

	void attr(String name, boolean value) throws IOException;


}
