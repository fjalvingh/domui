package to.etc.dbpool;

import java.sql.*;

import org.junit.*;

public class TestBetterSQLException {
	@Test
	public void test() throws Exception {



		String message = new BetterSQLException(
			"update CRM_SUBJECTS set this._PSN_FIRSTNAME=  ?,PSN_TIJ_EINDDATUM=10123,PSN_TITLE='hkjh\"assa\" =jh kj',psn_suffix_title=?,CNY_VAT_NR=?,CST_WEBSITE=? where CST_ID=? ;", //
			new Object[]{"valdfa", "valasdsadsad", "val", null, "sklj"}, 5, new SQLException()).getMessage();
		System.err.println(message);


//		Assert.assertEquals("java.sql.SQLException\n" + //
//			"\n" + //
//			"SQL: update CRM_SUBJECTS set this._PSN_FIRSTNAME=  ?,PSN_TIJ_EINDDATUM=10123,PSN_TITLE='hkjh\"assa\" =jh kj',psn_suffix_title=?,CNY_VAT_NR=?,CST_WEBSITE=? where CST_ID=? ;\n" + //
//			"Parameters:\n" + //
//			"#1 : java.lang.String : this._PSN_FIRSTNAME   = valdfa\n" + //
//			"                        PSN_TIJ_EINDDATUM     = 10123\n" + //
//			"                        PSN_TITLE             = hkjh\"assa\" =jh kj\n" + //
//			"#2 : java.lang.String : psn_suffix_title      = valasdsadsad\n" + //
//			"#3 : java.lang.String : CNY_VAT_NR            = val\n" + //
//			"#4 : [null]           : CST_WEBSITE           = [null]\n" + //
//			"#5 : java.lang.String : CST_ID                = sklj", message);

		message = new BetterSQLException(
			"Insert into VIEWPOINT.FIN_BANKS (FBK_ID,TCN,ROS_ID,FBK_IDENTIFICATION,FBK_BANKNAME,FBK_CITYNAME,FBK_BIC,FBK_BLOCKED_YN,FBK_FILENAME,FBK_FILENAME_SEPA,LOG_USER,LOG_MODULE,LOG_DATE,LOG_TIME) values (100000021,1,1003,'1','UNIT_TEST','UNIT_TEST','UNIT_TEST','N',null,null,?,?,?,?);", //
			new Object[]{"valdfa", "valasdsadsad", "val", null}, 4, new SQLException()).getMessage();
		System.err.println(message);

		message = new BetterSQLException("select aaa,bbb,dd,dddjjd as ggd from aaaaaaa.bbbbbbb ff where ff.ggg = 'aaa' and ff.kkk = ? ;", //
			new Object[]{"cccc"}, 1, new SQLException()).getMessage();
		System.err.println(message);

		message = new BetterSQLException("delete from aaaaaaa.bbbbbbb ff where ff.ggg = 'aaa' and ff.kkk = ? ;", //
			new Object[]{"cccc"}, 1, new SQLException()).getMessage();
		System.err.println(message);

		message = new BetterSQLException(
			"Hibernate: select this_.BPB_ID as BPB1_157_1_, this_.LOG_DATE as LOG2_157_1_, this_.LOG_MODULE as LOG3_157_1_, this_.LOG_TIME as LOG4_157_1_, this_.LOG_USER as LOG5_157_1_, this_.TCN as TCN157_1_, this_.ROS_ID as ROS7_157_1_, this_.BAD_ID as BAD14_157_1_, this_.BPB_CODE as BPB8_157_1_, this_.BPB_INVOERBAAR as BPB9_157_1_, this_.BOP_ID as BOP15_157_1_, this_.BPB_OMSCHRIJVING as BPB10_157_1_, this_.BPB_SUBJECTIEF as BPB11_157_1_, this_.BPB_UNIEK as BPB12_157_1_, this_.BPB_VAST_OMS as BPB13_157_1_, chartanswe2_.BAD_ID as BAD1_46_0_, chartanswe2_.LOG_DATE as LOG2_46_0_, chartanswe2_.LOG_MODULE as LOG3_46_0_, chartanswe2_.LOG_TIME as LOG4_46_0_, chartanswe2_.LOG_USER as LOG5_46_0_, chartanswe2_.TCN as TCN46_0_, chartanswe2_.ROS_ID as ROS7_46_0_, chartanswe2_.BAD_CODE as BAD8_46_0_, chartanswe2_.BAD_DOMEIN as BAD9_46_0_, chartanswe2_.BAD_VAST as BAD10_46_0_, chartanswe2_.BAD_LENGTH as BAD11_46_0_, chartanswe2_.BAD_MAXVALUE as BAD12_46_0_, chartanswe2_.BAD_MINVALUE as BAD13_46_0_, chartanswe2_.BAD_WNW as BAD14_46_0_, chartanswe2_.BAD_SCALE as BAD15_46_0_, chartanswe2_.BAD_SOORT as BAD16_46_0_ from BAE_PUNT_BEPALINGEN this_, BAE_ANTWOORD_DOMEINEN chartanswe2_ where this_.BAD_ID=chartanswe2_.BAD_ID(+) and this_.BPB_CODE=? and this_.ROS_ID=?;", //
			new Object[]{"cccc", new Integer(38976)}, 3, new SQLException()).getMessage();
		System.err.println(message);


//		Assert.assertEquals("", message);

	}

}


