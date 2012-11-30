package to.etc.domui.component.input;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Input component to select small things from a dataset. Each selected thing is shown as a
 * small "label" in a horizontal list of them, and each thing can be removed by a button on
 * it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 6, 2011
 */
public class LabelSelector<T> extends Div implements IControl<List<T>> {
	@Nonnull
	private Class<T> m_actualClass;

	@Nonnull
	private List<T> m_labelList = new ArrayList<T>();

	private Map<T, Span> m_divMap = new HashMap<T, Span>();

	private SearchInput<T> m_input;

	private INodeContentRenderer<T> m_contentRenderer;

	public interface ISearch<T> {
		T find(String name) throws Exception;

		List<T> findLike(String input, int i);
	}

	public interface INew<T> {
		@Nullable
		T create(String name) throws Exception;
	}

	@Nonnull
	final private ISearch<T> m_search;

	@Nullable
	private INew<T> m_instanceFactory;

	//	/** Set to allow new things to be created. */
	//	private IFactory<T> m_instanceFactory;

	public LabelSelector(@Nonnull Class<T> clz, @Nonnull ISearch<T> search) {
		m_actualClass = clz;
		m_search = search;
		setCssClass("ui-lsel");
	}

	//	/**
	//	 * Constructor to use DB.
	//	 * @param clz
	//	 * @param propertyName
	//	 */
	//	public LabelSelector(@Nonnull Class<T> clz, @Nonnull String propertyName) {
	//		m_actualClass = clz;
	//		setCssClass("ui-lsel-sel");
	//	}


	/**
	 * We create something which looks like an iput box, but it has label spans followed by a single input box.
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		m_divMap.clear();
		for(T lbl : m_labelList) {
			add(createLabel(lbl));
		}
		m_input = new SearchInput<T>(m_actualClass);
		add(m_input);
		m_input.setHandler(new SearchInput.IQuery<T>() {
			@Override
			public List<T> queryFromString(String input, int max) throws Exception {
				return queryLabels(input, max);
			}

			@Override
			public void onSelect(T instance) throws Exception {
				addLabel(instance);
			}

			@Override
			public void onEnter(String value) throws Exception {
				insertLabel(value);
			}
		});
	}

	/**
	 * This queries for labels with the specified name. It does so by querying the db, and removes
	 * the labels that are already in the current list from the result.
	 * @param input
	 * @param max
	 * @return
	 * @throws Exception
	 */
	private List<T> queryLabels(String input, int max) throws Exception {
		input = input.trim();
		if(input.length() < 1)
			return null;

		List<T> isl = m_search.findLike(input, max + m_labelList.size() + 1);
		for(T tisl : m_labelList) {
			isl.remove(tisl);					// Remove all that has been entered before
		}
		return isl;
	}

	/**
	 * Called after enter, it will actually create a new label if needed and allowed.
	 * @param value
	 * @throws Exception
	 */
	private void insertLabel(String value) throws Exception {
		value = value.toLowerCase().trim();
		if(value.length() <= 1)
			return;

		T sel = m_search.find(value);	// Find by this name (full)
		if(null != sel) {
			//-- Item by name exists. Not in already selected list?
			for(T il : m_labelList) {
				if(MetaManager.areObjectsEqual(value, il))
					return;
			}
			addLabel(sel);				// Just add the thingy.
			return;
		}

		//-- Name does not exist -> create..
		INew<T> ifa = m_instanceFactory;
		if(ifa == null)
			return;
		sel = ifa.create(value);
		if(null == sel)
			return;
		addLabel(sel);					// Just add the thingy.
	}

	private void addLabel(@Nonnull T instance) throws Exception {
		if(m_divMap.containsKey(instance))
			return;
		m_labelList.add(instance);
		Span s = createLabel(instance);
		m_input.appendBeforeMe(s);
	}

	private Span createLabel(final T lbl) throws Exception {
		final Span d = new Span();
		m_divMap.put(lbl, d);
		d.setCssClass("ui-lsel-item");

		if(m_contentRenderer == null)
			d.add(lbl.toString());
		else
			m_contentRenderer.renderNodeContent(this, d, lbl, null);
		Div btn = new Div();
		btn.setCssClass("ui-lsel-btn");
		d.add(btn);
//
//		Img i = new Img("THEME/lsel-delete.png");
//		d.add(i);
		btn.setClicked(new IClicked<Div>() {
			@Override
			public void clicked(Div clickednode) throws Exception {
				d.remove();
				m_labelList.remove(lbl);
				m_divMap.remove(lbl);
			}
		});

		return d;
	}

	public boolean isEnableAdding() {
		return false;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	IControl interface.									*/
	/*--------------------------------------------------------------*/

	public INodeContentRenderer<T> getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(INodeContentRenderer<T> contentRenderer) {
		m_contentRenderer = contentRenderer;
	}

	@Override
	public void setValue(@Nullable List<T> newlist) {
		m_labelList = newlist == null ? new ArrayList<T>() : newlist;
		m_divMap.clear();
		forceRebuild();
	}

	@Override
	public List<T> getValue() {
		return new ArrayList<T>(m_labelList);
	}

	@Override
	public List<T> getValueSafe() {
		return getValue();
	}

	@Override
	public void setDisabled(boolean d) {
	}

	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return null;
	}

	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public void setReadOnly(boolean ro) {
	}

	@Override
	public boolean isDisabled() {
		return false;
	}

	@Override
	public boolean isMandatory() {
		return false;
	}

	@Override
	public void setMandatory(boolean ro) {
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface (EXPERIMENTAL)					*/
	/*--------------------------------------------------------------*/

	/** When this is bound this contains the binder instance handling the binding. */
	private SimpleBinder m_binder;

	/**
	 * Return the binder for this control.
	 * @see to.etc.domui.component.input.IBindable#bind()
	 */
	@Override
	@Nonnull
	public IBinder bind() {
		if(m_binder == null)
			m_binder = new SimpleBinder(this);
		return m_binder;
	}

	/**
	 * Returns T if this control is bound to some data value.
	 *
	 * @see to.etc.domui.component.input.IBindable#isBound()
	 */
	@Override
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
	}



}
