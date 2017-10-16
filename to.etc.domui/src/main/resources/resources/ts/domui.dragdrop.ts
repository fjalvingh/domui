/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
//import WebUI from "domui.webui.util";

namespace WebUI {
	let _dragType: string;

	let _dragMode: number;

	let _dragNode: HTMLElement;

	let _dragCopy: HTMLElement;

	let _dragSourceOffset: Point;

	let _dragLastX: number;

	let _dragLastY: number;

	let _dragTimer: number;

	let _currentDropZone: any;

	let _dropRowIndex: number;

	let _dropRow: HTMLElement;


	/**
	 * When mouse is downed on an item that is draggable. This moves to PREDRAG
	 * mode where the first move will create a visible representation of this
	 * node, ready for dropping.
	 */
	export function dragMouseDown(item, evt): void {
		dragReset();
		_dragType = item.getAttribute('uitype');
		if(!_dragType)
			alert("This DRAGGABLE node has no 'uitype' attribute??");
		var dragAreaId = item.getAttribute('dragarea');
		if(dragAreaId) {
			_dragNode = document.getElementById(dragAreaId);
		} else
			_dragNode = item;
		_dragMode = 1; 									// PREDRAG
		$(document.body).bind("mousemove", dragMouseMove);
		$(document.body).bind("mouseup", dragMouseUp);
		var apos = WebUI.getAbsolutePosition(item);
		_dragSourceOffset = apos;
		apos.x = evt.clientX - apos.x;
		apos.y = evt.clientY - apos.y;
		if(evt.preventDefault)
			evt.preventDefault(); // Prevent ffox image dragging
		else {
			evt.returnValue = false;
		}
		if((document as any).attachEvent) {
			(document as any).attachEvent("onselectstart", WebUI.preventSelection);
		}
	}

	export function dragMouseUp(): void {
		// -- If we're in DRAGGING mode we may accept the drop
		try {
			if(_dragMode == 2) {
				dragClearTimer();
				var dz = dropTargetFind(_dragLastX, _dragLastY);
				if(dz) {
					dropClearZone(); // Discard any dropzone visuals
					dz._drophandler.drop(dz);
				} else {
					_dragNode.style.display = '';//no drop zone, so restore the dragged item
				}
			}
		} finally {
			dragReset();
		}
	}

	export function dragMouseMove(e): void {
		if(_dragMode == 0) {
			dragReset();
			return;
		}

		if(_dragMode == 1) {
			// -- preDRAG mode: create the node copy, then move it to the
			// offset' location.
			_dragCopy = dragCreateCopy(_dragNode);
			//MVE make this optional.
			_dragNode.style.display = 'none';

			_dragMode = 2;
			document.body.appendChild(_dragCopy);
		}
		_dragCopy.style.top = (e.clientY - _dragSourceOffset.y) + "px";
		_dragCopy.style.left = (e.clientX - _dragSourceOffset.x) + "px";
		// console.debug("currentMode: "+WebUI._dragMode+",
		// type="+WebUI._dragType);
		_dragLastX = e.clientX;
		_dragLastY = e.clientY;
		dragResetTimer();
	}

	export function dragCreateCopy(source): HTMLElement {
		var dv = document.createElement('div');

		// If we drag a TR we need to encapsulate the thingy in a table/tbody to prevent trouble.
		if(source.tagName != "TR") {
			dv.innerHTML = source.innerHTML;
		} else {
			//-- This IS a tr. Create a table/TBody then add the content model
			var t = document.createElement('table');
			dv.appendChild(t);
			var b = document.createElement('tbody');
			t.appendChild(b);
			b.innerHTML = source.innerHTML;			// Copy tr inside tbody we just constructed

			//-- Find parent table's CSS class so we can copy it's style.
			var dad = WebUI.findParentOfTagName(source, 'TABLE');
			if(dad) {
				t.className = dad.className;
			}
		}

		dv.style.position = 'absolute';
		dv.style.width = $(source).width() + "px";
		dv.style.height = $(source).height() + "px";
		//console.debug("DragNode isa "+source.tagName+", "+dv.innerHTML);
		return dv;
	}

