package to.etc.formbuilder.pages;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.misc.*;
import to.etc.domui.component.panellayout.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.domui.util.javascript.*;

/**
 * This is the peer component of the painter representing the paint area.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2013
 */
public class PaintPanel extends Div {
	@Nullable
	private LayoutInstance m_rootLayout;

	@Nonnull
	final private FormComponentRegistry m_registry;

	@Nonnull
	final private PageContainer m_pageContainer = new PageContainer();

	@Nonnull
	private Set<ComponentInstance> m_selectSet = new HashSet<ComponentInstance>();

	public interface ISelectionChanged {
		void selectionChanged(@Nonnull Set<ComponentInstance> newSelection, Set<ComponentInstance> oldSelection) throws Exception;
	}

	@Nonnull
	private List<ISelectionChanged> m_selectionChangedListeners = new ArrayList<ISelectionChanged>();

	public PaintPanel(@Nonnull FormComponentRegistry registry) {
		m_registry = registry;
	}

	public void addSelectionChanged(@Nonnull ISelectionChanged sel) {
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

	@Nonnull
	private FormComponentRegistry r() {
		return m_registry;
	}

	@Nonnull
	private PageContainer pc() {
		return m_pageContainer;
	}

	@Nonnull
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

	private void renderLayout(@Nonnull NodeContainer target, LayoutInstance layout) throws Exception {
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
	protected void renderJavascriptState(@Nonnull JavascriptStmt b) throws Exception {
		setFocus();
		renderJsState(b, m_rootLayout);
	}

	private void renderJsState(@Nonnull JavascriptStmt b, @Nonnull ComponentInstance ci) throws Exception {
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

	private void renderComponent(@Nonnull NodeContainer layoutc, @Nonnull ComponentInstance ci) throws Exception {
		NodeBase inst = ci.getRendered();
		layoutc.add(inst);
//		updateComponent(ci);
	}

	private void updateComponent(@Nonnull ComponentInstance ci) throws Exception {
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
	public void webActionDropComponent(@Nonnull JsonComponentInfo info) throws Exception {
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

	public void webActionMoveComponent(@Nonnull JsonComponentInfo info) throws Exception {
		System.out.println("Move event: " + info.getId() + " @(" + info.getX() + "," + info.getY() + ")");
		ComponentInstance ci = pc().getComponent(info.getId());
		LayoutInstance layout = ci.getParent();
		if(null == layout)
			throw new IllegalStateException("Moving a thingy that is not part of a layout?");

		layout.positionComponent(ci, new IntPoint(info.getX(), info.getY()));
	}

	public void webActionSelection(@Nonnull Set<String> idlist) throws Exception {
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
