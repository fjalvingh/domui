package to.etc.domuidemo.pages;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domuidemo.pages.basic.*;
import to.etc.domuidemo.pages.dbtable.*;
import to.etc.domuidemo.sourceviewer.*;

/**
 * DomUI demo application, home page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 18, 2010
 */
public class OldHome extends UrlPage {
	private CaptionedPanel		m_cp;

	@Override
	public void createContent() throws Exception {
		addPanel("Layout components");
		addLink(CaptionsDemoPage.class, "This shows components that have or are captions");

		addPanel("Basic component demo's");
		addLink(BasicOverviewPage.class, "Basic component demo page");
		addLink(MiniPage.class, "Data input using layer 2: components");
		addLink(WikiDemo.class, "'Big' HtmlEditor component (CkEditor, Work-In-Progress)");

		addPanel("Table and tree components");
		//		addLink(TestShuttle.class, "List shuttle component (verhuizen van dingesen van source naar target");

		addPanel("Accessing the database");
		addLink(SimplestDbTable.class, "Bare-bones example of showing data from a database");

		//		addLink(RubriekenTreeDemo.class, "Tree met data uit 2 recordtypes van de database (Hoofdrubrieken/rubrieken)");

		addPanel("Dynamic images, ImageCache and charts");
		//		addLink("/domvp/to.etc.testdomui.imagecache.VpImagePart.part?id=1100002020&resize=200x150", "Opvragen image via URL met resize verzoek via de ImageCache");
		//		addLink(FotoShow.class, "Demo van DataCellTable en ImageCache-based resizing van foto's uit de database");

		//		addPanel("Voorbeelden voor database en metadata gerelateerde pagina's");
		//		addLink(CraftsgroupListPage.class, "Generieke CRUD voor vakgroepen (Willem)");
		//		addLink(NormCostListPage.class, "Generieke CRUD voor normkosten (Willem)");
		//		addLink(SkillListPage.class, "Vaardigheden (skills) pagina (Willem, long lived conversation - master/detail)");
		//
		//		addPanel("Testcode voor state management, de history shelve enz");
		//		addLink(PersonsListPage.class, "Zoeken naar personen, persoondetails en opzoeken adres met een component");
		//		addLink(NormKostenList.class, "QD generieke list- en edit pagina, met implementatie voor NormKosten.");
		//

	}

	private void	addPanel(String name) {
		m_cp = new CaptionedPanel();
		add(m_cp);
		add(new VerticalSpacer(15));
		m_cp.setTitle(name);
		m_cp.setBackgroundColor("transparent");
	}

	private void	addLink(Class<? extends UrlPage> clz, String text) {
		addLink(clz, text, false);
	}
	private void	addLink(Class<? extends UrlPage> clz, String text, boolean nw) {
		Div	d	= new Div();
		m_cp.getContent().add(d);
		ALink	link = new ALink(clz);
		d.add(link);
		link.setText(text);

		ALink link2 = new ALink(SourcePage.class, new PageParameters("name", clz.getName().replace('.', '/') + ".java"));
		d.add("\u00a0");
		d.add(link2);
		Img si = new Img("img/java.png");
		link2.add(si);
		link2.setTitle("View sourcefile");
		if(nw)
			d.add(new Img("img/aniNew.gif"));
	}
	private void	addLink(String url, String text) {
		Div	d	= new Div();
		m_cp.getContent().add(d);
		ATag	link = new ATag();
		d.add(link);
		link.setText(text);
		link.setHref(url);
	}
}
