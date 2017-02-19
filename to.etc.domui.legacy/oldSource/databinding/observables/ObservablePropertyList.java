package to.etc.domui.databinding.observables;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.databinding.*;
import to.etc.domui.databinding.list.*;
import to.etc.domui.databinding.list2.*;
import to.etc.util.*;

/**
 * The thingy that describes a property containing a list that can be observed. This
 * both handles "property assignments" where a new list value is assigned to the property
 * (mimicking what {@link ObservablePropertyValue} does) but also handles list content
 * change reporting.
 *
 * <p>This instance will post a listener on the observable list <i>inside</i> the property
 * as soon as listeners on this instance are added. The listener on the observable list
 * will "propagate" changes on the list itself to the listeners for this property. If the
 * property value itself changes the listener is removed from the "old" list and re-added
 * to the "new" one, and a {@link ListChangeAssign} event gets fired.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 31, 2013
 */
public class ObservablePropertyList<C, T> extends ListenerList<List<T>, ListValueChangeEvent<T>, IListValueChangeListener<T>> implements IObservableListValue<T>, IPropertyChangeNotifier {
	@Nonnull
	final private C m_instance;

	@Nonnull
	final private PropertyMetaModel<List<T>> m_property;

	/**
	 * Contains the list event adapter for the current observable list inside the property, if one has
	 * been added.
	 */
	@Nullable
	private ListEventAdapter m_listEventAdapter;

	public ObservablePropertyList(@Nonnull C instance, @Nonnull PropertyMetaModel<List<T>> property) {
		m_instance = instance;
		m_property = property;
	}

	/**
	 * Get the property's current value (which should be an observable list).
	 * @see to.etc.domui.databinding.list.IObservableListValue#getValue()
	 */
	@Override
	@Nullable
	public List<T> getValue() throws Exception {
		return m_property.getValue(m_instance);
	}

	/**
	 * Set a new List into the property.
	 * @see to.etc.domui.databinding.list.IObservableListValue#setValue(java.util.List)
	 */
	@Override
	public void setValue(@Nullable List<T> value) throws Exception {
		/*
		 * 20130425 jal
		 * The old implementation did a getvalue before the setvalue, and fired an event when the
		 * old and new values changed. This should apparently not be done: the property we're observing
		 * itself will fire an event when modified.
		 */
		m_property.setValue(m_instance, value);
	}

	/**
	 * Registers a change listener for events on this property's list. This registers a change
	 * listener to be added to the observable list inside the property.
	 *
	 * @see to.etc.domui.databinding.ListenerList#addChangeListener(to.etc.domui.databinding.IChangeListener)
	 */
	@Override
	public synchronized void addChangeListener(@Nonnull IListValueChangeListener<T> listener) {
		if(null == m_listEventAdapter) {
			try {
				//-- Do we have a value?
				List<T> list = getValue();
				if(null != list) {
					if(!(list instanceof IObservableList))
						throw new ListNotObservableException(m_instance.getClass(), m_property.getName());
					ListEventAdapter lea = m_listEventAdapter = new ListEventAdapter();		// Create the adapter.
					IObservableList<T> olist = (IObservableList<T>) list;
					olist.addChangeListener(lea);
				}
			} catch(Exception x) {
				throw WrappedException.wrap(x);					// Sigh
			}
		}
		super.addChangeListener(listener);
	}

	/**
	 * This adapter changes {@link IObservableList} events of type {@link ListChangeEvent} to events for
	 * an observed list property ({@link ListValueChangeEvent}'s). An instance gets registered on the
	 * observable list contained in the property we're observing.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Nov 1, 2013
	 */
	private class ListEventAdapter implements IListChangeListener<T> {
		@Override
		public void handleChange(@Nonnull ListChangeEvent<T> event) throws Exception {
			ListValueChangeEvent<T> lvce = new ListValueChangeEvent<T>(ObservablePropertyList.this, event.getChanges());
			fireEvent(lvce);									// Send the converted event.
		}
	}

	/**
	 * Called from observer support, this handles registering the listener to the new observable list.
	 * @param old
	 * @param value
	 */
	@Override
	public <X> void notifyIfChanged(@Nullable X old, @Nullable X value) {
		if(MetaManager.areObjectsEqual(old, value))
			return;

		ListEventAdapter ea = m_listEventAdapter;
		if(null != ea) {
			//-- We registered an adapter apparently... Unregister from old and re-register on new...
			if(old != null) {
				if(!(old instanceof IObservableList))
					throw new ListNotObservableException(m_instance.getClass(), m_property.getName());
				IObservableList<T> olist = (IObservableList<T>) old;
				olist.removeChangeListener(ea);
			}

			//-- Re-register @ new
			if(null != value) {
				if(!(value instanceof IObservableList))
					throw new ListNotObservableException(m_instance.getClass(), m_property.getName());
				IObservableList<T> nlist = (IObservableList<T>) value;
				nlist.addChangeListener(ea);
			}

			//-- Send the event.
			ListChangeAssign<T> lca = new ListChangeAssign<T>((List<T>) old, (List<T>) value);
			List<ListChange<T>> res = new ArrayList<ListChange<T>>(1);
			res.add(lca);
			ListValueChangeEvent<T> lvca = new ListValueChangeEvent<T>(this, res);
			fireEvent(lvca);
		}
	}
}
