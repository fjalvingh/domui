package to.etc.domuidemo.pages.themereview;

import to.etc.domui.component.agenda.MonthPanel;
import to.etc.domui.component.buttons.ActionButton;
import to.etc.domui.component.buttons.CheckboxButton;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.HoverButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.buttons.SwitchButton;
import to.etc.domui.component.misc.PercentageCompleteRuler2;
import to.etc.domui.component.htmleditor.HtmlEditor;
import to.etc.domui.component.input.DateInput2;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.input.ValueLabelPair;
import to.etc.domui.dom.html.TextArea;
import to.etc.domui.component.layout.Caption2;
import to.etc.domui.component.layout.CaptionType;
import to.etc.domui.component.layout.CaptionedHeader;
import to.etc.domui.component.layout.CaptionedPanel;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.Dialog;
import to.etc.domui.component.layout.ErrorMessageDiv;
import to.etc.domui.component.headers.ExpandHeader;
import to.etc.domui.component.layout.Panel;
import to.etc.domui.component.layout.ScrollableTabPanel;
import to.etc.domui.component.layout.TabPanel;
import to.etc.domui.component.layout.Window;
import to.etc.domui.component.headers.GenericHeader;
import to.etc.domui.component.headers.GenericHeader.Type;
import to.etc.domui.component2.lookupinput.LookupInput2;
import to.etc.domui.component.headers.HamburgerMenu;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.component2.popupmenus.PopupMenu2;
import to.etc.domui.component.menu.UIAction;
import to.etc.domui.component.misc.DisplayCheckbox;
import to.etc.domui.component.misc.DisplaySpan;
import to.etc.domui.component.misc.EmbeddedCode;
import to.etc.domui.component.misc.Explanation;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MessageFlare;
import to.etc.domui.component.layout.MessageLine;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component.input.SearchAsYouType;
import to.etc.domui.component.tbl.DataCellTable;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.InstanceSelectionModel;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleListModel;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.component.tree3.Tree3;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.enumsetinput.EnumSetInput;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.component2.navigation.BreadCrumb2;
import to.etc.domui.component2.navigation.BreadCrumb2.IItem;
import to.etc.domui.component2.navigation.BreadCrumb2.Item;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.RadioGroup;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.state.UIContext;
import to.etc.domui.state.UIGoto;
import to.etc.domui.server.DomApplication;
import to.etc.domui.themes.IThemeVariant;
import to.etc.domui.util.bugs.Bug;
import to.etc.domuidemo.pages.components.choice.Medium;
import to.etc.domuidemo.pages.components.dialog.DialogMsg;
import to.etc.domuidemo.pages.components.tables.DemoNode;
import to.etc.domuidemo.pages.components.tables.TreeDemoModel;
import to.etc.webapp.query.QCriteria;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Every component the theme paints, on one page, so that a colour scheme can be judged in
 * one go instead of by opening every demo page in turn. The bar at the top switches the
 * session between the colour schemes the application offers ({@link DomApplication#getThemeVariants()});
 * the table below it shows the scheme's colour roles.
 *
 * <p>Things that only exist after a click - menus, dialogs, flares - open from the buttons in
 * the "Popups" cell. Everything else is shown in a state worth looking at: a selected row, an
 * expanded tree, a field with an error, a marked day.</p>
 */
public class ThemeReviewPage extends UrlPage {
	private String m_scheme;

	/**
	 * Optional: the colour scheme to switch to, as its variant name: "light-winter", "dark-nord". It makes
	 * a link that opens the page in one scheme, which is also how a screenshot tool gets there.
	 */
	@UIUrlParameter(name = "scheme", mandatory = false)
	public void setScheme(String scheme) {
		m_scheme = scheme;
	}

	public String getScheme() {
		return m_scheme;
	}

	@Override
	public void createContent() throws Exception {
		setPageTitle("Theme review");
		String scheme = m_scheme;
		if(null != scheme) {
			IThemeVariant wanted = DomApplication.get().findThemeVariant(scheme);	// an unknown name gives the default
			if(!wanted.getVariantName().equals(UIContext.getRequestContext().getThemeVariant().getVariantName())) {
				UIContext.getRequestContext().setThemeVariant(wanted);
				UIGoto.reload();					// the stylesheet link is in the head, which only a full render writes
				return;
			}
		}

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Theme review"));

		cp.add(schemeBar());
		cp.add(swatches());

		Div grid = new Div("dm-tr-grid");
		cp.add(grid);

		grid.add(cell("Text and headings", text()));
		grid.add(cell("Buttons", buttons()));
		grid.add(cell("Toggles and choices", toggles()));
		grid.add(cell("Inputs", inputs()));
		grid.add(cell("Lookup and search", lookups()));
		grid.add(cell("Notices", notices()));
		grid.add(cell("Panels", panels()));
		grid.add(cell("Headers", headers()));
		grid.add(cell("Popups, dialogs and flares", popups()));
		grid.add(cell("Navigation", navigation()));
		grid.add(cell("Calendar and progress", calendar()));
		grid.add(cell("Tree", tree()));

		Div wide = new Div("dm-tr-grid dm-tr-wide");
		cp.add(wide);
		wide.add(cell("TabPanel and ScrollableTabPanel", tabs()));
		wide.add(cell("DataTable: a selected row (hover a row for the hover colours)", table()));
		wide.add(cell("DataCellTable and HtmlEditor", cellsAndEditor()));
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	The scheme bar and the colour roles.						*/
	/*----------------------------------------------------------------------*/

	private Div schemeBar() {
		Div bar = new Div("dm-tr-bar");
		String current = UIContext.getRequestContext().getThemeVariant().getVariantName();
		bar.add(new Span("dm-tr-bar-lbl", "Colour scheme:"));
		for(IThemeVariant variant : DomApplication.get().getThemeVariants()) {
			bar.add(schemeButton(variant.getLabel(), variant, current));
		}
		return bar;
	}

	private DefaultButton schemeButton(String label, IThemeVariant variant, String current) {
		DefaultButton b = new DefaultButton(label, () -> {
			UIContext.getRequestContext().setThemeVariant(variant);
			appendJavascript("WebUI.refreshPage();");
		});
		if(variant.getVariantName().equals(current))
			b.css("is-primary");
		return b;
	}

	/**
	 * The scheme's colour roles (COLOR-ROLES.md), each shown with what goes on it: a surface
	 * with body text, a fill with its own text colour, a line as a frame. The colours come from
	 * the theme module in the demo's stylesheet (_themereview.scss), so they are the compiled
	 * ones; a chip's classes say which role it shows as ground (bg-), text (fg-) and frame (bd-).
	 */
	private Div swatches() {
		Div roles = new Div("dm-tr-roles");
		roleGroup(roles, "Surfaces",
			chip("surface-page", "bg-surface-page fg-text-default"),
			chip("surface-raised", "bg-surface-raised fg-text-default"),
			chip("surface-overlay", "bg-surface-overlay fg-text-default"),
			chip("surface-sunken", "bg-surface-sunken fg-text-default"),
			chip("surface-band", "bg-surface-band fg-text-default"),
			chip("surface-inverse", "bg-surface-inverse fg-text-inverse"));
		roleGroup(roles, "Text",
			chip("text-default", "bg-surface-page fg-text-default"),
			chip("text-strong", "bg-surface-page fg-text-strong"),
			chip("text-subtle", "bg-surface-page fg-text-subtle"),
			chip("text-faint", "bg-surface-page fg-text-faint"),
			chip("text-inverse", "bg-surface-inverse fg-text-inverse"));
		roleGroup(roles, "Lines",
			chip("border-subtle", "bg-surface-page fg-text-default bd-border-subtle"),
			chip("border-default", "bg-surface-page fg-text-default bd-border-default"),
			chip("border-strong", "bg-surface-page fg-text-default bd-border-strong"),
			chip("border-bold", "bg-surface-page fg-text-default bd-border-bold"));
		roleGroup(roles, "Fields and links",
			chip("field-surface", "bg-field-surface fg-text-default bd-field-border"),
			chip("field-surface-readonly", "bg-field-surface-readonly fg-text-default bd-field-border"),
			chip("field-border-hover", "bg-field-surface fg-text-default bd-field-border-hover"),
			chip("link-text", "bg-surface-page fg-link-text"),
			chip("link-text-visited", "bg-surface-page fg-link-text-visited"),
			chip("link-text-hover", "bg-surface-page fg-link-text-hover"));
		roleGroup(roles, "States",
			chip("hover-wash / -border", "bg-hover-wash fg-text-default bd-hover-border"),
			chip("selected-wash", "bg-selected-wash fg-text-default"),
			chip("selected-solid", "bg-selected-solid fg-selected-on-solid"),
			chip("selected-border", "bg-surface-page fg-text-default bd-selected-border"),
			chip("highlight", "bg-highlight fg-text-strong"),
			chip("focus-ring", "bg-surface-page fg-text-default bd-focus-ring"),
			chip("disabled-*", "bg-disabled-surface fg-disabled-text bd-disabled-border"),
			chip("scrim", "bg-surface-page fg-text-default dm-tr-scrim"),
			chip("shadow", "bg-surface-raised fg-text-default dm-tr-shadow"));

		//-- The colour families: one row each, a column per step.
		Div grid = new Div("dm-tr-fam");
		roles.add(new Div("dm-tr-rg-ttl", "Intents and chrome"));
		roles.add(grid);
		grid.add(new Div("dm-tr-fam-hd"));
		for(String step : new String[]{"wash", "tint", "solid", "text", "border"})
			grid.add(new Div("dm-tr-fam-hd", "-" + step));
		for(String f : new String[]{"primary", "control", "neutral", "info", "success", "warning", "danger", "chrome"}) {
			grid.add(new Div("dm-tr-fam-name", f));
			grid.add(sample("bg-" + f + "-wash fg-" + f + "-text"));
			grid.add(sample("bg-" + f + "-tint fg-text-strong"));
			grid.add(sample("bg-" + f + "-solid fg-" + f + "-on-solid"));
			grid.add(sample("bg-surface-page fg-" + f + "-text"));
			grid.add(sample("bg-surface-page fg-text-default bd-" + f + "-border"));
		}

		List<Div> categories = new ArrayList<>();
		for(int n = 1; n <= 7; n++) {
			categories.add(chip("category-" + n + "-solid", "bg-category-" + n + "-solid"));
			categories.add(chip("category-" + n + "-wash", "bg-category-" + n + "-wash fg-text-default"));
		}
		roleGroup(roles, "Categories", categories.toArray(new Div[0]));
		return roles;
	}

	private static void roleGroup(Div into, String title, Div... chips) {
		into.add(new Div("dm-tr-rg-ttl", title));
		Div row = new Div("dm-tr-rg");
		into.add(row);
		for(Div c : chips)
			row.add(c);
	}

	/**
	 * A role's chip: the sample, and the role's name under it.
	 */
	private static Div chip(String name, String roles) {
		Div chip = new Div("dm-tr-rc");
		chip.add(sample(roles));
		chip.add(new Div("dm-tr-rl", name));
		return chip;
	}

	private static Div sample(String roles) {
		StringBuilder sb = new StringBuilder("dm-tr-rs");
		for(String r : roles.split(" "))
			sb.append(' ').append(r.startsWith("dm-") ? r : "dm-tr-" + r);
		return new Div(sb.toString(), "Text");
	}

	private static Div cell(String title, NodeBase content) {
		Div c = new Div("dm-tr-cell");
		c.add(new Div("dm-tr-ttl", title));
		c.add(content);
		return c;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	The cells.													*/
	/*----------------------------------------------------------------------*/

	private Div text() throws Exception {
		Div d = new Div();
		d.add(new HTag(1, "Heading 1"));
		d.add(new HTag(2, "Heading 2"));
		d.add(new HTag(3, "Heading 3"));
		Para p = new Para();
		d.add(p);
		p.add("Body text, with ");
		ATag link = new ATag();
		link.setHref("#");
		link.add("a link");
		p.add(link);
		p.add(", ");
		p.add(new Span("dm-tr-muted", "muted text"));
		p.add(" and ");
		p.add(new Span("dm-tr-code", "code"));
		p.add(".");
		d.add(new EmbeddedCode("SELECT * FROM Album WHERE title ilike '%rock%'"));
		return d;
	}

	private Div buttons() throws Exception {
		Div d = new Div();
		Div row = new Div("dm-tr-row");
		d.add(row);
		row.add(new DefaultButton("Plain"));
		row.add(new DefaultButton("With icon", Icon.faHeart));
		row.add(new DefaultButton("", Icon.faTrash));
		DefaultButton dis = new DefaultButton("Disabled", Icon.faLock);
		dis.setDisabled(true);
		row.add(dis);

		Div colours = new Div("dm-tr-row");
		d.add(colours);
		for(String name : new String[]{"primary", "link", "info", "success", "warning", "danger", "dark", "light"}) {
			colours.add(new DefaultButton(name).css("is-" + name));
		}
		Div outl = new Div("dm-tr-row");
		d.add(outl);
		for(String name : new String[]{"primary", "info", "success", "danger"}) {
			outl.add(new DefaultButton(name).css("is-" + name, "is-outlined"));
		}
		outl.add(new DefaultButton("mini").mini());

		Div others = new Div("dm-tr-row");
		d.add(others);
		others.add(new LinkButton("LinkButton"));
		others.add(new LinkButton("Delete", Icon.faTrash));
		SmallImgButton sib = new SmallImgButton(Icon.faSearch);
		sib.setTitle("SmallImgButton");
		others.add(sib);
		others.add(new HoverButton("THEME/72x24_close.png"));
		ActionButton ab = new ActionButton(new UIAction("ActionButton", null, Icon.faMusic, null, node -> {}));
		ab.addAction(new UIAction("Another", null, Icon.faStar, null, node -> {}));
		others.add(ab);

		ButtonBar2 bb = new ButtonBar2();
		d.add(bb);
		bb.addButton("In a ButtonBar2", Icon.faCheck, () -> {});
		bb.addButton("Cancel", Icon.faTimes, () -> {});
		bb.addLinkButton("Help", Icon.faQuestionCircle, () -> {});
		return d;
	}

	private Div toggles() throws Exception {
		Div d = new Div();
		CheckboxButton on = new CheckboxButton();
		on.setChecked(true);
		CheckboxButton labelled = new CheckboxButton().setOnLabel("In stock").setOffLabel("Sold out");
		CheckboxButton disabled = new CheckboxButton();
		disabled.setChecked(true);
		disabled.setDisabled(true);
		SwitchButton sw = new SwitchButton();
		sw.setChecked(true);
		SwitchButton swOff = new SwitchButton();

		Checkbox cb = new Checkbox();
		cb.setChecked(true);
		Checkbox cb2 = new Checkbox();

		RadioGroup<Medium> radio = new RadioGroup<>();
		radio.addButton("CD", Medium.Cd);
		radio.addButton("Vinyl", Medium.Vinyl);
		radio.addButton("Download", Medium.Download);
		radio.setValue(Medium.Vinyl);

		RadioGroup<Medium> asButtons = RadioGroup.createEnumRadioGroupUnsorted(Medium.class).asButtons();
		asButtons.setValue(Medium.Cd);

		RadioGroup<Medium> roButtons = RadioGroup.createEnumRadioGroupUnsorted(Medium.class).asButtons();
		roButtons.setValue(Medium.Vinyl);
		roButtons.setDisabled(true);

		Div display = new Div("dm-tr-row");
		display.add(new DisplayCheckbox(Boolean.TRUE));
		display.add(new DisplayCheckbox(Boolean.FALSE));
		display.add(new DisplayCheckbox());

		FormBuilder fb = new FormBuilder(d);
		fb.label("CheckboxButton").control(on);
		fb.label("With labels").control(labelled);
		fb.label("Disabled").control(disabled);
		fb.label("SwitchButton on / off").item(row(sw, swOff));
		fb.label("Checkbox").item(row(cb, cb2));
		fb.label("RadioGroup").control(radio);
		fb.label("asButtons()").control(asButtons);
		fb.label("asButtons(), disabled").control(roButtons);
		fb.label("DisplayCheckbox").item(display);
		return d;
	}

	private Div inputs() throws Exception {
		Div d = new Div();
		d.add(new ErrorMessageDiv(d));				// a fence of its own: the empty mandatory field's error stays in this cell
		Text2<String> plain = new Text2<>(String.class);
		plain.setValue("Led Zeppelin IV");
		Text2<String> error = new Text2<>(String.class);
		error.setMandatory(true);
		Text2<BigDecimal> ro = new Text2<>(BigDecimal.class);
		ro.setValue(new BigDecimal("12.95"));
		ro.setReadOnly(true);
		Text2<String> dis = new Text2<>(String.class);
		dis.setValue("Cannot be changed");
		dis.setDisabled(true);
		DateInput2 date = new DateInput2();
		date.setValue(new Date());
		ComboFixed2<String> combo = new ComboFixed2<>(List.of(new ValueLabelPair<>("cd", "Compact disc"),
			new ValueLabelPair<>("lp", "Vinyl LP"), new ValueLabelPair<>("dl", "Download")));
		combo.setValue("lp");
		TextArea area = new TextArea(30, 2);
		area.setValue("A text area,\nwith two lines.");
		DisplaySpan<String> span = new DisplaySpan<>(String.class);
		span.setValue("A DisplaySpan");

		FormBuilder fb = new FormBuilder(d);
		fb.label("Text2").control(plain);
		fb.label("Mandatory, empty").control(error);
		fb.label("Read-only").control(ro);
		fb.label("Disabled").control(dis);
		fb.label("DateInput2").control(date);
		fb.label("ComboFixed2").control(combo);
		fb.label("TextArea").control(area);
		fb.label("DisplaySpan").control(span);
		error.getValueSafe();							// empty and mandatory: shows the error state
		return d;
	}

	private Div lookups() throws Exception {
		Div d = new Div();
		List<Genre> genres = getSharedContext().query(QCriteria.create(Genre.class));
		List<Artist> artists = getSharedContext().query(QCriteria.create(Artist.class).ascending("name").limit(30));

		LookupInput2<Customer> customer = new LookupInput2<>(Customer.class);
		customer.setValue(getSharedContext().get(Customer.class, Long.valueOf(1)));
		LookupInput2<Customer> empty = new LookupInput2<>(Customer.class);
		LookupInput2<Customer> ro = new LookupInput2<>(Customer.class);
		ro.setValue(getSharedContext().get(Customer.class, Long.valueOf(2)));
		ro.setReadOnly(true);

		ComboLookup2<Artist> artist = new ComboLookup2<>(artists);
		artist.setValue(artists.get(3));

		SearchAsYouType<Genre> sayt = new SearchAsYouType<>(Genre.class, "name");
		sayt.setData(genres);

		EnumSetInput<Genre> set = new EnumSetInput<>(Genre.class, genres, "name");
		set.setValue(Set.of(genres.get(0), genres.get(1)));

		FormBuilder fb = new FormBuilder(d);
		fb.label("LookupInput2").control(customer);
		fb.label("LookupInput2, empty").control(empty);
		fb.label("Read-only").control(ro);
		fb.label("ComboLookup2").control(artist);
		fb.label("SearchAsYouType (type 'ro')").control(sayt);
		fb.label("EnumSetInput").control(set);
		return d;
	}

	private Div notices() throws Exception {
		Div d = new Div();
		d.add(new MessageLine(MsgType.INFO, "MessageLine: the prices are <b>excluding</b> VAT."));
		d.add(new MessageLine(MsgType.WARNING, "MessageLine: this album has no tracks yet."));
		d.add(new MessageLine(MsgType.ERROR, "MessageLine: the connection was lost."));
		d.add(new Explanation("Explanation: search is on the album title."));
		d.add(new Explanation(MsgType.WARNING, "Explanation: deleting an artist deletes its albums."));
		d.add(new Explanation(MsgType.ERROR, "Explanation: the shop sells albums, not tracks."));

		Div fence = new Div();
		d.add(fence);
		ErrorMessageDiv emd = new ErrorMessageDiv(fence);
		fence.add(emd);
		fence.addGlobalMessage(UIMessage.warning(DialogMsg.albumStockLow, "Big Ones", Integer.valueOf(3)));
		fence.addGlobalMessage(UIMessage.error(DialogMsg.albumSoldOut, "Nevermind"));
		return d;
	}

	private Div panels() throws Exception {
		Div d = new Div();
		Panel panel = new Panel();
		d.add(panel);
		panel.add("A Panel: a box for things that belong together.");

		CaptionedPanel cpnl = new CaptionedPanel("CaptionedPanel", new Div());
		d.add(cpnl);
		Panel inner = new Panel();
		cpnl.getContent().add(inner);
		inner.add("A Panel inside a CaptionedPanel.");
		return d;
	}

	private Div headers() throws Exception {
		Div d = new Div();
		d.add(new GenericHeader(Type.SIMPLE, "GenericHeader SIMPLE"));
		d.add(new GenericHeader(Type.BLUE, "GenericHeader BLUE"));
		d.add(new GenericHeader(Type.HEADER_1, "GenericHeader HEADER_1"));
		d.add(new GenericHeader(Type.HEADER_2, "GenericHeader HEADER_2"));
		d.add(new GenericHeader(Type.HEADER_3, "GenericHeader HEADER_3"));
		Caption2 c1 = new Caption2(CaptionType.Default, "Caption2");
		c1.addButton(Icon.faPlus, "Add", () -> {});
		d.add(c1);
		d.add(new Caption2(CaptionType.Panel, "Caption2, panel style"));
		d.add(new CaptionedHeader("CaptionedHeader"));

		ExpandHeader xh = new ExpandHeader("ExpandHeader, with a menu");
		d.add(xh);
		Div content = new Div();
		content.add("The content of the ExpandHeader.");
		xh.setContent(content);
		xh.setExpanded(true);
		for(IUIAction a : actions()) {
			xh.addAction(a);
		}
		return d;
	}

	private Div popups() throws Exception {
		Div d = new Div();
		Div row = new Div("dm-tr-row");
		d.add(row);

		DefaultButton pm = new DefaultButton("PopupMenu2", Icon.faCaretDown);
		row.add(pm);
		pm.setClicked(() -> {
			PopupMenu2 menu = new PopupMenu2(pm);
			menu.text("Play it").icon(Icon.faMusic).click(() -> {}).append();
			menu.text("Add to the cart").icon(Icon.faShoppingCart).click(() -> {}).append();
			menu.text("Delete it").icon(Icon.faTrash).disableReason("Sold already").click(() -> {}).append();
			menu.show(pm);
		});

		DefaultButton hm = new DefaultButton("HamburgerMenu", Icon.faBars);
		row.add(hm);
		hm.setClicked(() -> {
			HamburgerMenu menu = new HamburgerMenu(actions());
			hm.appendAfterMe(menu);
			menu.setOnSelection(action -> {});
		});

		row = new Div("dm-tr-row");
		d.add(row);
		row.add(new DefaultButton("Window", () -> {
			Window w = new Window(true, false, 460, -1, "A Window");
			add(w);
			w.add(new Para().add("A modal floating window, with the page dimmed behind it."));
			Text2<String> t = new Text2<>(String.class);
			new FormBuilder(w).label("A field").control(t);
		}));
		row.add(new DefaultButton("Dialog", () -> {
			Dialog dlg = new Dialog(true, false, 460, -1, "A Dialog");
			add(dlg);
			dlg.add(new Para().add("A window with save and cancel buttons."));
		}));

		row = new Div("dm-tr-row");
		d.add(row);
		row.add(new DefaultButton("MsgBox info", () -> MsgBox2.on(this).info("The album has been saved.")));
		row.add(new DefaultButton("warning", () -> MsgBox2.on(this).warning("The album has no tracks.")));
		row.add(new DefaultButton("error", () -> MsgBox2.on(this).error("The album could not be saved.")));

		row = new Div("dm-tr-row");
		d.add(row);
		row.add(new DefaultButton("Flare info", () -> MessageFlare.display(this, MsgType.INFO, "The album has been saved.")));
		row.add(new DefaultButton("warning", () -> MessageFlare.display(this, MsgType.WARNING, "This album has no tracks yet.")));
		row.add(new DefaultButton("error", () -> MessageFlare.display(this, MsgType.ERROR, "The album could not be saved.")));
		row.add(new DefaultButton("Bug", Icon.faBug, () -> Bug.bug("The album has a price but no currency")));
		return d;
	}

	private Div navigation() throws Exception {
		Div d = new Div();
		List<IItem> items = new ArrayList<>();
		items.add(new Item(Icon.faDatabase.createNode(), "Chinook", "The whole catalogue", it -> {}));
		items.add(new Item(null, "Rock", null, it -> {}));
		items.add(new Item(null, "Led Zeppelin", null, it -> {}));
		items.add(new Item(null, "IV", null, it -> {}));
		d.add(new BreadCrumb2(items));
		d.add(new Para().add("The crumb's last step is the page you are on."));
		return d;
	}

	private Div calendar() throws Exception {
		Div d = new Div();
		MonthPanel mp = new MonthPanel();
		d.add(mp);
		Calendar cal = Calendar.getInstance();
		mp.setDate(cal.getTime());
		mp.setDayClicked((panel, date) -> {});
		cal.set(Calendar.DAY_OF_MONTH, 12);
		mp.setMarked(cal.getTime(), null);

		PercentageCompleteRuler2 r = new PercentageCompleteRuler2();
		r.setWidth(250);
		r.setValue(35.0);
		PercentageCompleteRuler2 full = new PercentageCompleteRuler2();
		full.setWidth(250);
		full.setValue(100.0);
		FormBuilder fb = new FormBuilder(d);
		fb.label("35%").control(r);
		fb.label("100%").control(full);
		return d;
	}

	private Div tree() throws Exception {
		Div d = new Div("dm-tr-tree");
		TreeDemoModel model = new TreeDemoModel(this);
		Tree3<DemoNode> tree = new Tree3<>(model);
		d.add(tree);
		tree.setContentRenderer((node, object) -> {
			NodeBase icon = object.getIcon().createNode();
			node.add(icon);
			icon.addCssClass("dm-tree2-icon");
			node.add(object.getText());
		});
		DemoNode root = model.getRoot();
		DemoNode letter = model.getChild(root, 0);
		tree.expandNode(letter);
		DemoNode artist = model.getChild(letter, 0);
		tree.expandNode(artist);
		if(model.getChildCount(artist) > 0)
			tree.setSelectedValue(model.getChild(artist, 0));
		return d;
	}

	private Div tabs() {
		Div d = new Div();
		TabPanel tp = new TabPanel(true);
		d.add(tp);
		Div one = new Div();
		one.add("The content of the selected tab.");
		tp.tab().label("Details").content(one).build();
		tp.tab().label("Tracks").image(Icon.faMusic).content(new Div()).build();
		tp.tab().label("Closable").content(new Div()).closable().build();

		ScrollableTabPanel stp = new ScrollableTabPanel();
		d.add(stp);
		for(int i = 1; i <= 14; i++) {
			Div c = new Div();
			c.add("The content of tab " + i);
			stp.tab().label("Tab number " + i).content(c).build();
		}
		return d;
	}

	private Div table() throws Exception {
		Div d = new Div();
		SimpleSearchModel<Album> model = new SimpleSearchModel<>(this, QCriteria.create(Album.class).ascending(Album_.title()));
		RowRenderer<Album> rr = new RowRenderer<>(Album.class);
		rr.column(Album_.title()).label("Album").width(40).ascending().sortdefault();
		rr.column(Album_.artist().name()).label("Artist").width(30).ascending();
		DataTable<Album> table = new DataTable<>(model, rr);
		d.add(table);
		table.setPageSize(6);

		InstanceSelectionModel<Album> selection = new InstanceSelectionModel<>(true);
		table.setSelectionModel(selection);
		table.setShowSelection(true);
		List<Album> first = getSharedContext().query(QCriteria.create(Album.class).ascending(Album_.title()).limit(3));
		selection.setInstanceSelected(first.get(1), true);
		d.add(new DataPager(table));
		return d;
	}

	private Div cellsAndEditor() throws Exception {
		Div d = new Div();
		List<Album> albums = getSharedContext().query(QCriteria.create(Album.class).ascending(Album_.title()).limit(8));
		DataCellTable<Album> grid = new DataCellTable<>(new SimpleListModel<>(albums));
		grid.setColumns(4);
		grid.setContentRenderer((node, album) -> {
			node.add(new Div("dm-tut-hi", album.getTitle()));
			node.add(album.getArtist().getName());
		});
		d.add(grid);

		HtmlEditor editor = new HtmlEditor();
		editor.setValue("<p>The <b>HtmlEditor</b>, with some <i>text</i> in it.</p>");
		d.add(editor);
		return d;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Helpers.													*/
	/*----------------------------------------------------------------------*/

	private static Div row(NodeBase... nodes) {
		Div r = new Div("dm-tr-row");
		for(NodeBase n : nodes)
			r.add(n);
		return r;
	}

	private static List<IUIAction> actions() {
		List<IUIAction> list = new ArrayList<>();
		list.add(new UIAction("Play the album", null, Icon.faMusic, null, node -> {}));
		list.add(new UIAction("Add to the cart", null, Icon.faShoppingCart, null, node -> {}));
		list.add(new UIAction("Delete the album", null, Icon.faTrash, "The album has been sold", node -> {}));
		return list;
	}
}
