package to.etc.domuidemo.pages.binding.editabletable;

import to.etc.domui.databinding.observables.ObservableList;
import to.etc.util.DateUtil;

import javax.annotation.DefaultNonNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-3-18.
 */
@DefaultNonNull
public class LineController {
	private final ObservableList<Line> m_lineList = new ObservableList<>();

	private final List<Date> m_months;

	private BigDecimal m_budgetted;

	public LineController() {
		initializeDemoData();

		List<Date> res = new ArrayList<>();
		Calendar cal = Calendar.getInstance();

		DateUtil.clearTime(cal);
		for(int i = 0; i < 24; i++) {
			cal.add(Calendar.MONTH, -1);
			res.add(cal.getTime());
		}
		m_months = res;

		m_budgetted = new BigDecimal("876543.21");
	}

	private void initializeDemoData() {
		m_lineList.add(new Line(DateUtil.dateFor(2017, 0, 1), DateUtil.dateFor(2017, 2, 1), AmountType.Amount, new BigDecimal("1234.56")));
		m_lineList.add(new Line(DateUtil.dateFor(2017, 2, 1), DateUtil.dateFor(2017, 5, 1), AmountType.Amount, new BigDecimal("567.89")));
		m_lineList.add(new Line(DateUtil.dateFor(2017, 2, 1), DateUtil.dateFor(2017, 5, 1), AmountType.Percentage, new BigDecimal("12")));
	}

	public ObservableList<Line> getLineList() {
		return m_lineList;
	}

	public boolean isReadOnly() {
		return false;
	}

	public List<Date> getProjectMonths() {
		return m_months;
	}

	public void delete(Line object) {
		m_lineList.remove(object);
	}

	/**
	 * Total amount of money allocated so far
	 */
	public BigDecimal getTotal() {
		BigDecimal res = BigDecimal.ZERO;
		for(Line line : m_lineList) {
			switch(line.getAmountType()) {
				default:
					throw new IllegalStateException(line.getAmountType() + " unhandled");

				case Amount:
					res = res.add(line.getAmount());
					break;

				case Percentage:
					res = res.setScale(2).add(getBudgetted().multiply(line.getPercentage()).divide(new BigDecimal(100)));
					break;
			}
		}
		return res;
	}

	public BigDecimal getRemainder() {
		return getBudgetted().subtract(getTotal());
	}

	public BigDecimal getBudgetted() {
		return m_budgetted;
	}
}
