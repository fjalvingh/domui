package to.etc.binaries.cache;

import java.io.*;
import java.sql.*;
import java.util.*;

import to.etc.binaries.images.*;
import to.etc.dbutil.*;


public class BinariesCacheDAOImpl implements BinariesCacheDAO {
	//    static private final String[] CREATES = 
	//    {   "create table sys_binary(sbi_id number(16, 0) not null primary key,"
	//        +   "sbi_type varchar(10) not null,"
	//        +   "sbi_mimetype varchar(40) not null,"
	//        +   "sbi_hash varchar(32) null,"
	//        +   "sbi_bytesize number(10, 0) not null,"
	//        +   "sbi_original number(16, 0) null,"
	//        +   "sbi_pw number(5, 0) null,"
	//        +   "sbi_ph number(5, 0) null,"
	//        +   "sbi_data blob)"
	//    };

	/**
	 * Checks to see if a binary with the specified hash, type and size has already been loaded.
	 * @param dbc
	 * @param size
	 * @param hash
	 * @return
	 * @throws SQLException
	 */
	public long checkExisting(Connection dbc, long size, String type, String hash) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc.prepareStatement("select sbi_id from sys_binary where sbi_type=? and sbi_hash=? and sbi_bytesize=? and sbi_original is null");
			ps.setString(1, type);
			ps.setString(2, hash);
			ps.setLong(3, size);
			rs = ps.executeQuery();
			if(!rs.next())
				return -1;
			return rs.getLong(1);
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * @param dbc       The connection.
	 * @param mime      The mime type of the stored data. Required.
	 * @param type      The type of the stored data. Required.
	 * @param original  The ID of the original record linked to the new record. Use -1 for the original itself.
	 * @param hash      The hash of the stored data, if already known. If you pass null the hash gets calculated by reading the file.
	 * @param pw        The width of the thing, in pixels; if not applicable pass -1.
	 * @param ph        The height of the thing, in pixels; if not applicable pass -1.
	 * @param data      The file to store. Required.
	 * @return
	 * @throws Exception
	 */
	public long insertBinary(Connection dbc, String type, long original, String hash, ImageDataSource bds) throws Exception {
		boolean wasac = dbc.getAutoCommit();
		if(wasac)
			dbc.setAutoCommit(false);
		CallableStatement ps = null;
		try {
			//-- 1. Get a sequence# for the thingy.
			//            ps    = dbc.prepareStatement("select red_sbi_seq.nextval from dual");
			//            rs    = ps.executeQuery();
			//            if(! rs.next())
			//                throw new IllegalStateException("No result from sequence");
			//            long    id  = rs.getLong(1);
			//            rs.close();
			//            ps.close();

			ps = dbc.prepareCall("begin insert into sys_binary(sbi_id,sbi_mimetype,sbi_bytesize,sbi_type,sbi_original,sbi_hash,sbi_pw,sbi_ph,sbi_data)"
				+ " values(sys_binary_sq.nextval,?,?,?,?,?,?,?,empty_blob()) returning sbi_id into ?; end;");
			ps.setString(1, bds.getMime());
			ps.setLong(2, bds.getSize());
			ps.setString(3, type);
			ps.registerOutParameter(8, Types.NUMERIC);
			if(original == -1)
				ps.setNull(4, Types.NUMERIC);
			else
				ps.setLong(4, original);
			if(hash == null)
				ps.setNull(5, Types.VARCHAR);
			else
				ps.setString(5, hash);
			if(bds.getWidth() == -1 || bds.getHeight() == -1) {
				ps.setNull(6, Types.NUMERIC);
				ps.setNull(7, Types.NUMERIC);
			} else {
				ps.setInt(6, bds.getWidth());
				ps.setInt(7, bds.getHeight());
			}
			int rc = ps.executeUpdate();
			if(rc != 1)
				throw new SQLException("Failed to insert a sys_binary record: rc=" + rc);
			long pk = ps.getLong(8);
			ps.close();
			ps = null;
			InputStream is = bds.getInputStream();
			if(is != null) {
				try {
					GenericDB.setBlob(dbc, "sys_binary", "sbi_data", "sbi_id=" + pk, bds.getInputStream());
				} finally {
					try {
						is.close();
					} catch(Exception x) {}
				}
			}
			if(wasac)
				dbc.commit();
			return pk;
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(wasac)
					dbc.setAutoCommit(wasac);
			} catch(Exception x) {}
		}
	}

	public List<BinaryInfo> reloadInfo(Connection dbc, long originalid) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<BinaryInfo> list = new ArrayList<BinaryInfo>();
		try {

			ps = dbc.prepareStatement("select sbi_id,sbi_mimetype,sbi_bytesize,sbi_type,sbi_original,sbi_pw,sbi_ph" + " from sys_binary" + " where sbi_id = ? or sbi_original = ?");
			ps.setLong(1, originalid);
			ps.setLong(2, originalid);
			rs = ps.executeQuery();
			while(rs.next()) {
				long key = rs.getLong(1);
				String mime = rs.getString(2).intern();
				int size = rs.getInt(3);
				String type = rs.getString(4).intern();
				long orig = rs.getLong(5);
				if(rs.wasNull())
					orig = -1;
				int pw = rs.getInt(6);
				if(rs.wasNull())
					pw = -1;
				int ph = rs.getInt(7);
				if(rs.wasNull())
					ph = -1;
				BinaryInfo bi = new BinaryInfo(key, orig, pw, ph, size, mime, type);
				list.add(bi);
			}
			return list;
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	public PreparedStatement findRecord(Connection dbc, long pk) throws Exception {
		PreparedStatement ps = dbc.prepareStatement("select sbi_bytesize,sbi_data from sys_binary where sbi_id=?");
		ps.setLong(1, pk);
		return ps;
	}

}
