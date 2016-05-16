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
package to.etc.dbpool.info;

import java.util.*;

final public class PerformanceCollector extends PerformanceStore implements IPerformanceCollector {
	static private final String SQL_EXEC_TIME = "stmt-exec-time";

	static private final String SQL_EXEC_COUNT = "stmt-exec-count";

	static private final String SQL_ROW_COUNT = "stmt-row-count";

	static private final String SQL_FETCH_TIME = "stmt-fetch-time";

	static private final String SQL_TOTAL_TIME = "stmt-total-time";

	static private final Comparator<StatementStatistics> C_BYEXEC = new Comparator<StatementStatistics>() {
		@Override
		public int compare(StatementStatistics a, StatementStatistics b) {
			return b.getExecutions() - a.getExecutions();
		}
	};

	static private final Comparator<StatementStatistics> C_BYROWS = new Comparator<StatementStatistics>() {
		@Override
		public int compare(StatementStatistics a, StatementStatistics b) {
			long r = a.getRows() - b.getRows();
			if(r == 0)
				return 0;
			return r > 0 ? -1 : 1;
		}
	};

	static private final Comparator<StatementStatistics> C_BYFETCH = new Comparator<StatementStatistics>() {
		@Override
		public int compare(StatementStatistics a, StatementStatistics b) {
			long r = a.getTotalFetchDuration() - b.getTotalFetchDuration();
			if(r == 0)
				return 0;
			return r > 0 ? -1 : 1;
		}
	};

	static private final Comparator<StatementStatistics> C_BYEXEC_TIME = new Comparator<StatementStatistics>() {
		@Override
		public int compare(StatementStatistics a, StatementStatistics b) {
			long v = a.getTotalExecuteDuration() - b.getTotalExecuteDuration();
			return v == 0 ? 0 : v < 0 ? 1 : -1;
		}
	};

	static private final Comparator<StatementStatistics> C_BYTOTAL_TIME = new Comparator<StatementStatistics>() {
		@Override
		public int compare(StatementStatistics a, StatementStatistics b) {
			long at = a.getTotalExecuteDuration() + a.getTotalFetchDuration();
			long bt = b.getTotalExecuteDuration() + b.getTotalFetchDuration();

			long v = at - bt;
			return v == 0 ? 0 : v < 0 ? 1 : -1;
		}
	};


	public PerformanceCollector() {
		//-- Define all store lists.
		define(SQL_TOTAL_TIME, "SQL: total time (execute+fetch) per statement", true, 20);
		define(SQL_EXEC_TIME, "SQL: longest statement execution time", true, 20);
		define(SQL_EXEC_COUNT, "SQL: statements executed most", true, 20);
		define(SQL_ROW_COUNT, "SQL: statements returning/altering largest number of rows", true, 20);
		define(SQL_FETCH_TIME, "SQL: statements with the largest result set fetch time (time till rs.close is called)", true, 20);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IPerformanceCollector code.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Save the max executed statements.
	 * @param counterList
	 */
	@Override
	public void saveCounters(String request, List<StatementStatistics> counterList) {
		Collections.sort(counterList, C_BYEXEC);
		int max = 20;
		if(counterList.size() < max)
			max = counterList.size();
		for(int i = 0; i < max; i++) {
			StatementStatistics sc = counterList.get(i);
			addItem(SQL_EXEC_COUNT, sc.getSQL(), sc.getExecutions(), request, sc);
		}

		//-- By rows altered/returned
		Collections.sort(counterList, C_BYROWS);
		max = 20;
		if(counterList.size() < max)
			max = counterList.size();
		for(int i = 0; i < max; i++) {
			StatementStatistics sc = counterList.get(i);
			addItem(SQL_ROW_COUNT, sc.getSQL(), sc.getRows(), request, sc);
		}

		//-- By fetch time
		Collections.sort(counterList, C_BYFETCH);
		max = 20;
		if(counterList.size() < max)
			max = counterList.size();
		for(int i = 0; i < max; i++) {
			StatementStatistics sc = counterList.get(i);
			addItem(SQL_FETCH_TIME, sc.getSQL(), sc.getTotalFetchDuration(), request, sc);
		}

		//-- By execution time
		Collections.sort(counterList, C_BYEXEC_TIME);
		max = 20;
		if(counterList.size() < max)
			max = counterList.size();
		for(int i = 0; i < max; i++) {
			StatementStatistics sc = counterList.get(i);
			addItem(SQL_EXEC_TIME, sc.getSQL(), sc.getTotalExecuteDuration(), request, sc);
		}

		//-- By total time
		Collections.sort(counterList, C_BYTOTAL_TIME);
		max = 20;
		if(counterList.size() < max)
			max = counterList.size();
		for(int i = 0; i < max; i++) {
			StatementStatistics sc = counterList.get(i);
			addItem(SQL_TOTAL_TIME, sc.getSQL(), sc.getTotalExecuteDuration() + sc.getTotalFetchDuration(), request, sc);
		}
	}
}
