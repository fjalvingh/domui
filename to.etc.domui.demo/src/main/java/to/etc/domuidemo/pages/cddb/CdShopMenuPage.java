package to.etc.domuidemo.pages.cddb;

import to.etc.domuidemo.pages.MenuPage;

/**
 * The CD shop's back office: the entry point to the screens that maintain the
 * catalogue, the customers and the sales.
 */
public class CdShopMenuPage extends MenuPage {
	public CdShopMenuPage() {
		super("The CD shop");
	}

	@Override
	public void createContent() throws Exception {
		addCaption("Catalogue");
		addLink(ArtistListPage.class, "Artists, and the albums we have of them");
		addLink(AlbumListPage.class, "Albums, and the tracks on them");
		addLink(CdCollection.class, "Tracks for sale");
		addLink(ReferenceDataPage.class, "Genres and media types");

		addCaption("Sales");
		addLink(CustomerListPage.class, "Customers, and what they bought");
		addLink(InvoiceListPage.class, "Sales invoices, and what was sold on them");

		addCaption("The shop itself");
		addLink(EmployeeListPage.class, "Staff, their reports and their customers");
	}
}
