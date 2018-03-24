var WebUI = {};

WebUI.truncateUtfBytes = function(str, nbytes) {
	//-- Loop characters and calculate running length
	var bytes = 0;
	var length = str.length;
	for(var ix = 0; ix < length; ix++) {
		var c = str.charCodeAt(ix);
		if(c < 0x80)
			bytes++;
		else if(c < 0x800)
			bytes += 2;
		else
			bytes += 3;
		if(bytes > nbytes)
			return ix;
	}
	return length;
};

WebUI.utf8Length = function(str) {
	var bytes = 0;
	var length = str.length;
	for(var ix = 0; ix < length; ix++) {
		var c = str.charCodeAt(ix);
		if(c < 0x80)
			bytes++;
		else if(c < 0x800)
			bytes += 2;
		else
			bytes += 3;
	}
	return bytes;
};
