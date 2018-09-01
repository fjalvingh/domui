package to.etc.domui.util.bugs.contributors;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.domui.component.tbl.PageableTabularComponentBase;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.component.tbl.TableModelTableBase;
import to.etc.domui.dom.PrettyXmlOutputWriter;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Page;
import to.etc.domui.login.IUser;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.state.AppSession;
import to.etc.domui.state.UIContext;
import to.etc.domui.state.UserLogItem;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.Msgs;
import to.etc.domui.util.bugs.BugItem;
import to.etc.domui.util.bugs.IBugInfoContributor;
import to.etc.util.IndentWriter;
import to.etc.util.StringTool;
import to.etc.webapp.query.QCriteria;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This contributor collects information when a bug is reported with a DomUI page
 * being current on the reporting thread, or when a DomUI request context is part
 * of the bug's issue context.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-8-18.
 */
final public class DomUIBugContributor implements IBugInfoContributor {
	private static final int MAX_VALUE_SIZE = 200;

	@Override public void onContribute(BugItem bug) throws Exception {
		RequestContextImpl requestContext = bug.findContextItem(RequestContextImpl.class);
		if(null == requestContext) {
			requestContext = (RequestContextImpl) UIContext.internalGetContext();
			if(null == requestContext)
				return;
		}

		//-- We are in a DomUI context - render the data
		StringContribution sb = new StringContribution();
		sb.append("\n\nDomUI Page context\n");

		try {
			IUser user = UIContext.getCurrentUser();
			if(null != user) {
				sb.append("User name: ").append(user.getDisplayName()).append(", login id ").append(user.getLoginID()).append("\n");
			}
		} catch(Exception xxx) {}

		sb.append("Page name: ").append(requestContext.getInputPath()).append("\n");

		sb.append("\n\nPage input parameters\n");

		String[] names = requestContext.getParameterNames();
		if(names != null) {
			for(String name : names) {
				boolean first = true;
				String[] values = requestContext.getParameters(name);
				if(values == null || values.length == 0) {
					sb.append(name).append(": ");
					sb.append("No value\n");
				} else {
					for(String value : values) {
						if(first)
							sb.append(name).append(": ");
						else
							sb.append(StringTool.strToFixedLength("", name.length())).append(": ");
						first = false;
						sb.append(value).append("\n");
					}
				}
			}
		}

		sb.append("\nClick/Event stream (new to old)\n");

		AppSession session = requestContext.getSession();
		//                                     012345678901 012345678901234567890123456789 0123456789012345678901234
		//                                     -13s 999ms   0MC0ZakN00016ddnzHC00FYG.c3    LocalEnvironmentsPage
		sb.append(StringTool.strToFixedLength("Time", 12));
		sb.append(StringTool.strToFixedLength("CID", 30));
		sb.append(StringTool.strToFixedLength("Page", 24));
		sb.append("Message\n");
		List<UserLogItem> logItems = new ArrayList<>(session.getLogItems());
		Collections.reverse(logItems);
		for(UserLogItem li : logItems) {
			sb.append(StringTool.strToFixedLength(li.time(), 12));
			sb.append(StringTool.strToFixedLength(li.getCid(), 30));
			sb.append(StringTool.strToFixedLength(lastName(li.getPage()), 24));
			sb.append(li.getText()).append("\n");
		}

		//-- Page content/context



		bug.addContribution(sb);
	}