	/**
	 * Resets the dropzone timer. Called when in DRAGGING mode and the mouse
	 * moves, this resets any "open" dropzone indicators and resets the timer on
	 * which drop zone effects are done. This causes the dropzone indicator
	 * delay when moving the mouse.
	 */
	export function dragResetTimer(): void {
		dragClearTimer();
		_dragTimer = setTimeout("WebUI.dragTimerFired()", 250);
	}

	export function dragClearTimer(): void {
		if(_dragTimer) {
			clearTimeout(_dragTimer);
			_dragTimer = undefined;
		}
	}

	/**
	 * Fires when in DRAGGING mode and the mouse has not moved for a while. It
	 * initiates the rendering of any drop zone indicators if the mouse is above
	 * a drop zone.
	 */
	export function dragTimerFired(): void {
		// console.debug("timer fired");
		var dz = dropTargetFind(_dragLastX, _dragLastY);
		if(!dz) {
			dropClearZone();
			return;
		}

		// -- Un-notify the previous dropzone and notify the new'un
		if(dz == _currentDropZone) {
			dz._drophandler.checkRerender(dz);
			return;
		}
		dropClearZone();
		_currentDropZone = dz;
		dz._drophandler.hover(dz);
		// console.debug("AlterClass on "+dz._dropTarget);
	}

	export function findDropZoneHandler(type): IDropZoneHandler {
		if(type == "ROW")
			return _ROW_DROPZONE_HANDLER;
		return _DEFAULT_DROPZONE_HANDLER;
	}

	export function dropClearZone(): void {
		if(_currentDropZone) {
			_currentDropZone._drophandler.unmark(_currentDropZone);
			_currentDropZone = undefined;
		}
	}

	/**
	 * Clears any node being dragged.
	 */
	export function dragReset(): void {
		dragClearTimer();
		if(_dragCopy) {
			$(_dragCopy).remove();
			_dragCopy = null;
		}
		if(_dragNode) {
			$(document.body).unbind("mousemove", dragMouseMove);
			$(document.body).unbind("mouseup", dragMouseUp);
			_dragNode = null;
		}
		dropClearZone();
		_dragMode = 0; // NOTDRAGGED

		if((document as any).detachEvent) {
			(document as any).detachEvent("onselectstart", WebUI.preventSelection);
		}

//		if(WebUI._selectStart){
//			document.onselectstart = WebUI._selectStart;
//		}
	}

	class DropInfo {
		_tbody: HTMLElement;
		_dropTarget: HTMLElement;
		_position: WebUI.Point;
		_width: number;
		_height: number;
		_types: string[];
		_drophandler: IDropZoneHandler;
	}

	let _dropList: DropInfo[];

	/**
	 * Gets or recalculates the list of possible drop targets and their absolute
	 * on-screen position. This list is used to determine if the mouse is "in" a
	 * drop target. The list gets cached globally in the WebUI object; if an
	 * AJAX request is done the list gets cleared.
	 */
	export function dropGetList(): any[] {
		if(_dropList)
			return _dropList;

		// -- Reconstruct the droplist. Find all objects that possess the ui-drpbl class.
		var dl = $(".ui-drpbl").get();
		_dropList = [];
		for(var i = dl.length; --i >= 0;) {
			var drop = dl[i];
			var types = drop.getAttribute('uitypes');
			if(!types)
				continue;
			var def = new DropInfo();
			def._dropTarget = drop; // Store the objects' DOM node,
			def._position = WebUI.getAbsolutePosition(drop);
			def._width = drop.clientWidth;
			def._height = drop.clientHeight;
			var tar = types.split(",");
			def._types = tar;
			def._drophandler = findDropZoneHandler(drop.getAttribute('uidropmode'));
			var id = drop.getAttribute('uidropbody');
			if(id) {
				def._tbody = document.getElementById(id);
				if(!def._tbody) {
					alert('Internal error: the TBODY ID=' + id + ' cannot be located (row dropTarget)');
					continue;
				}
				dropRemoveNonsense(def._tbody);
			}

			_dropList.push(def);
		}
		return _dropList;
	}

