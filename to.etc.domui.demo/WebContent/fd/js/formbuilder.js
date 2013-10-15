FB = new Object();
FB.Toolbox = new Object();
FB.Toolbox.createOverlays = function() {
//	$(".fb-pc").
};

FormBuilder = function(paintid, compid) {
	this._paintid = paintid;
	this._compid = compid;
	this.addComponentDraggables();
	window._fb = this;
};
FormBuilder.create = function(paintid,compid) {
	var pa = $('#'+paintid);
	var co = $('#'+compid);
	var fb = new FormBuilder(pa, co);

	pa.data('fb', fb);
};

$.extend(FormBuilder.prototype, {
	addComponentDraggables: function() {
		$(".fb-pc").draggable({
			containment: "body",
			appendTo: "body",
			distance: 20,
			helper: "clone",
			grid: [10, 10],
			cursorAt: {left: 0, top: 0}
		});
	},
	
	registerComponent: function(handle, instid) {
		var fb = this;
		this._instanceId = instid;
		$("#"+handle).draggable({
			containment: "body",
			appendTo: "body",
			distance: 20,
			helper: function() {return fb.dragHelper(instid); },
			grid: [10, 10],
			cursorAt: {left: 0, top: 0}
		});
	},

	dragHelper: function(id) {
		var node = $("#"+id);
		if(0 == node.length)
			throw "Node "+id+" is not found";
		console.debug("Node is", node);
		
		return node;
	},
	
	
});
