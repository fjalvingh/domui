package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TH;
import to.etc.domui.dom.html.THead;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.util.Pair;
import to.etc.util.StringTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

/**
 * Recognizes a data table, and gives accessors for the column things inside it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21yhggg.
 */
final public class PogDataTable extends AbstractPoProxyGenerator {
	static private final RefType COLUMNCLASS = new RefType(PROXYPACKAGE, "CpDataTableColumn");

	static private final RefType ROWBASECLASS = new RefType(PROXYPACKAGE, "CpDataTableRowBase");

	static private final RefType SUPPLIER = new RefType(Supplier.class, "String");

	private List<Col> m_colList = new ArrayList<>();

	public PogDataTable(NodeBase node) {
		super(node);
	}

	@Override
	public void generateCode(PoGeneratorContext context, PoClass rc, String baseName, IPoSelector selector) throws Exception {
		//-- Generate a row class
		String rowClassName = context.getRootClass().getClassName() + baseName + "Row";
		PoClass rowClass = context.addClass(rowClassName, ROWBASECLASS, Collections.emptyList());

		//-- Generate the table class
		RefType baseClass = new RefType(PROXYPACKAGE, "CpDataTable", rowClass.asType().asTypeString());

		//-- Add a constructor to the row class
		PoMethod cons = rowClass.addConstructor();
		cons.addParameter(baseClass, "dt");
		cons.addParameter(RefType.INT, "rowIndex");
		cons.append("super(dt, rowIndex);").nl();

		//-- Generate the accessor in the provided class (the accessor to the CpDataTable
		String fieldName = PoGeneratorContext.fieldName(baseName);
		String methodName = PoGeneratorContext.methodName(baseName);

		PoField field = rc.addField(baseClass, fieldName);
		PoMethod getter = rc.addMethod(field.getType(), "get" + methodName);
		getter.appendLazyInit(field, variable -> {
			getter.append(variable).append(" = ").append("new ");
			getter.appendType(rc, field.getType()).append("(this.wd(), ").append(selector.selectorAsCode()).append(");").nl();
		});

		String tableClassName = context.getRootClass().getClassName() + baseName;
		PoClass tableClass = context.addClass(new PoClass(context.getRootClass().getPackageName(), tableClassName, baseClass));

		//-- And a constructor to the table class.
		cons = tableClass.addConstructor();
		cons.addParameter(PoGeneratorContext.WDCONNECTOR, "connector");
		cons.addParameter(SUPPLIER, "selectorProvider");
		cons.append("super(connector, selectorProvider);").nl();

		//-- Generate thingies per column
		for(int i = 0; i < m_colList.size(); i++) {
			Col col = m_colList.get(i);
			generateTableClassColumn(context, tableClass, col, i);
			generateRowClassColumn(context, rowClass, col, i);
		}
	}

	///**
	// * Create a base name in case the testid is null.
	// */
	//@NonNull
	//@Override
	//public String getProposedBaseName(PoGeneratorContext context, NodeBase node) {
	//	String baseName = m_baseName = m_node.getTestID() == null
	//		? "Tbl" + context.nextCounter()
	//		: context.getRootClass().getBaseName(m_node.getTestID());
	//	return baseName;
	//}

	/**
	 * Generate the column accessor(s) for a single column. The accessor accesses whatever is hidden in the cell.
	 * We can have multiple controls inside a column, and a separate accessor method will be generated for each of
	 * them as good as it gets.
	 */
	private void generateRowClassColumn(PoGeneratorContext context, PoClass pc, Col col, int index) throws Exception {
		String baseName = col.getColumnName();

		List<Pair<String, IPoProxyGenerator>> controlList = col.getContentModelList();
		if(controlList.size() == 0) {
			//-- No model -> we'll generate a text only model.
			IPoProxyGenerator px = PoGeneratorRegistry.getDisplayTextGenerator(context, m_node);	// FIXME Node is odd here
			px.generateCode(context, pc, baseName, new PoSelectorCell(index));
			return;
		}

		for(int i = 0; i < controlList.size(); i++) {
			Pair<String, IPoProxyGenerator> pair = controlList.get(i);
			IPoProxyGenerator pg = pair.get2();
			String testId = pair.get1();

			/*
			 * Calculate a name as follows:
			 * If we are the ONLY control in the cell: use the name of the column, nothing more (i.e. col2).
			 * If we are the nth but names are unique: use column name + control identifier (i.e. col2Button).
			 * If names are not unique then use column name + control identifier + sequence #.
			 */
			String controlBaseName;
			if(controlList.size() == 1) {
				controlBaseName = baseName;
			} else {
				controlBaseName = baseName + StringTool.strCapitalizedIntact(pc.getBaseName(testId));
			}

			generateCellControl(context, pc, col, index, pg, controlBaseName, testId);
		}
	}