	export function dropClearList(): void {
		_dropList = undefined;
	}

	export function dropTargetFind(x, y): any {
		var dl = dropGetList();
		for(var i = dl.length; --i >= 0;) {
			var d = dl[i];

			// -- Contained and of the correct type?
			if(x >= d._position.x && x < d._position.x + d._width
				&& y >= d._position.y && y < d._position.y + d._height) {
				for(var j = d._types.length; --j >= 0;) {
					if(d._types[j] == _dragType)
						return d;
				}
			}
		}
		return null;
	}

	export function dropRemoveNonsense(body): void {
		for(var i = body.childNodes.length; --i >= 0;) {
			var n = body.childNodes[i];
			if(n.nodeName == '#text')
				body.removeChild(n);
		}
	}

	interface IDropZoneHandler {
		checkRerender(dz: DropInfo) : void;

		hover(dz: DropInfo) : void;

		unmark(dz: DropInfo) : void;

		drop(dz: DropInfo) : void;
	}

	/**
	 * This handles ROW mode drops. It locates the nearest row in the TBody for this
	 * dropTarget and decides to put the thingy BEFORE or AFTER that row. The
	 * boundary there then gets highlighted.
	 */
	class RowDropzoneHandler implements IDropZoneHandler {
		constructor() {
		}

		locateBest(dz: DropInfo) {
			var tbody = dz._tbody;
			if(!tbody)
				throw "No TBody!";

			// -- Use the current mouseish Y position to distinguish between rows.
			var mousePos = _dragLastY;
			var mouseX = _dragLastX;
			//console.debug("Starting position det: drag Y = "+mousePos);
			var gravity = 0; // Prefer upward gravity
			var lastrow = null;
			var rowindex = 0;
			var position = {top: 0, index: 0};
			for(var i = 0; i < tbody.childNodes.length; i++) {
				var tr = tbody.childNodes[i];
				if(tr.nodeName != 'TR')
					continue;
				lastrow = tr;
				var off = $(tr).offset();
				var prevPosition = position;
				position = {top: off.top, index: i};
				if(position) {
					//			console.debug('mouse:' +mousePos+','+mouseX+' row: prevPosition.top='+prevPosition.top+", position.top="+position.top+", index="+position.index);

					// -- Is the mouse IN the Y range for this row?
					if(mousePos >= prevPosition.top && mousePos < position.top) {
						// -- Cursor is WITHIN this node. Is it near the TOP or near the
						// BOTTOM?
						gravity = 0;
						if(prevPosition.top + position.top != 0) {
							var hy = (prevPosition.top + position.top) / 2;
							gravity = mousePos < hy ? 0 : 1;
						}
						//				console.debug('ACCEPTED top='+prevPosition.top+', bottom='+position.top+', hy='+hy+', rowindex='+(rowindex-1));
						//				console.debug('index='+prevPosition.index+', gravety='+gravity);

						var colIndex = this.getColIndex(tr, mouseX);
						return {
							index: rowindex - 1,
							iindex: prevPosition.index,
							gravity: gravity,
							row: tr,
							colIndex: colIndex
						};
					}

					// -- Is the thing between this row and the PREVIOUS one?
					//			if (mousePos < position.top) {
					//				// -- Use this row with gravity 0 (should insert BEFORE this row).
					//				//MVE
					//				console.debug('ACCEPTED BEFORE node by='+prevPosition.top+', ey='+position.top+', rowindex='+rowindex-1);
					//				return {
					//					index :rowindex,
					//					iindex :position.index,
					//					gravity :0,
					//					row :tr
					//				};
					//			}
					//console.debug('REFUSED by='+prevPosition.top+", ey="+position.top+", rowindex="+rowindex);
				} else {
					//			console.debug("row: no location.");
				}
				rowindex++;
			}
			//console.debug("ACCEPTED last one");

			// -- If we're here we must insert at the last location
			var colIndex = this.getColIndex(lastrow, mouseX);
			return {
				index: rowindex,
				iindex: position.index,
				gravity: 1,
				row: lastrow,
				colIndex: colIndex
			};
		}

