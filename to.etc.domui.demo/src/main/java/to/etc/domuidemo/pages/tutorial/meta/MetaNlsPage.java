package to.etc.domuidemo.pages.tutorial.meta;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.derbydata.db.Invoice_;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.NlsContext;

/**
 * Tutorial, "metadata", step 3: the same page in another language. Nothing here
 * chooses a language; every text comes from a bundle that is picked per request,
 * using the locale in {@link NlsContext}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MetaNlsPage extends UrlPage {
	private final Shipment m_shipment = new Shipment();

	@Override
	public void createContent() throws Exception {
		setPageTitle($("title"));

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, $("title")));

		//-- Force the locale of this request (and of the ones after it, it is kept in the session).
		Div langs = new Div();
		cp.add(langs);
		langs.add($("langs") + " ");
		addLanguageLink(langs, "en_GB", "English");
		langs.add(" | ");
		addLanguageLink(langs, "nl_NL", "Nederlands");
		langs.add(" | ");
		addLanguageLink(langs, "de_DE", "Deutsch (no bundle: falls back)");

		Div texts = new Div("dm-tut");
		cp.add(texts);
		addText(texts, $("current", NlsContext.getLocale().getDisplayName(NlsContext.getLocale())));
		addText(texts, $("explain"));
		addText(texts, $("greeting"));
		addText(texts, $("accented"));

		cp.add(new HTag(2, $("fromEntity")));

		//-- Invoice has a Dutch bundle for some of its properties, and not for the others.
		Invoice invoice = getSharedContext().get(Invoice.class, 1L);
		FormBuilder fb = new FormBuilder(cp);
		fb.property(invoice, Invoice_.invoiceDate()).readOnly().control();
		fb.property(invoice, Invoice_.total()).readOnly().control();
		fb.property(invoice, Invoice_.customer()).readOnly().control();
		fb.property(invoice, Invoice_.billingAddress()).readOnly().control();
		fb.property(invoice, Invoice_.billingCity()).readOnly().control();
		fb.property(invoice, Invoice_.billingCountry()).readOnly().control();

		cp.add(new HTag(2, $("fromEnum")));
		FormBuilder fb2 = new FormBuilder(cp);
		fb2.property(m_shipment, Shipment_.method()).control();
		fb2.property(m_shipment, Shipment_.insured()).control();

		cp.add(new HTag(2, $("fromFramework")));
		Div box = new Div("dm-tut-q");
		cp.add(box);
		box.add("NlsContext.getLocale()     " + NlsContext.getLocale() + "\n");
		box.add("Msgs.mandatory             " + Msgs.mandatory.getString() + "\n");
		box.add("Msgs.uiBoolTrue            " + Msgs.uiBoolTrue.getString() + "\n");
		box.add("Msgs.uiPagerText           " + Msgs.uiPagerText.format(1, 12, 120) + "\n");
	}

	private static void addText(Div into, String text) {
		Div d = new Div();
		into.add(d);
		d.add(text);
	}

	private void addLanguageLink(Div into, String locale, String text) {
		ATag a = new ATag();
		into.add(a);
		a.setHref(getClass().getName() + ".ui?___locale=" + locale);
		a.add(text);
	}
}
