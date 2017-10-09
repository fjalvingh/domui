interface JQuery {
	webui(xml: any) : void;		// fixme remove
	disableSelection(): void;
	enableSelection():  void;
	error(fn: Function):  void;
}

interface JQueryStatic {
	webui(xml: any) : void;
	expr: any;
	browser: any;
	dbg: any;
}