		getColIndex(tr, mouseX) {
			//determine the collumn
			var left = 0;
			var right = 0;
			var j;
			for(j = 0; j < tr.childNodes.length; j++) {
				var td = tr.childNodes[j];
				if(td.nodeName != 'TD')
					continue;
				left = right;
				right = $(td).offset().left;
				if(mouseX >= left && mouseX < right) {
					//because only the left position can be asked, the check is done for the previous collumn
					return j - 1;
				}

			}
			//TODO MVE should return maxColumn
			return 2;

		}

		checkRerender(dz: DropInfo) {
			var b = this.locateBest(dz);
			// console.debug("checkRerender: "+b.iindex+", "+b.index+", g="+b.gravity);
			if(b.iindex == _dropRowIndex)
				return;
			this.unmark(dz);
			this.renderTween(dz, b);
		}

		renderTween(dz: DropInfo, b) {
			var body = dz._tbody;

			var colCount = 0;
			if(dz._tbody.children.length > 0) {
				var temp = dz._tbody.children[0].children;
				$(temp).each(function() {
					colCount += $(this).attr('colspan') ? parseInt($(this).attr('colspan')) : 1;
				});
			}


			// -- To mark, we insert a ROW at the insert location and visualize that
			var tr = document.createElement('tr');
			//b.colIndex should define the correct collumn
			var colIndex = b.colIndex;
			for(var i = 0; i < colCount; i++) {
				this.appendPlaceHolderCell(tr, colIndex == i);
			}
			if(b.iindex >= body.childNodes.length)
				body.appendChild(tr);
			else
				body.insertBefore(tr, body.childNodes[b.iindex]);
			_dropRow = tr;
			_dropRowIndex = b.iindex;
		}

		appendPlaceHolderCell(tr, appendPlaceholder) {
			var td = document.createElement('td');
			if(appendPlaceholder) {
				td.appendChild(document.createTextNode(WebUI._T.dndInsertHere));
				td.className = 'ui-drp-ins';
			}
			tr.appendChild(td);

		}

		hover(dz: DropInfo) : void {
			var b = this.locateBest(dz);
			//	console.debug("hover: "+b.iindex+", "+b.index+", g="+b.gravity + ", col=" +b.colIndex);
			this.renderTween(dz, b);
		}

		unmark(dz: DropInfo) : void {
			if(_dropRow) {
				$(_dropRow).remove();
				_dropRow = undefined;
				_dropRowIndex = undefined;
			}
		}

		drop(dz: DropInfo) : void {
			this.unmark(dz);
			var b = this.locateBest(dz);
			WebUI.scall(dz._dropTarget.id, "WEBUIDROP", {
				_dragid: _dragNode.id,
				_index: (b.index + b.gravity),
				_colIndex: b.colIndex
			});
			dragReset();
		}
	}

	class DefaultDropzoneHandler implements IDropZoneHandler {
		constructor() {
		}

		checkRerender(dz: DropInfo) {
		}

		hover(dz : DropInfo) : void {
			$(dz._dropTarget).addClass("ui-drp-hover");
		}

		unmark(dz: DropInfo) {
			if(dz)
				$(dz._dropTarget).removeClass("ui-drp-hover");
		}

		drop(dz: DropInfo) {
			this.unmark(dz);
			WebUI.scall(dz._dropTarget.id, "WEBUIDROP", {
				_dragid: _dragNode.id,
				_index: 0
			});
			dragReset();
		}
	}

	let _DEFAULT_DROPZONE_HANDLER = new DefaultDropzoneHandler();

	let _ROW_DROPZONE_HANDLER = new RowDropzoneHandler();
}
