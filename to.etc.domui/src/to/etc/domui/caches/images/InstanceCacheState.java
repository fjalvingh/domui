package to.etc.domui.caches.images;

public enum InstanceCacheState {
	/** The instance is not properly linked yet. It will exist in the access map but it's size and LRU links are not used */
	NONE,

	/** The instance is VALID, and has been properly linked and it's size is registered in the cache. */
	LINKED,

	/** The instance is INVALID, and it's size and LRU chain are invalid */
	DISCARD
}
