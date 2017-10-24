interface JQuery {
	webui(xml: any) : void;		// fixme remove
	disableSelection(): void;
	enableSelection():  void;
	error(fn: Function):  void;
	colResizable(val: any): void;

	markerTransformed: boolean;

	doStretch() : void;
	fixOverflow() : void;
	setBackgroundImageMarker() : void;

	size() : number;

	ColorPicker: any;
	draggable: any;

}

interface JQueryStatic {
	webui(xml: any): void;
	executeXML(xml: any): void;

	expr: any;
	browser: any;
	dbg: any;

	cookie(name: string, value?: any, options?: any): any;
}
