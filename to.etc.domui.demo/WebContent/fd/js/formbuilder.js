FB = new Object();
FB.Toolbox = new Object();
FB.Toolbox.createOverlays = function() {
//	$(".fb-pc").
};

FormBuilder = function(paintid, compid) {
	this._paintid = paintid;
	this._compid = compid;
};
FormBuilder.create = function(paintid,compid) {
	var pa = $('#'+paintid);
	var co = $('#'+compid);
	var fb = new FormBuilder(pa, co);

	pa.data('fb', fb);
};









