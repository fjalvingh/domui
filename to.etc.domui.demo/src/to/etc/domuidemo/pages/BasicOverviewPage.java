package to.etc.domuidemo.pages;

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.input.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.layout.title.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;


public class BasicOverviewPage extends UrlPage {
	public BasicOverviewPage() {
		setTitle("Quick overview of available components");
	}

	@Override
	public String toString() {
		return "Hello  " + hashCode();
	}

	@Override
	public void createContent() throws Exception {
		ErrorPanel ep = new ErrorPanel();
		add(ep);

		//-- Tab panel
		TabPanel tp = new TabPanel();
		add(tp);
		tp.add(createTab1(), "Simple input");
		tp.add(createTab3(), "Panels and headers");
		tp.add(createTab2(), "Tab Two $");
	}

	private NodeBase createTab3() throws Exception {
		Div c = new Div();

		c.add(new InfoPanel("This showcases the available panels and headers. This thingy, by the way, is an InfoPanel."));
		CaptionedHeader ch = new CaptionedHeader("CaptionedHeader");
		c.add(ch);
		ch.setClear(ClearType.BOTH);
		Div d = new Div();
		c.add(d);
		d.setText("The CaptionedHeader is a header only, and has no content. Just add extra nodes to show content below it.");
		c.add(new VerticalSpacer(20)); // Add some pixels room

		CaptionedPanel cp = new CaptionedPanel("Captioned Panel");
		c.add(cp);
		cp.getContent().add("The Captioned Panel has an embedded content div, shown indented between a thin border. Use sparingly because it makes a page very busy looking");

		c.add(new VerticalSpacer(40));
		AppPageTitleBar apt = new AppPageTitleBar("This is an app", false);
		c.add(apt);
		c.add("The thing above here is an AppPageTitle. It has a bezier curve in it's style which is rendered using the svg theme engine.");

		c.add(new VerticalSpacer(40));
		d = new Div();
		c.add(d);
		d.setBorderBottomColor("#555");
		d.setBorderBottomStyle("dotted");
		d.setBorderBottomWidth(1);
		d.add("This text is in a DIV. Between this div (which ends in the dotted line) and the next one we have a VerticalSpacer(40), just a thing that makes 40 pixels of vertical space available.");

		c.add(new VerticalSpacer(40));
		d = new Div();
		c.add(d);
		d.setBorderTopColor("#555");
		d.setBorderTopStyle("dotted");
		d.setBorderTopWidth(1);
		d.add("And this DIV is just below the vertical spacer and starts with the dotted border just above.");

		return c;
	}

	private NodeBase createTab1() {
		final Div d = new Div();
		Table tab = new Table();
		d.add(tab);
		tab.setBorder(0);
		TBody tbl = new TBody();
		tab.add(tbl);

		/*final Text<String> un = */addInputLabel(tbl, "Text<String> Normal String input", String.class);
		/* final Text<Integer> pw = */addInputLabel(tbl, "Integer-only text input", Integer.class);

		HiddenText<String> hidden = new HiddenText<String>(String.class);
		addLabeled(tbl, "HiddenText<String> Password text input", hidden);
		final Checkbox cb = new Checkbox();
		addLabeled(tbl, "Example checkbox", cb);

		final Text<Date> datein = new Text<Date>(Date.class);
		addLabeled(tbl, "Silly Text<Date> input, using default converter", datein);
		datein.setTitle("Normally we would use DateInput for this, of course");

		/* final Text<Double>	dbl	= */addInputLabel(tbl, "Text<Double> input, bad \u20ac input?", Double.class);
		final DateInput di = new DateInput();
		addLabeled(tbl, "Normal DateInput, date-only, with onChange handler", di);
		di.setOnValueChanged(new IValueChanged<DateInput>() {
			@Override
			public void onValueChanged(DateInput component) throws Exception {
				MsgBox.message(d, MsgBox.Type.INFO, "The selected date is " + component.getValue() + ". (This is a MsgBox.message call, creating a FloatingWindow)");
			}
		});

		final DateInput datetime = new DateInput();
		datetime.setWithTime(true);
		addLabeled(tbl, "Date and time input", datetime);

		//-- Combo box
		List<ValueLabelPair<Integer>> vlist = new ArrayList<ValueLabelPair<Integer>>();
		vlist.add(new ValueLabelPair<Integer>(Integer.valueOf(1), "The number one"));
		vlist.add(new ValueLabelPair<Integer>(Integer.valueOf(2), "The number two"));
		vlist.add(new ValueLabelPair<Integer>(Integer.valueOf(3), "The number three"));
		vlist.add(new ValueLabelPair<Integer>(Integer.valueOf(4), "The number IV"));
		ComboFixed<Integer> combof = new ComboFixed<Integer>(vlist);
		combof.setMandatory(false);
		combof.setValue(Integer.valueOf(4)); // Combo's, like other components, present their properly typed value.
		addLabeled(tbl, "ComboFixed<Integer> using List<T> of value/label pairs", combof);

		return d;
	}

	private NodeBase createTab2() {
		final Div d = new Div();
		d.add(new TextNode("Hello again: "));
		DefaultButton ib = new DefaultButton("ClickMe");
		d.add(ib);

		ib.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(NodeBase b) throws Exception {
				FloatingWindow fw = FloatingWindow.create(BasicOverviewPage.this, "This floats");
				add(fw);
			}
		});

		ib = new DefaultButton("Test Link");
		d.add(ib);

		ib.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(NodeBase b) throws Exception {
				DefaultButton db = new DefaultButton("oo");
				BasicOverviewPage.this.add(db);
				db.setClicked(new IClicked<NodeBase>() {
					@Override
					public void clicked(NodeBase bah) throws Exception {
						System.out.println("klik");
					}
				});
			}
		});

		return new CaptionedPanel("Caption panel header", d);
	}

	private <T> Text<T> addInputLabel(TBody tbl, String label, Class<T> clz) {
		TR tr = new TR();
		tbl.add(tr);
		TD td = new TD();
		tr.add(td);
		td.add(label);
		td.setCssClass("ui-f-lbl");
		td = new TD();
		tr.add(td);
		Text<T> in = new Text<T>(clz);
		td.add(in);
		td.setCssClass("ui-f-in");
		in.setSize(20);

		return in;
	}

	private void addLabeled(TBody tbl, String label, NodeBase inp) {
		TR tr = new TR();
		tbl.add(tr);
		TD td = new TD();
		tr.add(td);
		td.setCssClass("ui-f-lbl");
		td.add(label);
		td = new TD();
		tr.add(td);
		td.add(inp);
		td.setCssClass("ui-f-in");
	}


}
