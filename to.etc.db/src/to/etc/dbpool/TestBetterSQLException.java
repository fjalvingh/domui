package to.etc.dbpool;

import java.sql.*;

import org.junit.*;

public class TestBetterSQLException {
	@Test
	public void test() throws Exception {



		String message = new BetterSQLException(
			"update CRM_SUBJECTS set this._PSN_FIRSTNAME=  ?,PSN_TIJ_EINDDATUM=10123,PSN_TITLE='hkjh\"assa\" =jh kj',psn_suffix_title=?,CNY_VAT_NR=?,CST_WEBSITE=? where CST_ID=? ;", //
			new Object[]{"valdfa", "valasdsadsad", "val", null, "sklj"}, 5, new SQLException()).getMessage();

		Assert.assertEquals("java.sql.SQLException\n" + //
			"\n" + //
			"SQL: update CRM_SUBJECTS set this._PSN_FIRSTNAME=  ?,PSN_TIJ_EINDDATUM=10123,PSN_TITLE='hkjh\"assa\" =jh kj',psn_suffix_title=?,CNY_VAT_NR=?,CST_WEBSITE=? where CST_ID=? ;\n" + //
			"Parameters:\n" + //
			"#1  : java.lang.String : this._PSN_FIRSTNAME   = valdfa\n" + //
			"                         PSN_TIJ_EINDDATUM     = 10123\n" + //
			"                         PSN_TITLE             = 'hkjh\"assa\" =jh kj'\n" + //
			"#2  : java.lang.String : psn_suffix_title      = valasdsadsad\n" + //
			"#3  : java.lang.String : CNY_VAT_NR            = val\n" + //
			"#4  : [null]           : CST_WEBSITE           = [null]\n" + //
			"#5  : java.lang.String : CST_ID                = sklj", message);

		message = new BetterSQLException(
			"Insert into VIEWPOINT.FIN_BANKS (FBK_ID,TCN,ROS_ID,FBK_IDENTIFICATION,FBK_BANKNAME,FBK_CITYNAME,FBK_BIC,FBK_BLOCKED_YN,FBK_FILENAME,FBK_FILENAME_SEPA,LOG_USER,LOG_MODULE,LOG_DATE,LOG_TIME) values (100000021,1,1003,'1','UNIT_TEST','UNIT_TEST','UNIT_TEST','N',null,null,?,?,?,?);", //
			new Object[]{"valdfa", "valasdsadsad", "val", null}, 4, new SQLException()).getMessage();

		Assert.assertEquals("java.sql.SQLException\n" + //
					"\n"
					+ //
					"SQL: Insert into VIEWPOINT.FIN_BANKS (FBK_ID,TCN,ROS_ID,FBK_IDENTIFICATION,FBK_BANKNAME,FBK_CITYNAME,FBK_BIC,FBK_BLOCKED_YN,FBK_FILENAME,FBK_FILENAME_SEPA,LOG_USER,LOG_MODULE,LOG_DATE,LOG_TIME) values (100000021,1,1003,'1','UNIT_TEST','UNIT_TEST','UNIT_TEST','N',null,null,?,?,?,?);\n"
					+ //
					"Parameters:\n" + //
					"                         FBK_ID               = 100000021\n" + //
					"                         TCN                  = 1\n" + //
					"                         ROS_ID               = 1003\n" + //
					"                         FBK_IDENTIFICATION   = '1'\n" + //
					"                         FBK_BANKNAME         = 'UNIT_TEST'\n" + //
					"                         FBK_CITYNAME         = 'UNIT_TEST'\n" + //
					"                         FBK_BIC              = 'UNIT_TEST'\n" + //
					"                         FBK_BLOCKED_YN       = 'N'\n" + //
					"                         FBK_FILENAME         = null\n" + //
					"                         FBK_FILENAME_SEPA    = null\n" + //
					"#1  : java.lang.String : LOG_USER             = valdfa\n" + //
					"#2  : java.lang.String : LOG_MODULE           = valasdsadsad\n" + //
					"#3  : java.lang.String : LOG_DATE             = val\n" + //
					"#4  : [null]           : LOG_TIME             = [null]", message);


		message = new BetterSQLException("select aaa,bbb,dd,dddjjd as ggd from aaaaaaa.bbbbbbb ff where ff.ggg = 'aaa' and ff.kkk = ? ;", //
			new Object[]{"cccc"}, 1, new SQLException()).getMessage();

		Assert.assertEquals("java.sql.SQLException\n" + //
			"\n" + //
			"SQL: select aaa,bbb,dd,dddjjd as ggd from aaaaaaa.bbbbbbb ff where ff.ggg = 'aaa' and ff.kkk = ? ;\n" + //
			"From : aaaaaaa.bbbbbbb\n" + //
			"Parameters:\n" + //
			"                         ff.ggg   = 'aaa'\n" + //
			"#1  : java.lang.String : ff.kkk   = cccc", message);


		message = new BetterSQLException("delete from aaaaaaa.bbbbbbb ff where ff.ggg = 'aaa' and ff.kkk = ? ;", //
			new Object[]{"cccc"}, 1, new SQLException()).getMessage();
		Assert.assertEquals("java.sql.SQLException\n" + //
			"\n" + //
			"SQL: delete from aaaaaaa.bbbbbbb ff where ff.ggg = 'aaa' and ff.kkk = ? ;\n" + //
			"From : aaaaaaa.bbbbbbb\n" + //
			"Parameters:\n" + //
			"                         ff.ggg   = 'aaa'\n" + //
			"#1  : java.lang.String : ff.kkk   = cccc", message);

		message = new BetterSQLException(
			"Hibernate: select this_.BPB_ID as BPB1_157_1_, this_.LOG_DATE as LOG2_157_1_, this_.LOG_MODULE as LOG3_157_1_, this_.LOG_TIME as LOG4_157_1_, this_.LOG_USER as LOG5_157_1_, this_.TCN as TCN157_1_, this_.ROS_ID as ROS7_157_1_, this_.BAD_ID as BAD14_157_1_, this_.BPB_CODE as BPB8_157_1_, this_.BPB_INVOERBAAR as BPB9_157_1_, this_.BOP_ID as BOP15_157_1_, this_.BPB_OMSCHRIJVING as BPB10_157_1_, this_.BPB_SUBJECTIEF as BPB11_157_1_, this_.BPB_UNIEK as BPB12_157_1_, this_.BPB_VAST_OMS as BPB13_157_1_, chartanswe2_.BAD_ID as BAD1_46_0_, chartanswe2_.LOG_DATE as LOG2_46_0_, chartanswe2_.LOG_MODULE as LOG3_46_0_, chartanswe2_.LOG_TIME as LOG4_46_0_, chartanswe2_.LOG_USER as LOG5_46_0_, chartanswe2_.TCN as TCN46_0_, chartanswe2_.ROS_ID as ROS7_46_0_, chartanswe2_.BAD_CODE as BAD8_46_0_, chartanswe2_.BAD_DOMEIN as BAD9_46_0_, chartanswe2_.BAD_VAST as BAD10_46_0_, chartanswe2_.BAD_LENGTH as BAD11_46_0_, chartanswe2_.BAD_MAXVALUE as BAD12_46_0_, chartanswe2_.BAD_MINVALUE as BAD13_46_0_, chartanswe2_.BAD_WNW as BAD14_46_0_, chartanswe2_.BAD_SCALE as BAD15_46_0_, chartanswe2_.BAD_SOORT as BAD16_46_0_ from BAE_PUNT_BEPALINGEN this_, BAE_ANTWOORD_DOMEINEN chartanswe2_ where this_.BAD_ID=chartanswe2_.BAD_ID(+) and this_.BPB_CODE=? and this_.ROS_ID=?;", //
			new Object[]{"cccc", new Integer(38976)}, 2, new SQLException()).getMessage();


		Assert
			.assertEquals(
				"java.sql.SQLException\n"
					+ //
					"\n"
					+ //
					"SQL: Hibernate: select this_.BPB_ID as BPB1_157_1_, this_.LOG_DATE as LOG2_157_1_, this_.LOG_MODULE as LOG3_157_1_, this_.LOG_TIME as LOG4_157_1_, this_.LOG_USER as LOG5_157_1_, this_.TCN as TCN157_1_, this_.ROS_ID as ROS7_157_1_, this_.BAD_ID as BAD14_157_1_, this_.BPB_CODE as BPB8_157_1_, this_.BPB_INVOERBAAR as BPB9_157_1_, this_.BOP_ID as BOP15_157_1_, this_.BPB_OMSCHRIJVING as BPB10_157_1_, this_.BPB_SUBJECTIEF as BPB11_157_1_, this_.BPB_UNIEK as BPB12_157_1_, this_.BPB_VAST_OMS as BPB13_157_1_, chartanswe2_.BAD_ID as BAD1_46_0_, chartanswe2_.LOG_DATE as LOG2_46_0_, chartanswe2_.LOG_MODULE as LOG3_46_0_, chartanswe2_.LOG_TIME as LOG4_46_0_, chartanswe2_.LOG_USER as LOG5_46_0_, chartanswe2_.TCN as TCN46_0_, chartanswe2_.ROS_ID as ROS7_46_0_, chartanswe2_.BAD_CODE as BAD8_46_0_, chartanswe2_.BAD_DOMEIN as BAD9_46_0_, chartanswe2_.BAD_VAST as BAD10_46_0_, chartanswe2_.BAD_LENGTH as BAD11_46_0_, chartanswe2_.BAD_MAXVALUE as BAD12_46_0_, chartanswe2_.BAD_MINVALUE as BAD13_46_0_, chartanswe2_.BAD_WNW as BAD14_46_0_, chartanswe2_.BAD_SCALE as BAD15_46_0_, chartanswe2_.BAD_SOORT as BAD16_46_0_ from BAE_PUNT_BEPALINGEN this_, BAE_ANTWOORD_DOMEINEN chartanswe2_ where this_.BAD_ID=chartanswe2_.BAD_ID(+) and this_.BPB_CODE=? and this_.ROS_ID=?;\n"
					+ //
					"From : BAE_PUNT_BEPALINGEN\n" + //
					"Parameters:\n" + //
					"                          this_.BAD_ID     = chartanswe2_.BAD_ID\n" + //
					"#1  : java.lang.String  : this_.BPB_CODE   = cccc\n" + //
					"#2  : java.lang.Integer : this_.ROS_ID     = 38976", message);

		message = new BetterSQLException(
			"INSERT INTO V_RED_REFERENCE_CODES (RCE_NAME, RCE_VALUE) SELECT 'bei_codes_' || to_char(BET_ID),  substr(bei_omschrijving, 1, 2)  FROM bae_element_instanties  WHERE rce_id_bei_code is null;", //
			new Object[]{}, 0, new SQLException()).getMessage();

		//no parameters
		Assert
			.assertEquals(
				"java.sql.SQLException\n" + //
					"\n" + //
					"SQL: INSERT INTO V_RED_REFERENCE_CODES (RCE_NAME, RCE_VALUE) SELECT 'bei_codes_' || to_char(BET_ID),  substr(bei_omschrijving, 1, 2)  FROM bae_element_instanties  WHERE rce_id_bei_code is null;\n",
				message);


		message = new BetterSQLException(
			"INSERT INTO V_BAE_ELEMENT_INSTANTIES (BOT_ID, BET_ID, BEI_AANBRENGDATUM, BEI_EINDE_LEVENSDUUR, BEI_WAARDE_ALFA, BEI_WAARDE_DATE, BEI_WAARDE_NUM, BEI_WAARDE_DEC, BEI_OMSCHRIJVING) SELECT t.BOT_ID, t.BET_ID, t.BEW_DATUM_INGANG, add_months(t.BEW_DATUM_INGANG, ? * NVL(NEC_LEVENSDUUR_CYCLUS,50)), t.BEW_WAARDE_ALFA, t.BEW_WAARDE_DATE, t.BEW_WAARDE_NUM, NVL(t.BEW_WAARDE_DEC,t.BEW_WAARDE_NUM), t2.BET_ELEMENT FROM BAE_ELEMENT_WAARDEN t, BAE_ELEMENTEN t2, NPO_ELEMENTCODES t3 WHERE t.BET_ID = t2.BET_ID AND t2.BET_MJB = 'Y' AND t2.NEC_ID = t3.NEC_ID AND t.bew_datum_ingang = (SELECT MAX(bew_datum_ingang) from bae_element_waarden b where b.bot_id = t.bot_id and b.bet_id = t.bet_id) AND NOT EXISTS (SELECT 1 FROM BAE_ELEMENT_INSTANTIES WHERE BOT_ID=t.BOT_ID AND BET_ID=t.BET_ID);", //
			new Object[]{"12"}, 1, new SQLException()).getMessage();
		System.err.println(message);
		//too complex fail and fallback to old format
		Assert
			.assertEquals(
				"java.sql.SQLException\n"
					+ //
					"\n"
					+ //
					"SQL: INSERT INTO V_BAE_ELEMENT_INSTANTIES (BOT_ID, BET_ID, BEI_AANBRENGDATUM, BEI_EINDE_LEVENSDUUR, BEI_WAARDE_ALFA, BEI_WAARDE_DATE, BEI_WAARDE_NUM, BEI_WAARDE_DEC, BEI_OMSCHRIJVING) SELECT t.BOT_ID, t.BET_ID, t.BEW_DATUM_INGANG, add_months(t.BEW_DATUM_INGANG, ? * NVL(NEC_LEVENSDUUR_CYCLUS,50)), t.BEW_WAARDE_ALFA, t.BEW_WAARDE_DATE, t.BEW_WAARDE_NUM, NVL(t.BEW_WAARDE_DEC,t.BEW_WAARDE_NUM), t2.BET_ELEMENT FROM BAE_ELEMENT_WAARDEN t, BAE_ELEMENTEN t2, NPO_ELEMENTCODES t3 WHERE t.BET_ID = t2.BET_ID AND t2.BET_MJB = 'Y' AND t2.NEC_ID = t3.NEC_ID AND t.bew_datum_ingang = (SELECT MAX(bew_datum_ingang) from bae_element_waarden b where b.bot_id = t.bot_id and b.bet_id = t.bet_id) AND NOT EXISTS (SELECT 1 FROM BAE_ELEMENT_INSTANTIES WHERE BOT_ID=t.BOT_ID AND BET_ID=t.BET_ID);\n"
					+ //
					"Parameters:\n" + //
					"#1:java.lang.String:12\n", message);


	}

}


