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
				var comp = fb._draggedType;
				if(comp) {
					$.dbg("type drop=", comp);
					var loc = ui.offset;
					var toploc = fb._paintid.offset().top;
	
					fb.sendEvent("DropComponent", {typeId: comp._typeName, x:loc.left, y:loc.top-toploc});
				}
				comp = fb._draggedComponent;
				if(comp) {
					$.dbg("comp drop=", comp);
					var loc = ui.offset;
					var toploc = fb._paintid.offset().top;
	
					fb.sendEvent("MoveComponent", {id: comp._id, x:loc.left, y:loc.top-toploc});
				}

//	            $(ui.draggable).clone().appendTo(this);
			}
		});
	},

	sendEvent: function(action, json) {
		var pupd = this._pendingUpdateList;
		if(pupd.length > 0) {
			this._pendingUpdateList = new Array();
		}
//		json.pending = pupd;
		var fields = new Object();
		fields.json = JSON.stringify(json);
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
				fb._draggedType = comp;					// $.data does not work because stuff gets copied.
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

	registerInstance: function(typeid, id, nodeid) {
		$.dbg("registering instance "+typeid+":"+id);
		var type = this.getComponentType(typeid);
		var node = $("#"+nodeid);
		var inst = new ComponentInstance(this, type, id, node);
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
				var node = $("#"+nodeid);
				fb._draggedComponent = inst;				// We're dragging this instance
				fb._draggedType = null;						// Don't add/register
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
ComponentInstance = function(builder, type, id, node) {
	this._builder = builder;
	this._type = type;
	this._id = id;
	this._node = node;
};


