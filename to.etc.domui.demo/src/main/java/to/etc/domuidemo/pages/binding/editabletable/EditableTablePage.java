package to.etc.domuidemo.pages.binding.editabletable;

import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.input.Text;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.IRowControlFactory;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.converter.MaxMinValidator;
import to.etc.domui.converter.MoneyBigDecimalFullConverter;
import to.etc.domui.converter.MoneyBigDecimalNoSign;
import to.etc.domui.dom.css.TextAlign;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.INodeContentRenderer;
import to.etc.domui.util.IRenderInto;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-3-18.
 */
public class EditableTablePage extends UrlPage {
	/**
	 * We just add the demo controller here; usually this is somehow obtained elsewhere.
	 */
	private LineController m_controller = new LineController();

	private DataTable<Line> m_dataTable;

	@Override public void createContent() throws Exception {
		add(new HTag(1, "Editable table using data binding and behaviors, example 1"));

		RowRenderer<Line> rr = createRowRenderer();
		DataTable<Line> dataTable = m_dataTable = new DataTable<>(rr);
		dataTable.setList(model().getLineList());

		//m_simpleListModel.setComparator(Comparator.comparing(Line::getPeriodeVan).thenComparing(Line::getPeriodeTm));
		dataTable.setPreventRowHighlight(true);
		add(dataTable);
		addTotalsRow();

		add(new VerticalSpacer(10));
		add(new LinkButton("Add Row", FaIcon.faPlus, a -> model().addEditRow()));
	}

	private RowRenderer<Line> createRowRenderer() {
		RowRenderer<Line> rr = new RowRenderer<>(Line.class);
		rr.column().label("Period from").renderer(createMonthRenderer(Line.pFROM));
		rr.column().label("Period till").renderer(createMonthRenderer(Line.pTILL));
		rr.column(Line.pAMOUNTTYPE).editable().factory(createAmountTypeControlFactory());
		rr.column(Line.pPERCENTAGE).editable().factory(createPercentageControlFactory());
		rr.column(Line.pAMOUNT).editable().factory(createAmountControlFactory());
		rr.column().label("Divide").renderer(createDivideRenderer());
		if(!model().isReadOnly()) {
			rr.column().renderer(createRemoveRenderer()).width("1%").nowrap();
		}
		return rr;
	}

	private void addTotalsRow() throws Exception {
		TBody body = DomUtil.nullChecked(m_dataTable).getFooterBody();
		addTotaal(body);
		addBegroting(body);
		addRestant(body);
	}

	private void addTotaal(TBody body) throws Exception {
		Text2<BigDecimal> totaalCtrl = createBigDecimalControl();
		totaalCtrl.bind().to(model(), "total");
		TR tr = addControlToBody(body, totaalCtrl, "Total amount");
		//tr.addCssClass(CssVpDomui.VP_BORDER_TOP.toString());
	}

	private void addBegroting(TBody body) throws Exception {
		Text2<BigDecimal> begrotingCtrl = createBigDecimalControl();
		begrotingCtrl.bind().to(model(), "budgetted");
		addControlToBody(body, begrotingCtrl, "Budgetted amount");
	}

	private void addRestant(TBody body) throws Exception {
		Text2<BigDecimal> restantCtrl = createBigDecimalControl();
		restantCtrl.bind().to(model(), "remainder");
		addControlToBody(body, restantCtrl, "Total remaining");
	}

	private Text2<BigDecimal> createBigDecimalControl() {
		Text2<BigDecimal> bedragCtrl = new Text2<>(BigDecimal.class);
		bedragCtrl.setReadOnly(true);
		bedragCtrl.setConverter(new MoneyBigDecimalFullConverter());
		bedragCtrl.setTextAlign(TextAlign.RIGHT);
		return bedragCtrl;
	}

	private TR addControlToBody(TBody body, NodeBase begrotingCtrl, String label) {
		TR tr = body.addRow();
		TD td = tr.addCell();
		td.setCssClass("ui-f-lbl");
		td.setColspan(4);
		td.add(label);
		TD td2 = tr.addCell();
		td2.setColspan(3);
		td2.add(begrotingCtrl);
		return tr;
	}


	@Nonnull
	private INodeContentRenderer<Line> createRemoveRenderer() {
		return (component, node, object, parameters) -> {
			if(null == object) {
				return;
			}
			LinkButton remove = new LinkButton("Remove", FaIcon.faTimes, clickedNode -> model().delete(object));
			node.add(remove);
			remove.setTitle("A completely useless and insultingly stupid explanation, because of course a word like remove is not blindingly obvious. But of course we have some idiots that want to explain the obvious, leaving no time nor any want to explain what SHOULD be explained.");
		};
	}

