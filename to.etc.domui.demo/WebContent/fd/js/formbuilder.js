FormBuilder = function(id, paintid, compid) {
	window._fb = this;

	this._id = id;
	this._paintid = paintid;
	this._compid = compid;
	this._componentTypeMap = new Object();
	this._pendingUpdateList = new Array();
	this._componentMap = new Object();
	this._componentNodeMap = new Object();
	this.register();
};
$.extend(FormBuilder.prototype, {
	register: function() {
		var fb = this;
		this._paintid.droppable({
			activeClass: "fb-pp-drop",
			drop: function(event, ui) {
				var comp = fb._draggedComponent;
				if(! comp)
					return;
				console.debug("comp=", comp);
				fb.sendEvent("DropComponent", {typeName: comp._typeName, x:ui.position.left, y:ui.position.top});
//	            $(ui.draggable).clone().appendTo(this);
			}
		});
	},
	
	sendEvent: function(action, fields) {
		var pupd = this._pendingUpdateList;
		if(pupd.length > 0) {
			this._pendingUpdateList = new Array();
		}

		fields.pending = JSON.stringify(pupd);
		WebUI.scall(this._id, action, fields);
	},

	registerComponentType: function(handle, typename) {
		var comp = new ComponentType(this, typename, handle);
		this._componentTypeMap[typename] = comp;
		var fb = this;

		$("#"+handle).draggable({
			containment: "body",
			appendTo: "body",
			distance: 20,
			helper: function() {
				var node = $("#"+handle).clone();
				fb._draggedComponent = comp;					// $.data does not work because stuff gets copied.
				return node;
			},
			grid: [10, 10],
			cursorAt: {left: 0, top: 0}
		});

//		var fb = this;
//		this._instanceId = instid;
//		$("#"+handle).draggable({
//			containment: "body",
//			appendTo: "body",
//			distance: 20,
//			helper: function() {return fb.dragHelper(instid); },
//			grid: [10, 10],
//			cursorAt: {left: 0, top: 0}
//		});
	},

	dragHelper: function(id) {
		var node = $("#"+id);
		if(0 == node.length)
			throw "Node "+id+" is not found";
		console.debug("Node is", node);

		return node;
	},
	
	getComponentType: function(typeid) {
		var type = this._componentTypeMap[typeid];
		if(! type)
			throw "Unknown component type "+typeid;
		return type;
	},

	registerInstance: function(typeid, nodeid) {
		$.dbg("registering instance "+typeid+":"+nodeid);
		var type = this.getComponentType(typeid);
		var node = $("#"+nodeid);
		var inst = new ComponentInstance(this, type, node);
		this._componentMap[nodeid] = inst;
		
		//-- Make the instance draggable too.
		var fb = this;

		$("#"+nodeid).draggable({
			cancel:"",											// NOT FUNNY 8-( Enable dragging on controls.
			containment: "body",
			appendTo: "body",
			distance: 10,
			helper: function() {
				console.debug("Dragging started");
				var node = $("#"+nodeid).clone();
				fb._draggedComponent = null;						// Don't add/register
				return node;
			},
			grid: [10, 10],
			cursorAt: {left: 0, top: 0}
		});

	}
	
	
});

FormBuilder.create = function(paintid,compid) {
	var pa = $('#'+paintid);
	var co = $('#'+compid);
	var fb = new FormBuilder(paintid, pa, co);

	pa.data('fb', fb);
};


/*----- FormComponent -----*/
ComponentType = function(builder, typename, rootid) {
	this._builder = builder;
	this._typeName = typename;
	this._id = rootid;
	
};

/*------ Component instance -----*/
ComponentInstance = function(builder, type, node) {
	this._builder = builder;
	this._type = type;
	this._node = node;
};


