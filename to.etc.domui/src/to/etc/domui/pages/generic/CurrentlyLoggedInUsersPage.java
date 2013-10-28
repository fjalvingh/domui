package to.etc.domui.pages.generic;

import java.text.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.title.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.server.ServerClientRegistry.Client;
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

		long cts = System.currentTimeMillis();

		Div content = new Div();
		content.setCssClass("ui-dt");
		add(content);

		Table t = new Table();
		content.add(t);
		t.getHead().setHeaders("UserID", "IP Address/host", "#requests", "Logged in since", "Last use", "Used pages");
		TBody b = new TBody();
		t.add(b);

		b.getTable().setCssClass("ui-dt");
		b.getTable().setCellPadding("0");
		b.getTable().setCellSpacing("0");

		final DateFormat df = new SimpleDateFormat("MM/dd HH:mm:ss");

		for(Client cl : activeClients) {
			final TR tr = b.addRow();
			final Client fcl = cl;
			TD td = tr.addCell();
			td.setText(cl.getRemoteUser());

			tr.addCell().setText(cl.getRemoteAddress() + "/" + cl.getRemoteHost());
			tr.addCell().setText(cl.getNRequests() + "");
			tr.addCell().setText(df.format(new Date(cl.getTsSessionStart())) + " (" + StringTool.strDurationMillis(cts - cl.getTsSessionStart()) + ")");

			tr.addCell().setText(df.format(new Date(cl.getTsLastRequest())) + " (" + StringTool.strDurationMillis(cts - cl.getTsLastRequest()) + " ago)");
			final LinkButton lb = new LinkButton("Last Used pages", new IClicked<LinkButton>() {
				@Override
				public void clicked(@Nonnull LinkButton clickednode) throws Exception {
					TR ntr = new TR();
					tr.appendAfterMe(ntr);
					ntr.addCell();
					TD d = ntr.addCell();
					d.setColspan(5);

					TBody nb = d.addTable("When", "Url");
					nb.getTable().setCssClass("listtbl vp-inline-edit");
					nb.getTable().setWidth("80%");
					long cts = System.currentTimeMillis();

					for(ServerClientRegistry.Use u : fcl.getLastUseList()) {
						nb.addRowAndCell().setText(df.format(new Date(u.getTimeStamp())) + " (" + StringTool.strDurationMillis(cts - u.getTimeStamp()) + " ago)");
						nb.addCell().setText(u.getUrl());
					}
					clickednode.remove();
				}
			});
			tr.addCell().add(lb);
		}
	}

	protected void createHeader() {
		add(new AppPageTitleBar("Users currently using this system", true));
	}
}
