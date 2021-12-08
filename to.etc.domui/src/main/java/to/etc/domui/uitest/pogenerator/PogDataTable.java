package to.etc.domui.uitest.pogenerator;

import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TH;
import to.etc.domui.dom.html.THead;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recognizes a data table, and gives accessors for the column things inside it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21yhggg.
 */
final public class PogDataTable extends AbstractPoProxyGenerator implements IPoAcceptNullTestid {
	static private final PoClass TABLEBASE = new PoClass(PROXYPACKAGE, "CpDataTableBase");

	static private final Pair<String, String> COLUMNCLASS = new Pair<>(PROXYPACKAGE, "CpDataTableColumn");

	private List<Col> m_colList = new ArrayList<>();

	private String m_baseName;

	public PogDataTable(NodeBase node) {
		super(node);
	}

	@Override
	public void generateCode(PoGeneratorContext context) throws Exception {
		String baseName = m_baseName = m_node.getTestID() == null
			? "Tbl" + context.nextCounter()
			: context.getRootClass().getBaseName(m_node.getTestID());

		//-- Generate the table class
		String tableClassName = context.getRootClass().getClassName() + m_baseName;
		PoClass tableClass = context.addClass(new PoClass(context.getRootClass().getPackageName(), tableClassName, TABLEBASE));

		//-- Generate a row class
		String rowClassName = context.getRootClass().getClassName() + m_baseName;
		PoClass rowClass = context.addClass(rowClassName, null, Collections.emptyList());

		//-- Generate thingies per column
		for(int i = 0; i < m_colList.size(); i++) {
			Col col = m_colList.get(i);
			generateTableClassColumn(context, tableClass, col, i);

		}
	}

	/**
	 * Generates a column accessor in the class, i.e. column[name], returning the general things that can be done with a column.
	 */
	private void generateTableClassColumn(PoGeneratorContext context, PoClass pc, Col col, int index) throws Exception {
		String fieldName = PoGeneratorContext.fieldName(col.getColumnName());
		String methodName = PoGeneratorContext.methodName(col.getColumnName());

		PoField field = pc.addField(COLUMNCLASS, fieldName);
		PoMethod getter = pc.addMethod(field.getType(), methodName);
		getter.appendLazyInit(field, variable -> {
			getter.append(variable).append(" = ").append("new ");
			getter.appendType(pc, field.getType()).append("(this, ").append(Integer.toString(index)).append(");").nl();
		});
	}

	@Override
	public boolean acceptChildren(PoGeneratorContext context) throws Exception {
		NodeContainer nc = (NodeContainer) m_node;
		List<Table> tables = nc.getChildren(Table.class);
		if(tables.size() == 0) {
			context.error("The data table is empty; fill it to generate its content model");
			return false;
		} else if(tables.size() > 1) {
			context.error("?? > 1 table in data table??");
		}
		Table tbl = tables.get(0);

		List<THead> heads = tbl.getChildren(THead.class);
		if(heads.size() > 1) {
			context.error("Too many THEAD items in DataTable");
		} else if(heads.size() == 0) {
			context.error("No data in table, cannot really do anything");
			return false;
		}
		THead head = heads.get(0);
		m_colList = scanColumnNames(context, head);

		return true;
	}

	private List<Col> scanColumnNames(PoGeneratorContext context, THead head) {
		List<Col> colList = new ArrayList<>();

		for(TR tr : head.getChildren(TR.class)) {
			int currentHeadIndex = 0;
			for(TH th : tr.getChildren(TH.class)) {
				currentHeadIndex = handleTh(colList, th, currentHeadIndex);

			}
		}

		//-- We should now have all columns in the table, with their headers. Calculate column names for each.
		for(int i = 0; i < colList.size(); i++) {
			Col col = colList.get(i);
			col.setColumnName(calculateColumnName(col, i));
		}
		return colList;



	}

	private String calculateColumnName(Col col, int index) {
		StringBuilder sb = new StringBuilder();
		for(TH th : col.getHeader()) {
			String tc = th.getTextOnly();
			if(null != tc && tc.length() > 0) {
				sb.append(tc);
			}
		}
		if(sb.length() > 2) {
			return PoGeneratorContext.makeName(sb.toString());
		}
		return "column" + (index + 1);
	}

	private int handleTh(List<Col> colList, TH th, int currentHeadIndex) {
		int colspan = th.getColspan();
		if(colspan <= 0)
			colspan = 1;

		for(int i = 0; i < colspan; i++) {
			while(colList.size() <= currentHeadIndex) {
				colList.add(new Col());
			}
			Col c = colList.get(currentHeadIndex);
			c.add(th);
			currentHeadIndex++;
		}
		return currentHeadIndex;
	}

	static private final class Col {
		private List<TH> m_header = new ArrayList<>(3);

		private String m_columnName;

		public void add(TH th) {
			m_header.add(th);
		}

		public String getColumnName() {
			return m_columnName;
		}

		public void setColumnName(String columnName) {
			m_columnName = columnName;
		}

		public List<TH> getHeader() {
			return m_header;
		}
	}
}
