package to.etc.domui.pages.generic;

import java.text.*;
import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.layout.title.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.server.ServerClientRegistry.Client;
import to.etc.util.*;

public class CurrentlyLoggedInUsersPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new AppPageTitleBar("Users currently using this system", true));
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
		TBody b = addTable("UserID", "IP Address/host", "#requests", "Logged in since", "Last use");
		b.getTable().setCssClass("listtbl");
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
				public void clicked(LinkButton clickednode) throws Exception {
					TR ntr = new TR();
					tr.appendAfterMe(ntr);
					ntr.addCell();
					TD d = ntr.addCell();
					d.setColspan(4);
					d.add(new CaptionedHeader("Used pages"));

					TBody nb = d.addTable("When", "Url");
					nb.getTable().setCssClass("listtbl");
					nb.getTable().setCellPadding("0");
					nb.getTable().setCellSpacing("0");
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
}
