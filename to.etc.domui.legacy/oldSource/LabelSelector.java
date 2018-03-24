package to.etc.domui.component.input;

import to.etc.domui.component.input.SearchAsYouTypeBase.ITypingListener;
import to.etc.domui.component.input.SearchAsYouTypeBase.Result;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.Msgs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FIXME Functional duplicate of EnumSetInput
 *
 * Input component to select small things from a dataset. Each selected thing is shown as a
 * small "label" in a horizontal list of them, and each thing can be removed by a button on
 * it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 6, 2011
 */
public class LabelSelector<T> extends Div implements IControl<List<T>>, ITypedControl<T> {

	private static final int MAX_LABELS_IN_TOOLTIP = 10;

	@Nonnull
	private Class<T> m_actualClass;

	@Nonnull
	private List<T> m_labelList = new ArrayList<T>();

	@Nonnull
	final private Map<T, Span> m_divMap = new HashMap<T, Span>();

	@Nullable
	private SearchAsYouTypeBase<T> m_input;

	@Nullable
	private IRenderInto<T> m_contentRenderer;

	public interface ISearch<T> {
		@Nullable
		T find(@Nonnull String name) throws Exception;

		@Nonnull
		List<T> findLike(@Nonnull String input, int i) throws Exception;
	}

	public interface INew<T> {
		@Nullable
		T create(@Nonnull String name) throws Exception;
	}

	public interface IAllow<T> {
		boolean allowSelection(@Nonnull T instance) throws Exception;
	}

	@Nonnull
	final private ISearch<T> m_search;

	@Nullable
	private IAllow<T> m_allowCheck;

	@Nullable
	private INew<T> m_instanceFactory;

	@Nullable
	private IValueChanged< ? > m_onValueChanged;

	private boolean m_disabled;

	private boolean m_defaultTooltip = true;

	public LabelSelector(@Nonnull Class<T> clz, @Nonnull ISearch<T> search) {
		m_actualClass = clz;
		m_search = search;
		setCssClass("ui-lsel");
		if(search instanceof INew< ? >) {
			m_instanceFactory = (INew<T>) search;
		}
	}

	/**
	 * We create something which looks like an input box, but it has label spans followed by a single input box.
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		m_divMap.clear();
		for(T lbl : m_labelList) {
			add(createLabel(lbl));
		}

		if(! m_disabled) {
			SearchAsYouTypeBase<T> input = m_input = new SearchAsYouTypeBase<T>("ui-lsel", m_actualClass);
			add(input);
			updateTooltip();
			input.setHandler(new ITypingListener<T>() {
				@Override
				public Result<T> queryFromString(String input, int max) throws Exception {
					return queryLabelsOnType(input, max);
				}

				@Override
				public void onSelect(T instance) throws Exception {
					addLabelOnInput(instance);
				}

				@Override
				public void onEnter(String value) throws Exception {
					insertLabel(value);
				}
			});
		}
	}

	@Nullable @Override public NodeBase getForTarget() {
		SearchAsYouTypeBase<T> input = m_input;
		if(null != input)
			return input.getForTarget();
		return null;
	}

	private void updateTooltip() throws Exception {
		if(isDefaultTooltip()) {
			fillTooltipWithAvailableLabels();
		}
	}

	private void fillTooltipWithAvailableLabels() throws Exception {
		StringBuilder sb = new StringBuilder();
		final List<T> availableLabels = getAvailableLabels();
		for(int i = 0; i < availableLabels.size(); i++) {
			if(i > 0) {
				sb.append(", ");
			}
			sb.append(availableLabels.get(i).toString());
		}
		DomUtil.nullChecked(m_input).setTitle(Msgs.BUNDLE.formatMessage(Msgs.UI_KEYWORD_SEARCH_HINT, sb.toString()));
	}

	/**
	 * This queries for labels with the specified name. It does so by querying the db, and removes
	 * the labels that are already in the current list from the result.
	 * @param input
	 * @param max
	 * @return
	 * @throws Exception
	 */
	private Result<T> queryLabelsOnType(String input, int max) throws Exception {
		input = input.trim();
		if(input.length() < 1)
			return null;

		return getLabels(input, max);
	}

	private List<T> getAvailableLabels() throws Exception {
		return getLabels("", MAX_LABELS_IN_TOOLTIP).getList();
	}

