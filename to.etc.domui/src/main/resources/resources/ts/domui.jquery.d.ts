interface JQuery {
	webui(xml: any) : void;
	disableSelection(): void;
	enableSelection():  void;
	error(fn: Function):  void;
}

interface JQueryStatic {
	expr: any;
	browser: any;
	dbg: any;
}
