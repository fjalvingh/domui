WebUI._ROW_DROPZONE_HANDLER = {
	locateBest : function(dz) {
		var tbody = dz._tbody;
		if (!tbody)
			throw "No TBody!";

		// -- Use the current mouseish Y position to distinguish between rows.
		var mousePos = WebUI._dragLastY;
		var mouseX = WebUI._dragLastX;
		//console.debug("Starting position det: drag Y = "+mousePos);
		var gravity = 0; // Prefer upward gravity
		var lastrow = null;
		var rowindex = 0;
		var position = { top: 0, index: 0};
		for ( var i = 0; i < tbody.childNodes.length; i++) {
			var tr = tbody.childNodes[i];
			if (tr.nodeName != 'TR')
				continue;
			lastrow = tr;
			var off = $(tr).offset();
			var prevPosition = position;
			position = { top: off.top, index: i };
			if (position) {
				//			console.debug('mouse:' +mousePos+','+mouseX+' row: prevPosition.top='+prevPosition.top+", position.top="+position.top+", index="+position.index);

				// -- Is the mouse IN the Y range for this row?
				if (mousePos >= prevPosition.top && mousePos < position.top) {
					// -- Cursor is WITHIN this node. Is it near the TOP or near the
					// BOTTOM?
					gravity = 0;
					if(prevPosition.top + position.top != 0){
						var hy = (prevPosition.top + position.top) / 2;
						gravity = mousePos < hy ? 0 : 1;
					}
					//				console.debug('ACCEPTED top='+prevPosition.top+', bottom='+position.top+', hy='+hy+', rowindex='+(rowindex-1));
					//				console.debug('index='+prevPosition.index+', gravety='+gravity);

					var colIndex = this.getColIndex(tr, mouseX);
					return {
						index :rowindex-1,
						iindex : prevPosition.index,
						gravity :gravity,
						row :tr,
						colIndex : colIndex
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
			index :rowindex,
			iindex :position.index,
			gravity :1,
			row :lastrow,
			colIndex : colIndex
		};
	},

	getColIndex : function(tr, mouseX) {
		//determine the collumn
		var left = 0;
		var right = 0;
		var j;
		for ( j = 0; j < tr.childNodes.length; j++) {
			var td = tr.childNodes[j];
			if (td.nodeName != 'TD')
				continue;
			left = right;
			right = $(td).offset().left;
			if(mouseX >= left && mouseX < right ){
				//because only the left position can be asked, the check is done for the previous collumn
				return j-1;
			}

		}
		//TODO MVE should return maxColumn
		return 2;

	},

	checkRerender : function(dz) {
		var b = this.locateBest(dz);
		// console.debug("checkRerender: "+b.iindex+", "+b.index+", g="+b.gravity);
		if (b.iindex == WebUI._dropRowIndex)
			return;

		this.unmark(dz);
		this.renderTween(dz, b);
	},

	renderTween : function(dz, b) {
		var body = dz._tbody;

		var colCount = 0;
		if(dz._tbody.rows.length > 0){
			var temp = dz._tbody.rows[0].cells;
			$(temp).each(function() {
				colCount += $(this).attr('colspan') ? parseInt($(this).attr('colspan')) : 1;
			});
		}


		// -- To mark, we insert a ROW at the insert location and visualize that
		var tr = document.createElement('tr');
		//b.colIndex should define the correct collumn
		var colIndex = b.colIndex;
		for(var i = 0; i<colCount;i++ ){
			this.appendPlaceHolderCell(tr, colIndex == i);
		}
		if (b.iindex >= body.childNodes.length)
			body.appendChild(tr);
		else
			body.insertBefore(tr, body.childNodes[b.iindex]);
		WebUI._dropRow = tr;
		WebUI._dropRowIndex = b.iindex;
	},

	appendPlaceHolderCell : function(tr, appendPlaceholder) {
		var td = document.createElement('td');
		if(appendPlaceholder){
			td.appendChild(document.createTextNode(WebUI._T.dndInsertHere));
			td.className = 'ui-drp-ins';
		}
		tr.appendChild(td);

	},

	hover : function(dz) {
		var b = this.locateBest(dz);
		//	console.debug("hover: "+b.iindex+", "+b.index+", g="+b.gravity + ", col=" +b.colIndex);
		this.renderTween(dz, b);
	},

	unmark : function(dz) {
		if (WebUI._dropRow) {
			$(WebUI._dropRow).remove();
			delete WebUI._dropRow;
			delete WebUI._dropRowIndex;
		}
	},

	drop : function(dz) {
		this.unmark(dz);
		var b = this.locateBest(dz);
		WebUI.scall(dz._dropTarget.id, "WEBUIDROP", {
			_dragid :WebUI._dragNode.id,
			_index :(b.index+b.gravity),
			_colIndex :b.colIndex
		});
		WebUI.dragReset();
	}
};