	private IRenderInto<Line> createDivideRenderer() {
		return (node, object) -> {
			Checkbox cb = new Checkbox();
			cb.bind().to(object, Line.pDIVIDE);
			cb.bind("readOnly").to(model(), "readOnly");
			node.add(cb);
		};
	}

	private IRowControlFactory<Line> createAmountControlFactory() {
		return row -> {
			Text<BigDecimal> ctrl = new Text<>(BigDecimal.class);
			ctrl.setConverter(new MoneyBigDecimalFullConverter());
			ctrl.setErrorLocation("Bedrag");
			ctrl.setCssClass("ui-numeric");
			ctrl.bind("readOnly").to(model(), "readOnly");
			ctrl.addValidator(new MaxMinValidator(new BigDecimal("-999999999.99"), new BigDecimal("999999999.99")));
			//ctrl.immediate();

			//ctrl.setOnValueChanged((IValueChanged<Text<BigDecimal>>) component -> {
			//	if(!ctrl.bindErrors()) {
			//		m_simpleListModel.fireModelChanged();
			//		model().calculateTotals();
			//	}
			//});
			//ctrl.addNotificationListener(LiquiditeitenBedragType.class, r -> {
			//	if(row.equals(r.getRow())) {
			//		toggleBedragDisabled(row, ctrl);
			//	}
			//});
			//ctrl.addNotificationListener(LiquiditeitenPercentageChanged.class, r -> {
			//	if(row.equals(r.getRow())) {
			//		model().calculateAmount(row);
			//	}
			//});
			//if(row.getBedragType() == LiquiditeitsBedragType.BEDRAG) {
			//	ctrl.setMandatory(true);
			//} else {
			//	ctrl.setTextAlign(TextAlign.RIGHT);
			//	ctrl.setReadOnly(true);
			//}
			return ctrl;
		};
	}

	private IRowControlFactory<Line> createPercentageControlFactory() {
		return row -> {
			Text2<BigDecimal> ctrl = new Text2<>(BigDecimal.class);
			ctrl.setConverter(new MoneyBigDecimalNoSign());
			ctrl.setCssClass("ui-numeric");
			ctrl.bind("readOnly").to(model(), "readOnly");
			ctrl.addValidator(new MaxMinValidator(new BigDecimal("0.01"), new BigDecimal("100.00")));
			//ctrl.immediate();
			//ctrl.addNotificationListener(LiquiditeitenBedragType.class, getShowHideNotificationListener(row, ctrl, true));
			//ctrl.setOnValueChanged(c -> {
			//	if(!ctrl.bindErrors()) {
			//		m_simpleListModel.fireModelChanged();
			//		model().calculateTotals();
			//		notify(new LiquiditeitenPercentageChanged(row));
			//	}
			//});
			//if(row.getBedragType() == LiquiditeitsBedragType.BEDRAG) {
			//	ctrl.setVisibility(VisibilityType.HIDDEN);
			//} else {
			//	ctrl.setMandatory(true);
			//}
			return ctrl;
		};
	}

	private IRowControlFactory<Line> createAmountTypeControlFactory() {
		return row -> {
			ComboFixed2<AmountType> bedragTypeCombo = ComboFixed2.createEnumCombo(AmountType.class);
			bedragTypeCombo.bind().to(row, Line.pAMOUNTTYPE);
			bedragTypeCombo.bind("readOnly").to(model(), "readOnly");
			bedragTypeCombo.setMandatory(true);
			bedragTypeCombo.immediate();
			//bedragTypeCombo.setOnValueChanged(c -> notify(new LiquiditeitenBedragType(row)));
			return bedragTypeCombo;
		};
	}


	private IRenderInto<Line> createMonthRenderer(String property) {
		return (node, object) -> {
			ComboLookup2<Date> yearMonthCombo = getMonthYearCombo();
			node.add(yearMonthCombo);
			yearMonthCombo.setMandatory(true);
			yearMonthCombo.bind().to(object, property);
			yearMonthCombo.bind("readOnly").to(model(), "readOnly");
			yearMonthCombo.setErrorLocation(property);				// FIXME Should come from row header
		};
	}

	private ComboLookup2<Date> getMonthYearCombo() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-yyyy");
		ComboLookup2<Date> combo = new ComboLookup2<>(model().getProjectMonths());
		combo.setContentRenderer((node, value) -> node.add(sdf.format(value)));
		combo.setMandatory(true);
		return combo;
	}

	public LineController model() {
		return m_controller;
	}



}
