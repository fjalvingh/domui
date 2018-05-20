package to.etc.domuidemo.pages.binding.editabletable;

import to.etc.annotations.GenerateProperties;
import to.etc.domui.dom.css.VisibilityType;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-3-18.
 */
@GenerateProperties
public class Line {
	public static String pFROM = "from";
	public static String pTILL = "till";

	public static String pAMOUNTTYPE = "amountType";

	public static String pPERCENTAGE = "percentage";

	public static String pAMOUNT = "amount";

	public static String pDIVIDE = "divide";

	private Date m_from;

	private Date m_till;

	private BigDecimal m_amount;

	private AmountType m_amountType = AmountType.Amount;

	private BigDecimal m_percentage;

	private boolean m_divide;

	private LineController m_controller;

	private Object[] m_parameters;

	public Line() {
	}

	public Line(Date from, Date till, AmountType at, BigDecimal what) {
		m_from = from;
		m_till = till;
		m_amountType = at;
		if(at == AmountType.Amount)
			m_amount = what;
		else
			m_percentage = what;
	}

	public Date getFrom() {
		return m_from;
	}

	public void setFrom(Date from) {
		m_from = from;
	}

	public Date getTill() {
		return m_till;
	}

	public void setTill(Date till) {
		m_till = till;
	}

	public BigDecimal getAmount() {
		return m_amount;
	}

	public void setAmount(BigDecimal amount) {
		m_amount = amount;
	}

	public AmountType getAmountType() {
		return m_amountType;
	}

	public void setAmountType(AmountType amountType) {
		m_amountType = amountType;
	}

	public BigDecimal getPercentage() {
		return m_percentage;
	}

	public void setPercentage(BigDecimal percentage) {
		m_percentage = percentage;
	}

	public boolean isDivide() {
		return m_divide;
	}

	public void setDivide(boolean divide) {
		m_divide = divide;
	}

	public VisibilityType getPercentageVisible() {
		return getAmountType() == AmountType.Percentage ? VisibilityType.VISIBLE : VisibilityType.HIDDEN;
	}
	public VisibilityType getAmountVisible() {
		return getAmountType() == AmountType.Amount ? VisibilityType.VISIBLE : VisibilityType.HIDDEN;
	}

	public LineController getController() {
		return m_controller;
	}

	public void setController(LineController controller) {
		m_controller = controller;
	}

	public Object[] getParameters() {
		return m_parameters;
	}

	public void setParameters(Object[] parameters) {
		m_parameters = parameters;
	}
}
