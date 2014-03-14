/**
 * Base class for a layout manager's helper inside the form builder.
 * @param id
 */
LayoutBase = function(id) {
	this._id = id;
	this._node = $("#"+id);
};

/**
 * XYLayout matches the DomUI "XYLayout" layout container.
 * @param id
 */
XYLayout = function(id) {
	LayoutBase(id);
};

$.extend(XYLayout.prototype, {
	
});