	private void generateCellControl(PoGeneratorContext context, PoClass pc, Col col, int index, IPoProxyGenerator pg, String baseName, String testId) throws Exception {
		pg.generateCode(context, pc, baseName, new PoSelectorCellComponent(index, testId));
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


	/*----------------------------------------------------------------------*/
	/*	CODING:	Detection part - calculate how it looks						*/
	/*----------------------------------------------------------------------*/

	/**
	 * Detect the structure of the datatable, its columns, and what is in each column and row.
	 */
	@Override
	public GeneratorAccepted acceptChildren(PoGeneratorContext context) throws Exception {
		NodeContainer nc = (NodeContainer) m_node;
		List<Table> tables = nc.getChildren(Table.class);
		if(tables.size() == 0) {
			context.error(m_node, "The data table is empty; fill it to generate its content model");
			return GeneratorAccepted.RefusedIgnoreChildren;
		} else if(tables.size() > 1) {
			context.error(m_node, "?? > 1 table in data table??");
		}
		Table tbl = tables.get(0);

		List<THead> heads = tbl.getChildren(THead.class);
		if(heads.size() > 1) {
			context.error(m_node, "Too many THEAD items in DataTable");
		} else if(heads.size() == 0) {
			context.error(m_node, "No data in table, cannot really do anything");
			return GeneratorAccepted.RefusedIgnoreChildren;
		}
		THead head = heads.get(0);
		m_colList = scanColumnNames(context, head);
		if(m_colList.size() == 0) {
			context.error(m_node, "No columns recognized");
			return GeneratorAccepted.RefusedIgnoreChildren;
		}

		//-- Ok, we have columns; now try to calculate a content model for each column by looking at all rows.
		TBody body = tbl.getBody();

		//-- Scan the content model for each cell and collect them per column.
		boolean warned = false;
		for(TR row : body.getChildren(TR.class)) {
			List<TD> children = row.getChildren(TD.class);
			for(int i = 0; i < children.size(); i++) {
				TD td = children.get(i);

				if(i >= m_colList.size()) {
					//-- We have a td without a th -> bugger. For now error it and ignore.
					if(! warned) {
						warned = true;
						context.error(m_node, "Row has a column (index=" + i + ") for which we have no header - ignoring that column");
					}
				} else {
					scanContentModel(context, td, m_colList.get(i));
				}
			}
		}

		//-- Now, for each column, try to find out the final content model.
		for(Col col : m_colList) {
			calculateCellContentModel(context, col);
		}
		return GeneratorAccepted.Accepted;
	}

	/**
	 * This code assumes more or less the same content model in each cell, and assumes
	 * similar pg's would be the same in the cell. This is error prone, and probably should
	 * be merged on a better testid (one that does not change per row).
	 */
	private void calculateCellContentModel(PoGeneratorContext context, Col col) throws Exception {
		//-- Map of (real) testid and occurrences of a generator for it, in different rows
		Map<String, List<IPoProxyGenerator>> perTypeMap = new HashMap<>();

		boolean warned = false;
		for(List<NodeGeneratorPair> list : col.getPerRowContentList()) {
			for(NodeGeneratorPair pg : list) {
				String testID = pg.getNode().getTestID();
				if(null == testID) {
					if(! warned) {
						warned = true;
						context.error(m_node, "Column " + col + " has a control (" + pg.getNode() + ") without a testID - skipping");
					}
				} else {
					String realTestID = calculateActualTestID(testID);	// Remove any container info from the test ID
					perTypeMap.computeIfAbsent(realTestID, a -> new ArrayList<>()).add(pg.getGenerator());
				}
			}
		}

		//-- Use the actual models
		for(Entry<String, List<IPoProxyGenerator>> e : perTypeMap.entrySet()) {
			col.addContentModel(e.getKey(), e.getValue().get(0));
		}

		//if(perTypeMap.size() == 0) {
		//	//-- No content model found. Just get a "cell content" model using the row selector.
		//	col.addContentModel(PoGeneratorRegistry.getDisplayTextGenerator(context, m_node));		// FIXME Node is odd
		//} else {
		//	//-- Use the actual models
		//	for(Entry<String, List<IPoProxyGenerator>> e : perTypeMap.entrySet()) {
		//		col.addContentModel(e.getKey(), e.getValue().get(0));
		//	}
		//}
	}

	/**
	 * Scans the TD for components.
	 */
	private void scanContentModel(PoGeneratorContext context, TD td, Col col) throws Exception {
		List<NodeGeneratorPair> generators = context.createGenerators(td);
		col.addGeneratorSet(generators);
	}

	private List<Col> scanColumnNames(PoGeneratorContext context, THead head) {
		List<Col> colList = new ArrayList<>();

		for(TR tr : head.getChildren(TR.class)) {
			int currentHeadIndex = 0;
			for(TH th : tr.getChildren(TH.class)) {
				currentHeadIndex = handleTh(colList, th, currentHeadIndex);

			}
		}

		//-- We should now have all columns in the table, with their headers. Calculate column names and indices for each.
		for(int i = 0; i < colList.size(); i++) {
			Col col = colList.get(i);
			col.setColumnName(calculateColumnName(col, i));
			col.setIndex(i);
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

	@Override
	public String identifier() {
		return "DataTable";
	}

	/**
	 * If the test ID passed has a row identifier added to it: extract the test ID.
	 */
	static private String calculateActualTestID(@NonNull String testID) {
		int ix = testID.lastIndexOf('/');
		if(ix == -1)
			return testID;

		return testID.substring(ix + 1);
	}

	static private final class Col {
		private List<TH> m_header = new ArrayList<>(3);

		private String m_columnName;

		private List<List<NodeGeneratorPair>> m_perRowContentList = new ArrayList<>();

		private List<Pair<String, IPoProxyGenerator>> m_contentModelList = new ArrayList<>();

		private int m_index;

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

		public void addGeneratorSet(List<NodeGeneratorPair> generators) {
			m_perRowContentList.add(generators);
		}

		public List<List<NodeGeneratorPair>> getPerRowContentList() {
			return m_perRowContentList;
		}

		public void addContentModel(String testId, IPoProxyGenerator pg) {
			m_contentModelList.add(new Pair<>(testId, pg));
		}

		public List<Pair<String, IPoProxyGenerator>> getContentModelList() {
			return m_contentModelList;
		}

		public int getIndex() {
			return m_index;
		}

		public void setIndex(int index) {
			m_index = index;
		}

		@Override
		public String toString() {
			return m_columnName;
		}
	}
}
