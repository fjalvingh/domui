package to.etc.domui.component.lookup.filter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Node;
import to.etc.util.DateUtil;
import to.etc.webapp.query.QDataContext;
import to.etc.xml.DomTools;
import to.etc.xml.XmlWriter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Translates a DateFromTo value to XML and vice versa
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/8/16.
 */
@NonNullByDefault
final class DateFromToTranslator implements ITranslator<DateFromTo> {

	private static final String DATE_FROM = "dateFrom";

	private static final String DATE_TO = "dateTo";

	private final SimpleDateFormat m_simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

	@Nullable
	@Override
	public DateFromTo deserialize(QDataContext dc, Node node) throws Exception {

		Node valueNode = DomTools.nodeFind(node, VALUE);
		Node dateFromNode = DomTools.nodeFind(valueNode, DATE_FROM);
		String df = DomTools.textFrom(dateFromNode);
		Node dateToNode = DomTools.nodeFind(valueNode, DATE_TO);
		String dt = DomTools.textFrom(dateToNode);

		Date dateFrom = null;
		if(df != null) {
			dateFrom = DateUtil.convertToDate(df, m_simpleDateFormat.toPattern());
		}

		Date dateTo = null;
		if(dt != null) {
			dateTo = DateUtil.convertToDate(dt, m_simpleDateFormat.toPattern());
		}
		return new DateFromTo(dateFrom, dateTo);
	}

	@Override
	public boolean serialize(XmlWriter writer, Object o) throws Exception {
		if(o instanceof DateFromTo) {
			String dateFrom = null;
			String dateTo = null;

			Date df = ((DateFromTo) o).getDateFrom();
			if(df != null) {
				dateFrom = m_simpleDateFormat.format(df);
			}

			Date dt = ((DateFromTo) o).getDateTo();
			if(dt != null) {
				dateTo = m_simpleDateFormat.format(dt);
			}

			writer.tag(VALUE);
			writer.tagfull(DATE_FROM, dateFrom);
			writer.tagfull(DATE_TO, dateTo);
			writer.tagendnl();
			return true;
		}
		return false;
	}
}
