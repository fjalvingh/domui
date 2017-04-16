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

import javax.annotation.*;

import to.etc.util.*;

/**
 * Pretty-printing output renderer. Slower than the non-pretty variant, used for
 * debugging.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
public class PrettyXmlOutputWriter extends XmlOutputWriterBase implements IBrowserOutput {
	private IndentWriter m_w;

	public PrettyXmlOutputWriter(@Nonnull Writer out) {
		super(new IndentWriter(out));
		m_w = (IndentWriter) getWriter();
	}

	@Override
	public void nl() throws IOException {
		m_w.forceNewline();
	}

	@Override
	public void inc() {
		m_w.inc();
	}

	@Override
	public void dec() {
		m_w.dec();
	}

	@Override
	public void setIndentEnabled(boolean ind) {
		m_w.setIndentEnabled(ind);
	}

	@Override
	public boolean isIndentEnabled() {
		return m_w.isIndentEnabled();
	}

	@Override
	protected void println() throws IOException {
		m_w.println();
	}
}
