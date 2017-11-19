FormBuilder = function(id, paintid, compid) {
	window._fb = this;

	this._id = id;
	this._paintid = paintid;
	this._compid = compid;
	this._componentTypeMap = {};
	this._pendingUpdateList = [];
	this._componentMap = {};
	this._componentNodeMap = {};
	this._selectionList = [];
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
					var loc1 = ui.offset;
					var toploc1 = fb._paintid.offset().top;
	
					fb.sendEvent("DropComponent", {typeId: comp._typeName, x:loc1.left, y:loc1.top-toploc1});
				}
				comp = fb._draggedComponent;
				if(comp) {
					$.dbg("comp drop=", comp);
					var loc2 = ui.offset;
					var toploc2 = fb._paintid.offset().top;
	
					fb.sendEvent("MoveComponent", {id: comp._id, x:loc2.left, y:loc2.top-toploc2});
				}

//	            $(ui.draggable).clone().appendTo(this);
			}
		});
	},

	sendEvent: function(action, json) {
		var pupd = this._pendingUpdateList;
		if(pupd.length > 0) {
			this._pendingUpdateList = [];
		}
		WebUI.sendJsonAction(this._id, action, json);
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
				fb._draggedComponent = null;
			
				return node;
			},
			grid: [10, 10],
//			cursorAt: {left: 0, top: 0}
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
		if(0 === node.length)
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
//			cursorAt: {left: 0, top: 0}
		}).dblclick(function(event) {
			fb.dblClick(inst, event);
		});
	},

	dblClick: function(component, e) {
		if(e.ctrlKey) {
			this.selectionToggle(component);			
		} else {
			var si = this.selectionIndex(component);
			this.selectionClear();
			if(si < 0) {
				this.selectionToggle(component);
			}
		}
		e.stopPropagation();
	},
	
	/*----------------- Selection -------------------------*/
	selection: function(item) {
		this.selectionClear();
		this.selectionToggle(item);
	},

	/**
	 * Clear all selections - this also resets selection state display on all selected items.
	 */
	selectionClear: function() {
		for(var i = this._selectionList.length; --i >= 0;) {
			var item = this._selectionList[i];
			item.setSelected(false);
		}
		this._selectionList = [];
	},

	selectionIndex: function(item) {
		for(var i = this._selectionList.length; --i >= 0;) {
			if(item === this._selectionList[i])
				return i;
		}
		return -1;
	},

	selectionToggle: function(item) {
		var ix = this.selectionIndex(item);
		if(ix < 0) {								// Not selected - then add
			this._selectionList.push(item);
			item.setSelected(true);
		} else {
			this.slice(ix, 1);						// Remove from selection list,
			item.setSelected(false);
		}

		//-- Send a SELECTION event with all selected item IDs
		var curs = [];
		for(var i = this._selectionList.length; --i >= 0;) {
			curs.push(this._selectionList[i]._id);
		}
		this.sendEvent("Selection", curs);
//		this.sendEvent("WFLSELECTION", {selected: curs});
	},

	
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
$.extend(ComponentInstance.prototype, {
	setSelected: function(on) {
		$.dbg("Selected "+on);
		if(on)
			$(this._node).addClass("fb-ui-selected");
		else
			$(this._node).removeClass("fb-ui-selected");
	}
});



