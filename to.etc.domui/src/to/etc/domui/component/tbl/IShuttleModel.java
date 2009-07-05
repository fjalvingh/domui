package to.etc.domui.component.tbl;

/**
 * The data model for a ListShuttle. This is a compound model which contains related TableModels, one
 * for the SOURCE list and one for the TARGET list. It is the responsibility of this model's implementation
 * to ensure that an item that is present in the SOURCE list is NOT PRESENT in the TARGET list and vice
 * versa!!
 * <p>The two Model implementations *must* implement proper model change events for changes to the model,
 * since those events are used by the ListShuttle to update the UI(!). Using the default implementations
 * for the ITableModel interface ensures that event handling is proper.</p>
 *
 * @param <S>		The type of the elements in the SOURCE model.
 * @param <T>		The type of the elements in the TARGET model.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 6, 2008
 */
public interface IShuttleModel<S, T> {
	/**
	 * Get the model for the SOURCE side of the shuttle. For a single IShuttleModel, this gets called only
	 * once; the result is cached within the ListShuttle. If this model is replaced you MUST set a new IShuttleModel
	 * onto the ListShuttle to make it recognise that the underlying model has changed. In addition, this model
	 * has a relationship with the TargetModel; properly maintaining that relationship is the responsibility of
	 * the model implementer!!
	 * @return
	 */
	public ITableModel<S> getSourceModel();

	/**
	 * Get the model for the TARGET side of the shuttle. For a single IShuttleModel, this gets called only
	 * once; the result is cached within the ListShuttle. If this model is replaced you MUST set a new IShuttleModel
	 * onto the ListShuttle to make it recognise that the underlying model has changed. In addition, this model
	 * has a relationship with the SourceModel; properly maintaining that relationship is the responsibility of
	 * the model implementer!!
	 * @return
	 */
	public ITableModel<T> getTargetModel();

	/**
	 * This gets called when items are moved by the user from SOURCE to TARGET. This method MUST update both the
	 * target model and the source model to reflect that change, and it must cause the appropriate model events
	 * to be sent. This usually entails deleting a record from source and adding it to target.
	 * If the model that the item moves to is sorted it is the responsibility of the model to insert the added
	 * item at the correct location, and to send the event for that location.
	 *
	 * @param six
	 * @param tix
	 * @throws Exception
	 */
	public void moveSourceToTarget(int six, int tix) throws Exception;

	/**
	 * This gets called when items are moved by the user from TARGET to SOURCE. This method MUST update both the
	 * target model and the source model to reflect that change, and it must cause the appropriate model events
	 * to be sent. This usually entails deleting a record from target and adding it back to source.
	 * If the model that the item moves to is sorted it is the responsibility of the model to insert the added
	 * item at the correct location, and to send the event for that location.
	 *
	 * @param tix
	 * @throws Exception
	 */
	public void moveTargetToSource(int tix) throws Exception;
}
