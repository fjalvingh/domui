// Jericho HTML Parser - Java based library for analysing and manipulating HTML
// Version 3.3
// Copyright (C) 2004-2009 Martin Jericho
// http://jericho.htmlparser.net/
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of either one of the following licences:
//
// 1. The Eclipse Public License (EPL) version 1.0,
// included in this distribution in the file licence-epl-1.0.html
// or available at http://www.eclipse.org/legal/epl-v10.html
//
// 2. The GNU Lesser General Public License (LGPL) version 2.1 or later,
// included in this distribution in the file licence-lgpl-2.1.txt
// or available at http://www.gnu.org/licenses/lgpl.txt
//
// This library is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the individual licence texts for more details.

package net.htmlparser.jericho;

import java.io.*;
import java.util.Queue;
import java.util.LinkedList;

class LoggerQueue implements Logger {
	private static final String ERROR="ERROR";
	private static final String WARN="WARN";
	private static final String INFO="INFO";
	private static final String DEBUG="DEBUG";

	private final Queue<String[]> queue=new LinkedList<String[]>();

	public void error(final String message) {
		queue.add(new String[] {ERROR,message});
	}

	public void warn(final String message) {
		queue.add(new String[] {WARN,message});
	}

	public void info(final String message) {
		queue.add(new String[] {INFO,message});
	}

	public void debug(final String message) {
		queue.add(new String[] {DEBUG,message});
	}

	public boolean isErrorEnabled() {
		return true;
	}

	public void setErrorEnabled(final boolean errorEnabled) {
		throw new UnsupportedOperationException();
	}

	public boolean isWarnEnabled() {
		return true;
	}

	public void setWarnEnabled(final boolean warnEnabled) {
		throw new UnsupportedOperationException();
	}

	public boolean isInfoEnabled() {
		return true;
	}

	public void setInfoEnabled(final boolean infoEnabled) {
		throw new UnsupportedOperationException();
	}

	public boolean isDebugEnabled() {
		return true;
	}

	public void setDebugEnabled(final boolean debugEnabled) {
		throw new UnsupportedOperationException();
	}

	public void outputTo(Logger logger) {
		while (true) {
			String[] item=queue.poll();
			if (item==null) return;
			String level=item[0];
			String message=item[1];
			if (level==ERROR)
				logger.error(message);
			else if (level==WARN)
				logger.warn(message);
			else if (level==INFO)
				logger.info(message);
			else if (level==DEBUG)
				logger.debug(message);
		}
	}
}