	private void renderDomTree(StringBuilder sb) throws Exception {
		Page page = UIContext.internalGetPage();
		if(null == page)
			return;

		sb.append("\nPage structure and component values\n");
		sb.append("Page class: ").append(page.getBody().getClass()).append("\n");

		String	s = Div.class.getName();
		int pos = s.lastIndexOf('.');
		final String dhtml = s.substring(0, pos+1);		// Package of all DomUI html classes
		final String domui = "to.etc.domui.";			// Root hierarchy of native stuff

		final StringWriter comp = new StringWriter();
		final IndentWriter ciw = new IndentWriter(comp);
		final StringWriter	sw	= new StringWriter(8192);
		final PrettyXmlOutputWriter xw = new PrettyXmlOutputWriter(sw);

		DomUtil.walkTree(page.getBody(), new DomUtil.IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				ciw.inc();
				try {
					String cn = n.getClass().getName();
					String name = cn;
					if(name.startsWith(domui))
						name = name.substring(name.lastIndexOf('.')+1);

					if(n instanceof IControl<?>) {
						renderControlValue(n, name);
						ciw.dec();				// after will not be calld.
						return SKIP;			// Do not descend into components.
					} else if(n instanceof TableModelTableBase<?>) {
						renderTableInfo((TableModelTableBase<?>) n, name);
						ciw.dec();				// after will not be calld.
						return SKIP;			// Do not descend into components.
					}
				} catch(Exception x) {
				}
				return null;
			}

			private void renderTableInfo(TableModelTableBase<?> n, String name) throws IOException {
				TableModelTableBase<?>	tmb = n;
				String mdlname = null;
				String query = null;
				String page = "";
				try {
					ITableModel<?> modl = tmb.getModel();
					mdlname = String.valueOf(modl);

					if(modl instanceof SimpleSearchModel) {
						SimpleSearchModel<?> ssm = (SimpleSearchModel<?>) modl;
						QCriteria<?> qc = ssm.getQuery();
						query = qc.toString();
					}
					if(tmb instanceof PageableTabularComponentBase) {
						PageableTabularComponentBase<?> pcb = (PageableTabularComponentBase<?>) tmb;
						page = Integer.toString( pcb.getCurrentPage() );
						int total = pcb.getPageCount();
						page += " of " + total;
					}
				} catch(Exception x) {
				}

				ciw.append("TableModelTable[").append(name).append("] model=").append(mdlname);
				ciw.append(", page ").append(page);
				ciw.append("\n");
				if(query != null) {
					ciw.inc();
					ciw.append("query=").append(query).append("\n");
					ciw.dec();
				}
			}

			private void renderControlValue(NodeBase n, String name) throws IOException {
				Object value = null;
				UIMessage msg = null;
				Exception error = null;
				try {
					value = ((IControl<Object>)n).getValue();
				} catch(Exception x) {
					error = x;
				}
				try {
					msg = n.getMessage();
				} catch(Exception x) {
				}

				if(msg == null && error != null) {
					msg = UIMessage.error(Msgs.BUNDLE, Msgs.VERBATIM, "exception: " + error);
				}

				String val$;
				if(value == null) {
					val$ = "null";
				} else if(DomUtil.isBasicType(value.getClass())) {
					val$ = String.valueOf(value);
				} else {
					ClassMetaModel cmm = MetaManager.findClassMeta(value.getClass());
					if(cmm.isPersistentClass())
						val$ = MetaManager.identify(value);
					else
						val$ = String.valueOf(value);
				}
				if(val$.length() > MAX_VALUE_SIZE) {
					val$ = val$.substring(0, MAX_VALUE_SIZE) + "... (truncated from " + val$.length() + " chars)";
				}

				ciw.append("comp[").append(name).append("] testid=").append(n.getTestID());
				if(value == null)
					ciw.append(", value=null\n");
				else {
					ciw.inc();
					ciw.append(", value=").append(val$);
					ciw.append(", class=").append(value.getClass().getName());
					if(null != msg) {
						ciw.append(", message=").append(msg.toString());
					}
					ciw.append("\n");
					ciw.dec();
				}
			}

			@Override
			public Object after(NodeBase n) throws Exception {
				ciw.dec();
				return null;
			}
		});

		ciw.flush();
		sb.append(comp.getBuffer());
	}

	@Nullable
	public String lastName(@Nullable String name) {
		if(null == name)
			return null;
		return name.substring(name.lastIndexOf('.') + 1);
	}

}
