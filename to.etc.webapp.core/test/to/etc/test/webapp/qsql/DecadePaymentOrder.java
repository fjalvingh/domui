package to.etc.test.webapp.qsql;

import java.util.*;

import to.etc.webapp.qsql.*;

/**
 * Mapping for decade view v_dec_betaalopdrachten.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Mar 22, 2010
 */
@QJdbcTable(table = "v_dec_betaalopdrachten")
public class DecadePaymentOrder {
	private DecadePaymentOrderPK m_id;

	private String m_bankAccount;

	public static final String BANK_ACCOUNT = "bankAccount";

	private double m_amount;

	public static final String AMOUNT = "amount";

	private String m_paymentDescription;

	public static final String PAYMENT_DESCRIPTION = "paymentDescription";

	private String m_description;

	public static final String DESCRIPTION = "description";

	private String m_decadeStatus;

	private Date m_valutaDate;

	public static final String VALUTA_DATE = "valutaDate";

	private String m_bankAccountRelation;

	public static final String BANK_ACCOUNT_RELATION = "bankAccountRelation";

	private String m_relationCode;

	public static final String RELATION_CODE = "relationCode";

	private String m_relationName;

	public static final String RELATION_NAME = "relationName";

	private Integer m_periodYear;

	public static final String PERIOD_YEAR = "periodYear";

	private Integer m_period;

	private Long m_bulkId;

	public static final String BULK_ID = "bulkId";

	@QJdbcId
	public DecadePaymentOrderPK getId() {
		return m_id;
	}

	public void setId(DecadePaymentOrderPK id) {
		m_id = id;
	}

	@QJdbcColumn(name = "bankrekening", length = 102)
	public String getBankAccount() {
		return m_bankAccount;
	}

	public void setBankAccount(String bankAccount) {
		this.m_bankAccount = bankAccount;
	}

	@QJdbcColumn(name = "bedrag", length = 15, scale = 3, nullable = false, columnConverter = DoubleType.class)
	public double getAmount() {
		return m_amount;
	}

	public void setAmount(double amount) {
		this.m_amount = amount;
	}

	@QJdbcColumn(name = "omschrijving", length = 2000, nullable = true)
	public String getDescription() {
		return m_description;
	}

	public void setDescription(String description) {
		this.m_description = description;
	}

	@QJdbcColumn(name = "betaalwijze", length = 40, nullable = true)
	public String getPaymentDescription() {
		return m_paymentDescription;
	}

	public void setPaymentDescription(String paymentDescription) {
		this.m_paymentDescription = paymentDescription;
	}

	@QJdbcColumn(name = "akst_id", length = 5, nullable = true)
	public String getDecadeStatus() {
		return m_decadeStatus;
	}

	public void setDecadeStatus(String decadeStatus) {
		m_decadeStatus = decadeStatus;
	}

	@QJdbcColumn(name = "valutadatum")
	public Date getValutaDate() {
		return m_valutaDate;
	}

	public void setValutaDate(Date valutaDate) {
		m_valutaDate = valutaDate;
	}

	@QJdbcColumn(name = "bankrek_relatie", length = 102, nullable = true)
	public String getBankAccountRelation() {
		return m_bankAccountRelation;
	}

	public void setBankAccountRelation(String bankAccountRelation) {
		m_bankAccountRelation = bankAccountRelation;
	}

	@QJdbcColumn(name = "relatiecode", length = 10, nullable = true)
	public String getRelationCode() {
		return m_relationCode;
	}

	public void setRelationCode(String relationCode) {
		m_relationCode = relationCode;
	}

	@QJdbcColumn(name = "relatie_naam", nullable = true)
	public String getRelationName() {
		return m_relationName;
	}

	public void setRelationName(String relationName) {
		m_relationName = relationName;
	}

	@QJdbcColumn(name = "jaar", length = 4, scale = 0, nullable = true)
	public Integer getPeriodYear() {
		return m_periodYear;
	}

	public void setPeriodYear(Integer periodYear) {
		m_periodYear = periodYear;
	}

	@QJdbcColumn(name = "periode", length = 2, scale = 0, nullable = true)
	public Integer getPeriod() {
		return m_period;
	}

	public void setPeriod(Integer period) {
		m_period = period;
	}

	@QJdbcColumn(name = "vzop_id", nullable = true)
	public Long getBulkId() {
		return m_bulkId;
	}

	public void setBulkId(Long bulkId) {
		m_bulkId = bulkId;
	}
}
