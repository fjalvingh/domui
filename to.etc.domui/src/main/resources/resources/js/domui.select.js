WebUI = WebUI || {};
$.extend(WebUI, {
	comboBox: function(id) {
		this._id = id;
		this._popped = false;
		$('#'+id+" .ui-cbb-ab").click($.proxy(this.handleClicked, this));
	}
});

$.extend(WebUI.comboBox.prototype, {
	handleClicked: function() {
		var pul = $('#'+this._id+" .ui-cbb-pu");
		if(this._popped) {
			pul.hide(200);
		} else {
			pul.show(200);
		}
		this._popped = ! this._popped;
	}
});

