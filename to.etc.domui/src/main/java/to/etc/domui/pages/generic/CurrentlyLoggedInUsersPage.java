package to.etc.domui.pages.generic;

import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.layout.title.AppPageTitleBar;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.ntbl.IRowButtonFactory;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowButtonContainer;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SortableListModel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.ServerClientRegistry;
import to.etc.domui.server.ServerClientRegistry.Client;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IRenderInto;
import to.etc.util.StringTool;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CurrentlyLoggedInUsersPage extends UrlPage {

	@Override
	public void createContent() throws Exception {
		createHeader();
		List<Client> activeClients = ServerClientRegistry.getInstance().getActiveClients();
		Collections.sort(activeClients, (o1, o2) -> DomUtil.compareNullableOnFunctions(o1, o2, String::compareTo, Client::getRemoteUser));

		final long cts = System.currentTimeMillis();
//		TBody b = addTable("UserID", "IP Address/host", "#requests", "Logged in since", "Last use");
//		b.getTable().setCssClass("listtbl");
//		b.getTable().setCellPadding("0");
//		b.getTable().setCellSpacing("0");

		final DateFormat df = new SimpleDateFormat("MM/dd HH:mm:ss");

		SortableListModel<Client> sm = new SortableListModel<Client>(Client.class, activeClients);
		RowRenderer<Client> rr = new RowRenderer<Client>(Client.class);

		rr.column("remoteUser").width("10%").label("User ID");
		rr.column().label("IP Address/host").width("1%").renderer(new IRenderInto<Client>() {
			@Override
			public void render(@Nonnull NodeContainer node, @Nonnull Client cl) throws Exception {
				node.add(cl.getRemoteAddress() + "/" + cl.getRemoteHost());
			}
		});
		rr.column("NRequests").width("1%").label("# requests");
		rr.column(Long.class, "tsSessionStart").width("1%").label("Logged in since").descending().renderer(new IRenderInto<Long>() {
			@Override
			public void render(@Nonnull NodeContainer node, @Nonnull Long cl) throws Exception {
				long ts = cl.longValue();
				node.add(df.format(new Date(ts)) + " (" + StringTool.strDurationMillis(cts - ts) + ")");
			}
		});

		rr.column(Long.class, "tsLastRequest").width("1%").label("Last use").sortdefault().descending().renderer(new IRenderInto<Long>() {
			@Override
			public void render(@Nonnull NodeContainer node, @Nonnull Long cl) throws Exception {
				long ts = cl.longValue();
				node.add(df.format(new Date(ts)) + " (" + StringTool.strDurationMillis(cts - ts) + ")");
			}
		});

		rr.setRowButtonFactory(new IRowButtonFactory<Client>() {
			@Override
			public void addButtonsFor(@Nonnull RowButtonContainer c, @Nonnull final Client data) throws Exception {
				c.addLinkButton("Last used pages", "THEME/btnEdit.png", new IClicked<LinkButton>() {
					@Override
					public void clicked(@Nonnull LinkButton clickednode) throws Exception {
						showClientData(data);
					}
				});
			}
		});

		DataTable<Client> dt = new DataTable<>(sm, rr);
		add(dt);
		dt.setPageSize(50);
		add(new DataPager(dt));
//
//
//		for(Client cl : activeClients) {
//			final TR tr = b.addRow();
//			final Client fcl = cl;
//			TD td = tr.addCell();
//			td.setText(cl.getRemoteUser());
//
//			tr.addCell().setText();
//
//			tr.addCell().setText(df.format(new Date(cl.getTsLastRequest())) + " (" + StringTool.strDurationMillis(cts - cl.getTsLastRequest()) + " ago)");
//			final LinkButton lb = new LinkButton("Last Used pages", new IClicked<LinkButton>() {
//				@Override
//				public void clicked(@Nonnull LinkButton clickednode) throws Exception {
//					TR ntr = new TR();
//					tr.appendAfterMe(ntr);
//					ntr.addCell();
//					TD d = ntr.addCell();
//					d.setColspan(4);
//					d.add(new CaptionedHeader("Used pages"));
//
//					TBody nb = d.addTable("When", "Url");
//					nb.getTable().setCssClass("listtbl");
//					nb.getTable().setCellPadding("0");
//					nb.getTable().setCellSpacing("0");
//					long cts = System.currentTimeMillis();
//
//					for(ServerClientRegistry.Use u : fcl.getLastUseList()) {
//						nb.addRowAndCell().setText(df.format(new Date(u.getTimeStamp())) + " (" + StringTool.strDurationMillis(cts - u.getTimeStamp()) + " ago)");
//						nb.addCell().setText(u.getUrl());
//					}
//					clickednode.remove();
//				}
//			});
//			tr.addCell().add(lb);
//		}
	}

	protected void showClientData(Client fcl) {
		Div d = new Div();
		TBody nb = d.addTable("When", "Url");
		nb.getTable().setCssClass("listtbl");
		nb.getTable().setCellPadding("0");
		nb.getTable().setCellSpacing("0");
		long cts = System.currentTimeMillis();

		final DateFormat df = new SimpleDateFormat("MM/dd HH:mm:ss");

		for(ServerClientRegistry.Use u : fcl.getLastUseList()) {
			TD td = nb.addRowAndCell();
			td.setNowrap(true);
			td.setText(df.format(new Date(u.getTimeStamp())) + " (" + StringTool.strDurationMillis(cts - u.getTimeStamp()) + " ago)");

			td = nb.addCell();
			td.setNowrap(true);
			td.setText(u.getUrl());
		}

		MsgBox.info(this, d);

	}

	protected void createHeader() {
		add(new AppPageTitleBar("Users currently using this system", true));
	}
}
