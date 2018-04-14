package to.etc.domuidemo.pages.binding.editabletable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.annotations.GenerateProperties;
import to.etc.domui.databinding.observables.ObservableList;
import to.etc.util.DateUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-3-18.
 */
@GenerateProperties
@NonNullByDefault
public class LineController {
	private final ObservableList<Line> m_lineList = new ObservableList<>();

	private final List<Date> m_months;

	private BigDecimal m_budgeted;


	public LineController() {
		initializeDemoData();

		List<Date> res = new ArrayList<>();
		Calendar cal = Calendar.getInstance();

		DateUtil.clearTime(cal);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		for(int i = 0; i < 24; i++) {
			cal.add(Calendar.MONTH, -1);
			res.add(cal.getTime());
		}
		m_months = res;
		m_budgeted = new BigDecimal("876543.21");
	}

	private void initializeDemoData() {
		m_lineList.add(new Line(DateUtil.dateFor(2017, 0, 1), DateUtil.dateFor(2017, 2, 1), AmountType.Amount, new BigDecimal("1234.56")));
		m_lineList.add(new Line(DateUtil.dateFor(2017, 2, 1), DateUtil.dateFor(2017, 5, 1), AmountType.Amount, new BigDecimal("567.89")));
		m_lineList.add(new Line(DateUtil.dateFor(2017, 8, 1), DateUtil.dateFor(2018, 0, 1), AmountType.Percentage, new BigDecimal("12")));
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
	 * Total amount of money allocated so far (actually part of the model)
	 */
	public BigDecimal getTotal() {
		BigDecimal res = BigDecimal.ZERO;
		for(Line line : m_lineList) {
			switch(line.getAmountType()) {
				default:
					throw new IllegalStateException(line.getAmountType() + " unhandled");

				case Amount:
					BigDecimal amount = line.getAmount();
					if(null != amount)
						res = res.add(amount);
					break;

				case Percentage:
					BigDecimal percentage = line.getPercentage();
					if(null != percentage) {
						res = res.setScale(2, BigDecimal.ROUND_HALF_DOWN).add(getBudgeted().setScale(2, BigDecimal.ROUND_HALF_DOWN).multiply(percentage).setScale(2, BigDecimal.ROUND_HALF_DOWN).divide(new BigDecimal(100)));
					}
					break;
			}
		}
		return res;
	}

	/**
	 * Get the remainder of the money to spend (actually part of the model)
	 */
	public BigDecimal getRemainder() {
		return getBudgeted().subtract(getTotal());
	}

	/**
	 * Get the budgeted amount (actually part of the model)
	 */
	public BigDecimal getBudgeted() {
		return m_budgeted;
	}

	public void addEditRow() {
		m_lineList.add(new Line());
	}
}