	private Result<T> getLabels(String input, int max) throws Exception {
		List<T> isl = m_search.findLike(input, max + m_labelList.size() + 1);
		for(T tisl : m_labelList) {
			isl.remove(tisl);					// Remove all that has been entered before
		}
		return new Result<>(isl, null);
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

		T sel = m_search.find(value);						// Find by this name (full)
		if(null != sel) {
			//-- Item by name exists. Not in already selected list?
			for(T il : m_labelList) {
				if(MetaManager.areObjectsEqual(value, il))
					return;
			}
			addLabelOnInput(sel);				// Just add the thingy.
			return;
		}

		//-- Name does not exist -> create..
		INew<T> ifa = m_instanceFactory;
		if(ifa == null)
			return;
		sel = ifa.create(value);
		if(null == sel)
			return;
		addLabelOnInput(sel);					// Just add the thingy.
	}

	private void addLabelOnInput(@Nonnull T instance) throws Exception {
		if(m_divMap.containsKey(instance))
			return;

		//-- Is adding this value allowed?
		if(null != m_allowCheck) {
			boolean ok = m_allowCheck.allowSelection(instance);
			if(!ok)
				return;
		}

		addItem(instance);
	}

	/**
	 * Call to add a selected item to the control.
	 * @param instance
	 * @throws Exception
	 */
	public void addItem(@Nonnull T instance) throws Exception {
		addLabel(instance);										// Just add the thingy.
	}

	public void setInstanceFactory(INew<T> instanceFactory) {
		m_instanceFactory = instanceFactory;
	}

	private void addLabel(@Nonnull T instance) throws Exception {
		if(m_divMap.containsKey(instance))
			return;
		m_labelList.add(instance);
		Span s = createLabel(instance);
		if(m_input != null)
			m_input.appendBeforeMe(s);
		else
			add(s);
		callValueChanged();
	}

	/**
	 * Call to remove an item from the control.
	 * @param instance
	 * @throws Exception
	 */
	public void removeItem(@Nonnull T instance) throws Exception {
		Span span = m_divMap.get(instance);
		if(span == null)
			return;
		span.remove();
		m_labelList.remove(instance);
		m_divMap.remove(instance);
	}


	private Span createLabel(@Nonnull final T lbl) throws Exception {
		final Span d = new Span();
		m_divMap.put(lbl, d);
		d.setCssClass("ui-lsel-item");

		IRenderInto<T> contentRenderer = m_contentRenderer;
		if(contentRenderer == null)
			d.add(lbl.toString());
		else
			contentRenderer.render(d, lbl);

		if(!m_disabled) {
			Div btn = new Div();
			btn.setCssClass("ui-lsel-btn");
			d.add(btn);
			btn.setClicked(new IClicked<Div>() {
				@Override
				public void clicked(@Nonnull Div clickednode) throws Exception {
					d.remove();
					m_labelList.remove(lbl);
					m_divMap.remove(lbl);
					callValueChanged();
				}
			});
		}

		return d;
	}

	private void callValueChanged() throws Exception {
		if(null != m_onValueChanged) {
			((IValueChanged<LabelSelector<T>>) m_onValueChanged).onValueChanged(this);
		}
		updateTooltip();
	}

	public boolean isEnableAdding() {
		return false;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	IControl interface.									*/
	/*--------------------------------------------------------------*/

	@Nullable
	public IRenderInto<T> getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(@Nullable IRenderInto<T> contentRenderer) {
		m_contentRenderer = contentRenderer;
	}

	@Override
	public void setValue(@Nullable List<T> newlist) {
		if(null == newlist)
			newlist = new ArrayList<T>();
		else
			newlist = new ArrayList<T>(newlist);
		m_labelList = newlist;
		m_divMap.clear();
		forceRebuild();
	}

	@Nonnull
	@Override
	public List<T> getValue() {
		return new ArrayList<T>(m_labelList);
	}

	@Nonnull
	@Override
	public List<T> getValueSafe() {
		return getValue();
	}

	@Nullable
	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return null;
	}

	@Override
	public void setOnValueChanged(@Nullable IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	@Override
	public boolean isReadOnly() {
		return isDisabled();
	}

	@Override
	public void setReadOnly(boolean ro) {
		setDisabled(ro);
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean d) {
		if(m_disabled == d)
			return;
		m_disabled = d;
		forceRebuild();
	}

	@Override
	public boolean isMandatory() {
		return false;
	}

	@Override
	public void setMandatory(boolean ro) {
	}

	@Nullable
	public IAllow<T> getAllowCheck() {
		return m_allowCheck;
	}

	public void setAllowCheck(@Nullable IAllow<T> allowCheck) {
		m_allowCheck = allowCheck;
	}

	public boolean isDefaultTooltip() {
		return m_defaultTooltip;
	}

	public void setDefaultTooltip(boolean defaultTooltip) {
		m_defaultTooltip = defaultTooltip;
	}

	@Nonnull @Override public Class<T> getActualType() {
		return m_actualClass;
	}


}
