package to.etc.domui.pages.generic;

import java.text.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.title.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.component.ntbl.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.server.ServerClientRegistry.Client;
import to.etc.domui.util.*;
import to.etc.util.*;

public class CurrentlyLoggedInUsersPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		createHeader();
		List<Client> activeClients = ServerClientRegistry.getInstance().getActiveClients();
		Collections.sort(activeClients, new Comparator<Client>() {
			@Override
			public int compare(Client o1, Client o2) {
				String a = o1.getRemoteUser();
				String b = o2.getRemoteUser();
				if(a == b)
					return 0;
				else if(a == null)
					return -1;
				else if(b == null)
					return 1;
				else
					return a.compareTo(b);
			}
		});

		final long cts = System.currentTimeMillis();
//		TBody b = addTable("UserID", "IP Address/host", "#requests", "Logged in since", "Last use");
//		b.getTable().setCssClass("listtbl");
//		b.getTable().setCellPadding("0");
//		b.getTable().setCellSpacing("0");

		final DateFormat df = new SimpleDateFormat("MM/dd HH:mm:ss");

		SortableListModel<Client> sm = new SortableListModel<Client>(Client.class, activeClients);
		RowRenderer<Client> rr = new RowRenderer<Client>(Client.class);

		rr.column("remoteUser").width("10%").label("User ID");
		rr.column().label("IP Address/host").width("1%").renderer(new INodeContentRenderer<Client>() {
			@Override
			public void renderNodeContent(@Nonnull NodeBase component, @Nonnull NodeContainer node, @Nullable Client cl, @Nullable Object parameters) throws Exception {
				if(null == cl)
					return;
				node.add(cl.getRemoteAddress() + "/" + cl.getRemoteHost());
			}
		});
		rr.column("NRequests").width("1%").label("# requests");
		rr.column(Long.class, "tsSessionStart").width("1%").label("Logged in since").descending().renderer(new INodeContentRenderer<Long>() {
			@Override
			public void renderNodeContent(@Nonnull NodeBase component, @Nonnull NodeContainer node, @Nullable Long cl, @Nullable Object parameters) throws Exception {
				if(null == cl)
					return;
				long ts = cl.longValue();
				node.add(df.format(new Date(ts)) + " (" + StringTool.strDurationMillis(cts - ts) + ")");
			}
		});

		rr.column(Long.class, "tsLastRequest").width("1%").label("Last use").sortdefault().descending().renderer(new INodeContentRenderer<Long>() {
			@Override
			public void renderNodeContent(@Nonnull NodeBase component, @Nonnull NodeContainer node, @Nullable Long cl, @Nullable Object parameters) throws Exception {
				if(null == cl)
					return;
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
