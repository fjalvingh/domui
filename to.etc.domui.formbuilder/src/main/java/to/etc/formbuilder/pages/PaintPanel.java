package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.panellayout.LayoutPanelBase;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.util.IntPoint;
import to.etc.domui.util.javascript.JavascriptStmt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is the peer component of the painter representing the paint area.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2013
 */
public class PaintPanel extends Div {
	@Nullable
	private LayoutInstance m_rootLayout;

	@NonNull
	final private FormComponentRegistry m_registry;

	@NonNull
	final private PageContainer m_pageContainer = new PageContainer();

	@NonNull
	private Set<ComponentInstance> m_selectSet = new HashSet<ComponentInstance>();

	public interface ISelectionChanged {
		void selectionChanged(@NonNull Set<ComponentInstance> newSelection, Set<ComponentInstance> oldSelection) throws Exception;
	}

	@NonNull
	private List<ISelectionChanged> m_selectionChangedListeners = new ArrayList<ISelectionChanged>();

	public PaintPanel(@NonNull FormComponentRegistry registry) {
		m_registry = registry;
	}

	public void addSelectionChanged(@NonNull ISelectionChanged sel) {
		m_selectionChangedListeners.add(sel);
	}

	private void createRootLayout() {
		if(null != m_rootLayout)
			return;
		IFbComponent root = r().findComponent(LayoutPanelBase.class);
		if(null == root)
			throw new IllegalStateException("Cannot find default root layout container");
		LayoutInstance li = pc().createLayout((IFbLayout) root);
		m_rootLayout = li;
	}

	@NonNull
	private FormComponentRegistry r() {
		return m_registry;
	}

	@NonNull
	private PageContainer pc() {
		return m_pageContainer;
	}

	@NonNull
	private LayoutInstance root() {
		if(null != m_rootLayout)
			return m_rootLayout;
		throw new IllegalStateException("Root layout not set");
	}

	@Override
	public void createContent() throws Exception {
		createRootLayout();
		setCssClass("fd-pp");
		renderLayout();
	}

	private void renderLayout() throws Exception {
		renderLayout(this, m_rootLayout);
	}

	private void renderLayout(@NonNull NodeContainer target, LayoutInstance layout) throws Exception {
		NodeContainer layoutc = layout.getRendered();
		target.add(layoutc);
//		updateComponent(layout);

		for(ComponentInstance ci : layout.getComponentList()) {
			if(ci instanceof LayoutInstance) {
				renderLayout(layoutc, (LayoutInstance) ci);
			} else {
				renderComponent(layoutc, ci);
			}
		}
	}

	/**
	 * Called at full-refresh time, this calls all Javascript methods needed to sync the browser.
	 * @see to.etc.domui.dom.html.NodeBase#renderJavascriptState(to.etc.domui.util.javascript.JavascriptStmt)
	 */
	@Override
	protected void renderJavascriptState(@NonNull JavascriptStmt b) throws Exception {
		setFocus();
		renderJsState(b, m_rootLayout);
	}

	private void renderJsState(@NonNull JavascriptStmt b, @NonNull ComponentInstance ci) throws Exception {
		b.method("window._fb.registerInstance")				//
			.arg(ci.getComponentType().getTypeID())			//
			.arg(ci.getId())								//
			.arg(ci.getRendered().getActualID())			//
			.end()											//
			.next();
		if(!(ci instanceof LayoutInstance))
			return;
		for(ComponentInstance c : ((LayoutInstance) ci).getComponentList()) {
			renderJsState(b, c);
		}
	}

	private void renderComponent(@NonNull NodeContainer layoutc, @NonNull ComponentInstance ci) throws Exception {
		NodeBase inst = ci.getRendered();
		layoutc.add(inst);
//		updateComponent(ci);
	}

	private void updateComponent(@NonNull ComponentInstance ci) throws Exception {
		renderJsState(appendStatement(), ci);
//
//		appendJavascript("window._fb.registerInstance('" + ci.getComponentType().getTypeID() + "','" + ci.getId() + "','" + ci.getRendered().getActualID() + "');");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Javascript actions.									*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @param ctx
	 * @throws Exception
	 */
	public void webActionDropComponent(@NonNull JsonComponentInfo info) throws Exception {
		IFbComponent componentType = r().findComponent(info.getTypeId());
		if(null == componentType) {
			MsgBox.error(this, "Internal: no type '" + info.getTypeId() + "'");
			return;
		}
		System.out.println("Drop event: " + componentType + " @(" + info.getX() + "," + info.getY() + ")");

		ComponentInstance ci = pc().createComponent(componentType);			// Create the instance
		LayoutInstance li = m_rootLayout;

		LayoutPanelBase lpb = (LayoutPanelBase) li.getRendered();
		li.addComponent(ci);
		lpb.add(ci.getRendered(), new IntPoint(info.getX(), info.getY()));

		updateComponent(ci);
	}

	public void webActionMoveComponent(@NonNull JsonComponentInfo info) throws Exception {
		System.out.println("Move event: " + info.getId() + " @(" + info.getX() + "," + info.getY() + ")");
		ComponentInstance ci = pc().getComponent(info.getId());
		LayoutInstance layout = ci.getParent();
		if(null == layout)
			throw new IllegalStateException("Moving a thingy that is not part of a layout?");

		layout.positionComponent(ci, new IntPoint(info.getX(), info.getY()));
	}

	public void webActionSelection(@NonNull Set<String> idlist) throws Exception {
		Set<ComponentInstance> selectSet = new HashSet<ComponentInstance>();
		for(String id : idlist) {
			ComponentInstance component = pc().getComponent(id);
			selectSet.add(component);
		}
		Set<ComponentInstance> oldSet = m_selectSet;
		m_selectSet = selectSet;
		for(ISelectionChanged ss : m_selectionChangedListeners)
			ss.selectionChanged(selectSet, oldSet);

	}


}
