package to.etc.server.misc;

/**
 *	This interface, when implemented by something that is put in a TimedTable or
 *  the like (...) contains methods that are called by the table when something
 *  happens with the entry.
 */
public interface TableEntry {
	/**
	 *	Called when this entry HAS BEEN removed by the janitor, or by expiry,
	 *  or because it was the LRU item... It is NOT called when the item is
	 *  manually removed by calling the remove method of the container.
	 *	When called the removal will ALREADY have taken place, and while
	 *  called the table is NOT locked.
	 */
	public void wasRemoved();

	/**
	 *	Returns the expiry time, in seconds, of this object. This is the time
	 *  that the object has to be unused before it can be released.
	 */
	public int getExpiry();

}
