package to.etc.domui.util.db;

import com.google.common.collect.Iterables;
import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.webapp.qsql.JdbcUtil;
import to.etc.webapp.query.IIdentifyable;
import to.etc.webapp.query.QField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final public class BatchUtil {

	private BatchUtil() {
	}

	/**
	 * Does bulk delete using JDBC.
	 * Use it when performance matters ;)
	 */
	public static <K, T extends IIdentifyable<K>> int bulkDelete(@NonNull Connection con, ClassMetaModel cmm, @NonNull List<T> items) throws SQLException {
		List<K> ids = items.stream().map(it -> it.getSafeId()).collect(Collectors.toList());
		return bulkDelete(con, cmm, false, ids);
	}

	/**
	 * Does bulk delete using JDBC.
	 * In case that 'except' is T, it deletes 'all other' records -> that can throw exception in case that specified list of 'except' ids is too large -> depends on database.
	 * Otherwise it deletes specified records using smaller chunks of records, so it is safe regarding database limits in statement length.
	 * Use it when performance matters ;)
	 */
	public static <K> int bulkDelete(@NonNull Connection con, ClassMetaModel cmm, boolean except, @NonNull List<K> ids) throws SQLException {
		if(ids.isEmpty() && !except) {
			return 0;
		}
		final PropertyMetaModel<?> pkPmm = cmm.getPrimaryKey();
		if(null == pkPmm) {
			throw new IllegalArgumentException("Found no PK for " + cmm.getTableName() + " !");
		}
		String[] pkCols = pkPmm.getColumnNames();
		if(pkCols.length != 1) {
			throw new IllegalArgumentException("Table: " + cmm.getTableName() + " has composite PK: " + pkCols + ". Composite PK is not supported!");
		}
		String pkCol = pkCols[0];
		int maxChunk = pkPmm.getActualType().isAssignableFrom(String.class)
			? 100
			: 1000;
		int chunkSize = except ? ids.size() : maxChunk;
		int result = 0;
		for(List<K> idsChunk : Iterables.partition(ids, chunkSize)) {
			String idsPlaceholders = idsChunk.stream().map(it -> "?").collect(Collectors.joining(","));
			Object[] idsValues = idsChunk.toArray(new Object[idsChunk.size()]);
			String notPart = except ? "not " : "";
			result = result + JdbcUtil.executeUpdate(con, "delete from " + cmm.getTableName() + " where " + notPart + pkCol + " in (" + idsPlaceholders + ")", idsValues);
		}
		return result;
	}

	/**
	 * Does bulk insert using JDBC. Use it when performance matters ;)
	 */
	public static <K, T extends IIdentifyable<K>> int bulkInsert(@NonNull Connection con, ClassMetaModel cmm, @NonNull List<T> items, QField<T, ?>... fields) throws Exception {
		if(items.isEmpty()) {
			return 0;
		}
		PropertyMetaModel<?>[] props = new PropertyMetaModel<?>[fields.length];
		Arrays.stream(fields).map(it -> cmm.getProperty(it)).collect(Collectors.toList()).toArray(props);

		String insertColumns = Arrays.stream(props).map(it -> it.getColumnNames()[0]).collect(Collectors.joining(","));
		String params = Arrays.stream(props).map(it -> "?").collect(Collectors.joining(","));
		String sql = "insert into " + cmm.getTableName() + " (" + insertColumns + ") values (" + params + ")";
		int inserted = 0;
		try(PreparedStatement ps = con.prepareStatement(sql)) {
			for(List<T> chunk : Iterables.partition(items, 10000)) {
				for(T item : chunk) {
					for(int index = 0; index < props.length; index++) {
						Object value = props[index].getValue(item);
						if(value instanceof IIdentifyable<?>) {
							value = ((IIdentifyable<?>) value).getId();
						}
						JdbcUtil.setParameter(ps, value, index + 1);
					}
					ps.addBatch();
				}
				inserted += ps.executeBatch().length;
				ps.clearBatch();
			}
		}
		return inserted;
	}

}
